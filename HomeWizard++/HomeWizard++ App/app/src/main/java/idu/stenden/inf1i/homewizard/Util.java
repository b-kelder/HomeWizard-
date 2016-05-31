package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wouter on 26-05-16.
 */
public class Util {

    public static void saveFirstSetup(Context context, boolean firstSetup){
        JSONObject object = new JSONObject();
        try{
            object.put("FirstSetup", firstSetup);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeFile(context, "firstSetup.json", object.toString());
    }

    public static JSONObject readFirstSetup(Context context){
        JSONObject object = null;
        try {
            object = new JSONObject(readFile(context, "firstSetup.json"));
        } catch (JSONException e) {
            saveFirstSetup(context, true);
            try {
                object = new JSONObject(readFile(context, "firstSetup.json"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return object;
    }

    public static void saveCustomSwitch(Context context, ArrayList<CustomSwitch> switchList){
        Gson gson = new Gson();
        writeFile(context, "customSwitch.json", gson.toJson(switchList));
    }

    public static ArrayList<CustomSwitch> readCustomSwitch(Context context){
        Gson gson = new Gson();
        try {
            Type collectionType = new TypeToken<ArrayList<CustomSwitch>>() {
            }.getType();
            ArrayList<CustomSwitch> list = gson.fromJson(readFile(context, "customSwitch.json"), collectionType);
            return list;
        } catch (Exception e) {
            saveCustomSwitch(context, new ArrayList<CustomSwitch>());
            readCustomSwitch(context);
            Log.e("FUCK", "FUCK");
        }
        return null;
    }

    public static void saveBrokerData(Context context, String ip, String port, String username, String password){
        JSONObject object = new JSONObject();
        try{
            object.put("ip", ip);
            object.put("port", port);
            object.put("username", username);
            object.put("password", password);
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
            saveBrokerData(context, "", "", "", "");
            try {
                object = new JSONObject(readFile(context, "broker.json"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return object;
    }

    public static void saveAdminPin(Context context, String pin, boolean isEnabled){
        JSONObject object = new JSONObject();
        try{
            object.put("pin", pin);
            object.put("enabled", isEnabled);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeFile(context, "admin.json", object.toString());
    }

    public static JSONObject readAdminPin(Context context){
        JSONObject object = null;
        try {
            object = new JSONObject(readFile(context, "admin.json"));
        } catch (JSONException e) {
            saveAdminPin(context, "0000", false);
            try {
                object = new JSONObject(readFile(context, "admin.json"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return object;
    }

    public static void saveLoginData(Context context, String email, String password){
        JSONObject object = new JSONObject();
        try{
            object.put("email", email);
            object.put("password", password);
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
            saveLoginData(context, "", "");
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
