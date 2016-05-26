package idu.stenden.inf1i.homewizard;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bram on 23/05/2016.
 */
public class AppDataContainer {

    static AppDataContainer instance;

    private ArrayList<HomewizardSwitch> homewizardSwitches = new ArrayList<HomewizardSwitch>();
    private ArrayList<CustomSwitch> customSwitches = new ArrayList<>();

    private AppDataContainer(){

    }

    public static AppDataContainer getInstance(){
        if(instance == null){
            instance = new AppDataContainer();
        }
        return instance;
    }

    public void save() {

    }

    public void load() {

    }

    public ArrayList<CustomSwitch> getCustomSwitches() {
        return customSwitches;
    }

    public void addCustomSwitch(CustomSwitch customSwitch) {
        customSwitches.add(customSwitch);
    }

    public void clearCustomSwitches() {
        customSwitches.clear();
    }

    public ArrayList<HomewizardSwitch> getHomewizardSwitches(){
        return homewizardSwitches;
    }

    public void addHomewizardSwitch(HomewizardSwitch homewizardSwitch){
        homewizardSwitches.add(homewizardSwitch);
    }

    public void clearHomewizardSwitches(){
        homewizardSwitches.clear();
    }


}
