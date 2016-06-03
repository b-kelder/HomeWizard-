package idu.stenden.inf1i.homewizard;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Managelights extends BaseMqttEventActivity {

    private MqttController mqttController;
    private ListView mainListView;
    private AppDataContainer appDataContainer;
    public static Context context;

    private boolean adminPinEnabled;
    private String adminPin;
    private int counter;
    private boolean loginEnabled = true;
    private long loginTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_managelights);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try
        {
            JSONObject adminPinSettings = Util.readAdminPin(Managelights.context);
            adminPinEnabled = adminPinSettings.getBoolean("enabled");
            adminPin = adminPinSettings.getString("pin");

            JSONObject getLoginAttempts = Util.readLoginAttempts(Managelights.context);
            loginEnabled = getLoginAttempts.getBoolean("enabled");
            loginTimestamp = getLoginAttempts.getLong("timestamp");
            counter = getLoginAttempts.getInt("attempts");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //  if adminpin is enabled and pin is not empty, show dialog
        if(adminPinEnabled && !adminPin.isEmpty()) {
            final Dialog login = new Dialog(this);

            login.requestWindowFeature(Window.FEATURE_NO_TITLE);
            login.setContentView(R.layout.login_dialog);
            login.setCanceledOnTouchOutside(false); // makes sure you can not cancel dialog by clicking outside of it
            login.setCancelable(false);

            final Button btnLogin = (Button) login.findViewById(R.id.btnLogin);
            final Button btnCancel = (Button) login.findViewById(R.id.btnCancel);
            final EditText txtPassword = (EditText) login.findViewById(R.id.txtPassword);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (txtPassword.getText().toString().trim().length() > 0) {
                        if(loginEnabled) {
                            if (txtPassword.getText().toString().equals(adminPin)) {
                                login.dismiss();
                                Util.saveLoginAttempts(context, 0, 2, true); // On login, reset login-attempts
                            } else if (counter == 0) {
                                Toast.makeText(Managelights.this, "Login disabled. To many failed attempts. Try again in 60 seconds.", Toast.LENGTH_LONG).show();
                                Util.saveLoginAttempts(context, new Date().getTime(), 0, false); // set login to false and attempts to 0
                                finish();
                            } else {
                                Toast.makeText(Managelights.this, "Incorrect pin", Toast.LENGTH_LONG).show();
                                // subtract 1 from current counter, and save
                                counter--;
                                Util.saveLoginAttempts(context, 0, counter, true);
                            }
                        }
                        else
                        {
                            // Very simple anti-brute force system.
                            long timespan = Math.abs((System.currentTimeMillis() - 60000 - loginTimestamp) / 1000); // display timer for when login is re-enabled

                            // when 60 seconds passed (in miliseconds), re-enable login
                            if(System.currentTimeMillis() - 60000 > loginTimestamp)
                            {
                                Util.saveLoginAttempts(context, 0, 2, true); // set login to true and reset attempts
                                Toast.makeText(Managelights.this, "Login attempts resetting." , Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else
                            {
                                Toast.makeText(Managelights.this, "Login is disabled for " + timespan + " seconds." , Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(Managelights.this, "Please enter a pin code", Toast.LENGTH_LONG).show();
                    }
                }
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            login.show();
        }

        appDataContainer = AppDataContainer.getInstance();

        //MQTT
        mqttController = MqttController.getInstance();
        mqttController.setContext(getApplicationContext());
        

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddChoice.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainListView = (ListView) findViewById(R.id.listViewManageLights);

        DeviceEditAdapter deviceEditAdapter = new DeviceEditAdapter(this, R.layout.row_manage, R.id.manageTxt, appDataContainer.getAllSwitches());
        appDataContainer.setDeviceEditAdapter(deviceEditAdapter);
        mainListView.setAdapter(deviceEditAdapter);
    }


	@Override
	protected void addEventListeners(){
		addEventListener(new MqttControllerMessageCallbackListener() {
			@Override
			public void onMessageArrived(String topic, MqttMessage message) {
                if(topic.equals("HYDRA/HMWZRETURN")){
                    try {
                        JSONObject json = new JSONObject(message.toString());
                        json = json.getJSONObject("request");
                        String route = json.getString("route");

                        // Since MainActivity SHOULD have added their event listener first this SHOULD be called after that
                        // so we can assume the lights array is updated
                        if (route.equals("/get-sensors")) {
                            AppDataContainer.getInstance().getDeviceEditAdapter().notifyDataSetChanged();
                            mainListView.invalidate();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
			}
		});
	}
}
