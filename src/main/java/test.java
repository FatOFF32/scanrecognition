import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static com.sun.org.apache.xml.internal.utils.DOMHelper.createDocument;
import static java.util.stream.Collectors.toCollection;

public class test {
    public static void main(String[] args) throws ParseException, IOException {

        // попробуем распарсить нужную нам информацию...
        //testParseResult();

        // Попытка поработать с нечетким поиском
        test.testSearch();


    }

    public static void testParseResult() throws ParseException {
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

        for (String wSt : list) {
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


        System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(date));
    }

    public static void testSearch() throws IOException, ParseException {
        //Query query = new FuzzyQuery(new Term("Счет-фактура", " бла бла бла Счет-fфактура бла бла бла"));

//        final Document teaserDoc = test.MessageIndexer.createWith("Счет-фактура бла бла бла  бла бла бла", "Счет-фактура бла бла бла Счет-фактура бла бла бла");
        final Document teaserDoc = test.MessageIndexer.createWith("Привет Хабр!", "Это демонстрация работы простейшего нечёткого поиска");
        final MessageIndexer indexer = new test.MessageIndexer("/tmp/teaser_index");
        indexer.index(true, teaserDoc);

        final TestLucene search = new test.TestLucene(indexer.readIndex());

//        final Scanner reader = new Scanner(System.in);  // Reading from System.in
//        System.out.print("Введите запрос:\t");
//        final String toSearch = reader.nextLine(); // Scans the next token

//        search.fuzzySearch("Счет-фактура", "body", 10);
        search.fuzzySearch("прривт", "body", 10);
    }

    public static class TestLucene {
        private final IndexReader reader;

        public TestLucene(IndexReader reader) {
            this.reader = reader;

        }

        // Нечеткий поиск

        /**
         * Search using FuzzyQuery.
         *
         * @param toSearch    string to search
         * @param searchField field where to search. We have "body" and "title" fields
         * @param limit       how many results to return
         * @throws IOException
         * @throws ParseException
         */

        public void fuzzySearch(final String toSearch, final String searchField, final int limit) throws IOException, ParseException {
            final IndexSearcher indexSearcher = new IndexSearcher(reader);

            final Term term = new Term(searchField, toSearch);

            final int maxEdits = 2; // This is very important variable. It regulates fuzziness of the query
            final Query query = new FuzzyQuery(term, maxEdits);
            final TopDocs search = indexSearcher.search(query, limit);
            final ScoreDoc[] hits = search.scoreDocs;
            showHits(hits);
        }

        private void showHits(final ScoreDoc[] hits) throws IOException {
            if (hits.length == 0) {
                System.out.println("\n\tНичего не найдено");
                return;
            }
            System.out.println("\n\tРезультаты поиска:");
            for (ScoreDoc hit : hits) {
                final String title = reader.document(hit.doc).get("title");
                final String body = reader.document(hit.doc).get("body");
                System.out.println("\n\tDocument Id = " + hit.doc + "\n\ttitle = " + title + "\n\tbody = " + body);
            }
        }

    }

    /**
     * Default Indexer that we will use in tutorial
     * Be default it will use RussianAnalyzer to analyze text
     */
    public static class MessageIndexer {
        private final String pathToIndexFolder;

        /**
         * Creates Lucene Document using two strings: body and title
         *
         * @return resulted document
         */
        public static Document createWith(final String titleStr, final String bodyStr) {
            final Document document = new Document();

            final FieldType textIndexedType = new FieldType();
            textIndexedType.setStored(true);
            textIndexedType.setIndexOptions(IndexOptions.DOCS);
            textIndexedType.setTokenized(true);

            //index title
            Field title = new Field("title", titleStr, textIndexedType);
            //index body
            Field body = new Field("body", bodyStr, textIndexedType);

            document.add(title);
            document.add(body);
            return document;
        }

        /**
         * Get instance of MessageIndex providing path where indexes will be stored
         *
         * @param pathToIndexFolder File System path where indexes will be stored. For example /tmp/tutorial_indexes
         */
        public MessageIndexer(final String pathToIndexFolder) {
            this.pathToIndexFolder = pathToIndexFolder;
        }

