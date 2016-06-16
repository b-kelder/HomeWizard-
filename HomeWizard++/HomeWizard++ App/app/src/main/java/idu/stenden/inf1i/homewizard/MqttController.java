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
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wouter on 19/05/2016.
 */

//TODO: Pass JSONObject of payload with onMessageArrived?
interface MqttControllerMessageCallbackListener
{
    void onMessageArrived(String topic, MqttMessage message);
}

public class MqttController
{

    static MqttController instance;

    private Context context;
    private MqttAndroidClient client;

    private ProgressDialog progressDialog;
    private CountDownTimer progressDialogTimeoutTimer;

    private List<MqttControllerMessageCallbackListener> messageListeners = new ArrayList<MqttControllerMessageCallbackListener>();

    private MqttController()
    {
        //Set up some default listeners

        //TODO: Merge these together for performance reasons

        /// Looks for not-logged-in-to-homewizard messages and tries to login
        addMessageListener(new MqttControllerMessageCallbackListener()
        {
            @Override
            public void onMessageArrived(String topic, MqttMessage message)
            {
                try
                {
                    JSONObject json = new JSONObject(message.toString());
                    String status = json.getString("status");
                    if (status.equals("failed_hydra"))
                    {
                        if (json.getInt("error") == 4)
                        {
                            //Not logged in, try again
                            JSONObject file = Util.readLoginData(context);
                            loginHomeWizard(file.getString("email"), file.getString("password"), context);
                        }
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });

        /// Handles initial login
        addMessageListener(new MqttControllerMessageCallbackListener()
        {
            @Override
            public void onMessageArrived(String topic, MqttMessage message)
            {
                try
                {
                    JSONObject json = new JSONObject(message.toString());
                    json = json.getJSONObject("request");
                    String route = json.getString("route");

                    if (route.equals("hydrastatus"))
                    {
                        JSONObject file = Util.readLoginData(context);
                        if (!file.getString("email").isEmpty())
                        {
                            loginHomeWizard(file.getString("email"), file.getString("password"), context);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        /// Displays device edit toasts
        addMessageListener(new MqttControllerMessageCallbackListener()
        {
            @Override
            public void onMessageArrived(String topic, MqttMessage message)
            {
                if (topic.contains("HYDRA/HMWZRETURN/sw/remove"))
                {
                    // A light was removed, update everything
                    try
                    {
                        JSONObject jsonObject = new JSONObject(message.toString());
                        if (jsonObject.getString("status").equals("ok"))
                        {
                            publish("HYDRA/HMWZ", "get-sensors");
                            Toast.makeText(context, "Device removed", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(context, "Error removing device", Toast.LENGTH_SHORT).show();
                        }

                    }
                    catch (JSONException e)
                    {
                    }
                }
                else if (topic.contains("HYDRA/HMWZRETURN/sw/add"))
                {
                    // A light was added, update everything
                    try
                    {
                        JSONObject jsonObject = new JSONObject(message.toString());
                        if (jsonObject.getString("status").equals("ok"))
                        {
                            publish("HYDRA/HMWZ", "get-sensors");
                            Toast.makeText(context, "Device added", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(context, "Error adding device", Toast.LENGTH_SHORT).show();
                        }

                    }
                    catch (JSONException e)
                    {
                    }
                }
            }
        });

        /// Displays login result toasts and refresh
        addMessageListener(new MqttControllerMessageCallbackListener()
        {
            @Override
            public void onMessageArrived(String topic, MqttMessage message)
            {

                if (topic.equals("HYDRA/AUTH/results"))
                {
                    dismissConnectingDialog();

                    JSONObject json = null;
                    try
                    {
                        json = new JSONObject(message.toString());
                        if (json.getString("status").equals("ok"))
                        {
                            Toast.makeText(context, "HomeWizard connected", Toast.LENGTH_SHORT).show();
                            publish("HYDRA/HMWZ", "get-sensors");
                        }
                        else
                        {
                            if (json.getInt("error") == 72)
                            {
                                // HomeWizard was already connected, use it anyway.
                                Toast.makeText(context, "HomeWizard connected", Toast.LENGTH_SHORT).show();
                                publish("HYDRA/HMWZ", "get-sensors");
                            }
                            else
                            {
                                Toast toast = Toast.makeText(context, "HomeWizard login failed", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        //

        /// Displays HUE login result toasts and refresh
        addMessageListener(new MqttControllerMessageCallbackListener()
        {
            @Override
            public void onMessageArrived(String topic, MqttMessage message)
            {

                if (topic.equals("HYDRA/HUERETURN/connect"))
                {
                    JSONObject json;
                    try
                    {
                        json = new JSONObject(message.toString());
                        if (json.getString("status").equals("ok"))
                        {
                            publish("HYDRA/HUE/get-lights", "");
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static MqttController getInstance()
    {
        if (instance == null)
        {
            Log.d("MQTT", "CREATED MQTTCONTROLLER");
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
        for (MqttControllerMessageCallbackListener l : listeners)
        {
            messageListeners.remove(l);
        }
    }

    public void setContext(Context applicationContext)
    {
        context = applicationContext;
    }

    public boolean isConnected()
    {
        if (client != null)
        {
            return client.isConnected();
        }
        else
        {
            return false;
        }
    }

    private void showDialog(final Context context, String title, String message, long timeoutmillis)
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialogTimeoutTimer.cancel();
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String tielete = title;

        progressDialogTimeoutTimer = new CountDownTimer(timeoutmillis, timeoutmillis)
        {
            @Override
            public void onTick(long l)
            {

            }

            @Override
            public void onFinish()
            {
                progressDialog.dismiss();
                Toast.makeText(context, "Attempt at " + tielete.toLowerCase() + " timed out.", Toast.LENGTH_SHORT).show();
            }
        };
        progressDialogTimeoutTimer.start();
    }

    private void dismissConnectingDialog()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialogTimeoutTimer.cancel();
        }
    }

    public void loginHomeWizard(String email, String password, Context context)
    {
        if (isConnected())
        {
            setContext(context);

            showDialog(context, "Connecting", "Connecting to HomeWizard...", 10000);

            Util.saveLoginData(context, email, password);
            this.publish("HYDRA/AUTH", "{\"email\":\"" + email + "\", \"password\":\"" + password + "\", \"type\":\"login\"}");
        }
    }

    public void connect(String broker, String clientId, String username, String password, Context context)
    {
        setContext(context);
        JSONObject brokerData = Util.readBrokerData(context);
        boolean useCertificate = false;
        try
        {
            useCertificate = brokerData.getBoolean("crt");
        }
        catch (JSONException e)
        {
        }
        String brokerUrl = (useCertificate) ? "ssl://" + broker : "tcp://" + broker;
        connect(brokerUrl, "HMwzPluSplUS", username, password, useCertificate);
    }

    private void connect(String broker, String clientId, String username, String password, boolean useCertificate)
    {
        MemoryPersistence persistence = new MemoryPersistence();
        if (client != null)
        {
            try
            {
                // Reset the old client's callback to prevent it from doing anything if the disconnect doesn't work out somehow.
                client.setCallback(new MqttCallback()
                {
                    @Override
                    public void connectionLost(Throwable cause)
                    {
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception
                    {
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token)
                    {
                    }
                });
                client.disconnect(1000);
            }
            catch (Exception e)
            {
                Log.e("MQTT", e.toString());
            }
        }

        client = new MqttAndroidClient(context, broker, clientId, persistence);

        showDialog(context, "Connecting", "Connecting to MQTT broker...", 10000);

        try
        {
            MqttConnectOptions options = new MqttConnectOptions();

            if (!username.isEmpty())
            {
                options.setPassword(password.toCharArray());
                options.setUserName(username);
            }

            if (useCertificate)
            {
                SslUtil sslUtil;
                try
                {
                    sslUtil = SslUtil.getInstance();
                }
                catch (RuntimeException e)
                {
                    sslUtil = SslUtil.newInstance(context);
                }
                options.setConnectionTimeout(60);
                options.setKeepAliveInterval(60);

                options.setSocketFactory(sslUtil.getSocketFactory(R.raw.tls_default_key, SslUtil.PASSWORD));
                options.setCleanSession(true);
            }

            client.connect(options, context, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    client.setCallback(new MqttCallback()
                    {
                        @Override
                        public void connectionLost(Throwable cause)
                        {

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception
                        {
                            Log.e("MQTT", "Recieved message on topic " + topic + " - " + message.toString());
                            for (MqttControllerMessageCallbackListener listener : messageListeners)
                            {
                                try
                                {
                                    listener.onMessageArrived(topic, message);
                                }
                                catch (Exception e)
                                {
                                    Log.e("MqttController", e.toString());
                                }
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token)
                        {

                        }
                    });

                    subscribe("HYDRA/HMWZRETURN");
                    subscribe("HYDRA/HMWZRETURN/#");
                    subscribe("HYDRA/STATUS/results");
                    subscribe("HYDRA/AUTH/results");
                    subscribe("HYDRA/HUERETURN/#");
                    subscribe("HYDRA/HUERETURN");

                    //Refresh homewizard
                    publish("HYDRA/STATUS", "get-status");
                    //Refresh HUE
                    try
                    {
                        JSONObject jsonObject = Util.readHueData(context);
                        if (!jsonObject.getString("ip").isEmpty())
                        {
                            publish("HYDRA/HUE/connect", jsonObject.getString("ip"));
                        }
                    }
                    catch (JSONException e)
                    {
                        Log.e("MainActivity", e.getMessage());
                    }

                    dismissConnectingDialog();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    Toast toast = Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT);
                    toast.show();

                    dismissConnectingDialog();

                    Log.e("MqttController", exception.toString());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean publish(String topic, String payload)
    {
        return publish(topic, payload, true);
    }

    public boolean publish(String topic, String payload, boolean connectToast)
    {

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(2);
        message.setRetained(false);

        if (isConnected())
        {
            try
            {
                client.publish(topic, message);
                return true;
            }
            catch (MqttException e)
            {
                e.printStackTrace();
            }
        }
        else if (connectToast)
        {
            Toast toast = Toast.makeText(context, "Not connected to broker", Toast.LENGTH_SHORT);
            toast.show();
        }
        return false;
    }

    public void subscribe(String topic)
    {
        try
        {
            client.subscribe(topic, 0);
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }
    }
}
