package root;

/*
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
*/
import com.sun.deploy.net.HttpUtils;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import net.sourceforge.tess4j.util.PdfUtilities;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.sun.org.apache.xml.internal.utils.DOMHelper.createDocument;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class test implements Serializable{
    public static void main(String[] args)  { //throws ParseException, IOException, InvalidTokenOffsetsException

        // попробуем распарсить нужную нам информацию...
        //testParseResult();

        // Попытка поработать с нечетким поиском
        //test.testSearch();

        // test rest сервиса
        //test.testRest();
        //test.testRest1();

        // тест HTTP сервиса по сканам
        //test.testHttp();
//        test.testHttp1();

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
/*
        try{
            System.out.println("1");
            throw new IOException("Error");
        }catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException("Error 2");
        } catch (RuntimeException e){
            System.out.println(e);
        }finally {
            System.out.println("2");
        }
        int local = 1;
        int root = 2;
*/
//
//        System.out.println("3");
        
//        String string = null;
//        System.out.println(string.isEmpty());
        HashMap <String, List<String>> hashMap1 = new HashMap<>();
        List<String> list1 = new ArrayList<>();
        list1.add("1");
        list1.add("2");
        list1.add("3");
        list1.add("4");

        List<String> list2 = new ArrayList<>();
        list2.add("5");
        list2.add("6");
        list2.add("7");
        list2.add("8");

        hashMap1.put("list1", list1);
        hashMap1.put("list2", list2);

        HashMap <String, List<String>> hashMap2 = new HashMap<>();
        List<String> list3 = new ArrayList<>();
        list3.add("1");
        list3.add("2");
        list3.add("3");
        list3.add("4");

        List<String> list4 = new ArrayList<>();
        list4.add("5");
        list4.add("6");
        list4.add("7");
        list4.add("8");


        hashMap2.put("list1", list3);
        hashMap2.put("list2", list4);

        System.out.println("Equal list? " + list1.equals(list3));
        System.out.println("Equal? " + hashMap1.equals(hashMap2));


        // test random
//        for (int i = 0; i < 50 ; i++) {
//            int rand = new Random().nextInt();
//            System.out.println("Random: " + rand);
//            System.out.println("Random %5: " + rand % 5);
//            System.out.println("Math: " + Math.random()*5);
//        }
//        boolean b = true|false;
//        System.out.println(Stream.iterate("1", (s)-> s + 3).limit(3).map(x-> x+x));
//        double d = 10;
//        test.probe(d);
//        List list = new ArrayList();
////        list.stream().sorted((a, b) -> b.compareTo(a)).co
////        Integer.parseInt("dfsdsf");
//
//        try {
//            System.out.println("aaa");
//            throw new Error("1");
//        } catch (Error e){
//            System.out.println("bbbb");
//        }finally {
//            System.out.println("cccc");
//        }
//
//        System.out.println("ddddd");

//        List<String> list = new ArrayList<>();
//        list.add("1");
//        list.add("2");
//        list.add(3);
        List<? super Number> list = new ArrayList<>();
        list.add(new Integer(1));
        list.add(2);
//        list.add(new Object()); //err
        Object obj = new Integer(10);
        list.add((Number) obj);
        HashMap<Integer, String> hm = new HashMap<>();
        hm.put(1, "1");
        hm.put(2, "2");

        Set<Integer> set = hm.keySet();

        hm.put(3, "3");

        for (Integer i : set)
            System.out.println(i);

        Number num1 = new Integer(10);
        Number num2 = new Integer(15);
        Number num3 = num1.longValue() + num2.longValue();
        System.out.println("num3 = " + num3);

        Number num4 = 10.656;
        Number num5 = new Double(15.111);
        Number num6 = num4.doubleValue() + num5.doubleValue();
        System.out.println("num4 = " + num4.doubleValue());
        System.out.println("num5 = " + num5.doubleValue());
        System.out.println("num4 + num5 = " + (num4.doubleValue() + num5.doubleValue()));
        System.out.println("num4 + num5 = " + (10.656 + 15.111));
        System.out.println("num6 = " + num6.doubleValue());

        Float fl;
        System.out.println("Double through Long: " + (double)num4.longValue());

        SortedSet set1 = new TreeSet();
        LocalDate.parse("2017-12-15", DateTimeFormatter.ofPattern("yyyy-MM-dd"));

//        System.out.println("String".replace('g', 'G') == "String".replace('g', 'G'));
//        System.out.println("String".replace('g', 'g') == "String");
//        System.out.println("String".replace('g', 'G') == "StrinG");
//        System.out.println("String".replace('g', 'g') == new String("String").replace('g', 'g'));

/*
        int i = 1, j = 10; //todo Прочиать про do как это работает.
        do {
            if (i++ > --j) continue;
        }while (i<5);
        System.out.println("i=" + i + " j= " + j);
*/


/*
        ArrayList<? extends Number> list = new ArrayList<Integer>();
        ArrayList<? extends Integer> list5 = new ArrayList<Integer>();
        ArrayList<? super Integer> list4 = new ArrayList<Integer>();
//        List<?> list1 = new ArrayList<?>(); //err
        List<?> list2 = new ArrayList<Integer>();
//        List<Number> list3 = new ArrayList<Integer>(); //err
//        list.add(10);// err
        list4.add(10);
        list4.add(new Integer(10));
//        list4.add(new Double(10)); //err
//        list5.add(new Integer(10)); //err
        list5.get(1).byteValue();
//        list2.add(10); //err
*/
/*
        // По сути такой же как и Object???
        ArrayList<? super Number> list4 = new ArrayList<>();
        list4.add(new Integer(10));
        list4.add(new Double(10));
        list4.get(0).
*/

/*
//        Double dou = (Double) new Integer(10); //err
        Double dou = (Double) (Number) new Integer(10); // err в момент выполнения java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.Double
        Number dou1 = new Integer(10);
//        Integer integer = dou1; //err
        Integer integer = (Integer) dou1;
*/


    }

    public static void probe(Number obj){
        System.out.println("Number");
    }

    public static void probe(Object obj){
        System.out.println("Object");
    }

//    private boolean areEqualWithArrayValue(Map<String, List<String>> first, Map<String, List<String>> second) {
//        if (first.size() != second.size()) {
//            return false;
//        }
//
//        return first.entrySet().stream()
//                .allMatch(e -> Arrays.equals(e.getValue(), second.get(e.getKey())));
//    }

    public static void testHttp1(){

        ClientConfig config = new ClientConfig();
//        config.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.OFF, LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));
        Client client = ClientBuilder.newClient(config);

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.universal("testOData", "123456");
        client.register(feature);
//        WebTarget webTarget = client.target(url).path(query); // Почему то так не всегда работает.
//        WebTarget webTarget = client.target("http://10.17.1.109/upp_fatov/hs/scans/getListScans/11B5675EE95F89E143257FC0002461CBcvcv");
        WebTarget webTarget = client.target("http://10.17.1.109/upp_fatov/hs/scans/getScan/Документ/ПлатежноеПоручениеИсходящее/db51a06d-31a2-11e8-8295-005056bc20b2"); //Правильный
//        WebTarget webTarget = client.target("http://10.17.1.109/upp_fatov/hs/scans/getScan/Документ/ПлатежноеПоручениеИсходящее/db51a06d-31a2-11e8-8295-чмчсмчсмсчмч"); // НЕ правильный

        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.MULTIPART_FORM_DATA);
        Response response = invocationBuilder.get();
        System.out.println(response.getStatus());

        if (response.getMediaType().isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE)){
            InputStream resut = response.readEntity(InputStream.class);
            try {
                BufferedInputStream buffIS = new BufferedInputStream(resut);

//                // Проверим на то, что файл пустой и вернём корректную ошибку
//                buffIS.mark(0);
//                if (buffIS.read() < 0)
//                    return Response.status(Response.Status.BAD_REQUEST)
//                            .entity("Переданный для преобразования файл - пустой!")
//                            .build();
//                else buffIS.reset();

                String FileName = response.getHeaderString("Content-Disposition").replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
                FileOutputStream filePDF = new FileOutputStream("D:\\Всякий хлам\\ТестСканов\\" + URLDecoder.decode(FileName, "UTF-8"));
                BufferedOutputStream outPDF = new BufferedOutputStream(filePDF);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while((bytesRead = buffIS.read(buffer)) > 0) {
                    outPDF.write(buffer, 0, bytesRead);
                }

                resut.close();
                outPDF.flush();

            } catch (IOException e) {

            } finally {
//                resut.close();
//                outPDF.flush();

            }
        }else if (response.getMediaType().isCompatible(MediaType.TEXT_PLAIN_TYPE)){
            String resut = response.readEntity(String.class);
            System.out.println(resut);
        }


    }

    public static void testHttp(){

        ClientConfig config = new ClientConfig();
//        config.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.OFF, LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));
        Client client = ClientBuilder.newClient(config);

        HttpAuthenticationFeature feature = HttpAuthenticationFeature.universal("testOData", "123456");
        client.register(feature);
//        WebTarget webTarget = client.target(url).path(query); // Почему то так не всегда работает.
//        WebTarget webTarget = client.target("http://10.17.1.109/upp_fatov/hs/scans/getListScans/11B5675EE95F89E143257FC0002461CBcvcv");
        WebTarget webTarget = client.target("http://10.17.1.109/upp_fatov/hs/scans/getScan/%D0%94%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82/%D0%9F%D0%BB%D0%B0%D1%82%D0%B5%D0%B6%D0%BD%D0%BE%D0%B5%D0%9F%D0%BE%D1%80%D1%83%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%D0%98%D1%81%D1%85%D0%BE%D0%B4%D1%8F%D1%89%D0%B5%D0%B5/db51a06d-31a2-11e8-8295-00sfdadsfasfdsa");
//        WebTarget webTarget = client.target("http://10.17.1.109/upp_fatov/hs/scans/getSkan/" +
//                "51a157cb-af0c-46a8-884a-a2bc55f896d3qqqqq");
//                "XFx2czEzLWZzYnVoXEJVSFNDXGFyY2hpdmVcUE9fT3V0XNCQ0LrQutCf0L7Qu18wMi4wMi4yMDE4IDAwMDAwXzAwMDAwMDAwMDAxXzAyLjAyLjIwMTggMTc1MDU2X9Ch0JDQndCa0KIt0J/Ql");
//                "%5C%5Cvs13-fsbuh%5CBUHSC%5Carchive%5CPO_Out%5C%D0%90%D0%BA%D0%BA%D0%9F%D0%BE%D0%BB_02.02.2018%2000000_00000000001_02.02.2018%20175056_%D0%A1%D0%90%D0%9D%D0%9A%D0%A2-%D0%9F%D0%95%D0%A2%D0%95%D0%A0%D0%91%D0%A3%D0%A0%D0%93%2040502810490160000006%20%28%D0%9A%D0%9E%D0%9E%29_7705596339_770501001.pdf");
//                "XFx2czEzLWZzYnVoXEJVSFNDXGFyY2hpdmVcUE9fT3V0XNCQ0LrQutCf0L7Qu18wMi4wMi4yMDE4IDAwMDAwXzAwMDAwMDAwMDAxXzAyLjAyLjIwMTggMTc1MDU2X9Ch0JDQndCa0KIt0J/QldCi0JXQoNCR0KPQoNCTIDQwNTAyODEwNDkwMTYwMDAwMDA2ICjQmtCe0J4pXzc3MDU1OTYzMzlfNzcwNTAxMDAxLnBkZg==");

        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        System.out.println(response.getStatus());
        String resut = response.readEntity(String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readValue(resut, JsonNode.class);
        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println(rootNode.get("value"));

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
        search.fuzzySearch("хер", "title", 10, analyzer);
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
            System.out.println("Позиция найденного символа: " + getIdxWord(search, indexSearcher, query, analyzer));
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
            //SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
            //Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
            Highlighter highlighter = new Highlighter(new QueryScorer(query));
            for (int i = 0; i < hits.scoreDocs.length; i++) {
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

        // Получение позиции символа в тексте
        private int getIdxWord1(TopDocs hits, IndexSearcher searcher, Query query, Analyzer analyzer) throws IOException, InvalidTokenOffsetsException {

            Highlighter highlighter = new Highlighter(new QueryScorer(query));
            for (int i = 0; i < hits.scoreDocs.length; i++) {
                int id = hits.scoreDocs[i].doc;
                Document doc = searcher.doc(id);
                String text = doc.get("title");

                TokenStream tokenStream = analyzer.tokenStream("title", text);

                TextFragment currentFrag = new TextFragment(new StringBuilder(), 0, 0); // docFrags.size()
                ((QueryScorer)highlighter.getFragmentScorer()).setMaxDocCharsToAnalyze(highlighter.getMaxDocCharsToAnalyze());


                TokenStream newStream = highlighter.getFragmentScorer().init(tokenStream);
                if (newStream != null)
                    tokenStream = newStream;

                highlighter.getFragmentScorer().startFragment(currentFrag);
                highlighter.getTextFragmenter().start(text, tokenStream);
                OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
                tokenStream.reset();
                while (tokenStream.incrementToken())
                    if (highlighter.getFragmentScorer().getTokenScore() > 0)
                        return offsetAtt.startOffset();
            }
            return -1;
        }


        // Получение позиции символа в тексте через lucene
        private int getIdxWord(TopDocs hits, IndexSearcher searcher, Query query, Analyzer analyzer) throws IOException, InvalidTokenOffsetsException {

            Highlighter highlighter = new Highlighter(new QueryScorer(query));
            for (int i = 0; i < hits.scoreDocs.length; i++) {
                int id = hits.scoreDocs[i].doc;
                Document doc = searcher.doc(id);
                String text = doc.get("title");

                TokenStream tokenStream = analyzer.tokenStream("title", text);

                TextFragment currentFrag = new TextFragment(new StringBuilder(), 0, 0); // docFrags.size()
                ((QueryScorer)highlighter.getFragmentScorer()).setMaxDocCharsToAnalyze(highlighter.getMaxDocCharsToAnalyze());


                TokenStream newStream = highlighter.getFragmentScorer().init(tokenStream);
                if (newStream != null)
                    tokenStream = newStream;

                highlighter.getFragmentScorer().startFragment(currentFrag);
                highlighter.getTextFragmenter().start(text, tokenStream);
                OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
                tokenStream.reset();
                while (tokenStream.incrementToken())
                    if (highlighter.getFragmentScorer().getTokenScore() > 0)
                        return offsetAtt.startOffset();
            }
            return -1;
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
