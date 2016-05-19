package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Bram on 19/05/2016.
 */
class DeviceAdapter extends ArrayAdapter<HomewizardSwitch> {

    public DeviceAdapter(Context context, int resource) {
        super(context, resource);
    }

    public DeviceAdapter(Context context, int resource, int textViewResourceId, List<?> objects) {
        super(context, resource, textViewResourceId, (List<HomewizardSwitch>) objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HomewizardSwitch sw = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, parent, false);
        }

        TextView swName = (TextView) convertView.findViewById(R.id.rowTextView);
        Switch swSwitch = (Switch) convertView.findViewById(R.id.rowSwitch);

        swName.setText(sw.getName());
        swSwitch.setChecked(sw.getStatus());

        final String switchId = String.valueOf(sw.getId());

        swSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MqttController.getInstance().publish("HMWZ/sw/" + switchId, "on");

                } else {
                    MqttController.getInstance().publish("HMWZ/sw/" + switchId, "off");
                }
            }
        }));

        return convertView;
    }
}
