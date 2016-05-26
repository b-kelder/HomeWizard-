package idu.stenden.inf1i.homewizard;

/**
 * Created by Bram on 19/05/2016.
 */


public class HomewizardSwitch {
    private String name;
    private String type;
    private boolean status;
    private int id;
    private int dimmer;

    public HomewizardSwitch(String name, String type, String status, String id){
        this.name = name;
        this.type = type;
        this.status = (status.equals("on")) ? true : false;
        this.id = Integer.parseInt(id);
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
}
