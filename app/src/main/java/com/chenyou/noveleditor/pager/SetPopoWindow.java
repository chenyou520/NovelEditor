package com.chenyou.noveleditor.pager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.chenyou.noveleditor.R;

public class SetPopoWindow extends PopupWindow {
    private Context mContext;
    private View mPopView;//根布局

    //设置布局的控件
    private ImageView editLessBrightness;//亮度低
    private SeekBar editBrightnessSeekbar;//亮度进度条
    private ImageView editMoreBrightness;//亮度高
    private LinearLayout editSetLl;//设置字体布局
    private LinearLayout setll;//弹窗布局
    private ImageView editFontSmall;//字体小
    private SliderFont seekbarFont;//字体大小进度条
    private ImageView editFontLarge;//字体大
    private RadioGroup editSetRg;//编辑页面背景颜色

    private int mScreenWidth;//图标活动范围
    private int mLastX;
    private int mFontIndex;//图标所处位置
    private boolean mChangeFont = false;//判断字体大小是否改变
    private int mNowPick;//选中的背景

    private SharedPreferences shared = null;
    private CallBack mCallBack;

    public SetPopoWindow(Context context, int width, int height) {
        super(context);
        mContext = context;
        shared = context.getSharedPreferences("setdata", Context.MODE_PRIVATE);
        mScreenWidth = width;
        mLastX = mScreenWidth / 2;
        initview(context);
        setPopupWindow();
    }


    /**
     * 初始化布局
     *
     * @param context
     */
    private void initview(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        //绑定布局
        mPopView = inflater.inflate(R.layout.edit_bottom_settings, null);

        setll = (LinearLayout) mPopView.findViewById(R.id.set_ll);
        editLessBrightness = (ImageView) mPopView.findViewById(R.id.edit_less_brightness);
        editBrightnessSeekbar = (SeekBar) mPopView.findViewById(R.id.edit_brightness_seekbar);
        editMoreBrightness = (ImageView) mPopView.findViewById(R.id.edit_more_brightness);
        editSetLl = (LinearLayout) mPopView.findViewById(R.id.edit_set_ll);
        editFontSmall = (ImageView) mPopView.findViewById(R.id.edit_font_small);
        seekbarFont = (SliderFont) mPopView.findViewById(R.id.seekbar_font);
        editFontLarge = (ImageView) mPopView.findViewById(R.id.edit_font_large);
        editSetRg = (RadioGroup) mPopView.findViewById(R.id.edit_set_rg);


        // 获取当前系统亮度值
        try {
            int brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            int progress = 0;
            if (brightness < 120) {
                progress = 0;
            } else {
                progress = (int) ((brightness - 120) / 1.35);
            }
            editBrightnessSeekbar.setProgress(progress);//设为当前系统的亮度
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        editBrightnessSeekbar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());

        //设置字体大小
        seekbarFont.setScreenWidth(mScreenWidth);
        seekbarFont.setOnTouchListener(new MyOnTouchListener());

        //设置背景颜色
        mNowPick = shared.getInt("mNowPick", R.id.edit_set_rb_whitle);
        editSetRg.check(mNowPick);
        editSetRg.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
    }

    /**
     * 设置窗口的相关属性
     */
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setFocusable(true);// 设置弹出窗口
        this.setAnimationStyle(R.style.setting_popu_anim);// 设置动画
        ColorDrawable dw = new ColorDrawable(0xb0000000);// 设置背景透明
        this.setBackgroundDrawable(dw);
        // 如果触摸位置在窗口外面则销毁
        mPopView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //View顶部距离父容器顶部的距离
                int height = setll.getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    /**
     * 亮度进度条设置
     */
    private class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //当进度改变时
            // progress代表改变的进度值
            // 而fromUser代表是否是用户的操作改变的，如果为false，一般是我们代码中进行了进度设置。如果为true代表的是用户手动拖动拖动条。
            int brightness = (int) (120 + 135 * progress / 100);
            if (mCallBack != null) {
                mCallBack.setLight(brightness);
            }
            SharedPreferences.Editor edit = shared.edit();
            edit.putInt("brightness", brightness);
            edit.commit();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    /**
     * 字体大小调节
     */
    private class MyOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                Toast.makeText(mContext, "ACTION_MOVE", Toast.LENGTH_SHORT).show();
                //判断Touch的位置是否在SliderFont上
                if (y > seekbarFont.getY() && y < seekbarFont.getY() + seekbarFont.getHeight() + 30 || mChangeFont) {
                    mChangeFont = true;
                    float specX = x - mLastX;
                    seekbarFont.move(specX);
                    mLastX = (int) x;
                    seekbarFont.invalidate();
                }

            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Toast.makeText(mContext, "ACTION_DOWN", Toast.LENGTH_SHORT).show();
                if (y > seekbarFont.getY() && y < seekbarFont.getY() + seekbarFont.getHeight()) {
                    mChangeFont = true;
                    seekbarFont.setCenter(x);
                    mLastX = (int) x;
                    seekbarFont.invalidate();
                }

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                Toast.makeText(mContext, "ACTION_UP", Toast.LENGTH_SHORT).show();
                //如果上面的事件确实滑动了SliderFont，就进行thumb调整
                if (mChangeFont) {

                    mFontIndex = seekbarFont.adJustCenter(x);
                    float fontSize = seekbarFont.getFontSize(mFontIndex);
                    if (mCallBack != null) {
                        mCallBack.setFontSize(fontSize, mFontIndex);
                    }
                    mLastX = (int) x;
                    //保存到SP中
                    SharedPreferences.Editor edit = shared.edit();
                    edit.putInt("mFontIndex", mFontIndex);
                    edit.putFloat("fontSize", fontSize);
                    edit.commit();
                }
                mChangeFont = false;

            }
            return true;
            // 这里如果返回true的话，touch事件将被拦截
            // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
        }
    }

    /**
     * 设置背景颜色
     */
    private class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.edit_set_rb_whitle:
                    mNowPick = R.id.edit_set_rb_whitle;
                    if (mCallBack != null) {
                        mCallBack.changeBackgound(mNowPick);
                    }
                    break;
                case R.id.edit_set_rb_pink:
                    mNowPick = R.id.edit_set_rb_pink;
                    if (mCallBack != null) {
                        mCallBack.changeBackgound(mNowPick);
                    }
                    break;
                case R.id.edit_set_rb_yellow:
                    mNowPick = R.id.edit_set_rb_yellow;
                    if (mCallBack != null) {
                        mCallBack.changeBackgound(mNowPick);
                    }
                    break;
                case R.id.edit_set_rb_green:
                    mNowPick = R.id.edit_set_rb_green;
                    if (mCallBack != null) {
                        mCallBack.changeBackgound(mNowPick);
                    }
                    break;
                case R.id.edit_set_rb_blue:
                    mNowPick = R.id.edit_set_rb_blue;
                    if (mCallBack != null) {
                        mCallBack.changeBackgound(mNowPick);
                    }
                    break;
                case R.id.edit_set_rb_black:
                    mNowPick = R.id.edit_set_rb_black;
                    if (mCallBack != null) {
                        mCallBack.changeBackgound(mNowPick);
                    }
                    break;
                default:
                    break;
            }
            SharedPreferences.Editor edit = shared.edit();
            edit.putInt("mNowPick", mNowPick);
            edit.commit();
        }
    }


    public interface CallBack {

        void setLight(int light);

        void setFontSize(float font, int index);

        void changeBackgound(int id);
    }

    public void setCallback(CallBack callback) {
        mCallBack = callback;
    }
}
