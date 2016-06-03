package idu.stenden.inf1i.homewizard;

import java.util.Arrays;

/**
 * Created by Wouter on 31-05-16.
 */
public class HueSwitch extends BaseSwitch {


    /*
    "name": l.name,
    "on": l.on,
    "colormode": l.colormode,
    "brightness": l.brightness,
    "hue": l.hue,
    "saturation": l.saturation,
    "xy": l.xy,
    "colortemp": l.colortemp,
    "colortemp_k": l.colortemp_k
	*/
    protected String colormode;
    protected int brightness;
    protected int hue;
    protected int saturation;
    protected float[] xy;

	public HueSwitch() {
		
	}


    @Override
    public String getType() {
        return "hue";
    }

    public String getColormode() {
        return colormode;
    }

    public void setColormode(String colormode) {
        this.colormode = colormode;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public float[] getXy() {
        return xy;
    }

    public void setXy(float[] xy) {
        this.xy = xy;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HueSwitch{");
        sb.append("colormode='").append(colormode).append('\'');
        sb.append(", brightness=").append(brightness);
        sb.append(", hue=").append(hue);
        sb.append(", saturation=").append(saturation);
        sb.append(", xy=").append(Arrays.toString(xy));
        sb.append('}');
        return sb.toString();
    }
}
