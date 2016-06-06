package idu.stenden.inf1i.homewizard;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.CompoundButton;
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
class DeviceEditAdapter extends ArrayAdapter<BaseSwitch> {

    private static final int VIEWTYPE_ITEM = 0;
    private static final int VIEWTYPE_SEPARATOR = 1;
    private static final int VIEWTYPE_COUNT = VIEWTYPE_SEPARATOR + 1;

    public DeviceEditAdapter(Context context, int resource) {
        super(context, resource);
    }

    public DeviceEditAdapter(Context context, int resource, int textViewResourceId, List<?> objects) {
        super(context, resource, textViewResourceId, (List<BaseSwitch>) objects);
    }

    @Override
    public int getItemViewType(int position){
        Log.e("DeviceEditAdapter", "getItemViewType, " + position);
        BaseSwitch sw = getItem(position);
        if(sw.getType().equals("separator")){
            return VIEWTYPE_SEPARATOR;
        } else {
            return VIEWTYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount(){
        return VIEWTYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final BaseSwitch sw = getItem(position);
        int viewType = getItemViewType(position);

        if(convertView == null) {
            switch(viewType) {
                case VIEWTYPE_ITEM:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_manage, parent, false);
                    break;
                case VIEWTYPE_SEPARATOR:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_seperator, parent, false);
                    break;
            }
        }

        switch(viewType){
            case VIEWTYPE_ITEM:{
                TextView btnName = (TextView) convertView.findViewById(R.id.manageTxt);
                final ImageButton btnChange = (ImageButton) convertView.findViewById(R.id.btnChange);
                final ImageButton btnDelete = (ImageButton) convertView.findViewById(R.id.btnDelete);

                btnName.setText(sw.getName());


                final Dialog delete = new Dialog(getContext());
                delete.requestWindowFeature(Window.FEATURE_NO_TITLE);
                delete.setContentView(R.layout.delete_dialog);

                final Button btnDoDelete = (Button) delete.findViewById(R.id.btnDelete);
                final Button btnNoDelete = (Button) delete.findViewById(R.id.btnNoDelete);

                btnName.setText(sw.getName());

                btnChange.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //MqttController.getInstance().publish("", "");
                        //TODO: Popup dialog with options for name and code change

                        if(HomewizardSwitch.class.isInstance(sw))
                        {
                            
                        }
                        else if(CustomSwitch.class.isInstance(sw))
                        {

                        }
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        delete.show();
                    }
                });

                btnDoDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(HomewizardSwitch.class.isInstance(sw)) {
                            HomewizardSwitch homewizardSwitch = (HomewizardSwitch)sw;
                            String switchId = String.valueOf(homewizardSwitch.getId());
                            //Put switchId in topic so we can filter the return message properly
                            MqttController.getInstance().publish("HYDRA/HMWZ/sw/remove/" + switchId, "");
                            delete.dismiss();
                        } else if(HueSwitch.class.isInstance(sw)) {
                            //TODO: Remove Hue?
                        } else {
                            //Remove CustomSwitch from list
                            CustomSwitch customSwitch = (CustomSwitch)sw;
                            AppDataContainer.getInstance().removeCustomSwitch(customSwitch);
                            AppDataContainer.getInstance().notifyDataSetChanged();
                            AppDataContainer.getInstance().save();
                            Toast.makeText(getContext(), "Custom device removed", Toast.LENGTH_SHORT).show();
                            delete.dismiss();
                        }
                    }
                });

                btnNoDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delete.dismiss();
                    }
                });
            } break;
            case VIEWTYPE_SEPARATOR:{
                TextView name = (TextView) convertView.findViewById(R.id.rowSepText);
                name.setText(sw.getName());
            } break;
        }



        return convertView;
    }
}
