package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bram on 19/05/2016.
 */
class DeviceEditAdapter extends ArrayAdapter<HomewizardSwitch> {

    //TODO: Add support for non-Homewizard items

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
        final ImageButton btnChange = (ImageButton) convertView.findViewById(R.id.btnChange);
        final ImageButton btnDelete = (ImageButton) convertView.findViewById(R.id.btnDelete);

        btnName.setText(sw.getName());

        final String switchId = String.valueOf(sw.getId());

        btnChange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //MqttController.getInstance().publish("", "");
                //TODO: Popup dialog with options for name and code change
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Put switchId in topic so we can filter the return message properly
                MqttController.getInstance().publish("HYDRA/HMWZ/sw/remove/" + switchId, "");
            }
        });

        return convertView;
    }
}
