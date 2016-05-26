package idu.stenden.inf1i.homewizard;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Wouter on 26-05-16.
 */
public class Util {


    public static void saveBrokerData(Context context, String ip, String port){
        JSONObject object = new JSONObject();
        try{
            object.put("ip", ip);
            object.put("port", port);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeFile(context, "broker.json", object.toString());
    }

    public static JSONObject readBrokerData(Context context){
        JSONObject object = null;
        try {
            object = new JSONObject(readFile(context, "broker.json"));
        } catch (JSONException e) {
            saveBrokerData(context, "", "");
            try {
                object = new JSONObject(readFile(context, "broker.json"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return object;
    }

    public static void saveLoginData(Context context, String email, String password, Object serial){
        JSONObject object = new JSONObject();
        try{
            object.put("email", email);
            object.put("password", password);
            object.put("serial", serial);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeFile(context, "login.json", object.toString());
    }

    public static JSONObject readLoginData(Context context){
        JSONObject object = null;
        try {
            object = new JSONObject(readFile(context, "login.json"));
        } catch (JSONException e) {
            saveLoginData(context, "", "", JSONObject.NULL);
            try {
                object = new JSONObject(readFile(context, "login.json"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return object;
    }

    public static String readFile(Context context, String file){
        String settings = "";
        try {
            InputStream inputStream = context.openFileInput(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                settings = stringBuilder.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return settings;
    }

    public static void writeFile(Context context, String file, String data){
        //write login info naar een file
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
