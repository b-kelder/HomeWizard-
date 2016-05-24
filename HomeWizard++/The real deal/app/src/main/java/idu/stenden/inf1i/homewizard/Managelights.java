package idu.stenden.inf1i.homewizard;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Managelights extends AppCompatActivity {

    private MqttController mqttController;
    private ListView mainListView;
    private DeviceEditAdapter listAdapter;
    private AppDataContainer appDataContainer;
	
	private boolean eventHandlersAdded = false;

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
		
		if(!eventHandlersAdded) {
			eventHandlersAdded = true;
			mqttController.addMessageListener(new MqttControllerMessageCallbackListener() {
				@Override
				public void onMessageArrived(String topic, MqttMessage message) {

				}
			});
		}
        

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

}
