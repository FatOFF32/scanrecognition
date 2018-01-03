import java.io.File;
import java.util.HashMap;

public class FileInfo {

    //TemplateRecognition templateRecognition; // решил пока не использовать ссылку на шаблон, будем использовать ИД.
    String templateID;
    File file;
    HashMap<WantedValues, String> foundWords;

    public FileInfo(String templateID, File file) {
        this.templateID = templateID;
        this.file = file;
    }
}
