package idu.stenden.inf1i.homewizard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AddChoice extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_choise);

        Button hmwzswitch = (Button) findViewById(R.id.hmwzbtnlight);
        hmwzswitch.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), AddHomewizardlight.class));
            }
        });

        Button hueBtn = (Button) findViewById(R.id.huebtnlight);
        hueBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), AddHueBridge.class));
            }
        });

        Button customMQTT = (Button) findViewById(R.id.BtnAddCustom);
        customMQTT.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), AddCustomMqtt.class));
            }
        });
    }
}
