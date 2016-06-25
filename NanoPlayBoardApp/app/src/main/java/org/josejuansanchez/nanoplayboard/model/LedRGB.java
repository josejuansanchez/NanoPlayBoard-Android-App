package org.josejuansanchez.nanoplayboard.model;

import java.io.Serializable;

/**
 * Created by josejuansanchez on 25/6/16.
 */
public class LedRGB implements Serializable {
    private int r;
    private int g;
    private int b;

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
