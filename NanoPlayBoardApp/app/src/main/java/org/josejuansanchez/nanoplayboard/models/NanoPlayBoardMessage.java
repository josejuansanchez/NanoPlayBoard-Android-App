package org.josejuansanchez.nanoplayboard.models;

import android.graphics.Color;

/**
 * Created by josejuansanchez on 24/6/16.
 */
public class NanoPlayBoardMessage {
    private int id;

    // output fields
    private Integer r;
    private Integer g;
    private Integer b;
    private Integer frequency;
    private Integer duration;
    private String text;
    private int[] pattern;

    // input fields
    private Integer potentiometer;
    private Integer ldr;
    private String error;

    public NanoPlayBoardMessage() {};

    public NanoPlayBoardMessage(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setColor(int color) {
        this.r = Color.red(color);
        this.g = Color.green(color);
        this.b = Color.blue(color);
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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

    public Integer getPotentiometer() {
        return potentiometer;
    }

    public Integer getLdr() {
        return ldr;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
