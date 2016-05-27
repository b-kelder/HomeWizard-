package idu.stenden.inf1i.homewizard;

/**
 * Created by Bram on 19/05/2016.
 */

//TODO: Rework switch system


public class HomewizardSwitch {
    private String name;
    private String type;
    private boolean status;
    private int id;
    private int dimmer;
    private boolean updating;       //Used to check if this switch is waiting for a state change response

    public HomewizardSwitch(String name, String type, String status, String id){
        this.name = name;
        this.type = type;
        this.status = (status.equals("on")) ? true : false;
        this.id = Integer.parseInt(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HomewizardSwitch{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", status=").append(status);
        sb.append(", id=").append(id);
        sb.append(", dimmer=").append(dimmer);
        sb.append(", updating=").append(updating);
        sb.append('}');
        return sb.toString();
    }

    public void sendStatus() {
        MqttController.getInstance().publish("HYDRA/HMWZ/sw/" + id, status ? "on" : "off");
    }

    public void sendDimmer() {
        MqttController.getInstance().publish("HYDRA/HMWZ/sw/dim/" + id, "" + dimmer);
    }

    public String getName(){
        return name;
    }

    public boolean getStatus(){
        return status;
    }

    public int getId(){
        return id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setStatus(boolean status){
        this.status = status;
    }

    public void setId(int id){
        this.id = id;
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

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }
}
