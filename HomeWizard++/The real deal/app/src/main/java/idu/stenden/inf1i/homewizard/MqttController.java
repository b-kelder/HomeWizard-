package idu.stenden.inf1i.homewizard;

import android.content.Context;
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

    private List<MqttControllerMessageCallbackListener> messageListeners = new ArrayList<MqttControllerMessageCallbackListener>();

    private MqttController(){

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
        messageListeners.add(listener);
    }
	
	public void removeMessageListener(MqttControllerMessageCallbackListener listener)
	{
		messageListeners.remove(listener);
	}
	
	public void removeMessageListeners(MqttControllerMessageCallbackListener[] listeners)
	{
		for(MqttControllerMessageCallbackListener l:listeners) {
			messageListeners.remove(l);
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

    public void connect(String broker, String clientId){
        MemoryPersistence persistence = new MemoryPersistence();
        client =  new MqttAndroidClient(context, broker, clientId, persistence);

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
                            for (MqttControllerMessageCallbackListener listener : messageListeners) {
                                listener.onMessageArrived(topic, message);
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
                    publish("HYDRA/STATUS", "get-status");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast toast = Toast.makeText(context, "Failed to connect to broker", Toast.LENGTH_SHORT);
                    toast.show();
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
