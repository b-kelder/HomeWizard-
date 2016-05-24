package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Bram on 19/05/2016.
 */
class DeviceEditAdapter extends ArrayAdapter<HomewizardSwitch> {

    public DeviceEditAdapter(Context context, int resource) {
        super(context, resource);
    }

    public DeviceEditAdapter(Context context, int resource, int textViewResourceId, List<?> objects) {
        super(context, resource, textViewResourceId, (List<HomewizardSwitch>) objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final HomewizardSwitch sw = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_manage, parent, false);
        }

        TextView btnName = (TextView) convertView.findViewById(R.id.manageTxt);
        final Button btnChange = (Button) convertView.findViewById(R.id.btnChange);
        final Button btnDelete = (Button) convertView.findViewById(R.id.btnDelete);

        MqttController.getInstance().addMessageListener(new MqttControllerMessageCallbackListener() {
            @Override
            public void onMessageArrived(String topic, MqttMessage message) {
                int id = Integer.parseInt(topic.substring(topic.lastIndexOf("/")+1));

            }
        });

        btnName.setText(sw.getName());

        final String switchId = String.valueOf(sw.getId());

        btnChange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MqttController.getInstance().publish("", "");
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MqttController.getInstance().publish("HYDRA/HMWZ/sw/remove/", switchId);
            }
        });

        return convertView;
    }

}
