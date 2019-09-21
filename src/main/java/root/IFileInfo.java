package root;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public interface IFileInfo {

    File getFile();
    HashMap<WantedValues, String> getFoundWords();
    void addFoundWord(WantedValues key, String value);
    void addFoundWord(WantedValues key, List<String> list);
    boolean foundWordIsEmpty();
    String getFilePath();
    TemplateRecognition getTemplateRecognition();

    static String getFileExtension(String fileName) {
        // если в имени файла есть точка и она не является первым символом в названии файла
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки до последней точки в названии файла, то есть ХХХХХ.txt -> .txt
            return fileName.substring(fileName.lastIndexOf("."));
            // в противном случае возвращаем пустую строку, то есть расширение не найдено
        else return "";
    }

}
