package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class FirstTimeTetup extends AppCompatActivity {

    Boolean firstSetup;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_setup);

        context = this;

        JSONObject firstSetupFile = Util.readFirstSetup(this);
        try {
            firstSetup = firstSetupFile.getBoolean("FirstSetup");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toast toast = Toast.makeText(context, firstSetup.toString(), Toast.LENGTH_SHORT);
        toast.show();

        if (firstSetup == false){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        Button skipBtn = (Button) findViewById(R.id.btnStep1Skip);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

        Button nextBtn = (Button) findViewById(R.id.btnStep1);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Util.saveFirstSetup(context, false);
                startActivity(new Intent(getApplicationContext(), SetupStep2.class));
                finish();
            }
        });
    }
}
