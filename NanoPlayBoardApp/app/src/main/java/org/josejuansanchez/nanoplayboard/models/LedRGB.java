package org.josejuansanchez.nanoplayboard.models;

import android.graphics.Color;

import java.io.Serializable;

/**
 * Created by josejuansanchez on 25/6/16.
 */
public class LedRGB extends NanoPlayBoardMessage implements Serializable {
    private int r;
    private int g;
    private int b;

    public LedRGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public LedRGB(int color) {
        this.r = Color.red(color);
        this.g = Color.green(color);
        this.b = Color.blue(color);
    }

    public LedRGB(int sketchId, int color) {
        this.setSketchId(sketchId);
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
}
