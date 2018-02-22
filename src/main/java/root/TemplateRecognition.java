package root;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class TemplateRecognition {

    String templateID;
    HashMap<WantedValues, List<String>> wantedWords;
    RatioRectangle ratioRectangle;

    TemplateRecognition(String templateID, HashMap<WantedValues, List<String>> wantedWords, RatioRectangle ratioRectangle) {
        this.templateID = templateID;
        this.wantedWords = wantedWords;
        this.ratioRectangle = ratioRectangle;
    }

    TemplateRecognition(String templateID, HashMap<WantedValues, List<String>> wantedWords) {
        this.templateID = templateID;
        this.wantedWords = wantedWords;
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
