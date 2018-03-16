package root;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class LuceneSearch {

    private static volatile LuceneSearch instance;
    private static boolean errorLucene = false; // Чтобы в случае возникновения ошибки не тормозить систему synchronized - ом


    private final Directory dir; // todo Подумать на счет использования RAMDirectory. Очиста индекса тут https://wiki.apache.org/lucene-java/LuceneFAQ (Как удалить документы из индекса?)
    private final Analyzer analyzer;
    private final IndexWriter writer;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);



    private LuceneSearch() throws IOException {
        dir = FSDirectory.open(Paths.get("/tmp/teaser_index"));
        analyzer = new RussianAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // todo Проверить будет ли работать
        writer = new IndexWriter(dir, iwc);
    }

    public static LuceneSearch getInstance(){
        if (instance == null && !errorLucene)
            synchronized (LuceneSearch.class){ // Double-Checked Locking на текущий момент самый правильный способ.
                if (instance == null){
                    try {
                        instance = new LuceneSearch();
                    } catch (IOException e) {
                        errorLucene = true;
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error("Работа с Lucene не возможна! Причина:",  e);
                    }
                }
            }
        return instance;
    }

    public static void addTextToIndex(final String fld, final String text) {

        LuceneSearch instance = LuceneSearch.getInstance();

        if (instance == null)
            return;

        final Document document = new Document();
        final FieldType textIndexedType = new FieldType();

        textIndexedType.setStored(true);
        textIndexedType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        textIndexedType.setTokenized(true);
        //textIndexedType.setStoreTermVectorPositions(true); // выдает ошибку...

        //index title
        Field title = new Field(fld, text, textIndexedType);

        document.add(title);

        try {
            instance.writer.addDocument(document);
            instance.writer.flush(); // todo проверить, будет ли закрытый writer добавлять документы в индексы
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Ошибка добавления документа в индекс Lucene:",  e);
        }
    }

    public static void deleteFieldFromIndex(String fld, String text){

        LuceneSearch instance = LuceneSearch.getInstance();

        if (instance == null)
            return;

        try {
            instance.writer.deleteDocuments(new Term(fld, text));
            instance.writer.commit(); // todo проверить, будет ли закрытый writer добавлять документы в индексы
            instance.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getIdxFoundWord(final String toSearch, final String searchField, final int limit) {

        LuceneSearch instance = LuceneSearch.getInstance();

        if (instance == null)
            return -1;

        try {
            final IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(instance.dir));
            final Term term = new Term(searchField, toSearch);

            final int maxEdits = 2; // This is very important variable. It regulates fuzziness of the query
            final Query query = new FuzzyQuery(term, maxEdits);
            final TopDocs search = indexSearcher.search(query, limit); // потокобезопасный, но использовать для всех потоков не можем, т.к. каждый поток создает свой индекс.
            final ScoreDoc[] hits = search.scoreDocs;

            // Индекс найденного текста получаем через выделение.
            Highlighter highlighter = new Highlighter(new QueryScorer(query));
            for (int i = 0; i < hits.length; i++) {
                int id = hits[i].doc;
                Document doc = indexSearcher.doc(id);
                String text = doc.get(searchField);

                TokenStream tokenStream = instance.analyzer.tokenStream(searchField, text);

                TextFragment currentFrag = new TextFragment(new StringBuilder(), 0, 0); // пустой
                ((QueryScorer) highlighter.getFragmentScorer()).setMaxDocCharsToAnalyze(highlighter.getMaxDocCharsToAnalyze());

                // Инициализируем токен
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
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Ошибка поиска слова: " + toSearch + ", файл: " + searchField + " в Lucene.  Описание:",  e);
        }
        return -1;
    }
}