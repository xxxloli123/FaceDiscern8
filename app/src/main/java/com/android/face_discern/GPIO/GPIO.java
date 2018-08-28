package com.android.face_discern.GPIO;

import android.os.Build;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GPIO {

    private static final String TAG = "GPIO";
    private File gpio = null;
    private boolean enable = false;

    private static HashMap<String, String> ledMap = new HashMap<>();

    static {
        ledMap.put("3128", "/sys/devices/misc_power_en.19/out8");
        ledMap.put("3288", "/sys/devices/misc_power_en.22/green_led");
    }

    public String read() {
        if(enable) return FileUtils.readFile(gpio);
        return "";
    }

    public void write(String value) {
        if (enable)
            FileUtils.writeFile(gpio,value);
    }

    public GPIO() {
        String filePath = filterFilePath(getRKModel());
        if (filePath != null) {
            gpio = new File(filePath);
            enable = gpio.exists();
        }
    }


    public static String getRKModel() {
        String intern = Build.PRODUCT.intern();
        if (intern.contains("312x"))
            intern = "rk3128";
        return intern;
    }

    private String filterFilePath(String rkModel) {
        String filePath = null;
        Iterator<Map.Entry<String, String>> iterator = ledMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            if (rkModel.contains(next.getKey())) {
                filePath = next.getValue();
                break;
            }
        }
        return filePath;
    }
}
