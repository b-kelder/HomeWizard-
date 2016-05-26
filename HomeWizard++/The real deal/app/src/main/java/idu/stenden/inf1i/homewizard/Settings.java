package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;

public class Settings extends BaseMqttEventActivity{

    //fields
    private MqttController mqttController;
    private String serial;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = this;

        super.onCreate(savedInstanceState);

        mqttController = MqttController.getInstance();

        setContentView(R.layout.activity_settings);

        //geef text velden aan.
        final EditText emailField = (EditText) findViewById(R.id.emailField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);
        final EditText brokerIP = (EditText) findViewById(R.id.brokerIP);
        final EditText brokerPort = (EditText) findViewById(R.id.brokerPort);

        try {
            JSONObject loginSettingsFile = Util.readLoginData(this);
            serial = loginSettingsFile.getString("serial");
            emailField.setText(loginSettingsFile.getString("email"));
            passwordField.setText(loginSettingsFile.getString("password"));

            JSONObject brokerSettings = Util.readBrokerData(Settings.context);
            brokerIP.setText(brokerSettings.getString("ip"));
            brokerPort.setText(brokerSettings.getString("port"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set button functionaliteit
        Button loginbutton = (Button) findViewById(R.id.loginButton);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //publish email/password
                mqttController.loginHomeWizard(emailField.getText().toString(), passwordField.getText().toString(), Settings.context);
            }
        });

        Button brokerSettings = (Button) findViewById(R.id.bkrConfirmBtn);
        brokerSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //publish email/password
                Util.saveBrokerData(Settings.context, brokerIP.getText().toString(), brokerPort.getText().toString());
                Toast toast = Toast.makeText(getApplicationContext(), "Trying to connect to broker", Toast.LENGTH_SHORT);
                toast.show();

                try {
                    JSONObject file =Util.readBrokerData(Settings.context);
                    mqttController.connect("tcp://" + file.getString("ip") + ":" + file.getString("port"), "Homewizard++", Settings.context);
                } catch (JSONException e) {
                    Toast toaster = Toast.makeText(getApplicationContext(), "Unable to connect to broker", Toast.LENGTH_SHORT);
                    toaster.show();
                    e.printStackTrace();
                }
            }
        });

        //set button functionaliteit
        Button clearbutton = (Button) findViewById(R.id.clearBtn);
        clearbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Util.saveLoginData(Settings.context, emailField.getText().toString(), passwordField.getText().toString(), serial);
                emailField.setText("");
                passwordField.setText("");
                Toast toast = Toast.makeText(getApplicationContext(), "Cleared all login data", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // handle adminpin buttons.
        final Switch adminPinButton = (Switch) findViewById(R.id.adminEnabled);
        final EditText adminPin = (EditText) findViewById(R.id.editAdminPin);
        adminPinButton.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    adminPin.setEnabled(true);
                } else {
                    adminPin.setEnabled(false);
                }
            }
        }));
        //Disable
        adminPin.setEnabled(false);

    }

	
	@Override
	protected void addEventListeners(){
		//als er een bericht terug word ontvangen
        final EditText emailField = (EditText) findViewById(R.id.emailField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);

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
                            Util.saveLoginData(Settings.context, emailField.getText().toString(), passwordField.getText().toString(), serial);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
	}
}
