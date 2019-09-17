package root;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.tess4j.util.LoadLibs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

class Recognizer implements Runnable {

    private final FileInfo fileInfo;

    // logger
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);

    public Recognizer(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    @Override
    public void run() {

        String result; //todo delete
        TemplateRecognition templateRec;
        BufferedImage bufferedImage;

        System.out.println(Thread.currentThread()); // TODO для теста удалить

        try {

            // Попытаемся прочитать данные из шаблона
            templateRec = fileInfo.getTemplateRecognition();

//            if (templateRec == null) { // todo удалить, не может быть null по логиче системы
//                if (LOGGER.isWarnEnabled())
//                    LOGGER.warn("Не найден шаблон для распознавания с ID: " + fileInfo.templateID);
//                return;
//            }

            // Todo Почитать, если instance потокобезопастный, то может быть сделать один на все потоки? Проверить увеличится ли скорость?
            // Распознаем нужную область
            ITesseract instance = new Tesseract();  // JNA Interface Mapping
            File tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build only; only English data bundled
            instance.setDatapath(tessDataFolder.getParent());
            instance.setLanguage("rus");
            try {
                // Преобразуем наш PDF в list IIOImage.
                List<IIOImage> iioImages = ImageIOHelper.getIIOImageList(fileInfo.getFile());
                if (iioImages.size() == 0) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("Файл пустой: " + fileInfo.getFile());
                    return;
                }

                bufferedImage = (BufferedImage)iioImages.get(0).getRenderedImage();

//                    // Для привязки распознаем только первую страницу (Может быть сделаем настраиваемо) todo удалить
//                    int width = iioImages.get(0).getRenderedImage().getWidth();
//                    int height = iioImages.get(0).getRenderedImage().getHeight();
//                    result = instance.doOCR(iioImages.subList(0, 1), templateRec.getAreaRecognition(width, height));

            } catch (IOException e) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Ошибка распознования:", e);
                return;
            }

            try {
                // Включение возможности переворачивания изображения, в слючае неудачной попытки извлечения текста
                for (int i = 0; i < 4; i++) {
                    if (foundWords(bufferedImage, instance, templateRec, i)){ // todo установить настройку переворачивания из шаблнона
                        break;
                    }
                    // Если ничего не нашли, попробуем перевернуть  изображение на 90 градусов и распознать ещё раз.
                    bufferedImage = ImageHelper.rotateImage(bufferedImage, 90);
                }
            } catch (TesseractException e) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Ошибка распознования:", e);
                return;
            } catch (RecognizeException e) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(Thread.currentThread() + fileInfo.getFilePath() + " " + e);
                // Не понятная ошибка. Иногда тессеракт возвращает пустой текст. В этому случае попробуем распознать файл другим процессом
                // todo Временное решение - это повторить процесс распознования. В идеале - разобраться почему так происходит.
                // todo Есть предположение, что это может происходить из-за того, что вр. файл "tif" или "png" был заменен другим процессом.
                filesInProcess.remove(fileInfo.file);
                return;
            }

            // todo $$$ Остановился тут! Переделать через callable и управлять filesInProcess и filesToSend через монитор дерикторирй.
            //  также думаю что от filesInProcess можно избавиться...
            //  Просто забрасываем файл в пул, сохраняем фьючи, если распознование не получилось, опять пулим задачу.

            // Запишим инфо файл в очередь, для отправки в 1С.
            filesToSend.put(fileInfo);

        } catch (InterruptedException e) {
            // Если выбрасывается исключение InterruptedException,
            // то флаг (isInterrupted()) не переводится в true. Для этого
            // вручную вызывается метод interrupt() у текущего потока.
            Thread.currentThread().interrupt();
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Recognizer was interrupt:", e);
        }
    }

    private boolean foundWords(BufferedImage bufferedImage, ITesseract instance, TemplateRecognition templateRec, int idxTrySearch) throws TesseractException, RecognizeException {

        // Для привязки распознаем только первую страницу (Может быть сделаем настраиваемо)
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        String result = instance.doOCR(bufferedImage, templateRec.getAreaRecognition(width, height));

        if (LOGGER.isDebugEnabled())
            LOGGER.debug(Thread.currentThread() + fileInfo.getFilePath() + "  распознанный текст: " + result);

        // С пустым текстом не работаем... См. описание где ловится исключение.
        if (result.isEmpty())
            throw new RecognizeException("Распознан пустой текст!");

        // Проиндексируем текст, если испольщуется нечеткий поиск (templateRec.useFuzzySearch)
        if (templateRec.useFuzzySearch)
            // fileInfo.getFilePath(), путь используется как указатель поля для индекса.
            LuceneSearch.addTextToIndex(fileInfo.getFilePath() + idxTrySearch, result);

        for (Map.Entry<WantedValues, List<String>> entry : templateRec.wantedWords.entrySet()) {
            String resultCopy = result.toLowerCase();
            // todo переделать все что ниже под объектную модель. (т.е. создать объект, который будет
            // todo содержать в себе все поля (idx, idxWord и т.д.) и будет иметь функции обработки нижеописанной логики)
            int idx = -1; // Индекс найденной строки по шаблону
            int idxWord = 0; // Индекс слова в массиве полученных слов
            boolean searchType = false;
            int idxJoinWord = -1;
            int i;
            for (i = 0; i < entry.getValue().size(); i++) {
                String st = entry.getValue().get(i);
                // Проверки на условия "Взять следующий за" и "Искать тип"
                // Предполагается что после "getNextAfter", могут быть ТОЛЬКО "searchType" ИЛИ "joinWordsTo".
                if (st.startsWith("^getNextAfter")) { //todo подумать над названием. Взять следующий после (например) 3
                    idx = idx == -1 ? 0 : idx; // Установим значение отличное от -1
                    if (i == entry.getValue().size() - 1)
                        break;
                    if (entry.getValue().get(i + 1).matches("\\d+")) { //todo затестить на корректность обработки символов и дабла
                        idxWord = Integer.parseInt(entry.getValue().get(i + 1));
                        if (i + 3 < entry.getValue().size() && entry.getValue().get(i + 2).startsWith("^searchType"))
                            searchType = true;
                        else if (i + 4 < entry.getValue().size() && entry.getValue().get(i + 2).startsWith("^joinWordsTo")) //TODO затестить
                            idxJoinWord = getIdxFoundWord(idx, entry.getValue().get(i + 3), resultCopy,
                                    fileInfo.getFilePath() + idxTrySearch, templateRec.useFuzzySearch, false);

                    } else if (entry.getValue().get(i + 1).startsWith("^searchType")) {
                        searchType = true;
                    } else if (i + 2 < entry.getValue().size() && entry.getValue().get(i + 1).startsWith("^joinWordsTo")) { //TODO затестить
                        idxJoinWord = getIdxFoundWord(idx, entry.getValue().get(i + 2), resultCopy,
                                fileInfo.getFilePath() + idxTrySearch, templateRec.useFuzzySearch, false);
                    }
                    break;
                } else if (st.startsWith("^searchType")) {
                    searchType = true;
                    break;
                } else if (st.startsWith("^joinWordsTo")) {
                    idxJoinWord = getIdxFoundWord(idx, entry.getValue().get(i + 1), resultCopy,
                            fileInfo.getFilePath() + idxTrySearch, templateRec.useFuzzySearch, false);
                    break;
                } else if (st.startsWith("^or"))
                    continue;

                // Получение индекса с использование нечеткого поиска.
                idx = getIdxFoundWord(idx, st, resultCopy, fileInfo.getFilePath() + idxTrySearch, templateRec.useFuzzySearch, true);

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(Thread.currentThread() + fileInfo.getFilePath() + "  фраза: " + st + " найдена: " + (idx != -1));

                if (idx == -1) {
                    // todo Везде где есть проверка на условие или, добавим проверку joinWordsTo. Выделить эти проверки в отдельные методы.
                    // Если следующее условие не "ИЛИ" то прерываем, в противном случае проверим условие "ИЛИ"
                    if (!(i < entry.getValue().size() - 2 && entry.getValue().get(i + 1).startsWith("^or")))
                        break;
                } else {
//                        idx = idx + st.length();//Найденный индекс + длинна найденного слова. // todo delete
                    // Если нашли слово, но следующее выражение стоит "ИЛИ", то последующее за "ИЛИ" слово - пропускаем
                    if (i < entry.getValue().size() - 2 && entry.getValue().get(i + 1).startsWith("^or"))
                        i = +2;
                }
            }

//                // Условие про объединение слов в одно. Когда необходимо все слова по какое-то слово объединить. (априоре последнее условие)
//                // Например, из строки "Счет-фактура № 320 012/ 15 от", мы должны получить номер "320012/15".
//                // в условии мы должны указать, что ищем слово счет-фактура и объеденяем полученное значение по слово "от"
//                if((i + 2 < entry.getValue().size() && entry.getValue().get(i + 1).startsWith("^joinWordsTo"))
//                        || (i + 4 < entry.getValue().size() && entry.getValue().get(i + 1).startsWith("^getNextAfter")
//                        && entry.getValue().get(i + 3).startsWith("^joinWordsTo"))){
//                    if(entry.getValue().get(i + 1).startsWith("^joinWordsTo")){
//                        // todo тут будем искать символ, и если нашли, производить обрезку
//                        //idx = getIdxFoundWord(idx, st, resultCopy, fileInfo.getFilePath() + idxTrySearch, templateRec.useFuzzySearch);
//                    }
//                }// todo обязательно написать описание всех этих функций с "^"

            // Нашли слово? Производим обрезку!
            if (idx != -1) {
                if (idxJoinWord != -1)
                    resultCopy = resultCopy.substring(idx, idxJoinWord);
                else resultCopy = resultCopy.substring(idx);
            }
//                if (idx != -1) // todo удалить.
//                    resultCopy = resultCopy.substring(idx);


            // Если не нашли искомые строки, то вставляем пустое значение.
            // В пративном случае получаем значение из текста.
            if (idx == -1) fileInfo.addFoundWord(entry.getKey(), "");
            else {
                // Получим коллекцию из слов, далее работать будем с ней.
                ArrayList<String> resultCol = Stream.of(resultCopy)
                        // Указываем, что он должен быть параллельным
                        .parallel()
                        // Убираем из каждой строки знаки препинания и переносы строки
                        // todo перенести спец символы (А также грязные символы), не участвующие в распозновании в настройку шаблона 1С
                        .map(line -> line.replaceAll(
                                "[\\Q!\"#$%&'()*+,.:;<=>?@[]^`{}~\n№\\E]" +
                                        "|( мг )|( мр )|( ме )|( мэ )|( м )|( н )|( ы )|( и )|( а )|( мо )|( м9 )|( ”9 )|( „9 )", " ")) // todo остановился тут, нужно убрать знак "-" "(\\pP&&[^-])|\\n" Правильно так: [\Q!"#$%&'()*+,./:;<=>?@[\]^_`{|}~\n\E]
                        // Каждую строку разбивваем на слова и уплощаем результат до стримма слов
                        .flatMap(line -> Arrays.stream(line.split(" ")))
                        // Обрезаем пробелы
                        .map(String::trim)
                        // Отбрасываем невалидные слова
                        .filter(word -> !"".equals(word))
                        // Оставляем только первые 3
                        //.limit(3) // Пока не ограничиваем поиск 3-мя значениями...
                        // Создаем коллекцию слов
                        .collect(toCollection(ArrayList::new));

                // Если тип дата, то составляем значение из з-х
                if (entry.getKey().type == DataTypesConversion.DATE) {
                    if (resultCol.size() > 2) {

                        String dateStr;
                        String pattern;
                        boolean continueSearch = true;
                        boolean successfulSearch = false;

                        // todo Если нам будут попадаться даты в с разделителями "-" или "_", обработать их тут!

                        // Если установлен признак searchType, то ищем дату, пока не найдём,
                        // иначе делаем одну итерацию поиска.
                        while (continueSearch) {

                            idxWord++;
                            continueSearch = searchType && resultCol.size() > idxWord + 1;

                            pattern = "dd"; // todo переделать под стригбилдер!!!
                            // Проверка дня. Если день указан одним числом и следующий месяц тоже одним числом,
                            // значит скорее всего это ошибка распознования. Например 1 7 февраля.
                            if (resultCol.get(idxWord-1).matches("\\d")
                                    && resultCol.get(idxWord).matches("\\d")&& resultCol.size() > 3){
                                resultCol.set(idxWord-1, resultCol.get(idxWord-1) + resultCol.get(idxWord));

                                int idxOffset = resultCol.size() < 10 ? resultCol.size() : 10;
                                for (i = 0; i < idxOffset; i++) {
                                    resultCol.set(idxWord + i, resultCol.get(idxWord + i + 1));
                                }
                            }
                            // Проверка месяца.
                            if (resultCol.get(idxWord).matches("\\d{2}"))
                                pattern += " MM";
                            else if (resultCol.get(idxWord).matches("[А-Яа-я]+$")) {
                                // Решаем проблему когда месяц указан строкой, а распознался как два слова. Например ян варя
                                if (resultCol.get(idxWord+1).matches("[А-Яа-я]+$")){
                                    resultCol.set(idxWord, resultCol.get(idxWord) + resultCol.get(idxWord+1));
                                    int idxOffset = resultCol.size() < 10 ? resultCol.size() : 10;
                                    for (i = 1; i < idxOffset; i++) {
                                        resultCol.set(idxWord + i, resultCol.get(idxWord + i + 1));
                                    }
                                }
                                pattern += " MMMM";
                            } else continue;

                            // Проверка года
                            if (resultCol.get(idxWord+1).matches("\\d{2}"))
                                pattern += " yy";
                            else if (resultCol.get(idxWord+1).matches("\\d{4}"))
                                pattern += " yyyy";
                                // Решаем распространенную проблему, когда год распознался с пробелами. Например 201 7
                            else if (resultCol.get(idxWord+1).matches("\\d+")
                                    && resultCol.size() > 3 && resultCol.get(idxWord+2).matches("\\d+")
                                    && (resultCol.get(idxWord+1).length() + resultCol.get(idxWord+2).length()) == 4){
                                resultCol.set(idxWord+1, resultCol.get(idxWord+1) + resultCol.get(idxWord+2));
                                pattern += " yyyy";
                            }
                            else continue;

                            // Получим строку из 3 слов для определения даты.
                            dateStr = String.join(" ", resultCol.subList(idxWord - 1, idxWord + 2));

                            try {
                                // Распарсим полученную дату, затем переведем её в формат ISO 8601 // todo перевести в LocalDate?
                                Date date = new SimpleDateFormat(pattern).parse(dateStr);
                                fileInfo.addFoundWord(entry.getKey(), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date));
                                continueSearch = false;
                                successfulSearch = true;
                            } catch (ParseException e) {
                                //continue; Не получилось? Продолжаем поиски!
                            }
                        }

                        if (!successfulSearch)
                            fileInfo.addFoundWord(entry.getKey(), "");

                    } else fileInfo.addFoundWord(entry.getKey(), "");
                } else if (resultCol.size() > 0)
                    // поиск данных по типу пока работает только для даты и числа.
                    if (searchType && entry.getKey().type == DataTypesConversion.DECIMAL) {
                        while (resultCol.size() > idxWord) {
                            if (resultCol.get(idxWord).matches("\\d+")) { //todo затестить на корректность обработки символов и дабла
                                fileInfo.addFoundWord(entry.getKey(), resultCol.get(idxWord));
                                break;
                            }
                            idxWord++;
                        }
                        // todo Объединение строк пока сделаем только для типа "Строка". Допилить потом и под число. Возможно приурочить к переделке всего что выше в объектную модель.
                    } else fileInfo.addFoundWord(entry.getKey(), resultCol, idxWord, idxJoinWord != -1);
                else fileInfo.addFoundWord(entry.getKey(), "");
            }
        }

