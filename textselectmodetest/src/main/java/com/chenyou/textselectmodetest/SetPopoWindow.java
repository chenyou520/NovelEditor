package com.chenyou.textselectmodetest;

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

public class SetPopoWindow extends PopupWindow {
    private final int mScreenWidth;
    private Context mContext;
    private View mPopView;//根布局
    private LinearLayout menu_ll;


    private CallBack mCallBack;

    public SetPopoWindow(Context context, int width, int height) {
        super(context);
        mContext = context;
        mScreenWidth = width;
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
        mPopView = inflater.inflate(R.layout.activity_menu, null);
        menu_ll = (LinearLayout) mPopView.findViewById(R.id.menu_ll);

    }

    /**
     * 设置窗口的相关属性
     */
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setFocusable(true);// 设置弹出窗口
        // 如果触摸位置在窗口外面则销毁
        mPopView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //View顶部距离父容器顶部的距离
                int height = menu_ll.getTop();
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



    public interface CallBack {

        void setLight(int light);

        void setFontSize(float font, int index);

        void changeBackgound(int id);
    }

    public void setCallback(CallBack callback) {
        mCallBack = callback;
    }
}
