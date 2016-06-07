package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseMqttEventActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MqttController mqttController;
    private AppDataContainer appDataContainer;

    private ListView mainListView;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appDataContainer = AppDataContainer.getInstance();
		appDataContainer.setSaveContext(this);
		//Load data on app startup
		appDataContainer.load();

        //MQTT
        mqttController = MqttController.getInstance();
        mqttController.setContext(getApplicationContext());


        try {
            JSONObject file = Util.readBrokerData(this);
            if(!mqttController.isConnected()){
                mqttController.connect("tcp://" + file.getString("ip") + ":" + file.getString("port"), "Homewizard++", file.getString("username"), file.getString("password"), this);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mainListView = (ListView) findViewById(R.id.mainListView);
        DeviceAdapter deviceAdapter = new DeviceAdapter(this, R.layout.row, R.id.rowTextView, appDataContainer.getAllSwitches());
        deviceAdapter.setNotifyOnChange(true);
        appDataContainer.setDeviceAdapter(deviceAdapter);
        mainListView.setAdapter(deviceAdapter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        //Save before exit
        appDataContainer.save();
    }


	@Override
	protected void addEventListeners() {
		addEventListener(new MqttControllerMessageCallbackListener() {
			@Override
            /// Updates the array adapter and the listview
			public void onMessageArrived(String topic, MqttMessage message) {
				try {
					JSONObject json = new JSONObject(message.toString());
					json = json.getJSONObject("request");
					String route = json.getString("route");

					if (route.equals("/get-sensors")) {
						appDataContainer.clearHomewizardSwitches();
                        appDataContainer.getDeviceAdapter().clear();
						json = new JSONObject(message.toString());
						json = json.getJSONObject("response");
						JSONArray array = json.getJSONArray("switches");
						for (int i = 0; i < array.length(); i++) {
							JSONObject switches = array.getJSONObject(i);

							String name = switches.getString("name");
                            String type = switches.getString("type");
							String status = switches.getString("status");
							String id = switches.getString("id");
                            HomewizardSwitch hmwzSwitch = new HomewizardSwitch(name, type, status, id);
                            if(type.equals("dimmer")){
                                hmwzSwitch.setDimmer(switches.getString("dimlevel"));
                            } else if(type.equals("hue")) {
                                //NOPE
                                continue;
                            }
							appDataContainer.addHomewizardSwitch(hmwzSwitch);
						}
						appDataContainer.getDeviceAdapter().notifyDataSetChanged();
                        mainListView.invalidate();

                        Toast.makeText(getApplicationContext(), "Device list refreshed", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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
        if (id == R.id.action_refresh) {
            try {
                JSONObject file = Util.readBrokerData(this);
                if(!mqttController.isConnected()){
                    mqttController.connect("tcp://" + file.getString("ip") + ":" + file.getString("port"), "Homewizard++", file.getString("username"), file.getString("password"), this);
                } else {
                    mqttController.publish("HYDRA/HMWZ", "get-sensors");
                    //Refresh HUE
                    JSONObject jsonObject = Util.readHueData(this);
                    if (!jsonObject.getString("ip").isEmpty()) {
                        mqttController.publish("HYDRA/HUE/connect", jsonObject.getString("ip"));
                    }
                }
            } catch (JSONException e) {
                Log.e("MainActivity", e.getMessage());
            }


        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, Settings.class));
        } else if (id == R.id.nav_lights) {
            startActivity(new Intent(MainActivity.this, Managelights.class));
        }else if(id == R.id.nav_help){
            startActivity(new Intent(MainActivity.this, HelpPage.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
