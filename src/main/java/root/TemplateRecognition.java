package root;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class TemplateRecognition {

    String templateID;
    HashMap<WantedValues, List<String>> wantedWords;
    RatioRectangle areaRecognition;

    public TemplateRecognition(String templateID, HashMap<WantedValues, List<String>> wantedWords, RatioRectangle areaRecognition) {
        this.templateID = templateID;
        this.wantedWords = wantedWords;
        this.areaRecognition = areaRecognition;
    }

    public TemplateRecognition(String templateID, HashMap<WantedValues, List<String>> wantedWords) {
        this.templateID = templateID;
        this.wantedWords = wantedWords;
    }
}
