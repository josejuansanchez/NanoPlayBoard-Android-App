package org.josejuansanchez.nanoplayboard.models;

/**
 * Created by josejuansanchez on 29/06/16.
 */
public class LedMatrix {
    private int id;
    private String text;
    private int[] pattern;

    public LedMatrix(String text) {
        this.text = text;
    }

    public LedMatrix(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public LedMatrix(int id, int[] pattern) {
        this.id = id;
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
