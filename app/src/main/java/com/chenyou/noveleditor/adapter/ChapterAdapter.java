package com.chenyou.noveleditor.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.chenyou.noveleditor.R;
import com.chenyou.noveleditor.activity.EditActivity;
import com.chenyou.noveleditor.utils.Utils;


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

public class ChapterAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private Context context;//ChapterActivity上下文
    private String bookPath;//书籍的路径目录
    private List<File> chapters;//存放书籍章节的列表
    private String chaptercontent;//章节的内容
    private Utils utils;

    public ChapterAdapter(View chapterLinearLayout, Context context, String bookPath) {
        ListView chapterlist_listview = (ListView) chapterLinearLayout.findViewById(R.id.chapterlist_listview);
        this.context = context;
        this.bookPath = bookPath;

        if (bookPath != null && !bookPath.isEmpty()) {
            getFileDir(bookPath);
            notifyDataSetChanged();
        }

        chapterlist_listview.setAdapter(this);
        chapterlist_listview.setOnItemClickListener(this);
        chapterlist_listview.setOnItemLongClickListener(this);
    }


    @Override
    public int getCount() {
        return chapters.size();
    }

    @Override
    public Object getItem(int position) {
        return chapters == null ? 0 : chapters.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.chapter_item, null);
            viewHolder = new ViewHolder();

            viewHolder.chapter_name = convertView.findViewById(R.id.chapter_name);
            viewHolder.chapter_number_words = convertView.findViewById(R.id.chapter_number_words);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        File file = chapters.get(position);
        String nameNoEx = getFileNameNoEx(file.getName());
        viewHolder.chapter_name.setText(nameNoEx);
        int words = analysis(file);
        viewHolder.chapter_number_words.setText("字数：" + words);
        return convertView;
    }

    static class ViewHolder {
        TextView chapter_name;//章节名字
        TextView chapter_number_words;//字数
    }


    /**
     * 点击按钮
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = chapters.get(position);
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra("mode", 1);
        intent.putExtra("chaptername", file.getName());
        intent.putExtra("chapterpath", bookPath);
        context.startActivity(intent);
    }

    /**
     * 长按事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final File file = chapters.get(position);
        String nameNoEx = getFileNameNoEx(file.getName());//去除文件名的后缀
        final String chapterpath = bookPath + "/" + "回收站" + "/" + file.getName();//将章节移入到回收站的路径
        utils = new Utils();

        //弹出删除对话框
        new AlertDialog.Builder(context)
                .setTitle("是否将该章节移入回收站？")
                .setMessage("移除章节：" + nameNoEx)
                .setPositiveButton("确定移除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //读取章节内容
                        chaptercontent = readTxtToFile(file);
                        //在章节目录中新创建个章节文件，将内容写入章节
                        saveChapter(chapterpath);
                        //删除掉章节目录的章节文件
                        utils.deleteFile(file.getPath());
                        //将章节从章节列表中移除，不再显示
                        chapters.remove(position);
                        //从新获取回收站的章节刷新显示
                        getFileDir(bookPath);
                    }
                })
                .setNegativeButton("取消移除", null)
                .setCancelable(false)
                .create()
                .show();

        return true;
    }

    /**
     * 获取所有章节
     *
     * @param bookPath
     */
    public void getFileDir(String bookPath) {
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
        chapters = new ArrayList<>(Arrays.asList(files));
        //排序
        Collections.sort(chapters, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o2.getName().compareTo(o1.getName());
            }
        });

        notifyDataSetChanged();

    }

    /**
     * 保存
     *
     * @param chapterpath
     */
    private void saveChapter(String chapterpath) {
        File newfile = new File(chapterpath);
        try {
            if (!newfile.exists()) {
                newfile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeTxtToFile(newfile);

    }

    /**
     * 写入保存章节
     *
     * @param file
     */
    private void writeTxtToFile(File file) {

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


    /**
     * 统计字数
     *
     * @param file
     * @return
     */
    private int analysis(File file) {
        String str = "";

        int character = 0;//字母数
        int sum = 0;//总字数
        int chineselenght = 0;//汉字数
        int spaces = 0;//空格数

        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            //判断SD卡是否存在,并且是否具有读写权限
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                if (file.exists()) {//判断文件是否存在
                    //打开文件输入流
                    fis = new FileInputStream(file);
                    //字符流写入了缓冲区
                    br = new BufferedReader(new InputStreamReader(fis));

                    while ((str = br.readLine()) != null) {//readLine()每次读取一行，转化为字符串，br.readLine()为null时，不执行

                        char[] b = str.toCharArray();//将字符串对象中的字符转换为一个字符数组
                        for (int i = 0; i < str.length(); i++) {
                            if (b[i] == ' ') {//如果字符数组中包含空格，spaces自加1
                                spaces++;//空格数
                            }

                            //中文及中文字符算两个字符,英文及英文字符算一个字符
                            //这里是根据ACSII值进行判定的中英文，其中中文及中文符号的ACSII值都是大于128的
                            char charAt = str.charAt(i);
                            if (charAt <= 128) {
                                character++;
                            } else {
                                chineselenght++;
                            }
                        }
                    }
                    sum = character + chineselenght - spaces;//总字数=字母数+汉字数和符号数-空格数
                    //关闭文件
                    br.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sum;
    }
}
