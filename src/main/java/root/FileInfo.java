package root;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileInfo implements IFileInfo {

    private TemplateRecognition templateRecognition;
    //String templateID; // templateID already has in templateRecognition // todo detele!
    private File file;
    private HashMap<WantedValues, String> foundWords;

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public HashMap<WantedValues, String> getFoundWords() {
        return foundWords;
    }

    public FileInfo(TemplateRecognition templateRecognition, File file) {
        this.templateRecognition = templateRecognition;
        this.file = file;
        this.foundWords = new HashMap<>();
    }

    @Override
    public void addFoundWord(WantedValues key, String value){
        foundWords.put(key, value);
    }

    @Override
    public void addFoundWord(WantedValues key, List<String> list){//, int idxWord, boolean JoinWord){ todo delete

//        if (JoinWord){todo delete
//
            StringBuilder sb = new StringBuilder();
            for (String word : list)
                sb.append(word);
            foundWords.put(key, sb.toString());

//        } else {todo delete
//            foundWords.put(key, list.get(idxWord));
//        }
    }

    @Override
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

//    public void clearFoundWord(){ // todo delete
//        foundWords.clear();
//    }

    @Override
    public String getFilePath(){
        return file.getPath();
    }

    @Override
    public TemplateRecognition getTemplateRecognition() {
        return templateRecognition;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "templateID='" + templateRecognition.getTemplateID() + '\'' +
                ", file=" + file +
                ", foundWords=" + foundWords +
                '}';
    }

//    public static String getFileExtension(String fileName) { todo перенес в интерфейс. Удалить!
//        // если в имени файла есть точка и она не является первым символом в названии файла
//        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
//            // то вырезаем все знаки до последней точки в названии файла, то есть ХХХХХ.txt -> .txt
//            return fileName.substring(fileName.lastIndexOf("."));
//            // в противном случае возвращаем пустую строку, то есть расширение не найдено
//        else return "";
//    }
}
