package idu.stenden.inf1i.homewizard;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wouter on 19/05/2016.
 */

interface MqttControllerMessageCallbackListener{
    void onMessageArrived(String topic, MqttMessage message);
}

public class MqttController {

    static MqttController instance;

    private Context context;
    private MqttAndroidClient client;

    private ProgressDialog connectingDialog;
    private CountDownTimer connectingDialogTimeoutTimer;

    private List<MqttControllerMessageCallbackListener> messageListeners = new ArrayList<MqttControllerMessageCallbackListener>();

    private MqttController(){
        //Set up some default listeners

        /// Handles initial login
        addMessageListener(new MqttControllerMessageCallbackListener() {
            @Override
            public void onMessageArrived(String topic, MqttMessage message) {
                try {
                    JSONObject json = new JSONObject(message.toString());
                    json = json.getJSONObject("request");
                    String route = json.getString("route");

                    if (route.equals("hydrastatus")) {
                        json = new JSONObject(message.toString());
                        String serial = json.getString("serial");

                        JSONObject file = Util.readLoginData(context);

                        if (serial.equals(file.getString("serial"))) {
                            publish("HYDRA/HMWZ", "get-sensors");
                        } else if (file.getString("email").length() > 1) {
                            //mqttController.publish("HYDRA/AUTH", "{\"email\":\"" + file.getString("email") + "\", \"password\":\"" + file.getString("password") + "\", \"type\":\"login\"}");
                            loginHomeWizard(file.getString("email"), file.getString("password"), context);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /// Displays device edit toasts
        addMessageListener(new MqttControllerMessageCallbackListener() {
            @Override
            public void onMessageArrived(String topic, MqttMessage message) {
                if(topic.equals("HYDRA/HMWZRETURN/sw/remove")) {
                    // A light was removed, update everything
                    publish("HYDRA/HMWZ", "get-sensors");
                    Toast.makeText(context, "Removed device", Toast.LENGTH_SHORT).show();
                } else if(topic.contains("HYDRA/HMWZRETURN/sw/add")) {
                    // A light was added, update everything
                    publish("HYDRA/HMWZ", "get-sensors");
                    Toast.makeText(context, "Added device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /// Displays login result toasts and refresh
        addMessageListener(new MqttControllerMessageCallbackListener() {
            @Override
            public void onMessageArrived(String topic, MqttMessage message) {

                //Toast.makeText(getApplicationContext(), "TRIGGERED SETTINGS EVENT LISTENER " + topic, Toast.LENGTH_SHORT).show();
                if (topic.equals("HYDRA/AUTH/results")) {
                    //haal serial code uit json bericht
                    dismissConnectingDialog();

                    JSONObject json = null;
                    try {
                        json = new JSONObject(message.toString());
                        if (json.getString("status").equals("ok")) {
                            Toast.makeText(context, "Login success", Toast.LENGTH_SHORT).show();
                            publish("HYDRA/HMWZ", "get-sensors");
                        } else {
                            Toast toast = Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static MqttController getInstance(){
        if(instance == null){
            instance = new MqttController();
        }
        return instance;
    }

    public boolean hasMessageListener(MqttControllerMessageCallbackListener listener)
    {
        return messageListeners.contains(listener);
    }

    public void addMessageListener(MqttControllerMessageCallbackListener listener)
    {
        Log.e("MQTT", "Added MQTT listener, size " + messageListeners.size());
        messageListeners.add(listener);
    }
	
	public void removeMessageListener(MqttControllerMessageCallbackListener listener)
	{
        if(messageListeners.remove(listener)){
            Log.e("MQTT", "Removed MQTT listener, size " + messageListeners.size());
        } else {
            Log.e("MQTT", "Could not remove MQTT listener, size " + messageListeners.size());
        }
	}
	
	public void removeMessageListeners(MqttControllerMessageCallbackListener[] listeners)
	{
		for(MqttControllerMessageCallbackListener l:listeners) {

            if(messageListeners.remove(l)){
                Log.e("MQTT", "Removed MQTT listener, size " + messageListeners.size());
            } else {
                Log.e("MQTT", "Could not remove MQTT listener, size " + messageListeners.size());
            }
		}
	}

    public void setContext(Context applicationContext){
        context = applicationContext;
    }

    public boolean isConnected()
    {
        if(client != null){
            return client.isConnected();
        }else{
            return false;
        }
    }

    private void showConnectingDialog(Context context, String title, String message, long timeoutmillis){
        if(connectingDialog != null){
            connectingDialog.dismiss();
            connectingDialogTimeoutTimer.cancel();
        }
        connectingDialog = new ProgressDialog(context);
        connectingDialog.setTitle(title);
        connectingDialog.setMessage(message);
        connectingDialog.setCancelable(false);
        connectingDialog.show();

        connectingDialogTimeoutTimer = new CountDownTimer(timeoutmillis, timeoutmillis) {
            @Override
            public void onTick(long l){

            }
            @Override
            public void onFinish() {
                connectingDialog.dismiss();
            }
        };
        connectingDialogTimeoutTimer.start();
    }

    private void dismissConnectingDialog(){
        if(connectingDialog != null){
            connectingDialog.dismiss();
            connectingDialogTimeoutTimer.cancel();
        }
    }

    public void loginHomeWizard(String email, String password, Context context){
        if(isConnected()){
            setContext(context);

            showConnectingDialog(context, "Connecting", "Connecting to HomeWizard...", 10000);

            Util.saveLoginData(context, email, password, JSONObject.NULL);
            this.publish("HYDRA/AUTH", "{\"email\":\"" + email + "\", \"password\":\"" + password + "\", \"type\":\"login\"}");
            Toast toast = Toast.makeText(context, "Trying to log in", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void connect(String broker, String clientId, Context context){
        setContext(context);
        connect(broker, clientId);
    }

    private void connect(String broker, String clientId){
        MemoryPersistence persistence = new MemoryPersistence();
        client =  new MqttAndroidClient(context, broker, clientId, persistence);

        showConnectingDialog(context, "Connecting", "Connecting to MQTT broker...", 10000);

        try {
            client.connect(context, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast toast = Toast.makeText(context, "Connected to broker", Toast.LENGTH_SHORT);
                    toast.show();

                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            //Toast toast = Toast.makeText(context, topic + " " + message.toString(), Toast.LENGTH_LONG);
                            //toast.show();
                            Log.e("MQTT", "Recieved message on topic " + topic + " - " + message.toString());
                            for (MqttControllerMessageCallbackListener listener : messageListeners) {
                                try {
                                    listener.onMessageArrived(topic, message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });

                    subscribe("HYDRA/HMWZRETURN");
                    subscribe("HYDRA/HMWZRETURN/#");
                    subscribe("HYDRA/STATUS/results");
                    subscribe("HYDRA/AUTH/results");

                    // Does this actually happen?
                    publish("HYDRA/STATUS", "get-status");

                    dismissConnectingDialog();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast toast = Toast.makeText(context, "Failed to connect to broker", Toast.LENGTH_SHORT);
                    toast.show();

                    dismissConnectingDialog();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String payload){

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(2);
        message.setRetained(false);

        if(isConnected()){
            try {
                client.publish(topic, message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else{
            Toast toast = Toast.makeText(context, "Not connected to broker", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void subscribe(String topic){
        try {
            client.subscribe(topic, 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
