/*
 * ParanoidAndroid Utils. (c) 2012 ParanoidAndroid Team
 */

package com.android.settings.paranoid;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.ExtendedPropertiesUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class RomUtils extends ExtendedPropertiesUtils{

    private static final String MOUNT_SYSTEM_RW = "busybox mount -o rw,remount /system";
    private static final String MOUNT_SYSTEM_RO = "busybox mount -o ro,remount /system";
    private static final int TRIGGER_REBOOT = 0;
    private static final int TRIGGER_SYSTEM_UI_RELOAD = 1;
    private static final int TRIGGER_SYSTEM_SERVER_RELOAD = 2;
    public static Context mContext;

    public static void setContext(Context context){
	mContext = context;
    }

     /*
      * CMD Utils
      */

    public static void setPropierty(String propierty, int value){
	RunCommands.execute(new String[]{MOUNT_SYSTEM_RW, "cd /system", "busybox sed -i 's|"+propierty+"=.*|"+propierty+"=" + value + "|' build.prop", "busybox chmod 644 build.prop", MOUNT_SYSTEM_RO}, 0);
    }

    public static void setLockscreenWallpaper(String temp){
        if(temp != null)
            RunCommands.execute(new String[]{"busybox mv "+temp+" "+"/data/data/com.android.settings/files/lcs_wallpaper", "chmod 700 /data/data/com.android.settings/files/lcs_wallpaper"}, 0);
        else
            RunCommands.execute(new String[]{"busybox rm /data/data/com.android.settings/files/lcs_wallpaper"}, 0);
    }

    public static void setHybridProperty(String propierty, String value){
	if (value.equals("-1"))
               RunCommands.execute(new String[]{MOUNT_SYSTEM_RW, "cd /system", "chmod 777 pad.prop", "busybox sed -i '/^" + propierty + "/ d' pad.prop | grep -v \n", "busybox chmod 644 pad.prop", MOUNT_SYSTEM_RO}, 0);
       else if(readFile("/system/pad.prop").contains(propierty))
               RunCommands.execute(new String[]{MOUNT_SYSTEM_RW, "cd /system", "busybox sed -i 's|"+propierty+"=.*|"+propierty+"=" + value + "|' pad.prop", "busybox chmod 644 pad.prop", MOUNT_SYSTEM_RO}, 0);
       else
               RunCommands.execute(new String[]{MOUNT_SYSTEM_RW, "cd /system", "chmod 777 pad.prop", "busybox printf \"\\n%b\" " + propierty + "=" + value + " >> pad.prop", "busybox chmod 644 pad.prop", MOUNT_SYSTEM_RO}, 0);
    }

     /*
      * I/O Utils
      */

    public static String getProp(String prop) {
        try {
            String output = null;
            Process p = Runtime.getRuntime().exec("getprop "+prop);
            p.waitFor();
            BufferedReader input = new BufferedReader (new InputStreamReader(p.getInputStream()));
            output = input.readLine();
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Action Utils
     */
    public static void restartSystemUI(){
	RunCommands.execute(new String[]{"su", "busybox killall com.android.systemui"} ,0);
    }

    public static void restartSystemServer(){
	RunCommands.execute(new String[]{"su", "busybox killall system_server"} ,0);
    }

    public static void triggerAction(int action) {
	switch(action){
	   case TRIGGER_REBOOT:
	      PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
              pm.reboot("ParanoidAndroid Triggered Reboot");
	   break;
	   case TRIGGER_SYSTEM_UI_RELOAD:
	      restartSystemUI();
	   break;
	   case TRIGGER_SYSTEM_SERVER_RELOAD:
	      restartSystemServer();
	   break;
	}
    }
}