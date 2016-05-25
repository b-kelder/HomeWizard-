package idu.stenden.inf1i.homewizard;

// TODO: Clean up imports here

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BaseMqttEventActivity extends AppCompatActivity {

    protected boolean eventHandlersAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        
        if(!eventHandlersAdded) {
			eventHandlersAdded = true;
			addEventHandlers();
		}
    }
	
	/// Override this with your event handlers
	protected void addEventHandlers(){
		
	}
}
