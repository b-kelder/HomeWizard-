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

    private ArrayList<HomewizardSwitch> itemsList = new ArrayList<HomewizardSwitch>();

    private AppDataContainer(){

    }

    public static AppDataContainer getInstance(){
        if(instance == null){
            instance = new AppDataContainer();
        }
        return instance;
    }

    public ArrayList<HomewizardSwitch> getArray(){
        return itemsList;
    }

    public void add(HomewizardSwitch homewizardSwitch){
        itemsList.add(homewizardSwitch);
    }

    public void clearArray(){
        itemsList.clear();
    }
}
