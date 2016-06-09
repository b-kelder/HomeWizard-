package idu.stenden.inf1i.homewizard;

import android.graphics.Color;
import android.util.Log;

/**
 * Created by Wouter on 26-05-16.
 */
public class CustomSwitch extends BaseSwitch {
    private String topic;
    private String payloadOn;
    private String payloadOff;
    private String rgb = "0,0,0";

    public CustomSwitch(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public CustomSwitch(String name, String topic, String payloadOn, String payloadOff, String type, boolean status, int dimmer, String rgb){
        this.name = name;
        this.topic = topic;
        this.payloadOff = payloadOff;
        this.payloadOn = payloadOn;
        this.type = type;
        this.status = status;
        this.dimmer = dimmer;
        this.rgb = rgb;
    }

    @Override
    public void sendStatus() {
        MqttController.getInstance().publish(topic, status ? payloadOn : payloadOff);
    }

    @Override
    public void sendDimmer() {
        MqttController.getInstance().publish(topic, "" + dimmer);
    }

    @Override
    public void sendRGB() {
        MqttController.getInstance().publish(topic, "" + rgb);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayloadOn() {
        return payloadOn;
    }

    public void setPayloadOn(String payloadOn) {
        this.payloadOn = payloadOn;
    }

    public String getPayloadOff() {
        return payloadOff;
    }

    public void setPayloadOff(String payloadOff) {
        this.payloadOff = payloadOff;
    }

    public String getRGB(){
        return rgb;
    }

    public int getRGBInt(){
        int color;
        try {
            String[] vals = rgb.split(",");
            color = Color.rgb(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[1]));
        } catch(Exception e){
            color = Color.rgb(255,255,255);
        }
        return color;
    }

    public void setRGB(String rgb){
        this.rgb = rgb;
    }
}
