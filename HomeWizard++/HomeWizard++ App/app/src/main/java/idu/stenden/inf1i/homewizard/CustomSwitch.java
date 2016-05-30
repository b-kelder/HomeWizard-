package idu.stenden.inf1i.homewizard;

/**
 * Created by Wouter on 26-05-16.
 */
public class CustomSwitch {
    private String topic;
    private String name;
    private String payloadOn;
    private String payloadOff;

    public CustomSwitch(String name, String topic, String payloadOn, String payloadOff){
        this.name = name;
        this.topic = topic;
        this.payloadOff = payloadOff;
        this.payloadOn = payloadOn;
    }

    public CustomSwitch(String name, String topic){
        this(name, topic, "on", "off");
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
}
