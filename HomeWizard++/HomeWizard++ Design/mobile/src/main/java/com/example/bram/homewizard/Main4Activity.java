package com.example.bram.homewizard;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

public class Main4Activity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int lampcount = 0;

        ArrayList<String> mylist = new ArrayList<String>();

        while (lampcount < 10) {
            lampcount++;
            mylist.add("lamp " + lampcount);
        }


        ListView mylistView = (ListView) findViewById(R.id.listView);

        // this-The current activity context.
        // Second param is the resource Id for list layout row item

        // Third param is input array
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mylist);
        if (mylistView != null) {
            mylistView.setAdapter(arrayAdapter);
        }


        ArrayList<NewsItem> image_details = getListData();
        final ListView lv1 = (ListView) findViewById(R.id.custom_list);
        if (lv1 != null) {
            lv1.setAdapter(new CustomListAdapter(this, image_details));
        }
        if (lv1 != null) {
            lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    Object o = lv1.getItemAtPosition(position);
                    NewsItem newsData = (NewsItem) o;
                    Toast.makeText(Main4Activity.this, "Selected :" + " " + newsData, Toast.LENGTH_LONG).show();
                }
            });
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Devices", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.bram.homewizard/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main4 Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.bram.homewizard/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private ArrayList<NewsItem> getListData() {
        ArrayList<NewsItem> results = new ArrayList<NewsItem>();
        NewsItem newsData = new NewsItem();
        newsData.setHeadline("Lamp 1 ");
        newsData.setReporterName("Status: AAN");
        newsData.setDate("AAN sinds: 5/13/2016 10:00");
        results.add(newsData);

        // Add some more dummy data for testing
        return results;
    }
}