package idu.stenden.inf1i.homewizard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HelpPage extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page);

        Button btnConfig = (Button) findViewById(R.id.btnConfig);
        Button btnAddRemove = (Button) findViewById(R.id.btnDeviceAddRemove);
        Button btnReturn = (Button) findViewById(R.id.btnDeviceReturn);

        btnConfig.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), HelpConfig.class));
            }
        });

        btnAddRemove.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), HelpDevices.class));
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(), HelpReturn.class));
            }
        });
    }
}