//            // Запишим инфо файл в очередь, для отправки в 1С. //todo удалить
//            filesToSend.put(fileInfo);

        // Удалим индекс, если испольщуется нечеткий поиск (templateRec.useFuzzySearch)
        if (templateRec.useFuzzySearch)
            LuceneSearch.deleteFieldFromIndex(fileInfo.getFilePath() + idxTrySearch, result);

        return !fileInfo.foundWordIsEmpty();

    }

    // endOffset - Вернёт индекс последненго символа найденного слова
    private int getIdxFoundWord(int idxStartsWith, String toSearch, String text, String file, boolean useFuzzySearch, boolean endOffset){

        int idx;
        idxStartsWith = idxStartsWith == -1 ? 0 : idxStartsWith;
        if (useFuzzySearch){
            idx = LuceneSearch.getIdxFoundWord(toSearch, file, idxStartsWith, 10, endOffset);
        } else {
            idx = text.indexOf(toSearch, idxStartsWith);
            if (endOffset && idx == -1)
                idx = idx + toSearch.length(); //Найденный индекс + длинна найденного слова.
        }

        return idx;
    }

    private boolean prepareResultString(String resultStr, Map.Entry<WantedValues, List<String>> entry){ // todo подумать, может и удалить её вовсе...

        int idx = -1;
        int idxWord = 0;
        boolean searchType = false;
        for (int i = 0; i < entry.getValue().size(); i++) {
            String st = entry.getValue().get(i);
            // Проверки на условия "Взять следующий за" и "Искать тип"
            if (st.startsWith("^getNextAfter")) { //todo подумать над названием
                idx = 0; // Установим значение отличное от -1
                if (i == entry.getValue().size()-1)
                    break;
                if (entry.getValue().get(i+1).matches("\\d+")){ //todo затестить на корректность обработки символов и дабла
                    idxWord = Integer.parseInt(entry.getValue().get(i+1));
                    if (i+3 < entry.getValue().size() && entry.getValue().get(i+2).startsWith("^searchType"))
                        searchType = true;
                }else if (entry.getValue().get(i+1).startsWith("^searchType"))
                    searchType = true;
                break;
            }
            if (entry.getValue().get(i+1).startsWith("^searchType")){
                searchType = true;
                break;
            }

            idx = resultStr.indexOf(st); // Тут мы поиск заменим со стандартного на поиск по проценту совпадения (сделаем настраиваемо) https://lucene.apache.org/core/ todo
            if (idx == -1)
                // Проверка на или
                if (i == entry.getValue().size()-1 || !entry.getValue().get(i+1).startsWith("^or"))
                    break;
                else resultStr = resultStr.substring(idx + st.length());
        }

        return idx != -1;

    }
}
