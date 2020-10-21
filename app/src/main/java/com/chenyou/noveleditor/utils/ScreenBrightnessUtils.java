package com.chenyou.noveleditor.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

/**
 * 亮度调节工具类
 */
public class ScreenBrightnessUtils {
    /**
     * 自动调节模式
     */
    public static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
    /**
     * 手动调节模式
     */
    public static final int SCREEN_BRIGHTNESS_MODE_MANUAL = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
    /**
     * 默认亮度
     */
    public static final int SCREEN_BRIGHTNESS_DEFAULT = 75;
    /**
     * 最大亮度
     */
    public static final int MAX_BRIGHTNESS = 100;
    /**
     * 最小亮度
     */
    public static final int MIN_BRIGHTNESS = 0;

    private final Context context;
    /**
     * 当前系统调节模式
     */
    private final int sysBrightness;
    /**
     * 当前系统调节模式
     */
    private boolean sysAutomaticMode;
    /**
     * 系统最大亮度
     */
    private static final int maxBrightness = 255;
    /**
     * 最小亮度
     */
    private static final int minBrightness = 120;

    public ScreenBrightnessUtils(Context context, int sysBrightness, boolean sysAutomaticMode) {
        this.context = context;
        this.sysBrightness = sysBrightness;
        this.sysAutomaticMode = sysAutomaticMode;
    }

    /**
     * 创建屏幕亮度工具
     *
     * @param context
     * @return
     */
    public static ScreenBrightnessUtils Builder(Context context) {
        int brightness;
        boolean automaticMode;
        try {
            // 获取当前系统亮度值
            brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            // 获取当前系统调节模式
            automaticMode = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return new ScreenBrightnessUtils(context, brightness, automaticMode);
    }

    /**
     * 返回当前系统亮度值
     *
     * @return
     */
    public int getSysBrightness() {
        return sysBrightness;
    }

    /**
     * 返回当前系统亮度调节模式
     *
     * @return
     */
    public boolean isSysAutomaticMode() {
        return sysAutomaticMode;
    }

    /**
     * 设置调节模式
     *
     * @param mode
     */
    public void setMode(int mode) {
        if (mode != SCREEN_BRIGHTNESS_MODE_AUTOMATIC && mode != SCREEN_BRIGHTNESS_MODE_MANUAL) {
            return;
        }
        sysAutomaticMode = mode == SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    /**
     * 设置屏幕亮度
     *
     * @param brightness 亮度值,值为0至100
     */
    public void setBrightness(int brightness) {
        int mid = maxBrightness - minBrightness;//255-120
        //设置亮度值
        int bri = (int) (minBrightness + mid * ((float) brightness) / MAX_BRIGHTNESS);
        ContentResolver resolver = context.getContentResolver();
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, bri);
    }

    /**
     * 亮度预览
     *
     * @param activity   预览activity
     * @param brightness 亮度值（0.47~1）
     */
    public static void brightnessPreview(Activity activity, float brightness) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        //lp.screenBrightness 取值 0.0 -- 1.0 ※设定值（float）的范围，默认小于 0（系统设定）、0.0（暗）～1.0（亮） ※调用处理的地方，例如， Activity.onCreate()等等
        lp.screenBrightness = brightness;
        window.setAttributes(lp);
    }

    /**
     * 亮度预览
     *
     * @param activity 预览activity
     * @param percent  百分比（0.0~1.00）
     */
    public static void brightnessPreviewFromPercent(Activity activity, float percent) {
        float brightness = percent + (1.0f - percent) * (((float) minBrightness) / maxBrightness);
        brightnessPreview(activity, brightness);
    }
}
