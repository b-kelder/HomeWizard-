package idu.stenden.inf1i.homewizard;

/**
 * Created by Wouter on 31-05-16.
 */
public abstract class BaseSwitch {
    protected String name;
    protected String type;
    protected boolean status;
    protected int dimmer;

    protected boolean respondToInput;

    public void sendStatus() {
    }

    public void sendDimmer() {
    }

    public void sendRGB() {
    }

    public boolean respondToInput() {
        return respondToInput;
    }

    public void setRespondToInput(boolean respondToInput) {
        this.respondToInput = respondToInput;
    }

    public String getName(){
        return name;
    }

    public boolean getStatus(){
        return status;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setStatus(boolean status){
        this.status = status;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDimmer() {
        return dimmer;
    }

    public void setDimmer(int dimmer) {
        this.dimmer = dimmer;
    }

    public void setDimmer(String dimmer) {
        this.dimmer = Integer.parseInt(dimmer);
    }
}
