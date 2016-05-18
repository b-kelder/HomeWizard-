package idu.stenden.inf1i.homewizard;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MqttAndroidClient client;
    private boolean connectSucces = false;
    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //client =  new MqttAndroidClient(getApplicationContext(), "tcp://10.110.111.141", "HomeWizard++");
        client =  new MqttAndroidClient(getApplicationContext(), "tcp://test.mosquitto.org", "HomeWizard++");

        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    connectSucces = true;
                    try {
                        client.subscribe("HMWZRETURN/#", 0);
                        client.subscribe("HMWZRETURN", 0);
                        mqtt("HMWZ", "get-sensors");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    connectSucces = false;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainListView = (ListView) findViewById( R.id.mainListView );

        // Create and populate a List of planet names.
        ArrayList<String> itemsList = new ArrayList<String>();

        listAdapter = new ArrayAdapter<String>(this, R.layout.row, R.id.rowTextView , itemsList);

        mainListView.setAdapter( listAdapter );



        //Switch knopje = (Switch) findViewById(R.id.knopje);

        //assert knopje != null;
        //knopje.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
        //    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //        if (isChecked) {
        //            The toggle is enabled
        //            mqtt("HMWZ/sw/1", "on");
        //
        //        } else {
        //            The toggle is disabled
        //            mqtt("HMWZ/sw/1", "off");
        //        }
        //    }
        //}));

        Button button = (Button) findViewById(R.id.testbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mqtt("HMWZ", "get-sensors");
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void mqtt(String topic, String payload){


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                JSONObject json = new JSONObject(message.toString());
                json = json.getJSONObject("request");
                String route = json.getString("route");

                if (route.equals("/get-sensors")){
                    listAdapter.clear();

                    json = new JSONObject(message.toString());
                    json = json.getJSONObject("response");
                    JSONArray array = json.getJSONArray("switches");

                    for (int i = 0; i < array.length(); i++){
                        JSONObject Swagtestsysteem = array.getJSONObject(i);

                        String name = Swagtestsysteem.getString("name");
                        String status = Swagtestsysteem.getString("status");
                        listAdapter.add(name);
                    }
                } else {
                    Log.d("Route", route);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(2);
        message.setRetained(false);

        if(connectSucces){
            try {
                client.publish(topic, message);
                Toast toast = Toast.makeText(getApplicationContext(), "MQTT message send", Toast.LENGTH_SHORT);
                toast.show();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), "Could not connect to broker", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
