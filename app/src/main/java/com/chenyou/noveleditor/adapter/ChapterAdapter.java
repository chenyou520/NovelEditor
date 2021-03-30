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


import java.io.BufferedInputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ChapterAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private Context context;//ChapterActivity上下文
    private String bookPath;//书籍的路径目录
    private List<File> chapters;//存放书籍章节的列表
    private String chaptercontent;//章节的内容
    private Utils utils;
    private int tempnNumi;//用于记录数据
    private int tempnNumj;//用于记录数据

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
        String s = readTxtToFile(file);
        s = stringFilter(s);
        viewHolder.chapter_number_words.setText("字数：" + s.length());

        String nameNoEx = getFileNameNoEx(file.getName());
        viewHolder.chapter_name.setText(nameNoEx);


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
                //对o1文件名截取第<xxx>章的做处理
                String s1 = o1.getName();
                s1 = s1.substring(1, s1.indexOf("章"));
                if (s1 != null) {
                    boolean numeric = isNumeric(s1);//判断是不是数字
                    if (numeric) {
                        tempnNumi = Integer.parseInt(s1);
                    } else {
                        char[] chars1 = s1.toCharArray();
                        tempnNumi = ConverToDigit(chars1);//转化为数字
                    }
                }else{
                    return o2.getName().compareTo(o1.getName());//按照文件名排序
                }


                //对o2文件名截取第<xxx>章的做处理
                String s2 = o2.getName();
                s2 = s2.substring(1, s2.indexOf("章"));
                if (s2 != null) {
                    boolean numeric = isNumeric(s2);//判断是不是数字
                    if (numeric) {
                        tempnNumj = Integer.parseInt(s2);
                    } else {
                        char[] chars2 = s2.toCharArray();
                        tempnNumj = ConverToDigit(chars2);//转化为数字
                    }
                }else {
                    return o2.getName().compareTo(o1.getName());//按照文件名排序
                }

                if (tempnNumi - tempnNumj > 0) {
                    return -1;
                } else {
                    return 1;
                }
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
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "unicode"));//将输入流写入缓存,指定格式为 "unicode"
            bufferedWriter.write(chaptercontent);//写入内容
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * 屏蔽空格回车等特殊字符
     *
     * @param str
     * @return
     * @throws PatternSyntaxException
     */
    public static String stringFilter(String str) throws PatternSyntaxException {
        String regEx = "[/\\:*?<>|\"\n\t\r\\s]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }

    /**
     * 读取章节文件并转码
     *
     * @param file
     * @return
     */
    private String readTxtToFile(File file) {
        FileInputStream fileInputStream;
        BufferedReader reader;
        String text = "";
        if (!file.exists()) {
            return null;
        } else {
            try {
                fileInputStream = new FileInputStream(file);
                BufferedInputStream in = new BufferedInputStream(fileInputStream);
                in.mark(4);
                byte[] first3bytes = new byte[3];
                in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
                in.reset();
                if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                        && first3bytes[2] == (byte) 0xBF) {// utf-8

                    reader = new BufferedReader(new InputStreamReader(in, "utf-8"));

                } else if (first3bytes[0] == (byte) 0xFF
                        && first3bytes[1] == (byte) 0xFE) {

                    reader = new BufferedReader(
                            new InputStreamReader(in, "unicode"));
                } else if (first3bytes[0] == (byte) 0xFE
                        && first3bytes[1] == (byte) 0xFF) {

                    reader = new BufferedReader(new InputStreamReader(in,
                            "utf-16be"));
                } else if (first3bytes[0] == (byte) 0xFF
                        && first3bytes[1] == (byte) 0xFF) {

                    reader = new BufferedReader(new InputStreamReader(in,
                            "utf-16le"));
                } else {

                    reader = new BufferedReader(new InputStreamReader(in, "GBK"));
                }

                String str = reader.readLine();

                while (str != null) {
                    text = text + str + "\n";
                    str = reader.readLine();

                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return text;
    }

    /**
     * 判断是否是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将中文数字转换成阿拉伯数字
     *
     * @param cnNumber
     * @return
     */
    static int ConverToDigit(char[] cnNumber) {
        int result = 0;
        int temp = 0;
        for (char c : cnNumber) {
            int temp1 = ToDigit(c);
            if (temp1 == 10000) {
                result += temp;
                result *= 10000;
                temp = 0;
            } else if (temp1 > 9) {
                if (temp1 == 10 && temp == 0) temp = 1;
                result += temp * temp1;
                temp = 0;
            } else temp = temp1;
        }
        result += temp;
        return result;
    }


    /**
     * 将中文数字转换成阿拉伯数字
     *
     * @param cn
     * @return
     */
    static int ToDigit(char cn) {
        int number = 0;
        switch (cn) {
            case '壹':
            case '一':
                number = 1;
                break;
            case '两':
            case '贰':
            case '二':
                number = 2;
                break;
            case '叁':
            case '三':
                number = 3;
                break;
            case '肆':
            case '四':
                number = 4;
                break;
            case '伍':
            case '五':
                number = 5;
                break;
            case '陆':
            case '六':
                number = 6;
                break;
            case '柒':
            case '七':
                number = 7;
                break;
            case '捌':
            case '八':
                number = 8;
                break;
            case '玖':
            case '九':
                number = 9;
                break;
            case '拾':
            case '十':
                number = 10;
                break;
            case '佰':
            case '百':
                number = 100;
                break;
            case '仟':
            case '千':
                number = 1000;
                break;
            case '萬':
            case '万':
                number = 10000;
                break;
            case '零':
            default:
                number = 0;
                break;
        }
        return number;
    }
}
