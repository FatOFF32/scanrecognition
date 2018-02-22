package root;

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

    @Override
    public String toString() {
        return "FileInfo{" +
                "templateID='" + templateID + '\'' +
                ", file=" + file +
                ", foundWords=" + foundWords +
                '}';
    }

    public static String getFileExtension(String fileName) {
        // если в имени файла есть точка и она не является первым символом в названии файла
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки до последней точки в названии файла, то есть ХХХХХ.txt -> .txt
            return fileName.substring(fileName.lastIndexOf("."));
            // в противном случае возвращаем пустую строку, то есть расширение не найдено
        else return "";
    }
}
