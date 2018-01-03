import java.io.File;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProcessMonitorAPI {

    // Файлы для обработки
    BlockingQueue<FileInfo> filesForProcess = new ArrayBlockingQueue(200);
    // Обработанные файлы, данные по которым отправляем в 1С
    BlockingQueue<FileInfo> filesToSend = new ArrayBlockingQueue(200);
    // Список шаблонов для распознования
    HashMap<String, TemplateRecognition> templatesRecognition = new HashMap<>();

    public ProcessMonitorAPI() {

        // Читаем данные через рест интерфейс из 1С, какие папки мониторить...
        // TODO

        // Запускаем поток, который мониторит папки и записывает информацию о файлах в fileInProcess
        // Также этот же поток, будет мониторить ProcessedFile (мб сделать другим потоком) и записывать инфо в 1С через rest.
        // TODO

        // Запускаем потоки (Их число будет настраиваться в 1С) которые будут ожидать take на fileInProcess,
        // а когда файл будет обработан, записывать его ProcessedFile
        // TODO
    }

    protected class MonitorDirectories extends Thread{

        // Директории для мониторинга
        HashMap<File, String> directoryForMonitor = new HashMap<>();
        // Файлы в обработке
        Set<File> filesInProcess = new HashSet<>();
        // Файлы которые уже обработали, их не трогаем
        Set<File> processedFile = new HashSet<>();
        // Текущее время для автообновления настроек из 1С. Будем запрашивать обновления каждую минуту.
        long curTime = System.currentTimeMillis();
        long updateTime = 60000;

        private void writeDirectoryForMonitor(){

            directoryForMonitor.clear();

            // Подключаемся к 1С чере rest, забираем данные о папках для мониторинга + ссылку на шаблон для распознования
            // TODO

            // Заполняем полученные папки в directoryForMonitor
            // TODO

            // Для теста пока будем использовать одну папку и один шаблон для распознования
            directoryForMonitor.put(new File("D:\\Учеба JAVA\\Для распознования\\Сканы"), "1111");

        }

        private void writeProcessedFile(){

            processedFile.clear();

            // Подключаемся к 1С через rest, получаем данные о отработанных файлах на стороне java, но не подтвержденных на стороне 1С..
            // TODO

            // Заполняем полученные файлы в processedFile
            // TODO

            // Для теста
            processedFile.add(new File("D:\\Учеба JAVA\\Для распознования\\Сканы\\7806474760_780601001_2015-02-10_00000973.pdf"));

        }

        private void writeTemplatesRecognition(){

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
            while (!Thread.currentThread().isInterrupted()){

                // Промониторим папки, новые файлы запишем в filesInProcess и filesForProcess, для последующего разбора
                File[] arrayFiles;
                for(Map.Entry<File, String> dir : directoryForMonitor.entrySet()){
                    if (dir.getKey().isFile()) continue;

                    arrayFiles = dir.getKey().listFiles();
                    for (File file : arrayFiles){
                        if (!filesInProcess.contains(file) && !processedFile.contains(file)){
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
                while (filesToSend.size() > 0){
                    curFileToSend = filesToSend.poll();
                    // Тут будет отправка файла в 1С
                    // todo

                    processedFile.add(curFileToSend.file);
                    filesInProcess.remove(curFileToSend.file);
                }

                // Автообновления обработанных файлов (чтобы список обработанных не расширялся до бесконечности)
                // А также рабочих директорий.
                if (curTime + updateTime < System.currentTimeMillis()){
                    writeDirectoryForMonitor();
                    writeProcessedFile();
                    writeTemplatesRecognition();
                    curTime = System.currentTimeMillis();
                }
            }
        }
    }
}

