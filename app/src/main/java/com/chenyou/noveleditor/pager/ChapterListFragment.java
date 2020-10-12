package com.chenyou.noveleditor.pager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.Toast;

import com.chenyou.noveleditor.R;

import com.chenyou.noveleditor.adapter.ChapterAdapter;
import com.chenyou.noveleditor.base.BaseFragment;

/**
 * 章节目录页面
 */
public class ChapterListFragment extends BaseFragment{

    private static String filepath;
    private String title;
    private Context context;
    private View view;
    private static ChapterAdapter chapterAdapter;
    private ChapterReceiver chapterReceiver;

    public ChapterListFragment(Context context, String title, String filepath) {
        super(context, title);
        this.title = title;
        this.context = context;
        this.filepath = filepath;
    }

    /**
     * 得到标题
     *
     * @return
     */
    public String getTitle() {
        return title;
    }


    /**
     * 加载布局
     *
     * @return
     */
    @Override
    public View initview() {
        view = View.inflate(context, R.layout.chapter_list_fragment, null);
        return view;
    }

    /**
     * 初始化布局
     */
    @Override
    public void initData() {
        super.initData();
        View chapter_ll = view.findViewById(R.id.chapter_ll);
        chapterAdapter = new ChapterAdapter(chapter_ll, context, filepath);
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
        if (chapterReceiver == null) {
            chapterReceiver = new ChapterReceiver();
            IntentFilter filter = new IntentFilter("chapter");
            context.registerReceiver(chapterReceiver, filter);
        }
    }

    /**
     * 解注册广播接收器
     */
    public void unRegistBR() {
        if (chapterReceiver != null) {
            context.unregisterReceiver(chapterReceiver);
            chapterReceiver = null;
        }
    }

    /**
     * 创建广播接收器
     */
    public static class ChapterReceiver extends BroadcastReceiver {

        public ChapterReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("chapter")) {
                chapterAdapter.getFileDir(filepath);
            }
        }
    }
}
