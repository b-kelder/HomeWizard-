package idu.stenden.inf1i.homewizard;

/**
 * Created by Bram on 19/05/2016.
 */


public class HomewizardSwitch {
    private String name;
    private boolean status;
    private int id;

    public HomewizardSwitch(String name, String status, String id){
        this.name = name;
        this.status = (status.equals("on")) ? true : false;
        this.id = Integer.parseInt(id);
    }

    public HomewizardSwitch(String name, boolean status, int id){
        this.name = name;
        this.status = status;
        this.id = id;
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

}
