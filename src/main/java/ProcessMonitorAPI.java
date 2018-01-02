import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProcessMonitorAPI {

    BlockingQueue<FileInfo> fileInProcess = new ArrayBlockingQueue(200);
    BlockingQueue<FileInfo> ProcessedFile = new ArrayBlockingQueue(200);

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
}
