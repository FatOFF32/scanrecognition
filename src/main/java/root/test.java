package root;

/*
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
*/
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
//import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;


import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.sun.org.apache.xml.internal.utils.DOMHelper.createDocument;
import static java.util.stream.Collectors.toCollection;

public class test {
    public static void main(String[] args) throws ParseException, IOException, InvalidTokenOffsetsException {

        // попробуем распарсить нужную нам информацию...
        //testParseResult();

        // Попытка поработать с нечетким поиском
        test.testSearch();

        // test rest сервиса
        //test.testRest();
        //test.testRest1();

//        SearchRules obj1 = SearchRules.SKIP_CHAR;
//        obj1.setValue(new Integer(10));
//        SearchRules obj2 = SearchRules.SKIP_CHAR;
//        obj2.setValue(new Integer(20));
//        SearchRules obj3 = SearchRules.SEARCH_TYPE;
//        obj3.setValue(DataTypesConversion.DATE);


//        File file = new File("D:\\Java\\Лекии_Задания\\wp\\Parts\\full.txt");
//        file.
//
        // test rest сервиса put
//        test.testRestPut();

//        try{
//            System.out.println("1");
//            throw new IOException("Error");
//        }catch (IOException e){
//            System.out.println(e);
//        }finally {
//            System.out.println("2");
//        }
//
//        System.out.println("3");
        
//        String string = null;
//        System.out.println(string.isEmpty());
    }

    public static void testRest1(){

        ClientConfig config = new ClientConfig();
//        config.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, LoggingFeature.Verbosity.); //LoggingFeature.DEFAULT_LOGGER_NAME
        config.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.OFF, LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));

        Client client = ClientBuilder.newClient(config); //new ClientConfig().register(LoggingFeature.class)
//        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("testOData", "123456");
//        client.register(feature);
//        WebTarget webTarget = client.target("http://10.17.1.109/upp_fatov/odata/standard.odata").path("/InformationRegister_со_ОбработанныеСканыАвтораспознавателем");
        WebTarget webTarget = client.target("http://localhost:5431/recognizer/updateAuthData");


        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        // "{\"ПутьКФайлу\":\"D:\\Java\\Для распознования\\Сканы\\СФ\\7806003433_780601001_2015-02-06_Ссф00233575.pdf\",\"ШаблонАвтораспознавания_Key\":\"6e4ee607-fb57-11e7-bef1-005056bc20b2\",\"РаспознанныеДанныеJSON\":\"{\"Дата\":\"\",\"Номер\":\"\"}\"}"
//        "{\"ПутьКФайлу\":\"\",\"ШаблонАвтораспознавания_Key\":\"6e4ee607-fb57-11e7-bef1-005056bc20b2\",\"РаспознанныеДанныеJSON\":\"\"}"
//        Response response = invocationBuilder.post(Entity.entity("{\"ПутьКФайлу\":\"D:\\Java\\Для распознования\\Сканы\\СФ\\7806003433_780601001_2015-02-06_Ссф00233575.pdf\",\"ШаблонАвтораспознавания_Key\":\"6e4ee607-fb57-11e7-bef1-005056bc20b2\",\"РаспознанныеДанныеJSON\":\"{\"Дата\":\"\",\"Номер\":\"\"}\"}", MediaType.APPLICATION_JSON));
        Response response = invocationBuilder.post(Entity.entity(
                "{\"URLRESTService1C\" : \"http://10.17.1.109/upp_fatov/odata/standard.odata\",\"Пользователь\" : \"testOData\", \"Пароль\" : \"111\"}", MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }


/*
        ClientConfig config = new ClientConfig();
//        config.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, LoggingFeature.Verbosity.PAYLOAD_ANY);
//        config.register(new LoggingFeature(Logger.getLogger("Test", Level.FINE, LoggingFeature.Verbosity.PAYLOAD_ANY, 8192));

        Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFeature.class)); //.register( LoggingFilter.class )
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.universal("testOData", "123456");
        client.register(feature);
        WebTarget webTarget = client.target("http://10.17.1.109/upp_fatov/odata/standard.odata").path("/Catalog_со_ШаблоныАвтораспознавания?");


        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        String resut = response.readEntity(String.class);
*/
    }

