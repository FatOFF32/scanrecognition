package root;

import java.awt.*;

// todo переопределить equals
public class RatioRectangle {
    private final double ratioSpecifiedX;
    private final double ratioSpecifiedY;
    private final double ratioWidth;
    private final double ratioHeight;

    public RatioRectangle(double ratioSpecifiedX, double ratioSpecifiedY, double ratioWidth, double ratioHeight) {
        this.ratioSpecifiedX = Math.min(Math.abs(ratioSpecifiedX), 1);
        this.ratioSpecifiedY = Math.min(Math.abs(ratioSpecifiedY), 1);
        this.ratioWidth = Math.min(Math.abs(ratioWidth), 1);
        this.ratioHeight = Math.min(Math.abs(ratioHeight), 1);
    }

    double getRatioSpecifiedX(){
        return ratioSpecifiedX;
    }

    double getRatioSpecifiedY(){
        return ratioSpecifiedY;
    }

    double getRatioWidth(){
        return ratioWidth;
    }

    double getRatioHeight(){
        return ratioHeight;
    }
    public Rectangle getAreaRecognition(int width, int height){

        return new Rectangle((int)(width * getRatioSpecifiedX()),
                (int)(height * getRatioSpecifiedY()),
                (int)(width * getRatioWidth()),
                (int)(height * getRatioHeight()));
    }
}
