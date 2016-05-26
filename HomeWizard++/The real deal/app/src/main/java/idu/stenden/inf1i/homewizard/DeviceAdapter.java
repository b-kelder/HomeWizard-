package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bram on 19/05/2016.
 */
class DeviceAdapter extends ArrayAdapter<HomewizardSwitch> {

	protected ArrayList<MqttControllerMessageCallbackListener> viewMessageCallbacks = new ArrayList<MqttControllerMessageCallbackListener>();

    public DeviceAdapter(Context context, int resource) {
        super(context, resource);
    }

    public DeviceAdapter(Context context, int resource, int textViewResourceId, List<?> objects) {
        super(context, resource, textViewResourceId, (List<HomewizardSwitch>) objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final HomewizardSwitch sw = getItem(position);
        if(convertView == null){
            if(sw.getType().equals("dimmer")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_dim, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, parent, false);
            }
        }

        TextView swName;// = (TextView) convertView.findViewById(R.id.rowTextView);
        final Switch swSwitch;// = (Switch) convertView.findViewById(R.id.rowSwitch);
        final SeekBar swBar;
        final String switchId = String.valueOf(sw.getId());

        MqttControllerMessageCallbackListener callbackListener;

        if(sw.getType().equals("dimmer")) {
            //Treat it as a dimmer
            swName = (TextView) convertView.findViewById(R.id.rowDimTextView);
            swBar = (SeekBar) convertView.findViewById(R.id.rowDimSeekBar);
            swBar.setMax(100);
            swBar.setProgress(sw.getDimmer());

            callbackListener = new MqttControllerMessageCallbackListener() {
                @Override
                public void onMessageArrived(String topic, MqttMessage message) {

                    int id = Integer.parseInt(topic.substring(topic.lastIndexOf("/")+1));

                    if(id == sw.getId()) {
                        try {
                            JSONObject returnValue = new JSONObject(message.toString());
                            //Unlock the bar
                            if(!swBar.isEnabled() && returnValue.getJSONObject("request").getString("route").equals("/sw/dim")) {
                                if (returnValue.getString("status").equals("ok")) {
                                    sw.setDimmer(swBar.getProgress());
                                    swBar.setEnabled(true);
                                } else {
                                    swBar.setProgress(sw.getDimmer());
                                    swBar.setEnabled(true);
                                }
                            } else {
                                //This was an on/off toggle, don't do anything
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            //Bar change
            swBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    //Not relevant
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //Not relevant
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //User stopped touching, set correct dim level
                    int dimValue = seekBar.getProgress();
                    if(dimValue == 0) {
                        MqttController.getInstance().publish("HYDRA/HMWZ/sw/" + switchId, "off");
                    } else {
                        MqttController.getInstance().publish("HYDRA/HMWZ/sw/" + switchId, "on");
                    }
                    MqttController.getInstance().publish("HYDRA/HMWZ/sw/dim/" + switchId, dimValue + "");
                    swBar.setEnabled(false);
                }
            });
        } else {
            //Treat it as a normal switch
            swName = (TextView) convertView.findViewById(R.id.rowTextView);
            swSwitch = (Switch) convertView.findViewById(R.id.rowSwitch);

            callbackListener = new MqttControllerMessageCallbackListener() {
                @Override
                public void onMessageArrived(String topic, MqttMessage message) {

                    int id = Integer.parseInt(topic.substring(topic.lastIndexOf("/")+1));

                    if(id == sw.getId()) {
                        try {
                            JSONObject returnValue = new JSONObject(message.toString());

                            if(!swSwitch.isEnabled()) {
                                if (returnValue.getString("status").equals("ok")) {
                                    swSwitch.setEnabled(true);
                                } else {
                                    if(!returnValue.getJSONObject("request").get("route").equals("/sw/dim")){
                                        swSwitch.toggle();
                                    }
                                    swSwitch.setEnabled(true);
                                }
                                //Update data
                                sw.setStatus(swSwitch.isChecked());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            swSwitch.setChecked(sw.getStatus());

            swSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (swSwitch.isEnabled()) {
                        if (isChecked) {
                            MqttController.getInstance().publish("HYDRA/HMWZ/sw/" + switchId, "on");
                        } else {
                            MqttController.getInstance().publish("HYDRA/HMWZ/sw/" + switchId, "off");
                        }
                        swSwitch.setEnabled(false);
                    }
                }
            }));
        }

        swName.setText(sw.getName());

		viewMessageCallbacks.remove(callbackListener);		//If there's already an equivalent callbackListener
        MqttController.getInstance().removeMessageListener(callbackListener);
		viewMessageCallbacks.add(callbackListener);
		MqttController.getInstance().addMessageListener(callbackListener);


        return convertView;
    }
	
	@Override
	public void clear() {
		super.clear();
		MqttController.getInstance().removeMessageListeners((MqttControllerMessageCallbackListener[])viewMessageCallbacks.toArray());
	}

}
