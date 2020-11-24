package com.chenyou.textselectmodetest;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 描述：文本的选中模式的使用
 * 开发者：开发者的乐趣JRT
 * 创建时间：2017-3-12 12:29
 * CSDN地址：http://blog.csdn.net/Jiang_Rong_Tao/article
 * E-mail：jrtxb520@163.com
 **/
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText mTvSelect;
    //    private TextPage mTvSelect;
    private MainActivity mContext;
    private int mScreenWidth;//屏幕宽
    private int mScreenHeight;//屏幕高
    private int x;
    private int y;

    private PopupWindow popupWindow;

    private View view;

    private TextView selectAll;
    private TextView copy;
    private TextView cut;
    private TextView paste;
    private TextView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        //获取屏幕宽高
        WindowManager manager = this.getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        init();

    }

    private void init() {
        mTvSelect = (EditText) findViewById(R.id.tv_select);
//        mTvSelect = (TextPage) findViewById(R.id.tv_select);
        mTvSelect.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopWindow();
                return true;
            }
        });
        showMenu();
    }

    private void showMenu() {
        mTvSelect.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @SuppressLint("NewApi")
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                try {
                    //找到 TextView中的成员变量mEditor
                    Field mEditor = TextView.class.getDeclaredField("mEditor");
                    mEditor.setAccessible(true);
                    //根具持有对象拿到mEditor变量里的值 （android.widget.Editor类的实例）
                    Object object = mEditor.get(mTvSelect);

                    //--------------------显示选择控制工具------------------------------//
                    //拿到隐藏类Editor
                    Class mClass = Class.forName("android.widget.Editor");
                    //取得方法 getSelectionController
                    @SuppressLint("SoonBlockedPrivateApi")
                    Method method = mClass.getDeclaredMethod("getSelectionController");
                    //取消访问私有方法的合法性检查
                    method.setAccessible(true);
                    //调用方法，返回SelectionModifierCursorController类的实例
                    Object resultobject = method.invoke(object);
                    //查找 SelectionModifierCursorController类中的show方
                    Method show = resultobject.getClass().getDeclaredMethod("show");
                    //执行SelectionModifierCursorController类的实例的show方法
//                    show.invoke(resultobject);
                    mTvSelect.setHasTransientState(true);

                    //--------------------忽略最后一次TouchUP事件-----------------------//
                    //查找变量Editor类中mDiscardNextActionUp
                    Field mSelectionActionMode = mClass.getDeclaredField("mDiscardNextActionUp");
                    mSelectionActionMode.setAccessible(true);
                    //赋值为true
//                    if (mTvSelect.getSelectionStart() != mTvSelect.getSelectionEnd()) {
//                        mSelectionActionMode.set(object, true);
//                    } else {
                        mSelectionActionMode.set(object, true);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    private void showPopWindow() {
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.view_clipboard_popu, null);
//            final ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);


            //TODO 设置点击监听实现相应的处理。

            copy = (TextView) view.findViewById(R.id.copy);
            selectAll = (TextView) view.findViewById(R.id.select_all);
            cut = (TextView) view.findViewById(R.id.cut);
            paste = (TextView) view.findViewById(R.id.paste);
            search = (TextView) view.findViewById(R.id.search);


            //TODO 全选点击事件
            selectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTvSelect.selectAll();
                }
            });

            //TODO 复制点击事件
            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectText = getSelectText(SelectMode.COPY);
                    mTvSelect.setText(selectText);
                    Toast.makeText(MainActivity.this, "已复制到粘贴板", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                }
            });

            //TODO 剪切点击事件
            cut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String txt = getSelectText(SelectMode.CUT);
                    mTvSelect.setText(txt);
                    Toast.makeText(mContext, "选中的内容已剪切到剪切板", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                }
            });

            //TODO 粘贴点击事件
            paste.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //获取剪切班管理者
                    ClipboardManager cbs = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
                    int index = mTvSelect.getSelectionStart();
                    Editable editable = mTvSelect.getText();
                    editable.insert(index, cbs.getText().toString());
                    Toast.makeText(MainActivity.this, "粘贴好了", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                }

            });

            //TODO 搜索的点击事件
            search.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "搜索", Toast.LENGTH_SHORT).show();
                }
            });

            popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        }

        //TODO 弹框的一些设置   位置  点击事件等
//        popupWindow.setFocusable(true);
//        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(mTvSelect, Gravity.CENTER, 0, y);
    }

    /**
     * 统一处理复制和剪切的操作
     *
     * @param mode 用来区别是复制还是剪切
     * @return
     */
    private String getSelectText(SelectMode mode) {
        //获取剪切板管理者
        ClipboardManager cbs = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
        //获取选中的起始位置
        int selectionStart = mTvSelect.getSelectionStart();
        int selectionEnd = mTvSelect.getSelectionEnd();
        Log.i(TAG, "selectionStart=" + selectionStart + ",selectionEnd=" + selectionEnd);
        //截取选中的文本
        String txt = mTvSelect.getText().toString();
        String substring = txt.substring(selectionStart, selectionEnd);
        Log.i(TAG, "substring=" + substring);
        //将选中的文本放到剪切板
        cbs.setPrimaryClip(ClipData.newPlainText(null, substring));
        //如果是复制就不往下操作了
        if (mode == SelectMode.COPY)
            return txt;
        txt = txt.replace(substring, "");
        return txt;
    }

    /**
     * 用枚举来区分是复制还是剪切
     */
    public enum SelectMode {
        COPY, CUT
    }
}
