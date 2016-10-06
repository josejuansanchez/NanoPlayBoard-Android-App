package org.josejuansanchez.nanoplayboard.events;

/**
 * Created by josejuansanchez on 05/10/16.
 */

public class MqttStringEvent {
    public final String topic;
    public final String data;

    public MqttStringEvent(String topic, String data) {
        this.topic = topic;
        this.data = data;
    }
}