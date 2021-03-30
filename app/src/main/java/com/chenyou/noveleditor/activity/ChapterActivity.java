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

import com.bumptech.glide.util.Util;
import com.chenyou.noveleditor.R;

import com.chenyou.noveleditor.adapter.ViewPagerAdapter;
import com.chenyou.noveleditor.base.BaseFragment;
import com.chenyou.noveleditor.pager.ChapterListFragment;
import com.chenyou.noveleditor.pager.DustbinFragment;
import com.chenyou.noveleditor.utils.Utils;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private List<File> chapters;
    private String bookname;
    private Utils utils = new Utils();
    private String time;

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
        bookname = getIntent.getStringExtra("bookname");
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

    /**
     * 导出全文
     * 新建章节
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.full_export://全文导出
                chapters = getFileDir(filepath);
                File fullexport = new File(filepath + "/全文导出");
                if (!fullexport.exists()) {
                    fullexport.mkdirs();
                }
                File bookfile = new File(fullexport.getPath() + "/" + bookname + ".txt");
                try {
                    String str = "";
                    if (!bookfile.exists()) {
                        bookfile.createNewFile();
                    }
                    for (int i = 0; i < chapters.size(); i++) {
                        File file = chapters.get(i);
                        String nameNoEx = getFileNameNoEx(file.getName());
                        String content = readTxtToFile(file);
                        str += nameNoEx + "\n" + content + "\n" + "\n";
                        writeTxtToFile(bookfile, str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(ChapterActivity.this, "已导出全文", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_new_chapter://新建章节
                //跳转到编辑页面
//                System.out.println("filepath:" + filepath);
                Intent intent = new Intent(ChapterActivity.this, EditActivity.class);
                intent.putExtra("mode", 0);
                intent.putExtra("chapterpath", filepath);
                startActivityForResult(intent, CHAPTERTOEDIT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 写入保存章节
     *
     * @param file
     */
    private void writeTxtToFile(File file, String chaptercontent) {
        //章节内容
        FileOutputStream fileOutputStream;
        BufferedWriter bufferedWriter;
        try {
            fileOutputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "utf-8"));//将输入流写入缓存,指定格式为 "utf-8"
            bufferedWriter.write(chaptercontent);//写入内容
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有章节
     *
     * @param bookPath
     */
    public List<File> getFileDir(String bookPath) {
        final File file = new File(bookPath);
        //判断是否是章节文件，并且后缀为: .txt
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {//过滤掉文件夹
                    return false;
                }
                if (pathname.isFile() && file.getName().endsWith(".txt")) {
                    return pathname.getName().endsWith(".txt");
                }
                return true;
            }
        });
        //将所有章节文件添加到fileList中new ArrayList<>(Arrays.asList(otherUserFromArray));
        assert files != null;
        List<File> chapters = new ArrayList<>(Arrays.asList(files));
        //排序
        Collections.sort(chapters, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return chapters;
    }

    /**
     * 读取章节文件
     *
     * @param file
     * @return
     */
    private String readTxtToFile(File file) {
        FileInputStream fileInputStream;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder = new StringBuilder();
        if (!file.exists()) {
            return null;
        } else {
            try {
                fileInputStream = new FileInputStream(file);
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "utf-8"));//指定格式为 "utf-8"
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 去除后缀
     *
     * @param filename
     * @return
     */
    private String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
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
     * 当Tab的item被选中时，发送广播通知更新
     *
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
