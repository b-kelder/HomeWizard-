package idu.stenden.inf1i.homewizard;

import android.content.Context;
import android.graphics.Color;
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

    public static float[] RGBtoXYB(int rgbColor){
        float red, green, blue;

        int r = (rgbColor >> 16) & 0xFF;
        int g = (rgbColor >> 8) & 0xFF;
        int b = (rgbColor >> 0) & 0xFF;

        red = (float)r / 255f;
        green = (float)g / 255f;
        blue = (float)b / 255f;

        red = (red > 0.04045f) ? (float)Math.pow((red + 0.055f) / (1.0f + 0.055f), 2.4f) : (red / 12.92f);
        green = (green > 0.04045f) ? (float)Math.pow((green + 0.055f) / (1.0f + 0.055f), 2.4f) : (green / 12.92f);
        blue = (blue > 0.04045f) ? (float)Math.pow((blue + 0.055f) / (1.0f + 0.055f), 2.4f) : (blue / 12.92f);

        float X = red * 0.664511f + green * 0.154324f + blue * 0.162028f;
        float Y = red * 0.283881f + green * 0.668433f + blue * 0.047685f;
        float Z = red * 0.000088f + green * 0.072310f + blue * 0.986039f;

        float x = X / (X + Y + Z);
        float y = Y / (X + Y + Z);

        //TODO: Bounds checking

        return new float[] {x, y, Y};
    }

    public static int XYBtoRGB(float[] xyB) {
        //TODO: Bounds checking

        float x = xyB[0]; // the given x value
        float y = xyB[1]; // the given y value
        float z = 1.0f - x - y;
        float Y = xyB[2]; // The given brightness value
        float X = (Y / y) * x;
        float Z = (Y / y) * z;

        float r =  X * 1.656492f - Y * 0.354851f - Z * 0.255038f;
        float g = -X * 0.707196f + Y * 1.655397f + Z * 0.036152f;
        float b =  X * 0.051713f - Y * 0.121364f + Z * 1.011530f;


        if (r > b && r > g && r > 1.0f) {
            // red is too big
            g = g / r;
            b = b / r;
            r = 1.0f;
        }
        else if (g > b && g > r && g > 1.0f) {
            // green is too big
            r = r / g;
            b = b / g;
            g = 1.0f;
        }
        else if (b > r && b > g && b > 1.0f) {
            // blue is too big
            r = r / b;
            g = g / b;
            b = 1.0f;
        }

        //Gamma correction
        r = r <= 0.0031308f ? 12.92f * r : (1.0f + 0.055f) * (float)Math.pow(r, (1.0f / 2.4f)) - 0.055f;
        g = g <= 0.0031308f ? 12.92f * g : (1.0f + 0.055f) * (float)Math.pow(g, (1.0f / 2.4f)) - 0.055f;
        b = b <= 0.0031308f ? 12.92f * b : (1.0f + 0.055f) * (float)Math.pow(b, (1.0f / 2.4f)) - 0.055f;

        if (r > b && r > g) {
            // red is biggest
            if (r > 1.0f) {
                g = g / r;
                b = b / r;
                r = 1.0f;
            }
        }
        else if (g > b && g > r) {
            // green is biggest
            if (g > 1.0f) {
                r = r / g;
                b = b / g;
                g = 1.0f;
            }
        }
        else if (b > r && b > g) {
            // blue is biggest
            if (b > 1.0f) {
                r = r / b;
                g = g / b;
                b = 1.0f;
            }
        }

        int red, green, blue;
        red = (int)Math.ceil(r * 255f);
        green = (int)Math.ceil(g * 255f);
        blue = (int)Math.ceil(b * 255f);


        return Color.rgb(red, green, blue);
    }

    public static void saveHueData(Context context, String ip, String username){
        JSONObject object = new JSONObject();
        try{
            object.put("ip", ip);
            object.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeFile(context, "hue.json", object.toString());
    }

    public static JSONObject readHueData(Context context){
        JSONObject object = null;
        try {
            object = new JSONObject(readFile(context, "hue.json"));
        } catch (JSONException e) {
            saveHueData(context, "", "");
            try {
                object = new JSONObject(readFile(context, "hue.json"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return object;
    }

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

    public static void saveBrokerData(Context context, String ip, String port, String username, String password, boolean useCrt){
        JSONObject object = new JSONObject();
        try{
            object.put("ip", ip);
            object.put("port", port);
            object.put("username", username);
            object.put("password", password);
            object.put("crt", useCrt);
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
            saveBrokerData(context, "", "", "", "", false);
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
            saveAdminPin(context, "", false);
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

public static void saveLoginAttempts(Context context, long timeStamp, int attempts, boolean isEnabled){
        JSONObject object = new JSONObject();
        try{
            object.put("timestamp", timeStamp);
            object.put("attempts", attempts);
            object.put("enabled", isEnabled);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        writeFile(context, "trackLogin.json", object.toString());
    }

    public static JSONObject readLoginAttempts(Context context){
        JSONObject object = null;
        try {
            object = new JSONObject(readFile(context, "trackLogin.json"));
        } catch (JSONException e) {
            saveLoginAttempts(context, 0, 2, true);
            try {
                object = new JSONObject(readFile(context, "trackLogin.json"));
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
