package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class SetupStep3 extends BaseMqttEventActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_step3);

        context = this;

        final EditText emailField = (EditText) findViewById(R.id.setuphmwzUser);
        final EditText passwordField = (EditText) findViewById(R.id.setuphmwzPassword);

        Button loginbutton = (Button) findViewById(R.id.btnNextStep3);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!emailField.getText().toString().isEmpty() && passwordField.getText().toString().isEmpty()) {
                    Util.saveLoginData(context, emailField.getText().toString(), passwordField.getText().toString());
                    Util.saveFirstSetup(context, false);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(context, "One or more of the required fields are empty", Toast.LENGTH_SHORT).show();
                }
            }
        });



        Button skipBtn = (Button) findViewById(R.id.btnSkipStep3);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Util.saveFirstSetup(context, false);
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}
