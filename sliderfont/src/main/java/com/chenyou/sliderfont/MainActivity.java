package com.chenyou.sliderfont;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnTouchListener, SeekBar.OnSeekBarChangeListener {
    private RelativeLayout mFontP;
    private SliderFont1 mFontSlider;
    private TextView mTextView;
    private boolean mChangeFont = false;
    private float mLastX;
    private int mFontIndex;
    private int mScreenWidth;
    private SeekBar seekbar_brightness;
    private Button button;
    private RelativeLayout relative;
    private ModelPopup pop;


    private SharedPreferences shared = null;
    private int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();
        initdata();
    }

    private void initview() {
        mFontP = findViewById(R.id.relative);
        mFontSlider = findViewById(R.id.slider);
        mTextView = findViewById(R.id.tv_textview);
        button = (Button) findViewById(R.id.button);
        seekbar_brightness = (SeekBar) findViewById(R.id.seekbar_brightness);
        relative = (RelativeLayout) findViewById(R.id.relative);
    }

    private void initdata() {
        WindowManager manager = getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        //其实最好的是得到mFontP的宽度，但是现在mFontP还没有绘制
        mFontSlider.setScreenWidth(mScreenWidth);
        mFontSlider.setOnTouchListener(this);

        shared = getSharedPreferences("base64", MODE_PRIVATE);
        num = shared.getInt("seekBarNum", 0);
        seekbar_brightness.setProgress(num);
        seekbar_brightness.setOnSeekBarChangeListener(this);

        pop = new ModelPopup(this, new ModelPopup.OnPopListener() {
            @Override
            public void onBtn1() {
                Toast.makeText(MainActivity.this, "点击了1", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBtn2() {
                Toast.makeText(MainActivity.this, "点击了3",  Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBtn3() {
                Toast.makeText(MainActivity.this, "点击了4", Toast.LENGTH_SHORT).show();
            }
        }, true);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.showAtLocation(relative, Gravity.BOTTOM, 0, 0);
            }
        });
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        System.out.println("X:"+x);
        System.out.println("y:"+y);
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            System.out.println("X:"+x);
            System.out.println("y:"+y);
            //判断Touch的位置是否在SliderFont上
            if (y > mFontP.getY() && y < mFontP.getY() + mFontP.getHeight() + 30 || mChangeFont) {
                mChangeFont = true;
                float specX = x - mLastX;
                mFontSlider.move(specX);
                mLastX = x;
                mFontSlider.invalidate();
            }

        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (y > mFontP.getY() && y < mFontP.getY() + mFontP.getHeight()) {
                mChangeFont = true;
                mFontSlider.setCenter(x);
                mLastX = x;
                mFontSlider.invalidate();
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //如果上面的事件确实滑动了SliderFont，就进行thumb调整
            if (mChangeFont) {

                mFontIndex = mFontSlider.adJustCenter(x);
                float fontSize = mFontSlider.getFontSize(mFontIndex);
                mTextView.setTextSize(fontSize);
                mLastX = x;
            }
            mChangeFont = false;

        }
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        changeAppBrightness(seekBar.getProgress());
        ScreenBrightnessUtils builder = ScreenBrightnessUtils.Builder(this);
//        builder.setMode(sysAutomaticMode);
        builder.setBrightness(progress);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    // 获取系统屏幕亮度
    public int getScreenBrightness() {
        int value = 0;
        ContentResolver cr = getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {

        }
        return value;
    }

    // 获取app亮度
    public void changeAppBrightness(int brightness) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        window.setAttributes(lp);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        SharedPreferences.Editor editor = shared.edit();
        editor.clear();
        editor.putInt("seekBarNum", seekbar_brightness.getProgress());
        editor.commit();
    }


}
