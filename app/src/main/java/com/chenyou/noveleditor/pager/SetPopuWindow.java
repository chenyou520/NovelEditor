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

public class SetPopuWindow extends PopupWindow {
    private Context mContext;
    private View mPopView;//根布局

    //设置布局的控件

    private LinearLayout setll;//弹窗布局
    private RadioGroup editSetRg;//编辑页面背景颜色
    private RadioGroup mFontSize;//设置字体大小

    private int mNowPick;//选中的背景
    private int fonesize;//选中的字体大小

    private SharedPreferences shared = null;
    private CallBack mCallBack;

    public SetPopuWindow(Context context, int width, int height) {
        super(context);
        mContext = context;
        shared = context.getSharedPreferences("setdata", Context.MODE_PRIVATE);
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

        mFontSize = (RadioGroup) mPopView.findViewById(R.id.font_size);
        editSetRg = (RadioGroup) mPopView.findViewById(R.id.edit_set_rg);

        //设置字体大小
        fonesize = shared.getInt("fonesize", R.id.default_size);
        mFontSize.check(fonesize);
        mFontSize.setOnCheckedChangeListener(new FontSizeOnCheckedChangeListener());
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
//        ColorDrawable dw = new ColorDrawable(0xb0000000);// 设置背景透明
        this.setBackgroundDrawable(new ColorDrawable(0));//背景透明
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
     * 设置字体大小
     */
    private class FontSizeOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.small_size:
                    fonesize = R.id.small_size;
                    if (mCallBack != null) {
                        mCallBack.setFontSize(fonesize);
                    }
                    break;
                case R.id.medium_size:
                    fonesize = R.id.medium_size;
                    if (mCallBack != null) {
                        mCallBack.setFontSize(fonesize);
                    }
                    break;
                default:
                case R.id.default_size:
                    fonesize = R.id.default_size;
                    if (mCallBack != null) {
                        mCallBack.setFontSize(fonesize);
                    }
                    break;
                case R.id.big_size:
                    fonesize = R.id.big_size;
                    if (mCallBack != null) {
                        mCallBack.setFontSize(fonesize);
                    }
                    break;
                case R.id.super_size:
                    fonesize = R.id.super_size;
                    if (mCallBack != null) {
                        mCallBack.setFontSize(fonesize);
                    }
                    break;
            }
            SharedPreferences.Editor edit = shared.edit();
            edit.putInt("fonesize", fonesize);
            edit.commit();
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
        void setFontSize(int fontid);

        void changeBackgound(int id);
    }

    public void setCallback(CallBack callback) {
        mCallBack = callback;
    }

}
