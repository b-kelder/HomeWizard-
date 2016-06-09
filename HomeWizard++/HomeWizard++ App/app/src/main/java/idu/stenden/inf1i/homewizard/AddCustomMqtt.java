package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddCustomMqtt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custommqtt);

        final EditText customName = (EditText) findViewById(R.id.customName);
        final EditText customTopic = (EditText) findViewById(R.id.customMqttTopic);
        final EditText payloadOff = (EditText) findViewById(R.id.payloadStateoff);
        final EditText payloadOn = (EditText) findViewById(R.id.payloadStateon);
        final TextView payloadOffText = (TextView) findViewById(R.id.payLoadOffText);
        final TextView payloadOnText = (TextView) findViewById(R.id.payLoadOnText);

        final Context toastContext = this;
        final CheckBox isDimmer = (CheckBox) findViewById(R.id.customDimmer);
        final CheckBox isRGB = (CheckBox) findViewById(R.id.customRGB);
        final CheckBox isButton = (CheckBox) findViewById(R.id.customButton);
        Button addHMWZ = (Button) findViewById(R.id.customAddBtn);

        isDimmer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    payloadOn.setEnabled(false);
                    payloadOff.setEnabled(false);
                    isRGB.setEnabled(false);
                    isButton.setEnabled(false);
                } else {
                    payloadOn.setEnabled(true);
                    payloadOff.setEnabled(true);
                    isRGB.setEnabled(true);
                    isButton.setEnabled(true);
                }
            }
        });

        isRGB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    isDimmer.setEnabled(false);
                    isButton.setEnabled(false);
                } else {
                    isDimmer.setEnabled(true);
                    isButton.setEnabled(true);
                }
            }
        });

        isButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    isDimmer.setEnabled(false);
                    isRGB.setEnabled(false);
                    payloadOffText.setText("Button text:");
                    payloadOnText.setText("MQTT Payload:");
                } else {
                    isDimmer.setEnabled(true);
                    isRGB.setEnabled(true);
                    payloadOffText.setText("MQTT Payload (switch state off):");
                    payloadOnText.setText("MQTT Payload (switch state on):");
                }
            }
        });

        addHMWZ.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!customName.getText().toString().isEmpty() && !customTopic.getText().toString().isEmpty() && !payloadOff.getText().toString().isEmpty() && !payloadOn.getText().toString().isEmpty()
                        || (!customName.getText().toString().isEmpty() && !customTopic.getText().toString().isEmpty()) && isDimmer.isChecked()) {

                    String type = "switch";
                    if(isDimmer.isChecked()) {
                        type = "dimmer";
                    }if(isRGB.isChecked()) {
                        type = "colorpicker";
                    }if(isButton.isChecked()) {
                        type = "button";
                    }
					AppDataContainer.getInstance().addCustomSwitch(new CustomSwitch(customName.getText().toString(), customTopic.getText().toString(), payloadOn.getText().toString(), payloadOff.getText().toString(), type, false, 0, "0,0,0"));
                    AppDataContainer.getInstance().notifyDataSetChanged();
					AppDataContainer.getInstance().save();

                    Toast.makeText(toastContext, "Custom device added", Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    Toast toaster = Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT);
                    toaster.show();
                }
            }
        });
    }
}
