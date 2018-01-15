package root;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class TemplateRecognition {

    String templateID;
    HashMap<WantedValues, List<String>> wantedWords;
    Rectangle areaRecognition;

    public TemplateRecognition(String templateID, HashMap<WantedValues, List<String>> wantedWords, Rectangle areaRecognition) {
        this.templateID = templateID;
        this.wantedWords = wantedWords;
        this.areaRecognition = areaRecognition;
    }

    public TemplateRecognition(String templateID, HashMap<WantedValues, List<String>> wantedWords) {
        this.templateID = templateID;
        this.wantedWords = wantedWords;
    }
}
