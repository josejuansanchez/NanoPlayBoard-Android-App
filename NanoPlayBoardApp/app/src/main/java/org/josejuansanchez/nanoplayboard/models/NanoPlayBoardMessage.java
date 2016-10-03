package org.josejuansanchez.nanoplayboard.models;

/**
 * Created by josejuansanchez on 24/6/16.
 */
public class NanoPlayBoardMessage {
    private int potentiometer;
    private int ldr;
    private String error;
    private int id;

    public NanoPlayBoardMessage() {};

    public NanoPlayBoardMessage(int id) {
        this.id = id;
    }

    public int getPotentiometer() {
        return potentiometer;
    }

    public void setPotentiometer(int potentiometer) {
        this.potentiometer = potentiometer;
    }

    public int getLdr() {
        return ldr;
    }

    public void setLdr(int ldr) {
        this.ldr = ldr;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
