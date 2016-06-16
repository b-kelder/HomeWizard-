package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class AddHueBridge extends AppCompatActivity
{
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hue_bridge);
        context = this;

        //Load IP
        JSONObject jsonObject = Util.readHueData(this);
        String ip;
        try
        {
            ip = jsonObject.getString("ip");
        }
        catch (JSONException e)
        {
            ip = "";
        }

        final EditText bridgeIP = (EditText) findViewById(R.id.hueBridgeIP);
        bridgeIP.setText(ip);

        Button nextBtn = (Button) findViewById(R.id.btnHueBridgeConnect);
        nextBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                MqttController.getInstance().publish("HYDRA/HUE/connect", bridgeIP.getText().toString());
                MqttController.getInstance().subscribe("HYDRA/HUERETURN/connect");

                MqttController.getInstance().addMessageListener(new MqttControllerMessageCallbackListener()
                {
                    @Override
                    public void onMessageArrived(String topic, MqttMessage message)
                    {
                        JSONObject json = null;
                        String status = null;
                        try
                        {
                            json = new JSONObject(message.toString());
                            status = json.getString("status");

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }

                        if (status.equals("ok"))
                        {
                            try
                            {
                                Util.saveHueData(context, bridgeIP.getText().toString(), json.getString("username"));

                                Toast.makeText(getApplicationContext(), "Device added", Toast.LENGTH_SHORT).show();

                                finish();
                                startActivity(new Intent(getApplicationContext(), AddChoice.class));

                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }
}
