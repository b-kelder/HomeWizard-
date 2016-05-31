package idu.stenden.inf1i.homewizard;

/**
 * Created by Bram on 19/05/2016.
 */

public class HomewizardSwitch extends BaseSwitch {

    private int id;                 //HomeWizard id
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

    @Override
    public void sendStatus() {
        MqttController.getInstance().publish("HYDRA/HMWZ/sw/" + id, status ? "on" : "off");
    }

    @Override
    public void sendDimmer() {
        MqttController.getInstance().publish("HYDRA/HMWZ/sw/dim/" + id, "" + dimmer);
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