        /**
         * Indexing documents using provided Analyzer
         *
         * @param create to decide create new or append to previous one
         * @throws IOException
         */
        public void index(final Boolean create, List<Document> documents, Analyzer analyzer) throws IOException {
            final Directory dir = FSDirectory.open(Paths.get(pathToIndexFolder));
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            final IndexWriter w = new IndexWriter(dir, iwc);
            w.addDocuments(documents);
            w.close();
        }

        /**
         * Indexing documents with RussianAnalyzer as analyzer
         *
         * @param create to decide create new or append to previous one
         * @throws IOException
         */
        public void index(final Boolean create, List<Document> documents) throws IOException {
            final Analyzer analyzer = new RussianAnalyzer();
            index(create, documents, analyzer);
        }

        /**
         * Indexing one document
         *
         * @param create to decide create new or append to previous one
         * @throws IOException
         */
        public void index(final Boolean create, Document document) throws IOException {
            final List<Document> oneDocumentList = new ArrayList<>();
            oneDocumentList.add(document);
            index(create, oneDocumentList);
        }

        /**
         * Get IndexReader by using pathToIndexFolder
         *
         * @return IndexReader or IOException if any
         * @throws IOException
         */
        public IndexReader readIndex() throws IOException {
            final Directory dir = FSDirectory.open(Paths.get(pathToIndexFolder));
            return DirectoryReader.open(dir);
        }

        public String getPathToIndexFolder() {
            return pathToIndexFolder;
        }

        public static String recognize() {
            File imageFile = new File("D:\\Учеба JAVA\\Для распознования\\7806474760_780601001_2015-02-10_00000973.pdf"); //"D:\\Учеба JAVA\\ДЗ\\img1.jpg");
            ITesseract instance = new Tesseract();  // JNA Interface Mapping
//        ITesseract instance = new Tesseract1(); // JNA Direct Mapping
//        File tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build only; only English data bundled
//        instance.setDatapath(tessDataFolder.getAbsolutePath());
            instance.setLanguage("rus");
            String result = "";
            try {
                result = instance.doOCR(imageFile);
                System.out.println(result);
            } catch (TesseractException e) {
                System.err.println(e.getMessage());
            }
            return result;
/*
        TessBaseAPI tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(DATA_PATH, "eng");
        tessBaseApi.setImage(bitmap);
        String extractedText = tessBaseApi.getUTF8Text();
        tessBaseApi.end();
        return extractedText;
*/
        }

// Нечеткий поиск, пока оставил
//    String fieldName = "myField";
//
//    //создание тестового индекса
//    Directory directory = new RAMDirectory();//в "настоящей" Системе здесь должно быть FSDirectory.open(dir)
//    RussianAnalyzer analyzer = new RussianAnalyzer(Version.LUCENE_7_2_0);
//        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
//    IndexWriter writer = new IndexWriter(directory, config);
//        writer.addDocument(createDocument(fieldName, "Я живу у мамы"));
//        writer.addDocument(createDocument(fieldName, "В доме было холодно"));
//        writer.commit();
//        writer.close();
//
//    //поиск
//    int startFrom = 0;
//    int pageSize = 20;
//    DirectoryReader ireader = DirectoryReader.open(directory);
//    IndexSearcher indexSearcher = new IndexSearcher(ireader);
//    //FuzzyQuery осуществляет поиск неточных вхождений
//    FuzzyQuery wildcardQuery = new FuzzyQuery(new Term(fieldName, "мама"));
//    TopDocs topDocs = indexSearcher.search(wildcardQuery, startFrom + pageSize);
//    ScoreDoc[] hits = topDocs.scoreDocs;
//        for (int i = startFrom; i < topDocs.totalHits; i++) {
//        if (i > (startFrom + pageSize) - 1) {
//            break;
//        }
//        Document hitDoc = indexSearcher.doc(hits[i].doc);
//        if (hitDoc != null) {
//            System.out.println(hitDoc.get(fieldName));
//        }
//    }

    }
}
