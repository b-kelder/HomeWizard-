package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Settings extends BaseMqttEventActivity{

    //fields
    private MqttController mqttController;
    private String serial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //geef text velden aan.
        final EditText emailField = (EditText) findViewById(R.id.emailField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);
        final EditText brokerIP = (EditText) findViewById(R.id.brokerIP);
        final EditText brokerPort = (EditText) findViewById(R.id.brokerPort);

        try {
            JSONObject loginSettingsFile = new JSONObject(readFile("login.json"));
            serial = loginSettingsFile.getString("serial");
            emailField.setText(loginSettingsFile.getString("email"));
            passwordField.setText(loginSettingsFile.getString("password"));

            JSONObject brokerSettings = new JSONObject(readFile("broker.json"));
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
                mqttController.publish("HYDRA/AUTH", "{\"email\":\"" + emailField.getText().toString() + "\", \"password\":\"" + passwordField.getText().toString() + "\", \"type\":\"login\"}");
                Toast toast = Toast.makeText(getApplicationContext(), "Trying to log in", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        Button brokerSettings = (Button) findViewById(R.id.bkrConfirmBtn);
        brokerSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //publish email/password
                writeFile("broker.json", "{\"ip\":\"" + brokerIP.getText() + "\", \"port\":\"" + brokerPort.getText() + "\"}");
                Toast toast = Toast.makeText(getApplicationContext(), "Trying to connect to broker", Toast.LENGTH_SHORT);
                toast.show();

                try {
                    JSONObject file = new JSONObject(readFile("broker.json"));
                    mqttController.connect("tcp://" + file.getString("ip") + ":" + file.getString("port"), "Homewizard++");
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
                writeFile("login.json", "{\"email\":\"\", \"password\":\"\"}");
                emailField.setText("");
                passwordField.setText("");
                Toast toast = Toast.makeText(getApplicationContext(), "Cleared all login data", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        //subscribe op topic
        mqttController = MqttController.getInstance();
        if(mqttController.isConnected()){
            mqttController.subscribe("HYDRA/AUTH/results");
        }
    }
	
	@Override
	protected void addEventHandlers(){
		//als er een bericht terug word ontvangen
		mqttController = MqttController.getInstance();
		mqttController.addMessageListener(new MqttControllerMessageCallbackListener() {
			@Override
			public void onMessageArrived(String topic, MqttMessage message) {

				if (topic.equals("HYDRA/AUTH/results")) {
					//haal serial code uit json bericht
					JSONObject json = null;
					try {
						json = new JSONObject(message.toString());
						if (json.getString("status").equals("ok")) {
							String serial = json.getString("serial");
							writeFile("login.json", "{\"email\":\"" + emailField.getText().toString() + "\", \"password\":\"" + passwordField.getText().toString() + "\", \"serial\":\"" + serial + "\"}");
						} else {
							Toast toast = Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT);
							toast.show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

    public String getSerial(){
        return serial;
    }

    private String readFile(String file){
        String settings = "";
        try {
            InputStream inputStream = openFileInput(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                settings = stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return settings;
    }

    private void writeFile(String file, String data){
        //write login info naar een file
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(file, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
