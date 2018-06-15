package root;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileInfo {

    //TemplateRecognition templateRecognition; // решил пока не использовать ссылку на шаблон, будем использовать ИД.
    String templateID;
    File file;
    HashMap<WantedValues, String> foundWords;

    public FileInfo(String templateID, File file) {
        this.templateID = templateID;
        this.file = file;
        this.foundWords = new HashMap<>();
    }

    public void addFoundWord(WantedValues key, String value){
        foundWords.put(key, value);
    }

    public void addFoundWord(WantedValues key, ArrayList<String> list, int idxWord, boolean JoinWord){

        if (JoinWord){

            StringBuilder sb = new StringBuilder();
            for (String word : list)
                sb.append(word);
            foundWords.put(key, sb.toString());

        } else {
            foundWords.put(key, list.get(idxWord));
        }
    }

    public boolean foundWordIsEmpty(){

        boolean isEmpty = true;
        for (Map.Entry<WantedValues, String> entry : foundWords.entrySet()){
            if (!entry.getValue().isEmpty()){
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    public void clearFoundWord(){
        foundWords.clear();
    }

    public String getFilePath(){
        return file.getPath();
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
