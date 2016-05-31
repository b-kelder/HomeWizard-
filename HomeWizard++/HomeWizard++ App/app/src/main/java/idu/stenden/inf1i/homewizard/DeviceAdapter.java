package idu.stenden.inf1i.homewizard;

import android.app.Application;
import android.content.Context;
import android.inputmethodservice.Keyboard;
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


class DeviceAdapter extends ArrayAdapter<BaseSwitch> {

    private static final int VIEWTYPE_SWITCH = 0;
    private static final int VIEWTYPE_DIMMER = 1;
    private static final int VIEWTYPE_COUNT = VIEWTYPE_DIMMER + 1;

    public DeviceAdapter(Context context, int resource) {
        super(context, resource);
    }

    public DeviceAdapter(Context context, int resource, int textViewResourceId, List<?> objects) {
        super(context, resource, textViewResourceId, (List<BaseSwitch>) objects);
    }

    @Override
    public int getItemViewType(int position){
        Log.e("DeviceAdapter", "getItemViewType, " + position);
        BaseSwitch sw = getItem(position);
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

        final BaseSwitch sw = getItem(position);
        int viewType = getItemViewType(position);


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

        //Sub-views
        final TextView swName;
        final Switch swSwitch;
        final SeekBar swBar;

        switch(viewType) {
            case VIEWTYPE_DIMMER: {
                //TODO: Fix this after fixing normal switches
                //Treat it as a dimmer
                swName = (TextView) convertView.findViewById(R.id.rowDimTextView);
                swBar = (SeekBar) convertView.findViewById(R.id.rowDimSeekBar);

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
                        if(HomewizardSwitch.class.isInstance(sw)) {
                            HomewizardSwitch homewizardSwitch = (HomewizardSwitch)sw;
                            //User stopped touching, set correct dim level
                            if(!homewizardSwitch.isUpdating()) {
                                int dimValue = seekBar.getProgress();

                                seekBar.setEnabled(false);

                                homewizardSwitch.setDimmer(dimValue);
                                homewizardSwitch.setStatus(dimValue > 0);
                                homewizardSwitch.sendStatus();
                                homewizardSwitch.sendDimmer();
                                homewizardSwitch.setUpdating(true);
                            }
                        } else {
                            //CustomSwitch
                            int dimValue = seekBar.getProgress();

                            sw.setDimmer(dimValue);
                            sw.sendDimmer();
                        }

                    }
                });

                if(HomewizardSwitch.class.isInstance(sw)) {
                    HomewizardSwitch homewizardSwitch = (HomewizardSwitch)sw;
                    if (!homewizardSwitch.isUpdating()) {
                        homewizardSwitch.setUpdating(true);
                        swBar.setMax(100);
                        swBar.setProgress(homewizardSwitch.getDimmer());
                        homewizardSwitch.setUpdating(false);
                    } else {
                        swBar.setMax(100);
                        swBar.setProgress(homewizardSwitch.getDimmer());
                    }

                    swBar.setEnabled(!homewizardSwitch.isUpdating());
                } else {
                    //CustomSwitch
                    swBar.setMax(100);
                    swBar.setProgress(sw.getDimmer());
                    swBar.setEnabled(true);
                }

            } break;
            case VIEWTYPE_SWITCH: {
                //Treat it as a switch
                swName = (TextView) convertView.findViewById(R.id.rowTextView);
                swSwitch = (Switch) convertView.findViewById(R.id.rowSwitch);

                swSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(HomewizardSwitch.class.isInstance(sw)) {
                            HomewizardSwitch homewizardSwitch = (HomewizardSwitch) sw;
                            //If we're not still waiting for a response from a previous toggle
                            if (!homewizardSwitch.isUpdating()) {
                                buttonView.setEnabled(false);

                                homewizardSwitch.setStatus(isChecked);
                                homewizardSwitch.sendStatus();
                                homewizardSwitch.setUpdating(true);
                            }
                        } else {
                            //CustomSwitch
                            sw.setStatus(isChecked);
                            sw.sendStatus();
                        }
                    }
                }));

                if(HomewizardSwitch.class.isInstance(sw)) {
                    HomewizardSwitch homewizardSwitch = (HomewizardSwitch) sw;
                    if (!homewizardSwitch.isUpdating()) {
                        homewizardSwitch.setUpdating(true);
                        swSwitch.setChecked(homewizardSwitch.getStatus());
                        homewizardSwitch.setUpdating(false);
                    } else {
                        swSwitch.setChecked(sw.getStatus());
                    }

                    swSwitch.setEnabled(!homewizardSwitch.isUpdating());
                } else {
                    //CustomSwitch
                    swSwitch.setChecked(sw.getStatus());
                    swSwitch.setEnabled(true);
                }
            } break;
            default: {
                // Shouldn't ever happen but stops the compiler from complaining
                swName = null;
            }
        } //switch

        swName.setText(sw.getName());

        return convertView;
    }
	
	@Override
	public void clear() {
        Log.i("DeviceAdapter", "Clearing DeviceAdapter");
		super.clear();
		//MqttController.getInstance().removeMessageListeners((MqttControllerMessageCallbackListener[])viewMessageCallbacks.toArray());
	}

}
