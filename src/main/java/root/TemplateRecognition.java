package root;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

// todo переопределить equals. Сделать его сравнимым чтобы не создавать лишних объектов. wantedWords проверяем просто на equals
public class TemplateRecognition {

    String templateID;
    HashMap<WantedValues, List<String>> wantedWords;
    RatioRectangle ratioRectangle;
    boolean useFuzzySearch;

    TemplateRecognition(String templateID, HashMap<WantedValues, List<String>> wantedWords, boolean useFuzzySearch, RatioRectangle ratioRectangle) {
        this.templateID = templateID;
        this.wantedWords = wantedWords;
        this.ratioRectangle = ratioRectangle;
        this.useFuzzySearch = useFuzzySearch;
    }

    TemplateRecognition(String templateID, HashMap<WantedValues, List<String>> wantedWords, boolean useFuzzySearch) {
        this.templateID = templateID;
        this.wantedWords = wantedWords;
        this.useFuzzySearch = useFuzzySearch;
    }

    double getRatioSpecifiedXArea(){
        return ratioRectangle != null ? ratioRectangle.getRatioSpecifiedX() : 0;
    }

    double getRatioSpecifiedYArea(){
        return ratioRectangle != null ? ratioRectangle.getRatioSpecifiedY() : 0;
    }

    double getRatioWidthArea(){
        return ratioRectangle != null ? ratioRectangle.getRatioWidth() : 1;
    }

    double getRatioHeightArea(){
        return  ratioRectangle != null ? ratioRectangle.getRatioHeight() : 1;
    }

    Rectangle getAreaRecognition(int width, int height){

        return new Rectangle((int)(width * getRatioSpecifiedXArea()),
                (int)(height * getRatioSpecifiedYArea()),
                (int)(width * getRatioWidthArea()),
                (int)(height * getRatioHeightArea()));
    }
}
