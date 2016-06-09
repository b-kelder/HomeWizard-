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
        final EditText maxDimmerValue = (EditText) findViewById(R.id.AddMaxDimmerValue);
        final TextView maxDimmerText = (TextView) findViewById(R.id.AddMaxDimmerText);
        final TextView payloadOffText = (TextView) findViewById(R.id.payLoadOffText);
        final TextView payloadOnText = (TextView) findViewById(R.id.payLoadOnText);

        final Context toastContext = this;
        final CheckBox isDimmer = (CheckBox) findViewById(R.id.customDimmer);
        final CheckBox isRGB = (CheckBox) findViewById(R.id.customRGB);
        final CheckBox isButton = (CheckBox) findViewById(R.id.customButton);
        final CheckBox isText = (CheckBox) findViewById(R.id.customText);
        Button addHMWZ = (Button) findViewById(R.id.customAddBtn);

        maxDimmerValue.setVisibility(View.GONE);
        maxDimmerText.setVisibility(View.GONE);

        isDimmer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    maxDimmerValue.setVisibility(View.VISIBLE);
                    maxDimmerText.setVisibility(View.VISIBLE);

                    payloadOn.setVisibility(View.GONE);
                    payloadOffText.setVisibility(View.GONE);
                    payloadOff.setVisibility(View.GONE);
                    payloadOnText.setVisibility(View.GONE);
                    isRGB.setEnabled(false);
                    isButton.setEnabled(false);
                    isText.setEnabled(false);
                } else {
                    maxDimmerValue.setVisibility(View.GONE);
                    maxDimmerText.setVisibility(View.GONE);

                    payloadOn.setVisibility(View.VISIBLE);
                    payloadOff.setVisibility(View.VISIBLE);
                    payloadOffText.setVisibility(View.VISIBLE);
                    payloadOnText.setVisibility(View.VISIBLE);
                    isRGB.setEnabled(true);
                    isButton.setEnabled(true);
                    isText.setEnabled(true);
                }
            }
        });

        isRGB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    maxDimmerValue.setVisibility(View.VISIBLE);
                    maxDimmerText.setVisibility(View.VISIBLE);

                    isDimmer.setEnabled(false);
                    isButton.setEnabled(false);
                    isText.setEnabled(false);
                } else {
                    maxDimmerValue.setVisibility(View.GONE);
                    maxDimmerText.setVisibility(View.GONE);

                    isDimmer.setEnabled(true);
                    isButton.setEnabled(true);
                    isText.setEnabled(true);
                }
            }
        });

        isButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    isDimmer.setEnabled(false);
                    isRGB.setEnabled(false);
                    isText.setEnabled(false);
                    payloadOffText.setText("Button text:");
                    payloadOnText.setText("MQTT Payload:");
                } else {
                    isDimmer.setEnabled(true);
                    isRGB.setEnabled(true);
                    isText.setEnabled(true);
                    payloadOffText.setText("MQTT Payload (switch state off):");
                    payloadOnText.setText("MQTT Payload (switch state on):");
                }
            }
        });

        isText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    payloadOn.setVisibility(View.GONE);
                    payloadOff.setVisibility(View.GONE);
                    payloadOffText.setVisibility(View.GONE);
                    payloadOnText.setVisibility(View.GONE);
                    isDimmer.setEnabled(false);
                    isRGB.setEnabled(false);
                    isButton.setEnabled(false);
                } else {
                    payloadOn.setVisibility(View.VISIBLE);
                    payloadOff.setVisibility(View.VISIBLE);
                    payloadOffText.setVisibility(View.VISIBLE);
                    payloadOnText.setVisibility(View.VISIBLE);
                    isDimmer.setEnabled(true);
                    isRGB.setEnabled(true);
                    isButton.setEnabled(true);
                }
            }
        });

        addHMWZ.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String regexStr = "^[0-9]*$";

                if (!customName.getText().toString().isEmpty() && !customTopic.getText().toString().isEmpty() && !payloadOff.getText().toString().isEmpty() && !payloadOn.getText().toString().isEmpty()
                        || (!customName.getText().toString().isEmpty() && !maxDimmerValue.getText().toString().isEmpty() && !customTopic.getText().toString().isEmpty()) && isDimmer.isChecked() && maxDimmerValue.getText().toString().trim().matches(regexStr)
                        || (!customName.getText().toString().isEmpty() && !customTopic.getText().toString().isEmpty()) && isText.isChecked()
                        || (!customName.getText().toString().isEmpty() && !customTopic.getText().toString().isEmpty() && !payloadOff.getText().toString().isEmpty() && !payloadOn.getText().toString().isEmpty() && !maxDimmerValue.getText().toString().isEmpty() && maxDimmerValue.getText().toString().trim().matches(regexStr) )) {
                    String type = "switch";
                    if(isDimmer.isChecked()) {
                        type = "dimmer";
                    }if(isRGB.isChecked()) {
                        type = "colorpicker";
                    }if(isButton.isChecked()) {
                        type = "button";
                    }if(isText.isChecked()) {
                        type = "text";
                    }
					AppDataContainer.getInstance().addCustomSwitch(new CustomSwitch(customName.getText().toString(), customTopic.getText().toString(), payloadOn.getText().toString(), payloadOff.getText().toString(), type, false, 0, "0,0,0", maxDimmerValue.getText().toString()));
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
