package org.josejuansanchez.nanoplayboard.models;

import java.io.Serializable;

/**
 * Created by josejuansanchez on 29/06/16.
 */
public class LedMatrix extends NanoPlayBoardMessage implements Serializable {
    String text;
    int[] pattern;

    public LedMatrix(String text) {
        this.text = text;
    }

    public LedMatrix(int sketchId, String text) {
        this.setSketchId(sketchId);
        this.text = text;
    }

    public LedMatrix(int sketchId, int[] pattern) {
        this.setSketchId(sketchId);
        this.pattern = pattern;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int[] getPattern() {
        return pattern;
    }

    public void setPattern(int[] pattern) {
        this.pattern = pattern;
    }
}
