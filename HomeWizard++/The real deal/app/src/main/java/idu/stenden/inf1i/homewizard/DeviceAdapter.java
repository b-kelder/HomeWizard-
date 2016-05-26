package idu.stenden.inf1i.homewizard;

import android.app.Application;
import android.content.Context;
import android.util.Log;
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

    private static final int VIEWTYPE_SWITCH = 0;
    private static final int VIEWTYPE_DIMMER = 1;
    private static final int VIEWTYPE_COUNT = VIEWTYPE_DIMMER + 1;

	protected ArrayList<MqttControllerMessageCallbackListener> viewMessageCallbacks = new ArrayList<MqttControllerMessageCallbackListener>();

    public DeviceAdapter(Context context, int resource) {
        super(context, resource);
    }

    public DeviceAdapter(Context context, int resource, int textViewResourceId, List<?> objects) {
        super(context, resource, textViewResourceId, (List<HomewizardSwitch>) objects);
    }

    @Override
    public int getItemViewType(int position){
        HomewizardSwitch sw = getItem(position);
        if(sw.getType().equals("dimmer")){
            return VIEWTYPE_DIMMER;
        } else {
            return VIEWTYPE_SWITCH;
        }
    }

    @Override
    public int getViewTypeCount(){
        return VIEWTYPE_COUNT;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final HomewizardSwitch sw = getItem(position);
        int viewType = getItemViewType(position);

        Log.i("DeviceAdapter", "getView " + position + " " + convertView + " type = " + viewType + " switch " + sw.getId());

        if(convertView == null) {
            switch(viewType) {
                case VIEWTYPE_DIMMER:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_dim, parent, false);
                    break;
                case VIEWTYPE_SWITCH:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, parent, false);
                    break;
            }
        }

        final TextView swName;
        final Switch swSwitch;
        final SeekBar swBar;
        final String switchId = String.valueOf(sw.getId());

        MqttControllerMessageCallbackListener callbackListener;

        switch(viewType) {
            case VIEWTYPE_DIMMER: {
                //Treat it as a dimmer
                swName = (TextView) convertView.findViewById(R.id.rowDimTextView);
                swBar = (SeekBar) convertView.findViewById(R.id.rowDimSeekBar);
                swBar.setMax(100);

                //Prevent old listener from doing bad things
                swBar.setOnSeekBarChangeListener(null);

                swBar.setProgress(sw.getDimmer());
                if(sw.isWaitingForResponse()){
                    swBar.setEnabled(false);
                } else {
                    swBar.setEnabled(true);
                }

                // Event listeners from this point down
                callbackListener = new MqttControllerMessageCallbackListener() {
                    @Override
                    public void onMessageArrived(String topic, MqttMessage message) {

                        int id = Integer.parseInt(topic.substring(topic.lastIndexOf("/")+1));

                        if(id == sw.getId()) {
                            try {
                                JSONObject returnValue = new JSONObject(message.toString());
                                //Unlock the bar
                                if(sw.isWaitingForResponse() && returnValue.getJSONObject("request").getString("route").equals("/sw/dim")) {
                                    if (returnValue.getString("status").equals("ok")) {
                                        sw.setDimmer(swBar.getProgress());
                                        swBar.setEnabled(true);
                                    } else {
                                        swBar.setProgress(sw.getDimmer());
                                        swBar.setEnabled(true);
                                    }
                                    sw.setWaitingForResponse(false);
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
                        sw.setWaitingForResponse(true);
                        swBar.setEnabled(false);
                    }
                });


            } break;
            case VIEWTYPE_SWITCH: {
                swName = (TextView) convertView.findViewById(R.id.rowTextView);
                swSwitch = (Switch) convertView.findViewById(R.id.rowSwitch);

                //Prevent 'old' listener from messing things up...
                swSwitch.setOnClickListener(null);

                swSwitch.setChecked(sw.getStatus());
                if(sw.isWaitingForResponse()){
                    swSwitch.setEnabled(false);
                } else {
                    swSwitch.setEnabled(true);
                }

                // Event listeners from this point down
                callbackListener = new MqttControllerMessageCallbackListener() {
                    @Override
                    public void onMessageArrived(String topic, MqttMessage message) {

                        int id = Integer.parseInt(topic.substring(topic.lastIndexOf("/")+1));

                        if(id == sw.getId()) {
                            try {
                                JSONObject returnValue = new JSONObject(message.toString());

                                if(sw.isWaitingForResponse()) {
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
                                    sw.setWaitingForResponse(false);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                swSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //If we're not still waiting for a response from a previous toggle
                        if (!sw.isWaitingForResponse()) {
                            if (isChecked) {
                                MqttController.getInstance().publish("HYDRA/HMWZ/sw/" + switchId, "on");
                            } else {
                                MqttController.getInstance().publish("HYDRA/HMWZ/sw/" + switchId, "off");
                            }
                            sw.setWaitingForResponse(true);
                            swSwitch.setEnabled(false);
                        } else {
                            Log.e("DeviceAdapter", "Toggled switch that should be disabled!");
                        }
                    }
                }));
            } break;
            default: {
                callbackListener = null;
                swName = null;
            }
        } //switch

        swName.setText(sw.getName() + "(" + sw.getId() + ")");

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
