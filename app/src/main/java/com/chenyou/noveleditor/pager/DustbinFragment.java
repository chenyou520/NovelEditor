package com.chenyou.noveleditor.pager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.Toast;

import com.chenyou.noveleditor.R;
import com.chenyou.noveleditor.adapter.DustbinAdapter;
import com.chenyou.noveleditor.base.BaseFragment;

import java.io.File;


/**
 * 垃圾箱页面
 */
public class DustbinFragment extends BaseFragment {
    private Context context;
    private static String filepath;
    private View view;
    private static DustbinAdapter dustbinAdapter;
    private DustbinReceiver dustbinReceiver;

    public DustbinFragment(Context context, String title, String filepath) {
        super(context, title);
        this.context = context;
        this.filepath = filepath;
    }

    @Override
    public View initview() {
        view = View.inflate(context, R.layout.chapter_list_fragment, null);
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        View chapter_ll = view.findViewById(R.id.chapter_ll);
        dustbinAdapter = new DustbinAdapter(chapter_ll, context, filepath);
        registBR();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegistBR();
    }

    /**
     * 注册广播接收器
     */
    public void registBR() {
        if (dustbinReceiver == null) {
            dustbinReceiver = new DustbinReceiver();
            IntentFilter filter = new IntentFilter("dustbin");
            context.registerReceiver(dustbinReceiver, filter);
            Toast.makeText(context, "注册dustbin广播接收器", Toast.LENGTH_SHORT).show();
        }
    }

    public void unRegistBR() {
        if (dustbinReceiver != null) {
            context.unregisterReceiver(dustbinReceiver);
            dustbinReceiver = null;
            Toast.makeText(context, "解注册dustbin广播接收器", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建广播接收器
     */
    public static class DustbinReceiver extends BroadcastReceiver {

        public DustbinReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("dustbin")) {
                //回收站文件夹
                String dustbinPath = filepath + "/" + "回收站";
                dustbinAdapter.getFileDir(dustbinPath);
            }
        }
    }
}
