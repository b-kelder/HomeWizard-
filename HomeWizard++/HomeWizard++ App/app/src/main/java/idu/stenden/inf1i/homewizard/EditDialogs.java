package idu.stenden.inf1i.homewizard;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

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

    public static void showCustomSwitchDialog(Context context, final CustomSwitch customSwitch) {
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

        nameView.setText(customSwitch.getName());
        topicView.setText(customSwitch.getTopic());
        onView.setText(customSwitch.getPayloadOn());
        offView.setText(customSwitch.getPayloadOff());

        if(customSwitch.getType().equals("dimmer")) {
            onView.setEnabled(false);
            offView.setEnabled(false);
        }

        //// Apply button
        final Button applyButton = (Button) dialog.findViewById(R.id.editCustomSwitchButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Store data in object
                customSwitch.setName(nameView.getText().toString());
                customSwitch.setTopic(topicView.getText().toString());
                customSwitch.setPayloadOn(onView.getText().toString());
                customSwitch.setPayloadOff(offView.getText().toString());

                AppDataContainer.getInstance().notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

    public static void showHueDialog(Context context, final HueSwitch hueSwitch) {
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
            }
        });
    }
}
