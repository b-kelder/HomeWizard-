package idu.stenden.inf1i.homewizard;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by Wouter on 06-06-16.
 */
public class EditDialogs {
    public static void showHomeWizardSwitchDialog(Context context, final HomewizardSwitch homewizardSwitch) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_dialog_hmwz_switch);
        dialog.setCancelable(true);
        dialog.show();

        //// Item specific code
        final TextView nameView = (TextView) dialog.findViewById(R.id.editHmwzSwitchName);
        nameView.setText(homewizardSwitch.getName());

        //// Apply button
        final Button applyButton = (Button) dialog.findViewById(R.id.editHmwzSwitchButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Store data in object
                homewizardSwitch.setName(nameView.getText().toString());

                homewizardSwitch.sendEdit();

                AppDataContainer.getInstance().notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

    public static void showCustomSwitchDialog(final Context context, final CustomSwitch customSwitch) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_dialog_custom_switch);
        dialog.setCancelable(true);
        dialog.show();

        //// Item specific code
        final TextView nameView = (TextView) dialog.findViewById(R.id.editCustomSwitchName);
        final TextView topicView = (TextView) dialog.findViewById(R.id.editCustomSwitchTopic);
        final TextView onView = (TextView) dialog.findViewById(R.id.editCustomSwitchOn);
        final TextView offView = (TextView) dialog.findViewById(R.id.editCustomSwitchOff);
        final TextView offText = (TextView) dialog.findViewById(R.id.editCustomSwitchOffText);
        final TextView onText = (TextView) dialog.findViewById(R.id.editCustomSwitchOnText);
        final TextView dimmerText = (TextView) dialog.findViewById(R.id.editCustomSwitchMaxDimmerText);
        final TextView dimmerView = (TextView) dialog.findViewById(R.id.editCustomSwitchMaxDimmer);

        nameView.setText(customSwitch.getName());
        topicView.setText(customSwitch.getTopic());
        onView.setText(customSwitch.getPayloadOn());
        offView.setText(customSwitch.getPayloadOff());
        dimmerView.setText(""+customSwitch.getMaxDimmerValue());

        if(customSwitch.getType().equals("dimmer")) {
            onView.setVisibility(View.GONE);
            onText.setVisibility(View.GONE);
            offText.setVisibility(View.GONE);
            offView.setVisibility(View.GONE);
        } else if(customSwitch.getType().equals("button")) {
            offText.setText("Button text:");
            onText.setText("MQTT Payload:");
            dimmerText.setVisibility(View.GONE);
            dimmerView.setVisibility(View.GONE);
        } else if(customSwitch.getType().equals("text")) {
            onView.setVisibility(View.GONE);
            onText.setVisibility(View.GONE);
            offView.setVisibility(View.GONE);
            offText.setVisibility(View.GONE);
            dimmerText.setVisibility(View.GONE);
            dimmerView.setVisibility(View.GONE);
        }else if(customSwitch.getType().equals("switch")) {
            dimmerText.setVisibility(View.GONE);
            dimmerView.setVisibility(View.GONE);
        }

        //// Apply button
        final Button applyButton = (Button) dialog.findViewById(R.id.editCustomSwitchButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Store data in object
                Boolean error = false;
                //check if name and topic fields are not empty
                if(!nameView.getText().toString().isEmpty() && !topicView.getText().toString().isEmpty()) {
                    //If dimmer
                    if (customSwitch.getType().equals("dimmer")) {
                        //check if max dimmer field is not empty
                        if (!dimmerView.getText().toString().isEmpty()) {
                            //save dimmer data
                            customSwitch.setName(nameView.getText().toString());
                            customSwitch.setTopic(topicView.getText().toString());
                            customSwitch.setMaxDimmerValue(dimmerView.getText().toString());
                        } else {
                            error = true;
                        }
                    //If button or switch
                    } else if (customSwitch.getType().equals("button") || customSwitch.getType().equals("switch")) {
                        //check if the off (Button text) and on (Button payload) fields are not empty
                        if (!offView.getText().toString().isEmpty() && !onView.getText().toString().isEmpty()) {
                            //save button/switch data
                            customSwitch.setName(nameView.getText().toString());
                            customSwitch.setTopic(topicView.getText().toString());
                            customSwitch.setPayloadOn(onView.getText().toString());
                            customSwitch.setPayloadOff(offView.getText().toString());
                        } else {
                            error = true;
                        }
                    //if text field
                    } else if(customSwitch.getType().equals("text")) {
                        //save text field data
                        customSwitch.setName(nameView.getText().toString());
                        customSwitch.setTopic(topicView.getText().toString());
                    }
                    //if colorpicker
                      else if(customSwitch.getType().equals("colorpicker")) {
                        //check if max dimmer value, off and on text are not empty
                        if (!dimmerView.getText().toString().isEmpty() && !offView.getText().toString().isEmpty() && !onView.getText().toString().isEmpty()) {
                            //save colorpicker data
                            customSwitch.setName(nameView.getText().toString());
                            customSwitch.setTopic(topicView.getText().toString());
                            customSwitch.setPayloadOn(onView.getText().toString());
                            customSwitch.setPayloadOff(offView.getText().toString());
                            customSwitch.setMaxDimmerValue(dimmerView.getText().toString());
                        } else {
                            error = true;
                        }
                    }
                } else {
                    error = true;
                }

                if (error){
                    Toast.makeText(context, "Fields can not be empty", Toast.LENGTH_SHORT).show();
                } else {
                    AppDataContainer.getInstance().notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
    }

    public static void showHueDialog(final Context context, final HueSwitch hueSwitch) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_dialog_hmwz_switch);
        dialog.setCancelable(true);
        dialog.show();

        //// Item specific code
        final TextView nameView = (TextView) dialog.findViewById(R.id.editHmwzSwitchName);
        nameView.setText(hueSwitch.getName());

        //// Apply button
        final Button applyButton = (Button) dialog.findViewById(R.id.editHmwzSwitchButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!nameView.getText().toString().isEmpty()) {
                    JSONObject payload = new JSONObject();
                    try {
                        payload.put("light", hueSwitch.getId());
                        payload.put("name", nameView.getText().toString());
                    } catch (JSONException e) {
                        Log.e("EditDialogs", e.getMessage());
                    }

                    MqttController.getInstance().publish("HYDRA/HUE/set-name", payload.toString());

                    // Store data in object
                    hueSwitch.setName(nameView.getText().toString());

                    AppDataContainer.getInstance().notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Fields can not be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
