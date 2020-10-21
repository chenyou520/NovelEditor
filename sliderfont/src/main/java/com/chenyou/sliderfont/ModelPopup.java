package com.chenyou.sliderfont;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class ModelPopup extends PopupWindow implements android.view.View.OnClickListener {

    private OnPopListener listener;
    private View pop;

    /**
     *
     * @param context
     * @param listener
     *            接口回调
     * @param isShowMd
     *            可以控制按钮数量
     */
    public ModelPopup(Context context, OnPopListener listener, boolean isShowMd) {
        super(context);
        this.listener = listener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pop = inflater.inflate(R.layout.custom_popup_window, null);
        this.setContentView(pop);
        this.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        Button btn_1 = (Button) pop.findViewById(R.id.id_btn_take_photo);
        Button btn_2 = (Button) pop.findViewById(R.id.id_btn_cancelo);
        Button btn_3 = (Button) pop.findViewById(R.id.id_btn_select);

        if (!isShowMd) {
            btn_3.setVisibility(View.GONE);
        }
        btn_1.setOnClickListener(this);
        btn_2.setOnClickListener(this);
        btn_3.setOnClickListener(this);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 点击外面的控件也可以使得PopUpWindow dimiss
        this.setOutsideTouchable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.mypopwindow_anim_style);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(dw);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.id_btn_take_photo:
                listener.onBtn1();
                break;
            case R.id.id_btn_select:
                listener.onBtn2();
                break;
            case R.id.id_btn_cancelo:
                listener.onBtn3();
                break;
        }
        dismiss();
    }

    /***
     * 按钮回调接口
     *
     * @author Sloven
     *
     */
    public interface OnPopListener {

        public void onBtn1();

        public void onBtn2();

        public void onBtn3();
    }
}