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

public class Settings extends AppCompatActivity{

    //fields
    private MqttController mqttController;
    private String settings;
    private String serial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //geef text velden aan.
        final EditText emailField = (EditText) findViewById(R.id.emailField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);

        settings = readFile();

        try {
            JSONObject settingsFile = new JSONObject(settings);
            serial = settingsFile.getString("serial");
            emailField.setText(settingsFile.getString("email"));
            passwordField.setText(settingsFile.getString("password"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //subscribe op topic
        mqttController = MqttController.getInstance();
        mqttController.subscribe("HYDRA/AUTH/results");

        //set button functionaliteit
        Button loginbutton = (Button) findViewById(R.id.loginButton);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //publish email/password
                mqttController.publish("HYDRA/AUTH", "{\"email\":\"" + emailField.getText().toString() + "\", \"password\":\"" + passwordField.getText().toString() + "\", \"type\":\"login\"}");
            }
        });

        //set button functionaliteit
        Button clearbutton = (Button) findViewById(R.id.clearBtn);
        clearbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                writeFile("{\"email\":\"\", \"password\":\"\"}");
                emailField.setText("");
                passwordField.setText("");
            }
        });


        //als er een bericht terug word ontvangen
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
                            writeFile("{\"email\":\"" + emailField.getText().toString() + "\", \"password\":\"" + passwordField.getText().toString() + "\", \"serial\":\"" + serial + "\"}");
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

    private String readFile(){
        String settings = "";
        try {
            InputStream inputStream = openFileInput("config.json");

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

    private void writeFile(String data){
        //write login info naar een file
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.json", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
