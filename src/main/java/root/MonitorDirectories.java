package root;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class MonitorDirectories extends Thread {

    private ProcessMonitor pm;
    private HashMap<File, String> directoryForMonitor; // Директории для мониторинга
    private HashMap<String, TemplateRecognition> templatesRecognition; // Список шаблонов для распознования
    private HashSet<String> listKeyWordsSearch; // todo подумать, может быть использовать их для чего то в 1с?
    private Set<File> processedFile;// Файлы которые уже обработали, их не трогаем
    private Set<File> filesInProcess;// Файлы в обработке
    private ThreadPoolExecutor poolExecutor; // Пул потоков распознавателя, задачи для обработки
//    private BlockingQueue<IFileInfo> filesToSend; // Обработанные файлы, данные по которым отправляем в 1С todo delete
    private List<Future<IFileInfo>> taskInProcess; // Обработанные файлы, данные по которым отправляем в 1С
    // Текущее время для автообновления настроек из 1С. Будем запрашивать обновления каждую минуту.
    private long curTime = System.currentTimeMillis();
    private long updateTime = 60000;
//    // Информацию для rest сервиса получаем из 1С todo почистить коментарии с переменными
//    private static volatile String url = ""; // Инициализируем для synchronized // "http://localhost/BuhCORP/odata/standard.odata"; //"http://10.17.1.109/upp_fatov/odata/standard.odata";
//    private static volatile String userName; // = "testOData";//"test";
//    private static volatile String pass; // = "123456";//"111";
//    private static volatile Integer quantityThreads = 1;
//    private static volatile int restPort; // Устанавливаем в параметрах запуска

    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);

    // todo Why I used this synchronized? May be it isn't necessary?
    // todo I should return this method to ProcessMonitor because this object can be stopped (RecognizerStatus.ISSHUTDOWN)
    // todo I made singleton ProcessMonitor, and I should use this object
