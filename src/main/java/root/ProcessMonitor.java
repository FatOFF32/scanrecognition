package root;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageIOHelper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.client.ClientConfig;
//import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class ProcessMonitor {

    // todo перенести инициализацию в конструктор. У сложенных классов тоже!

    // Пул потоков распознавателя, задачи для обработки
    ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(1,1,15, TimeUnit.MINUTES, new ArrayBlockingQueue(200));
    // Обработанные файлы, данные по которым отправляем в 1С
    BlockingQueue<FileInfo> filesToSend = new ArrayBlockingQueue(200);
    // Список шаблонов для распознования
    HashMap<String, TemplateRecognition> templatesRecognition = new HashMap<>();
    ReadWriteLock tempRecLock = new ReentrantReadWriteLock();
    // Информацию для rest сервиса получаем из 1С todo почистить коментарии с переменными
    private static volatile String url = ""; // Инициализируем для synchronized // "http://localhost/BuhCORP/odata/standard.odata"; //"http://10.17.1.109/upp_fatov/odata/standard.odata";
    private static volatile String userName; // = "testOData";//"test";
    private static volatile String pass; // = "123456";//"111";
    private static volatile Integer quantityThreads = 1;
    private static volatile int restPort; // Устанавливаем в параметрах запуска

    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);

    public static synchronized void setSettings1C(JsonNode rootNode) {

            ProcessMonitor.url = rootNode.get("URLRESTService1C").asText();
            ProcessMonitor.userName = rootNode.get("Пользователь").asText();
            ProcessMonitor.pass = rootNode.get("Пароль").asText();
            ProcessMonitor.quantityThreads = rootNode.get("КоличествоПроцессовАвтораспознавания").asInt();

            ProcessMonitor.class.notifyAll();
    }

    public ProcessMonitor(int restPort) {

        ProcessMonitor.restPort = restPort;
        initialize();

    }

    void initialize() {

        int curQuantityThreads = quantityThreads;

        long curTime = System.currentTimeMillis();
        long updateTime = 600000; // Очищаем данные индекса lucene каждые 600 сек (10 минут).

        // RESTServ, рест сервис, который будет принимать настройки из 1С
        Thread restServ = new RESTServ();
        restServ.start();

        // MonitorDirectories будет:
        // 1. Получать настройки из 1С через REST
        // 2. Мониторить папки и запускать задачи в poolExecutor
        // 3. Мониторить filesToSend и записывать инфо в 1С через rest.
        Thread monitor = new MonitorDirectories();
        monitor.start();

        // Мониторим потоки RESTServ и MonitorDirectories, если какой то отвалится, запускаем заново. // todo возможно переделать в поток, чтобы не вешать майн
        // Также проверяем, если количество процессов изменилось, устанавливаем максимальное количество потоков пула.
        while (true){
            // Проверим, менялось ли количество потоков
            if (quantityThreads != curQuantityThreads) {
                if (quantityThreads < 2) {
                    quantityThreads = 1;
                    curQuantityThreads = quantityThreads;
                } else curQuantityThreads = quantityThreads;

                poolExecutor.setCorePoolSize(quantityThreads);
                poolExecutor.setMaximumPoolSize(quantityThreads);
            }

            // Закроем writer lucene, чтобы удалить не нужные индексы, затем откроем их снова
            if (curTime + updateTime < System.currentTimeMillis()) {
                try {
                    LuceneSearch.rediscoverWriter();
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("Не удалось удалить пересоздать индекс. Причина: ", e);
                }
                curTime = System.currentTimeMillis();
            }

            // Проверим, живы ли наши потоки, если нет, пересоздадим и запустим их.
            if (!restServ.isAlive()){
                restServ = new RESTServ();
                restServ.start();
            }
            if (!monitor.isAlive()){
                monitor = new MonitorDirectories();
                monitor.start();
            }

            // Поспим 10 секунд, затем опять проверим настройки и работоспособность потоков
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Process monitor was interrupt", e);
            }
        }
    }

    protected class RESTServ extends Thread {

        @Override
        public void run() {

            // Переделать под протокол SSL todo

            ResourceConfig config = new ResourceConfig();
            config.packages("resourceRestServ");
            config.register(MultiPartFeature.class); // Для использования передачи файлов (MULTIPART_FORM_DATA)
            ServletHolder servlet = new ServletHolder(new ServletContainer(config));

            Server server = new Server(restPort);
            ServletContextHandler context = new ServletContextHandler(server, "/*");
            context.addServlet(servlet, "/*");

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{context});

            server.setHandler(handlers);

            try {
                server.start();
                server.join();
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Server error", e);
            } finally {
                server.destroy();
            }
        }
    }

    JsonNode getResultQuery1C(String query) {

        ClientConfig config = new ClientConfig();
//        config.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.OFF, LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));
        Client client = ClientBuilder.newClient(config);

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.universal(userName, pass);
        client.register(feature);
//        WebTarget webTarget = client.target(url).path(query); // Почему то так не всегда работает.
        WebTarget webTarget = client.target(url + query);


        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        if (response.getStatus() != 200) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Не удалось выполнить GET запрос к oData 1С  \n\t\t" + url + query + " \n\t\t HTTP error code : " + response.getStatus());
            return null;
        }

        // Результат будем писать в лог, на самом низком уровне логирования
        String resut = response.readEntity(String.class);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Результат запроса к oData 1С. Запрос: \n\t\t" + url + query + "\n\t\t Результат: " + resut);

        // Через джексон
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readValue(resut, JsonNode.class);
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Не удалось преобразовать JSON в объект. \n\t\t" + resut + "\n\t\t", e);
            return null;
        }

        return rootNode.get("value");

    }

    void putObjectTo1C(String query, String jsonObj) {

        ClientConfig config = new ClientConfig();
//        config.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.OFF, LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));

        Client client = ClientBuilder.newClient(config);
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(userName, pass);
        client.register(feature);
//        WebTarget webTarget = client.target(url).path(query); // Так почему то не работает!
        WebTarget webTarget = client.target(url + query);


        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.post(Entity.entity(jsonObj, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Не удалось выполнить POST запрос к oData 1С. \n\t\t Адрес: " + url + query +
                        "\n\t\t Запрос: " + jsonObj + "\n\t\t HTTP error code : " + response.getStatus());
            return;
        }

        // Результат будем писать в лог, на самом низком уровне логирования
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Запрос успешно отправлен в 1С через oData. \n\t\t Адрес: " + url + query +
                    "\n\t\t Запрос: " + jsonObj + "\n\t\t HTTP error code : " + response.getStatus());

    }

    protected class MonitorDirectories extends Thread {

        // Директории для мониторинга
        HashMap<File, String> directoryForMonitor = new HashMap<>();
        // Файлы в обработке
        Set<File> filesInProcess = new HashSet<>();
        // Файлы которые уже обработали, их не трогаем
        Set<File> processedFile = new HashSet<>();
        // Текущее время для автообновления настроек из 1С. Будем запрашивать обновления каждую минуту.
        long curTime = System.currentTimeMillis();
        long updateTime = 60000; // todo увеличить время обновление шаблонов сканов из-за синхронайзта

        private void writeTemplatesRecognition() {

            // Заблокируем шаблоны на чтение, пока модифицируем данные.
            tempRecLock.writeLock().lock();
            try {

                directoryForMonitor.clear();
                templatesRecognition.clear();

                // Подключаемся к 1С чере rest, забираем данные о папках для мониторинга и на шаблон для распознования // todo переделать под StringBuilder
                JsonNode templates = getResultQuery1C("/Catalog_со_ШаблоныАвтораспознавания?" + // Имя справочника
                        "$filter=DeletionMark%20ne%20true" + //$select=Ref_Key,КаталогПоискаСканов,СтрокиПоиска" + // выборки, фильтры (фильтры пока убрал)
                        "&$orderby=СтрокиПоиска/ИмяИскомогоЗначения,СтрокиПоиска/LineNumber%20asc"); // Сортировка

                if (templates == null)
                    return;
                for (JsonNode template : templates) {

                    // Запишем данные для мониторинга директорий
                    directoryForMonitor.put(new File(template.get("КаталогПоискаСканов").asText()), template.get("Ref_Key").asText());

                    JsonNode searchStrings = template.get("СтрокиПоиска");
                    HashMap<WantedValues, List<String>> wantedWords = new HashMap<>();
                    for (JsonNode sStr : searchStrings) {

                        WantedValues wv = new WantedValues(sStr.get("ИмяИскомогоЗначения").asText(),
                                sStr.get("ТипИскомогоЗначения").asText());
                        wantedWords.putIfAbsent(wv, new ArrayList<>());

                        List<String> list = wantedWords.get(wv);
                        list.add(sStr.get("СтрокаПоиска").asText().toLowerCase());
                    }

                    // Получим и запишим в спец. объект коэффициенты расположения области в скане для распознования
                    RatioRectangle ratioRectangle = new RatioRectangle(
                            template.get("КоэффНачалаОбластиX").asDouble(),
                            template.get("КоэффНачалаОбластиY").asDouble(),
                            template.get("КоэффРазмераОбластиX").asDouble(),
                            template.get("КоэффРазмераОбластиY").asDouble());

                    templatesRecognition.put(template.get("Ref_Key").asText(),
                            new TemplateRecognition(template.get("Ref_Key").asText(), wantedWords,
                                    template.get("ИспользоватьНечеткийПоиск").asBoolean(), ratioRectangle));
                }
            } finally {
                tempRecLock.writeLock().unlock();
            }
        }

        private void writeProcessedFile() {

            processedFile.clear();

            // Подключаемся к 1С через rest, получаем данные о отработанных файлах на стороне java, но не подтвержденных на стороне 1С..
            JsonNode templates = getResultQuery1C("/InformationRegister_со_ОбработанныеСканыАвтораспознавателем?$select=ПутьКФайлу");
            if(templates == null)
                return;

            for (JsonNode template : templates){

                // Заполняем полученные файлы в processedFile
                processedFile.add(new File(template.get("ПутьКФайлу").asText()));

            }
        }

        private void TestWriteTemplatesRecognition() { // для теста удалить todo

            directoryForMonitor.clear();
            templatesRecognition.clear();

            // Подключаемся к 1С через rest, получаем данные о шаблонах распознования...
            // TODO

            // Заполняем полученные данные в templatesRecognition
            // TODO

            // Для теста пока будет один шаблон поиска по номеру и дате
            HashMap<WantedValues, List<String>> wantedWords = new HashMap<>();
            List<String> list = new ArrayList<>();
            list.add("Счет-фактура");
            list.add("№"); // смотри рабочие моменты п.1 plan
            wantedWords.put(new WantedValues("Номер", "Строка"), list);
            list = new ArrayList<>();
            list.add("Счет-фактура");
            list.add("№");
            list.add("от");
            wantedWords.put(new WantedValues("Дата", "Дата"), list);
            templatesRecognition.put("1111", new TemplateRecognition("1111", wantedWords, false)); //Подумать, а надо ли ИД в TemplateRecognition, он же есть в мапе.

            // Для теста пока будем использовать одну папку и один шаблон для распознования удалить todo
            directoryForMonitor.put(new File("D:\\Учеба JAVA\\Для распознования\\Сканы"), "1111"); //"D:\\Учеба JAVA\\Для распознования\\Сканы" D:\Java\Для распознования\Сканы

            // Для теста удалить // TODO
//            processedFile.add(new File("D:\\Учеба JAVA\\Для распознования\\Сканы\\7806474760_780601001_2015-02-10_00000973.pdf")); // "D:\\Учеба JAVA\\Для распознования\\Сканы\\7806474760_780601001_2015-02-10_00000973.pdf" "D:\\Java\\Для распознования\\Сканы\\7806474760_780601001_2015-02-10_00000973.pdf"

        }

        @Override
        public void run() {

            // Поспим, пока не прилетит настройка из 1С. Будет возникать во время первого запуска автораспознавателя
            while (url.isEmpty()){
                synchronized (ProcessMonitor.class){ //class, т.к переменные статические
                    try {
                        ProcessMonitor.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Заполним первоначальные данные о отработанных файлах, их обрабатывать не нужно
            writeProcessedFile();

            // Заполним папки для мониторинга и шаблоны для распознования
            writeTemplatesRecognition();

            FileInfo curFileToSend;
            while (!Thread.currentThread().isInterrupted()) {

//                try {

                // Промониторим папки, новые файлы запишем в filesInProcess и добавим в пул задач (poolExecutor), для последующего разбора
                File[] arrayFiles;
                for (Map.Entry<File, String> dir : directoryForMonitor.entrySet()) {
                    if (dir.getKey().isFile()) continue;

                    arrayFiles = dir.getKey().listFiles();
                    for (File file : arrayFiles) {
                        if (!filesInProcess.contains(file) && !processedFile.contains(file)) {
                            filesInProcess.add(file);

                            poolExecutor.submit(new Recognizer(new FileInfo(dir.getValue(), file)));
                        }
                    }
                }

                // Отправим файлы в 1С, перепишим их из обрабатывающихся в обработанные.
                while (filesToSend.size() > 0) {
                    curFileToSend = filesToSend.poll();

                    // Преобразуем полученные данные в JSON и отправим в 1С
                    String jsonObj = "";
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode procData = mapper.createObjectNode();
                    procData.put("ПутьКФайлу", curFileToSend.file.getPath());
                    procData.put("ШаблонАвтораспознавания_Key", curFileToSend.templateID);

                    ObjectNode resRecognition = mapper.createObjectNode();
                    for (Map.Entry<WantedValues, String> wv : curFileToSend.foundWords.entrySet())
                        resRecognition.put(wv.getKey().name, wv.getValue());

                    try {
                        procData.put("РаспознанныеДанныеJSON", mapper.writeValueAsString(resRecognition));
                        jsonObj = mapper.writeValueAsString(procData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    putObjectTo1C("/InformationRegister_со_ОбработанныеСканыАвтораспознавателем", jsonObj);

                    processedFile.add(curFileToSend.file);
                    filesInProcess.remove(curFileToSend.file);
                }

                // Автообновления списка обработанных файлов (чтобы список обработанных не расширялся до бесконечности),
                // а также рабочих директорий.
                if (curTime + updateTime < System.currentTimeMillis()) {
                    writeProcessedFile();
                    writeTemplatesRecognition();
                    curTime = System.currentTimeMillis();
                }
//                } catch (InterruptedException e) {
//                    // Если выбрасывается исключение InterruptedException,
//                    // то флаг (isInterrupted()) не переводится в true. Для этого
//                    // вручную вызывается метод interrupt() у текущего потока.
//                    Thread.currentThread().interrupt();
//                    e.printStackTrace();
//                }
            }
        }
    }

    protected class Recognizer implements Runnable {

        private final FileInfo fileInfo;

        public Recognizer(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {

            String result;
            TemplateRecognition templateRec;

            System.out.println(Thread.currentThread()); // TODO для теста удалить

            try {

                // Попытаемся прочитать данные из шаблона
                tempRecLock.readLock().lock();
                try {
                    templateRec = templatesRecognition.get(fileInfo.templateID);
                }finally {
                    tempRecLock.readLock().unlock();
                }

                if (templateRec == null) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Не найден шаблон для распознавания с ID: " + fileInfo.templateID);
                    return;
                }

                // Распознаем нужную область
                ITesseract instance = new Tesseract();  // JNA Interface Mapping
                instance.setLanguage("rus");
                try {
                    // Преобразуем наш PDF в list IIOImage.
                    List<IIOImage> iioImages = ImageIOHelper.getIIOImageList(fileInfo.file);
                    if (iioImages.size() == 0) {
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug("Файл пустой: " + fileInfo.file);
                        return;
                    }

                    // Для привязки распознаем только первую страницу (Может быть сделаем настраиваемо)
                    int width = iioImages.get(0).getRenderedImage().getWidth();
                    int height = iioImages.get(0).getRenderedImage().getHeight();
                    result = instance.doOCR(iioImages.subList(0, 1), templateRec.getAreaRecognition(width, height));

                } catch (TesseractException | IOException e) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Ошибка распознавания:", e);
                    return;
                }

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(Thread.currentThread() + fileInfo.getFilePath() + "  распознанный текст: " + result);

                // Проиндексируем текст, если испольщуется нечеткий поиск (templateRec.useFuzzySearch)
                if (templateRec.useFuzzySearch)
                    // fileInfo.getFilePath(), путь используется как указатель поля для индекса.
                    LuceneSearch.addTextToIndex(fileInfo.getFilePath(), result);

                for (Map.Entry<WantedValues, List<String>> entry : templateRec.wantedWords.entrySet()) {
                    String resultCopy = result.toLowerCase();
                    int idx = -1; // Индекс найденной строки по шаблону
                    int idxWord = 0; // Индекс слова в массиве полученных слов
                    boolean searchType = false;
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        String st = entry.getValue().get(i);
                        // Проверки на условия "Взять следующий за" и "Искать тип"
                        if (st.startsWith("^getNextAfter")) { //todo подумать над названием. Взять следующий после (например) 3
                            idx = 0; // Установим значение отличное от -1
                            if (i == entry.getValue().size() - 1)
                                break;
                            if (entry.getValue().get(i + 1).matches("\\d+")) { //todo затестить на корректность обработки символов и дабла
                                idxWord = Integer.parseInt(entry.getValue().get(i + 1));
                                if (i + 3 < entry.getValue().size() && entry.getValue().get(i + 2).startsWith("^searchType"))
                                    searchType = true;
                            } else if (entry.getValue().get(i + 1).startsWith("^searchType"))
                                searchType = true;
                            break;
                        } else if (st.startsWith("^searchType")) {
                            searchType = true;
                            break;
                        } else if (st.startsWith("^or"))
                            continue;

                        //idx = resultCopy.indexOf(st); // Тут мы поиск заменим со стандартного на поиск по проценту совпадения (сделаем настраиваемо) https://lucene.apache.org/core/ todo
                        // Получение индекса с использование нечеткого поиска.
                        idx = getIdxFoundWord(idx, st, resultCopy, fileInfo.getFilePath(), templateRec.useFuzzySearch);

                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug(Thread.currentThread() + fileInfo.getFilePath() + "  фраза: " + st + " найдена: " + (idx != -1));

                        if (idx == -1) {
                            // Если следующее условие не "ИЛИ" то прерываем, в противном случае проверим условие "ИЛИ"
                            if (!(i < entry.getValue().size() - 2 && entry.getValue().get(i + 1).startsWith("^or")))
                                break;
                        } else {
                            idx = idx + st.length();//Найденный индекс + длинна найденного слова.
                            // Если нашли слово, но следующее выражение стоит "ИЛИ", то последующее за "ИЛИ" слово - пропускаем
                            if (i < entry.getValue().size() - 2 && entry.getValue().get(i + 1).startsWith("^or"))
                                i = +2;
                        }
                    }

                    // Нашли слово? Производим обрезку!
                    if (idx != -1)
                        resultCopy = resultCopy.substring(idx);

                    // Если не нашли искомые строки, то вставляем пустое значение.
                    // В пративном случае получаем значение из текста.
                    if (idx == -1) fileInfo.addFoundWord(entry.getKey(), "");
                    else {
                        // Получим коллекцию из слов, далее работать будем с ней.
                        ArrayList<String> resultCol = Stream.of(resultCopy)
                                // Указываем, что он должен быть параллельным
                                .parallel()
                                // Убираем из каждой строки знаки препинания и переносы строки
                                .map(line -> line.replaceAll("[\\Q!\"#$%&'()*+,.:;<=>?@[]^`{}~\n\\E]", " ")) // todo остановился тут, нужно убрать знак "-" "(\\pP&&[^-])|\\n" Правильно так: [\Q!"#$%&'()*+,./:;<=>?@[\]^_`{|}~\n\E]
                                // Каждую строку разбивваем на слова и уплощаем результат до стримма слов
                                .flatMap(line -> Arrays.stream(line.split(" ")))
                                // Обрезаем пробелы
                                .map(String::trim)
                                // Отбрасываем невалидные слова
                                .filter(word -> !"".equals(word))
                                // Оставляем только первые 3
                                //.limit(3) // Пока не ограничиваем поиск 3-мя значениями...
                                // Создаем коллекцию слов
                                .collect(toCollection(ArrayList::new));

                        // Если тип дата, то составляем значение из з-х
                        if (entry.getKey().type == DataTypesConversion.DATE) {
                            if (resultCol.size() > 2) {

                                String dateStr;
                                String pattern;
                                boolean continueSearch = true;
                                boolean successfulSearch = false;

                                // todo Если нам будут попадаться даты в с разделителями "-" или "_", обработать их тут!

                                // Если установлен признак searchType, то ищем дату, пока не найдём,
                                // иначе делаем одну итерацию поиска.
                                while (continueSearch) {

                                    idxWord++;
                                    continueSearch = searchType && resultCol.size() > idxWord + 1;
                                    // пока работаем с 2 форматами дат...
                                    if (resultCol.get(idxWord).matches("\\d{2}"))
                                        pattern = "dd MM yyyy";
                                    else if (resultCol.get(idxWord).matches("[А-Яа-я]+$"))
                                        pattern = "dd MMMM yyyy";
                                    else continue;

                                    // Получим строку из 3 слов для определения даты.
                                    dateStr = String.join(" ", resultCol.subList(idxWord - 1, idxWord + 2));

                                    try {
                                        // Распарсим полученную дату, затем переведем её в формат ISO 8601
                                        Date date = new SimpleDateFormat(pattern).parse(dateStr);
                                        fileInfo.addFoundWord(entry.getKey(), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date));
                                        continueSearch = false;
                                        successfulSearch = true;
                                    } catch (ParseException e) {
                                        //continue; Не получилось? Продолжаем поиски!
                                    }
                                }

                                if (!successfulSearch)
                                    fileInfo.addFoundWord(entry.getKey(), "");

                            } else fileInfo.addFoundWord(entry.getKey(), "");
                        } else if (resultCol.size() > 0)
                            // поиск данных по типу пока работает только для даты и числа.
                            if (searchType && entry.getKey().type == DataTypesConversion.DECIMAL) {
                                while (resultCol.size() > idxWord) {
                                    if (resultCol.get(idxWord).matches("\\d+")) { //todo затестить на корректность обработки символов и дабла
                                        fileInfo.addFoundWord(entry.getKey(), resultCol.get(idxWord));
                                        break;
                                    }
                                    idxWord++;
                                }
                            } else fileInfo.addFoundWord(entry.getKey(), resultCol.get(idxWord));
                        else fileInfo.addFoundWord(entry.getKey(), "");
                    }
                }

                // Запишим инфо файл в очередь, для отправки в 1С.
                filesToSend.put(fileInfo);

                // Удалим индекс, если испольщуется нечеткий поиск (templateRec.useFuzzySearch)
                if (templateRec.useFuzzySearch)
                    LuceneSearch.deleteFieldFromIndex(fileInfo.getFilePath(), result);

            } catch (InterruptedException e) {
                // Если выбрасывается исключение InterruptedException,
                // то флаг (isInterrupted()) не переводится в true. Для этого
                // вручную вызывается метод interrupt() у текущего потока.
                Thread.currentThread().interrupt();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Recognizer was interrupt:", e);
            }
//            }
        }

        private int getIdxFoundWord(int idxStartsWith, String toSearch, String text, String file, boolean useFuzzySearch){

            int idx;
            idxStartsWith = idxStartsWith == -1 ? 0 : idxStartsWith;
            if (useFuzzySearch){
                idx = LuceneSearch.getIdxFoundWord(toSearch, file, idxStartsWith, 10);
            }else idx = text.indexOf(toSearch, idxStartsWith);

            return idx;
        }

        private boolean prepareResultString(String resultStr, Map.Entry<WantedValues, List<String>> entry){ // todo подумать, может и удалить её вовсе...

            int idx = -1;
            int idxWord = 0;
            boolean searchType = false;
            for (int i = 0; i < entry.getValue().size(); i++) {
                String st = entry.getValue().get(i);
                // Проверки на условия "Взять следующий за" и "Искать тип"
                if (st.startsWith("^getNextAfter")) { //todo подумать над названием
                    idx = 0; // Установим значение отличное от -1
                    if (i == entry.getValue().size()-1)
                        break;
                    if (entry.getValue().get(i+1).matches("\\d+")){ //todo затестить на корректность обработки символов и дабла
                        idxWord = Integer.parseInt(entry.getValue().get(i+1));
                        if (i+3 < entry.getValue().size() && entry.getValue().get(i+2).startsWith("^searchType"))
                            searchType = true;
                    }else if (entry.getValue().get(i+1).startsWith("^searchType"))
                        searchType = true;
                    break;
                }
                if (entry.getValue().get(i+1).startsWith("^searchType")){
                    searchType = true;
                    break;
                }

                idx = resultStr.indexOf(st); // Тут мы поиск заменим со стандартного на поиск по проценту совпадения (сделаем настраиваемо) https://lucene.apache.org/core/ todo
                if (idx == -1)
                    // Проверка на или
                    if (i == entry.getValue().size()-1 || !entry.getValue().get(i+1).startsWith("^or"))
                        break;
                    else resultStr = resultStr.substring(idx + st.length());
            }

            return idx != -1;

        }
    }
}

