import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import javax.print.attribute.standard.NumberUp;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class test {
    public static void main(String[] args) throws ParseException {

        // попробуем распарсить нужную нам информацию...
        List<String> list = new ArrayList<>();
//        String st = "Счет-фактура № 00000973 от 10 Февраля 2015 г.".toLowerCase();
        String st = "Счет-фактура № 00000973 от 10.02.2015 г.".toLowerCase();
        // 1. Если просто номер СФ
/*
        list.add("Счет-фактура".toLowerCase());
        list.add("№".toLowerCase());
*/
        // 2. Дата СФ
        list.add("Счет-фактура".toLowerCase());
        list.add("№".toLowerCase());
        list.add("от".toLowerCase());

        for (String wSt : list){
            int idx = st.indexOf(wSt); // Тут мы поиск заменим со стандартного на поиск по проценту совпадения https://lucene.apache.org/core/ todo
            if (idx == -1) break;
            st = st.substring(idx + wSt.length());
        }

        // Проба
        //String[] res = st.trim().replaceAll("\\p{Punct}", " ").split("\\s");
//        System.out.println(Arrays.toString(res));

        // Рабочий вариант
        ArrayList<String> res = Stream.of(st)
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

        String pattern;
        if (res.get(1).matches("\\d{2}")) // можно и так d+
            pattern = "dd MM yyyy";
        else pattern = "dd MMMM yyyy";

        Date date = new SimpleDateFormat(pattern).parse(String.join(" ", res));

        System.out.println(String.join(" ", res));


        System.out.println(new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssz" ).format(date));

        Query query = new FuzzyQuery(new Term("Счет-фактура", " бла бла бла Счdет-fфактура бла бла бла"));
        new IndexSearcher()

    }

    public static void recognize(){
        File imageFile = new File("D:\\Учеба JAVA\\Для распознования\\7806474760_780601001_2015-02-10_00000973.pdf"); //"D:\\Учеба JAVA\\ДЗ\\img1.jpg");
        ITesseract instance = new Tesseract();  // JNA Interface Mapping
//        ITesseract instance = new Tesseract1(); // JNA Direct Mapping
//        File tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build only; only English data bundled
//        instance.setDatapath(tessDataFolder.getAbsolutePath());
        instance.setLanguage("rus");
        try {
            String result = instance.doOCR(imageFile);
            System.out.println(result);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }

/*
        TessBaseAPI tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(DATA_PATH, "eng");
        tessBaseApi.setImage(bitmap);
        String extractedText = tessBaseApi.getUTF8Text();
        tessBaseApi.end();
        return extractedText;
*/
    }
}
