package idu.stenden.inf1i.homewizard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

        Button addHMWZ = (Button) findViewById(R.id.customAddBtn);

        addHMWZ.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!customName.getText().toString().isEmpty() && !customTopic.getText().toString().isEmpty() && !payloadOff.getText().toString().isEmpty() && !payloadOn.getText().toString().isEmpty()) {
                    //TODO: Add support for non-homewizard MQTT devices
					appDataContainer.add(new CustomSwitch(customName.getText().toString(), customTopic.getText().toString(), payloadOn.getText().toString(), payloadOff.getText().toString()));
					appDataContainer.save();
                } else {
                    Toast toaster = Toast.makeText(getApplicationContext(), "Vul velden in", Toast.LENGTH_SHORT);
                    toaster.show();
                }
            }
        });
    }
}
