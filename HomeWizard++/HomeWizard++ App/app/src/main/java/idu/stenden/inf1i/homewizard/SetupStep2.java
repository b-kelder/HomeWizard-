package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SetupStep2 extends AppCompatActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_step2);

        context = this;

        final EditText brokerIP = (EditText) findViewById(R.id.setupIP);
        final EditText brokerPort = (EditText) findViewById(R.id.setupPort);
        final EditText brokerUser = (EditText) findViewById(R.id.setupUsername);
        final EditText brokerPass = (EditText) findViewById(R.id.setupPassword);

        Button brokerSettings = (Button) findViewById(R.id.btnStep2);
        brokerSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //publish email/password
                Util.saveBrokerData(context, brokerIP.getText().toString(), brokerPort.getText().toString(), brokerUser.getText().toString(), brokerPass.getText().toString());
                startActivity(new Intent(getApplicationContext(), SetupStep3.class));
            }
        });

        Button skipBtn = (Button) findViewById(R.id.btnStep2Skip);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Util.saveFirstSetup(context, false);
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}
