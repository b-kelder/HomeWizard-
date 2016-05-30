package com.example.bram.homewizard;

/**
 * Created by Bram on 10/05/2016.
 */
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MQTT{

    public static MqttAndroidClient client;
    public MQTT() {

    }

    public static void test(Context context) {
        try {
            client = new MqttAndroidClient(context, "tcp://test.mosquitto.org:1883",  "test");
            client.connect(null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    MqttMessage msg = new MqttMessage("Buenos Dias Hermanos Pollos".getBytes());

                    try{
                        Log.d("mqtt", "Publish");
                        client.publish("HMWZ/get-status", msg);
                        client.disconnect();
                    }
                    catch(MqttException e){
                        Log.d("mqtt", "Publish exeption");
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.d("mqtt", "Epic fail");
                    Log.d("mqtt", throwable.getCause() +"  ,  "+throwable.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
