package org.josejuansanchez.nanoplayboard.models;

import java.io.Serializable;

/**
 * Created by josejuansanchez on 29/06/16.
 */
public class LedMatrix implements Serializable {
    String text;

    public LedMatrix(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
