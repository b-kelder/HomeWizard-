package idu.stenden.inf1i.homewizard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddHomewizardlight extends AppCompatActivity {

    private MqttController mqttController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_homewizardlight);

        final EditText HMWZlightname = (EditText) findViewById(R.id.HMWZlightname);
        final EditText HMWZlightcode = (EditText) findViewById(R.id.HMWZlightcode);

        mqttController = MqttController.getInstance();
        mqttController.setContext(getApplicationContext());

        Button loginbutton = (Button) findViewById(R.id.HMWZlightadd);

        loginbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!HMWZlightname.getText().toString().isEmpty() && !HMWZlightcode.getText().toString().isEmpty()) {
                    mqttController.publish("HYDRA/HMWZ/sw/add/" + HMWZlightname.getText() + "/switch/" + HMWZlightcode.getText(), "/0");
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast toaster = Toast.makeText(getApplicationContext(), "Vul shit in", Toast.LENGTH_SHORT);
                    toaster.show();
                }
            }
        });
    }
}
