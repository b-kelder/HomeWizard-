package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class SetupStep3 extends BaseMqttEventActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_step3);

        context = this;

        final EditText emailField = (EditText) findViewById(R.id.setuphmwzUser);
        final EditText passwordField = (EditText) findViewById(R.id.setuphmwzPassword);

        Button loginbutton = (Button) findViewById(R.id.btnNextStep3);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //publish email/password
                MqttController.getInstance().loginHomeWizard(emailField.getText().toString(), passwordField.getText().toString(), Settings.context);
                Util.saveFirstSetup(context, false);
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });



        Button skipBtn = (Button) findViewById(R.id.btnSkipStep3);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Util.saveFirstSetup(context, false);
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    @Override
    protected void addEventListeners(){
        //als er een bericht terug word ontvangen
        final EditText emailField = (EditText) findViewById(R.id.setuphmwzUser);
        final EditText passwordField = (EditText) findViewById(R.id.setuphmwzPassword);

        addEventListener(new MqttControllerMessageCallbackListener() {
            @Override
            /// Stores last successful login data and serial
            public void onMessageArrived(String topic, MqttMessage message) {

                //Toast.makeText(getApplicationContext(), "TRIGGERED SETTINGS EVENT LISTENER " + topic, Toast.LENGTH_SHORT).show();
                if (topic.equals("HYDRA/AUTH/results")) {
                    //haal serial code uit json bericht
                    JSONObject json = null;
                    try {
                        json = new JSONObject(message.toString());
                        if (json.getString("status").equals("ok")) {
                            String serial = json.getString("serial");
                            Util.saveLoginData(SetupStep3.context, emailField.getText().toString(), passwordField.getText().toString(), serial);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
