package com.chenyou.noveleditor.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;


import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chenyou.noveleditor.R;

import com.chenyou.noveleditor.adapter.ViewPagerAdapter;
import com.chenyou.noveleditor.base.BaseFragment;
import com.chenyou.noveleditor.pager.ChapterListFragment;
import com.chenyou.noveleditor.pager.DustbinFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class ChapterActivity extends AppCompatActivity implements TabLayout.BaseOnTabSelectedListener {

    private static final int CHAPTERTOEDIT = 400;//跳转到编辑页面的返回码
    private static final String action1 = "chapter";//声明第一个动作
    private static final String action2 = "dustbin";//声明第二个动作

    private Toolbar chapter_toolbar;//顶部标题栏
    private TextView chapter_bookname;//标题栏名称
    private TabLayout chapter_tabLayout;//viewpager页面指示器
    private ViewPager chapter_viewpager;//viewpager

    private Intent getIntent;//从MainActivity获取消息
    private Intent intent = new Intent();//返回消息到MainActivity
    private Context context = this;
    private ArrayList<BaseFragment> fragments;
    private ViewPagerAdapter viewPagerAdapter;

    private String[] titles = {
            "章节目录", "回收站"
    };
    private String filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);
        getIntent = getIntent();
        initView();
        setToolbar();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        chapter_toolbar = (Toolbar) findViewById(R.id.chapter_toolbar);
        chapter_bookname = (TextView) findViewById(R.id.chapter_bookname);
        chapter_tabLayout = (TabLayout) findViewById(R.id.chapter_tabLayout);
        chapter_viewpager = (ViewPager) findViewById(R.id.chapter_viewpager);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //设置标题栏的名字为书名
        String bookname = getIntent.getStringExtra("bookname");
        chapter_bookname.setText(bookname);

        //书籍保存路径
        filepath = getIntent.getStringExtra("bookpath");

        fragments = new ArrayList<>();
        fragments.add(new ChapterListFragment(context, titles[0], filepath));
        fragments.add(new DustbinFragment(context, titles[1], filepath));

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        chapter_viewpager.setAdapter(viewPagerAdapter);

        //tabLayout关联viewpager,默认自动刷新
        chapter_tabLayout.setupWithViewPager(chapter_viewpager);
        chapter_tabLayout.setTabMode(TabLayout.MODE_FIXED);
        chapter_tabLayout.addOnTabSelectedListener(this);
    }

    /**
     * 设置标题栏
     */
    private void setToolbar() {
        //把默认标题去掉
        chapter_toolbar.setTitle("");
        //取代原本的actionbar
        setSupportActionBar(chapter_toolbar);
        //决定左上角的图标是否可以点击。没有向左的小图标。 true 图标可以点击  false 不可以点击。
        getSupportActionBar().setHomeButtonEnabled(true);
        //// 给左上角图标的左边加上一个返回的图标 。
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        chapter_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * 绑定Menu布局
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chapter_menu, menu);
        // 绑定toobar跟menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.full_export:
                break;
            case R.id.menu_new_chapter://新建章节
                //跳转到编辑页面
                System.out.println("filepath:" + filepath);
                Intent intent = new Intent(ChapterActivity.this, EditActivity.class);
                intent.putExtra("mode", 0);
                intent.putExtra("chapterpath", filepath);
                startActivityForResult(intent, CHAPTERTOEDIT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHAPTERTOEDIT && resultCode == RESULT_OK && data != null) {//书籍点击
            String newchaptername = data.getExtras().getString("chaptername", "请创建新章节");
            intent.putExtra("newchapter", newchaptername);
            setResult(RESULT_OK, intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    /**
     * 当Tab的item被选中时
     * @param tab
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Intent intent = new Intent();//创建Intent对象
        switch (tab.getPosition()) {
            case 0:
                // 发送消息
                Intent intent1 = new Intent();//创建Intent对象
                intent1.setAction(action1);//为Intent添加动作chapter
                sendBroadcast(intent1);//发送广播
                break;
            case 1:
                // 发送消息
                Intent intent2 = new Intent();//创建Intent对象
                intent2.setAction(action2);//为Intent添加动作dustbin
                sendBroadcast(intent2);//发送广播
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
