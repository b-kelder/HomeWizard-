package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, parent, false);
        }

        TextView swName = (TextView) convertView.findViewById(R.id.rowTextView);
        final Switch swSwitch = (Switch) convertView.findViewById(R.id.rowSwitch);
		
		MqttControllerMessageCallbackListener callbackListener = new MqttControllerMessageCallbackListener() {
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
                                swSwitch.toggle();
                                swSwitch.setEnabled(true);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
		
		viewMessageCallbacks.remove(callbackListener);		//If there's already an equivalent callbackListener
		viewMessageCallbacks.add(callbackListener);
		MqttController.getInstance().addMessageListener(callbackListener);

        swName.setText(sw.getName());
        swSwitch.setChecked(sw.getStatus());

        final String switchId = String.valueOf(sw.getId());

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

        return convertView;
    }
	
	@Override
	public void clear() {
		super.clear();
		MqttController.getInstance().removeMessageListeners((MqttControllerMessageCallbackListener[])viewMessageCallbacks.toArray());
	}

}
