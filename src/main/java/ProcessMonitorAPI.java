import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class ProcessMonitorAPI {

    // Файлы для обработки
    BlockingQueue<FileInfo> filesForProcess = new ArrayBlockingQueue(200);
    // Обработанные файлы, данные по которым отправляем в 1С
    BlockingQueue<FileInfo> filesToSend = new ArrayBlockingQueue(200);
    // Список шаблонов для распознования
    HashMap<String, TemplateRecognition> templatesRecognition = new HashMap<>();

    public ProcessMonitorAPI() {

        List<Thread> threads = new ArrayList<>();

        // MonitorDirectories будет:
        // 1. Получать настройки из 1С через REST
        // 2. Мониторить папки и записывать информацию о файлах в filesForProcess
        // 3. Мониторить filesToSend и записывать инфо в 1С через rest.
        threads.add(new MonitorDirectories());


        // Создаем потоки (Их число будет настраиваться в 1С) которые будут ожидать take на filesForProcess,
        // а когда файл будет обработан, записывать его filesToSend
        // Пока запускаем треды по кличеству процессоров. В дальнейшем будем получать настройку из 1С todo
        int proc = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < proc; i++) {
            // Пока запустим напрямую, в дальнейшем переделаем Executors.newFixedThreadPool(proc); todo
            threads.add(new Recognizer());
        }

        // Запустим потоки
        for (Thread thread : threads)
            thread.start();

        // Тут будет развернут REST сервис, который будет возвращать информацию о текущей работе приложения
        // А также мониторить потоки, если какой то отвалится, запускать заново
        // TODO

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
        long updateTime = 60000;

        private void writeDirectoryForMonitor() {

            directoryForMonitor.clear();

            // Подключаемся к 1С чере rest, забираем данные о папках для мониторинга + ссылку на шаблон для распознования
            // TODO

            // Заполняем полученные папки в directoryForMonitor
            // TODO

            // Для теста пока будем использовать одну папку и один шаблон для распознования
            directoryForMonitor.put(new File("D:\\Java\\Для распознования\\Сканы"), "1111"); //"D:\\Учеба JAVA\\Для распознования\\Сканы"

        }

        private void writeProcessedFile() {

            processedFile.clear();

            // Подключаемся к 1С через rest, получаем данные о отработанных файлах на стороне java, но не подтвержденных на стороне 1С..
            // TODO

            // Заполняем полученные файлы в processedFile
            // TODO

            // Для теста
            processedFile.add(new File("D:\\Java\\Для распознования\\Сканы\\7806474760_780601001_2015-02-10_00000973.pdf")); // "D:\\Учеба JAVA\\Для распознования\\Сканы\\7806474760_780601001_2015-02-10_00000973.pdf"

        }

        private void writeTemplatesRecognition() {

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
            templatesRecognition.put("1111", new TemplateRecognition("1111", wantedWords)); //Подумать, а надо ли ИД в TemplateRecognition, он же есть в мапе.

        }

        @Override
        public void run() {

            // Прочитаем какие папки нужно мониторить.
            writeDirectoryForMonitor();

            // Заполним первоначальные данные о отработанных файлах, их обрабатывать не нужно
            writeProcessedFile();

            // Заполним шаблоны для распознования
            writeTemplatesRecognition();

            FileInfo curFileToSend;
            while (!Thread.currentThread().isInterrupted()) {

                // Промониторим папки, новые файлы запишем в filesInProcess и filesForProcess, для последующего разбора
                File[] arrayFiles;
                for (Map.Entry<File, String> dir : directoryForMonitor.entrySet()) {
                    if (dir.getKey().isFile()) continue;

                    arrayFiles = dir.getKey().listFiles();
                    for (File file : arrayFiles) {
                        if (!filesInProcess.contains(file) && !processedFile.contains(file)) {
                            filesInProcess.add(file);
                            try {
                                filesForProcess.put(new FileInfo(dir.getValue(), file));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Отправим файлы в 1С, перепишим их из обрабатывающихся в обработанные.
                while (filesToSend.size() > 0) {
                    curFileToSend = filesToSend.poll();
                    // Тут будет отправка распознанных данных в 1С (скорее всего РС(путь к файлу, структура распознанная)
                    // todo

                    processedFile.add(curFileToSend.file);
                    filesInProcess.remove(curFileToSend.file);

                    // Для теста выведем сообщение в консоль. удалить потом todo
                    System.out.println(curFileToSend);
                }

                // Автообновления списка обработанных файлов (чтобы список обработанных не расширялся до бесконечности)
                // А также рабочих директорий.
                if (curTime + updateTime < System.currentTimeMillis()) {
                    writeDirectoryForMonitor();
                    //writeProcessedFile(); т.к. пока нет запроса о обработанный файлах в 1С не выполняем, иначе идёт вечное распознование todo
                    writeTemplatesRecognition();
                    curTime = System.currentTimeMillis();
                }
            }
        }
    }

    protected class Recognizer extends Thread {

        @Override
        public void run() {

            FileInfo fileInfo;
            String result;
            TemplateRecognition templateRec;

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    // Дождемся файла
                    fileInfo = filesForProcess.take();

                    // Распознаем нужную область
                    templateRec = templatesRecognition.get(fileInfo.templateID);
                    if (templateRec == null) continue; // возможно тут будет запись в лог файл todo

                    ITesseract instance = new Tesseract();  // JNA Interface Mapping
                    instance.setLanguage("rus");
                    try {
                        result = instance.doOCR(fileInfo.file, templateRec.areaRecognition);
                    } catch (TesseractException e) {
                        System.err.println(e.getMessage()); // Тут будет запись в лог файл TODO
                        continue;
                    }

                    // Для теста, удалить потом todo
                    System.out.println(result);
                    result.toLowerCase(); // Почему то не работает todo

                    // Сформируем структуру ответа с найденными словами.
                    fileInfo.foundWords = new HashMap<>();
                    for (Map.Entry<WantedValues, List<String>> entry : templateRec.wantedWords.entrySet()) {
                        String resultCopy = new String(result);
                        int idx = -1;
                        for (String st : entry.getValue()) {
                            idx = resultCopy.indexOf(st); // Тут мы поиск заменим со стандартного на поиск по проценту совпадения https://lucene.apache.org/core/ todo
                            if (idx == -1) break;
                            resultCopy = resultCopy.substring(idx + st.length());
                        }

                        // Если не нашли искомые строки, то вставляем пустое значение.
                        // В пративном случае получаем значение из текста.
                        if (idx == -1) fileInfo.foundWords.put(entry.getKey(), "");
                        else {
                            // Получим коллекцию из 3 слов, для того, чтобы было удобно парсить дату.
                            ArrayList<String> resultCol = Stream.of(resultCopy)
                                    // Указываем, что он должен быть параллельным
                                    .parallel()
                                    // Убираем из каждой строки знаки препинания
                                    .map(line -> line.replaceAll("\\pP", " "))
                                    // Каждую строку разбивваем на слова и уплощаем результат до стримма слов
                                    .flatMap(line -> Arrays.stream(line.split(" ")))
                                    // Обрезаем пробелы
                                    .map(String::trim)
                                    // Отбрасываем невалидные слова
                                    .filter(word -> !"".equals(word))
                                    // Оставляем только первые 3
                                    .limit(3)
                                    // Создаем коллекцию слов
                                    .collect(toCollection(ArrayList::new));

                            // Если тип дата, то составляем значение из з-х
                            if (entry.getKey().type == DataTypesConversion.DATE) {
                                if (resultCol.size() == 3) {
                                    String pattern;

                                    // пока работаем с 2 форматами дат...
                                    if (resultCol.get(1).matches("\\d{2}"))
                                        pattern = "dd MM yyyy";
                                    else pattern = "dd MMMM yyyy";

                                    // Распарсим полученную дату, затем переведем её в формат ISO 8601
                                    Date date = new SimpleDateFormat(pattern).parse(String.join(" ", resultCol));
                                    fileInfo.foundWords.put(entry.getKey(), new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).format(date));

                                } else fileInfo.foundWords.put(entry.getKey(), "");
                            } else if (resultCol.size() > 0) fileInfo.foundWords.put(entry.getKey(), resultCol.get(0));
                            else fileInfo.foundWords.put(entry.getKey(), "");
                        }
                    }

                    // Запишим инфо файл в очередь, для отправки в 1С.
                    filesToSend.put(fileInfo);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

