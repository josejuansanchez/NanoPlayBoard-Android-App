package org.josejuansanchez.nanoplayboard.models;

/**
 * Created by josejuansanchez on 29/06/16.
 */
public class LedMatrix {
    private int sketchId;
    private String text;
    private int[] pattern;

    public LedMatrix(String text) {
        this.text = text;
    }

    public LedMatrix(int sketchId, String text) {
        this.sketchId = sketchId;
        this.text = text;
    }

    public LedMatrix(int sketchId, int[] pattern) {
        this.sketchId = sketchId;
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
