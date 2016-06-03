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

        final Context toastContext = this;
        final CheckBox isDimmer = (CheckBox) findViewById(R.id.customDimmer);
        Button addHMWZ = (Button) findViewById(R.id.customAddBtn);

        isDimmer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    payloadOn.setEnabled(false);
                    payloadOff.setEnabled(false);
                } else {
                    payloadOn.setEnabled(true);
                    payloadOff.setEnabled(true);
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
                    }
					AppDataContainer.getInstance().addCustomSwitch(new CustomSwitch(customName.getText().toString(), customTopic.getText().toString(), payloadOn.getText().toString(), payloadOff.getText().toString(), type, false, 0));
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
