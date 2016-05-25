package idu.stenden.inf1i.homewizard;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class Managelights extends BaseMqttEventActivity {

    private MqttController mqttController;
    private ListView mainListView;
    private DeviceEditAdapter listAdapter;
    private AppDataContainer appDataContainer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_managelights);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appDataContainer = AppDataContainer.getInstance().getInstance();

        //MQTT
        mqttController = MqttController.getInstance();
        mqttController.setContext(getApplicationContext());
        

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddChoise.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainListView = (ListView) findViewById(R.id.listViewManageLights);

        listAdapter = new DeviceEditAdapter(this, R.layout.row_manage, R.id.manageTxt, appDataContainer.getArray());

        mainListView.setAdapter(listAdapter);
    }
	
	@Override
	protected void addEventListeners(){
		addEventListener(new MqttControllerMessageCallbackListener() {
			@Override
			public void onMessageArrived(String topic, MqttMessage message) {
                if(topic.equals("HYDRA/HMWZRETURN/sw/remove")) {
                    // A light was removed, update everything
                    mqttController.publish("HYDRA/HMWZ", "get-sensors");
                    Toast.makeText(getApplicationContext(), "Removed device", Toast.LENGTH_SHORT).show();
                } else if(topic.contains("HYDRA/HMWZRETURN/sw/add")) {
                    // A light was added, update everything
                    mqttController.publish("HYDRA/HMWZ", "get-sensors");
                    Toast.makeText(getApplicationContext(), "Added device", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject json = new JSONObject(message.toString());
                        json = json.getJSONObject("request");
                        String route = json.getString("route");

                        if (route.equals("/get-sensors")) {
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
