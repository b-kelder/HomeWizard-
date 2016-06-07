package idu.stenden.inf1i.homewizard;

import android.app.Activity;
import android.graphics.Color;

/**
 * Created by Wouter on 03-06-16.
 */
public class Disco {
    public static void doDisco(int lightId) {
        int index = 0;
        int loops = 0;
        while(true) {
            int[] colors = {
                    0xFFFF0000,
                    0xFFFF00FF,
                    0xFF00FF00,
                    0xFF0000FF,
                    0xFF00FFFF,
                    0xFFFFFF00,
                    0xFF593001
            };

            int color = colors[index];

            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = (color >> 0) & 0xFF;

            float[] hsv = new float[3];
            Color.RGBToHSV(r, g, b, hsv);


            float[] xyB = Util.RGBtoXYB(color);
            final String pld = "{\"lights\":" + lightId + ", \"command\":{\"xy\": [" + String.valueOf(xyB[0]) + "," + String.valueOf(xyB[1]) + "]}, \"bri\": " + String.valueOf((int)Math.ceil(xyB[2] * 255f)) + "}";


            if(!MqttController.getInstance().publish("HYDRA/HUE/set-light", pld, false)) {
                return;
            }


            if(index < colors.length - 1) {
                index++;
            } else {
                index = 0;
                loops++;

                if(loops > 5) {
                    return;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