/*

    public static void testRestPut(){

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();
        ObjectNode objjson = mapper.createObjectNode();
        objjson.put("Поле1", "Значение 1");
        objjson.put("Поле2", "Значение 2");
        obj.put("ПутьКФайлу", "D:\\Учеба JAVA\\Для распознования\\Сканы\\Счет-фактура № 1 от 10 октября 2014 г(бест).pdf");
//        obj.put("РаспознанныеДанныеJSON", objjson);
        obj.put("РаспознанныеДанныеJSON", "Какой то json1");
        obj.put("ШаблонАвтораспознавания_Key", "a1bc9909-f7c5-11e7-b618-001bb1fa66bf");
//        ArrayNode arr = mapper.createArrayNode();
//        arr.add(obj);
//        arr.add(obj1);

        String str = "";
        try {
            str = mapper.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String str1 = mapper1.writeValueAsString(arr);

        Client rest1C = Client.create(new DefaultClientConfig());
        rest1C.addFilter(new HTTPBasicAuthFilter("test", "111"));
        rest1C.addFilter(new LoggingFilter());
        WebResource webResource = rest1C.resource("http://localhost/BuhCORP/odata/standard.odata/InformationRegister_со_ОбработанныеСканыАвтораспознавателем");
        ClientResponse response = webResource.accept("application/json")
                .type("application/json").post(ClientResponse.class, str);


        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
//        System.out.println(response.getEntity(String.class));
        String resut = response.getEntity(String.class);

    }

    public static void testRest(){

        Client rest1C = Client.create(new DefaultClientConfig());
        rest1C.addFilter(new HTTPBasicAuthFilter("test", "111"));
//        rest1C.addFilter(new LoggingFilter());
        WebResource webResource = rest1C.resource("http://localhost/BuhCORP/odata/standard.odata/Catalog_со_ШаблоныАвтораспознавания?$select=Ref_Key,СтрокиПоиска$filter=DeletionMark%20ne%20true");
        ClientResponse response = webResource.accept("application/json")
                .type("application/json").get(ClientResponse.class);


        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
//        System.out.println(response.getEntity(String.class));
        String resut = response.getEntity(String.class);

        // Через джексон
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readValue(resut, JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String message = rootNode.get("message").asText(); // get property message
        JsonNode childNode = rootNode.get("value"); // get object Place
        for (JsonNode node : childNode){
            node.get("Ref_Key");
            JsonNode searchStrings = node.get("СтрокиПоиска");
            for (JsonNode sStr : searchStrings){
                node.get("Ref_Key");
                ArrayNode searchString = (ArrayNode) node.get("СтрокиПоиска");
            }
        }
//        String place = childNode.get("name").asText(); // get property name
//        System.out.println(message + " " + place); // print "Hi World!"

        // Через симпл
        JSONParser parser = new JSONParser();
        JSONObject parse = null;
        try {
            parse = (JSONObject) parser.parse(resut);
            System.out.println(parse);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
//        if (parse.get("userId") != null && parse.get("password") != null
        JSONArray templates = (JSONArray) parse.getOrDefault("value", new JSONArray());

        for (Object temp : templates){
//            ((JSONObject) temp).getOrDefault()


        }


    }

*/
    public static void testParseResult() throws ParseException {
        List<String> list = new ArrayList<>();
//        String st = "Счет-фактура № 00000973 от 10 Февраля 2015 г.".toLowerCase();
        String st = "Счет-фактура № 00000973 от 10.02.2015 г.".toLowerCase();
        // 1. Если просто номер СФ
        list.add("Счет-фактура".toLowerCase());
        list.add("№".toLowerCase());


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

    public static void testSearch() throws IOException, ParseException, InvalidTokenOffsetsException {
        //Query query = new FuzzyQuery(new Term("Счет-фактура", " бла бла бла Счет-fфактура бла бла бла"));

        final Document teaserDoc = test.MessageIndexer.createWith("бла бла бла Счт-фактура  бла бла бла", "бла бла бла Счт-фактура бла бла бла");
//        final Document teaserDoc = test.MessageIndexer.createWith("Привет Хабр!", "Это демонстрация работы простейшего нечёткого поиска");
        final MessageIndexer indexer = new test.MessageIndexer("/tmp/teaser_index");
        Analyzer analyzer = new RussianAnalyzer();
        indexer.index(true, teaserDoc, analyzer);

        final TestLucene search = new test.TestLucene(indexer.readIndex());

//        final Scanner reader = new Scanner(System.in);  // Reading from System.in
//        System.out.print("Введите запрос:\t");
//        final String toSearch = reader.nextLine(); // Scans the next token

        //search.fuzzySearch("Счет", "title", 10);
        search.fuzzySearch("Счет", "title", 10, analyzer);
//        search.fuzzySearch("прривт", "title", 10);
    }

    public static class TestLucene {
        private final IndexReader reader;

        public TestLucene(IndexReader reader) {
            this.reader = reader;

        }

        // Нечеткий поиск

/*
*
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
            final TopDocs search = indexSearcher.search(query, limit); // todo потокобезопасный, использовать для всех потоков
            final ScoreDoc[] hits = search.scoreDocs;

            //Explanation explanation = indexSearcher.explain(query, 1); //мое
            showHits(hits);
        }

        public void fuzzySearch(final String toSearch, final String searchField, final int limit, Analyzer analyzer) throws IOException, ParseException, InvalidTokenOffsetsException {
            final IndexSearcher indexSearcher = new IndexSearcher(reader);

            final Term term = new Term(searchField, toSearch);

            final int maxEdits = 2; // This is very important variable. It regulates fuzziness of the query
            final Query query = new FuzzyQuery(term, maxEdits);
            final TopDocs search = indexSearcher.search(query, limit); // todo потокобезопасный, использовать для всех потоков
            final ScoreDoc[] hits = search.scoreDocs;

            //Explanation explanation = indexSearcher.explain(query, 1); //мое
            showHits(hits);
            highlight(search, indexSearcher, query, analyzer);
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

        private void highlight(TopDocs hits, IndexSearcher searcher, Query query, Analyzer analyzer) throws IOException, InvalidTokenOffsetsException {
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
            for (int i = 0; i < 10; i++) {
                int id = hits.scoreDocs[i].doc;
                Document doc = searcher.doc(id);
                String text = doc.get("title");

                // Тут получим OffsetAttribute offsetAtt = (OffsetAttribute)tokenStream.addAttribute(OffsetAttribute.class);
                // затем tokenStream.incrementToken()
                // получаем highlighter.getFragmentScorer().getTokenScore() и если больше 0 то это найденное слово
                // получаем начальный символ, это и будет наш начальный символ интересующей нас строки offsetAtt.startOffset()
                TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "title", analyzer);
                TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, 10);//highlighter.getBestFragments(tokenStream, text, 3, "...");
                for (int j = 0; j < frag.length; j++) {
                    if ((frag[j] != null) && (frag[j].getScore() > 0)) {
                        System.out.println((frag[j].toString()));
                    }
                }
            }
        }
    }

/*
*
     * Default Indexer that we will use in tutorial
     * Be default it will use RussianAnalyzer to analyze text


*/
    public static class MessageIndexer {
        private final String pathToIndexFolder;

/*
*
         * Creates Lucene Document using two strings: body and title
         *
         * @return resulted document

*/

        public static Document createWith(final String titleStr, final String bodyStr) {
            final Document document = new Document();

            final FieldType textIndexedType = new FieldType();
            textIndexedType.setStored(true);
            textIndexedType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            textIndexedType.setTokenized(true);
            //textIndexedType.setStoreTermVectorPositions(true); // выдает ошибку...

            //index title
            Field title = new Field("title", titleStr, textIndexedType);
            //index body
            Field body = new Field("body", bodyStr, textIndexedType);

            document.add(title);
            document.add(body);
            return document;
        }

/*
*
         * Get instance of MessageIndex providing path where indexes will be stored
         *
         * @param pathToIndexFolder File System path where indexes will be stored. For example /tmp/tutorial_indexes

*/

        public MessageIndexer(final String pathToIndexFolder) {
            this.pathToIndexFolder = pathToIndexFolder;
        }

/*
*
         * Indexing documents using provided Analyzer
         *
         * @param create to decide create new or append to previous one
         * @throws IOException

*/

        public void index(final Boolean create, List<Document> documents, Analyzer analyzer) throws IOException {
            final Directory dir = FSDirectory.open(Paths.get(pathToIndexFolder)); // Подумать на счет использования RAMDirectory. Очиста индекса тут https://wiki.apache.org/lucene-java/LuceneFAQ (Как удалить документы из индекса?)
            final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            // мои доработки
            //iwc.ыу;
            final IndexWriter w = new IndexWriter(dir, iwc);
            w.addDocuments(documents);
            w.close();
        }

/*
*
         * Indexing documents with RussianAnalyzer as analyzer
         *
         * @param create to decide create new or append to previous one
         * @throws IOException

*/

        public void index(final Boolean create, List<Document> documents) throws IOException {
            final Analyzer analyzer = new RussianAnalyzer();
            index(create, documents, analyzer);
        }

/*
*
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

        public void index(final Boolean create, Document document, Analyzer analyzer) throws IOException {
            final List<Document> oneDocumentList = new ArrayList<>();
            oneDocumentList.add(document);
            index(create, oneDocumentList, analyzer);
        }

/*
*
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

    public static class TestJSON{
        String Ref_Key;
        String LineNumber;

        @Override
        public String toString() {
            return "TestJSON{" +
                    "Ref_Key='" + Ref_Key + '\'' +
                    ", LineNumber='" + LineNumber + '\'' +
                    '}';
        }
    }

}