//    public static synchronized void setSettings1C(JsonNode rootNode) {
//
//        MonitorDirectories.url = rootNode.get("URLRESTService1C").asText();
//        MonitorDirectories.userName = rootNode.get("Пользователь").asText();
//        MonitorDirectories.pass = rootNode.get("Пароль").asText();
//        MonitorDirectories.quantityThreads = rootNode.get("КоличествоПроцессовАвтораспознавания").asInt();
//
//        MonitorDirectories.class.notifyAll();
//    }

    MonitorDirectories() {

        pm = ProcessMonitor.getInstance();
        directoryForMonitor = new HashMap<>();
        templatesRecognition = new HashMap<>();
        listKeyWordsSearch = new HashSet<>();
        processedFile = new HashSet<>();
        filesInProcess = new HashSet<>();
        poolExecutor = new ThreadPoolExecutor(1, 1, 15, TimeUnit.MINUTES, new ArrayBlockingQueue<>(200));
//        filesToSend = new ArrayBlockingQueue<>(200); //todo delete
        taskInProcess = new LinkedList<>();

        // Заполним список ключевых слов поиска. Необходим для последующей обработке в коде
        listKeyWordsSearch.add("^getNextAfter");
        listKeyWordsSearch.add("^searchType");
        listKeyWordsSearch.add("^joinWordsTo");
        listKeyWordsSearch.add("^or");
    }

    void shutdown(long timeout) {

        this.interrupt();
        poolExecutor.shutdown();

        try {
            poolExecutor.awaitTermination(timeout, TimeUnit.SECONDS);
            sendRecognizedData();
        } catch (InterruptedException e) {
            e.printStackTrace();
            poolExecutor.shutdownNow();
        }

    }

    void updateSettings() {

        // Проверим, менялось ли количество потоков
        int quantityThreads = pm.getQuantityThreads();
        if (quantityThreads < 2)
            quantityThreads = 1;

        if (quantityThreads != poolExecutor.getMaximumPoolSize()) {
            poolExecutor.setCorePoolSize(quantityThreads);
            poolExecutor.setMaximumPoolSize(quantityThreads);
        }
    }

    private JsonNode getResultQuery1C(String query) {

        ClientConfig config = new ClientConfig();
//        config.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.OFF, LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));
        Client client = ClientBuilder.newClient(config);

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.universal(pm.getUserName(), pm.getPass());
        client.register(feature);
//        WebTarget webTarget = client.target(url).path(query); // Почему то так не всегда работает.
        WebTarget webTarget = client.target(pm.getUrl() + query);


        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        if (response.getStatus() != 200) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Не удалось выполнить GET запрос к oData 1С  \n\t\t" + pm.getUrl() + query + " \n\t\t HTTP error code : " + response.getStatus());
            return null;
        }

        // Результат будем писать в лог, на самом низком уровне логирования
        String resut = response.readEntity(String.class);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Результат запроса к oData 1С. Запрос: \n\t\t" + pm.getUrl() + query + "\n\t\t Результат: " + resut);

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

    private boolean putObjectTo1C(String query, String jsonObj) {

        ClientConfig config = new ClientConfig();
//        config.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.OFF, LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));

        Client client = ClientBuilder.newClient(config);
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(pm.getUserName(), pm.getPass());
        client.register(feature);
//        WebTarget webTarget = client.target(url).path(query); // Так почему то не работает!
        WebTarget webTarget = client.target(pm.getUrl() + query);


        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.post(Entity.entity(jsonObj, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Не удалось выполнить POST запрос к oData 1С. \n\t\t Адрес: " + pm.getUrl() + query +
                        "\n\t\t Запрос: " + jsonObj + "\n\t\t HTTP error code : " + response.getStatus());
            return false;
        }

        // Результат будем писать в лог, на самом низком уровне логирования
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Запрос успешно отправлен в 1С через oData. \n\t\t Адрес: " + pm.getUrl() + query +
                    "\n\t\t Запрос: " + jsonObj + "\n\t\t HTTP error code : " + response.getStatus());

        return true;
    }

    private void sendRecognizedData() throws InterruptedException {

        IFileInfo curFileToSend;
        Future<IFileInfo> curTask;
        Iterator<Future<IFileInfo>> iterator = taskInProcess.listIterator();
//        while (filesToSend.size() > 0) { //todo del
        while (iterator.hasNext()) {
//            curFileToSend = filesToSend.poll(); // todo del
            curTask = iterator.next();
            if (curTask.isCancelled()) {
                iterator.remove();
                continue;
            }
            else if (curTask.isDone()) {
                try {
                    curFileToSend = curTask.get();
                } catch (ExecutionException e) {
                    e.printStackTrace(); // todo добавить запись в лог!
                    continue;
                }
            } else continue;


            // Преобразуем полученные данные в JSON и отправим в 1С
            String jsonObj = "";
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode procData = mapper.createObjectNode();
            procData.put("ПутьКФайлу", curFileToSend.getFile().getPath());
            procData.put("ШаблонАвтораспознавания_Key", curFileToSend.getTemplateRecognition().getTemplateID());

            ObjectNode resRecognition = mapper.createObjectNode();
            for (Map.Entry<WantedValues, String> wv : curFileToSend.getFoundWords().entrySet())
                resRecognition.put(wv.getKey().name, wv.getValue());

            try {
                procData.put("РаспознанныеДанныеJSON", mapper.writeValueAsString(resRecognition));
                jsonObj = mapper.writeValueAsString(procData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Todo $$$ Остановился тут! Нужно сделать в цикле проверку соединения с 1с. Если не удалось,
            //  то делаем несколько попыток соединения, если соединения нет, прерываем цикл.
            if (putObjectTo1C("/InformationRegister_со_ОбработанныеСканыАвтораспознавателем", jsonObj)) {
                processedFile.add(curFileToSend.getFile());
                filesInProcess.remove(curFileToSend.getFile());
            } else if (pm.isRunning()) { // Если не удалось отправить - подождем, возможно идет обноление БД 1C
                Thread.sleep(10000);
//                filesToSend.put(curFileToSend); todo del
            }
        }

    }

    private void writeTemplatesRecognition() {

        // Подключаемся к 1С через rest, забираем данные о папках для мониторинга и на шаблон для распознования // todo переделать под StringBuilder
        JsonNode templates = getResultQuery1C("/Catalog_со_ШаблоныАвтораспознавания?" + // Имя справочника
                "$filter=DeletionMark%20ne%20true" + //$select=Ref_Key,КаталогПоискаСканов,СтрокиПоиска" + // выборки, фильтры (фильтры пока убрал)
                "&$orderby=СтрокиПоиска/ИмяИскомогоЗначения,СтрокиПоиска/LineNumber%20asc"); // Сортировка

        if (templates == null)
            return;

        // todo In my opinion, this code can be optimized for GC. Can we not clear these HashMap (directoryForMonitor and templatesRecognition),
        //  and create new HashMaps with updated data. After that we can compare these HashMaps and updated HashMaps, then
        //  if these HashMaps aren't equal, we can replace the old HashMaps with the updated HashMaps. If these HashMaps
        //  are equal, the updated HashMaps will clean minor GC.
        directoryForMonitor.clear();
        templatesRecognition.clear();

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
                // If this word isn't key, we should save it with lower case.
                if (listKeyWordsSearch.contains(sStr.get("СтрокаПоиска").asText())) {
                    list.add(sStr.get("СтрокаПоиска").asText());
                } else list.add(sStr.get("СтрокаПоиска").asText().toLowerCase());
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
    }

    private void writeProcessedFile() {

        // Подключаемся к 1С через rest, получаем данные о отработанных файлах на стороне java, но не подтвержденных на стороне 1С..
        JsonNode templates = getResultQuery1C("/InformationRegister_со_ОбработанныеСканыАвтораспознавателем?$select=ПутьКФайлу");
        if (templates == null)
            return;

        processedFile.clear();

        for (JsonNode template : templates) {

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

        try {

            // Поспим, пока не прилетит настройка из 1С. Будет возникать во время первого запуска автораспознавателя
            while (pm.isWaitingForSettings()) {
                this.wait();
            }

            // Заполним первоначальные данные о отработанных файлах, их обрабатывать не нужно
            writeProcessedFile();

            // Заполним папки для мониторинга и шаблоны для распознования
            writeTemplatesRecognition();

            while (!Thread.currentThread().isInterrupted()) {

                // Промониторим папки, новые файлы запишем в filesInProcess и добавим в пул задач (poolExecutor), для последующего разбора
                File[] arrayFiles;
                for (Map.Entry<File, String> dir : directoryForMonitor.entrySet()) {
                    if (dir.getKey().isFile()) continue;

                    arrayFiles = dir.getKey().listFiles();
                    if (arrayFiles == null) continue;
                    for (File file : arrayFiles) {

                        if (poolExecutor.getQueue().remainingCapacity() == 0)
                            break;

                        if (!filesInProcess.contains(file) && !processedFile.contains(file)) {
                            filesInProcess.add(file);
                            poolExecutor.submit(new Recognizer(new FileInfo(templatesRecognition.get(dir.getValue()), file)));
                        }
                    }
                }

                // Отправим файлы в 1С, перепишим их из обрабатывающихся в обработанные.
                sendRecognizedData();

                // Автообновления списка обработанных файлов (чтобы список обработанных не расширялся до бесконечности),
                // а также рабочих директорий.
                if (curTime + updateTime < System.currentTimeMillis()) {
                    writeProcessedFile();
                    writeTemplatesRecognition();
                    curTime = System.currentTimeMillis();
//                    System.out.println("Файлы в работе:" + filesInProcess.toString()); // todo Удалить
                }
            }
        } catch (InterruptedException e) {
            // Если исключение InterruptedException вызвано не методом interrupt,
            // то флаг (isInterrupted()) не переводится в true. Для этого
            // вручную вызывается метод interrupt() у текущего потока.
            Thread.currentThread().interrupt();
            e.printStackTrace();
            // Interrupt pool executor when status isn't "shutdown"
            if (!pm.isShutdown())
                poolExecutor.shutdownNow();
        }
    }
}

