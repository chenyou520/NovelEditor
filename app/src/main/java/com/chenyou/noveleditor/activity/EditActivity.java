package com.chenyou.noveleditor.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.chenyou.noveleditor.R;
import com.chenyou.noveleditor.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class EditActivity extends AppCompatActivity {

    private EditText edit_name;
    private EditText edit_content;
    private ImageButton igbtn;

    private Intent getIntent;//从MainActivity获取消息
    private Intent intent = new Intent();
    private int openMode = 0;//编辑页面的入口请求码，mode=0则新建章节页面，mode=1则编辑章节页面
    private String old_chaptercontent = "";//读取的内容
    private String old_chaptername = "";//读取的标题
    private String filepath;
    private File old_file;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1000:
                    autoupdateMessage();
                    break;
            }
            removeMessages(1000);
            sendEmptyMessageDelayed(1000, 10000);//10秒自动保存一次
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        initview();
        initData();
    }

    /**
     * 初始化页面
     */
    private void initview() {
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_content = (EditText) findViewById(R.id.edit_content);
        igbtn = (ImageButton) findViewById(R.id.igbtn);
        getIntent = getIntent();
        openMode = getIntent.getIntExtra("mode", 0);
        filepath = getIntent.getExtras().getString("chapterpath");
        System.out.println("filepath:" + filepath);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        if (openMode == 1) {//打开已存在的章节
            String str = getIntent.getExtras().getString("chaptername");
            old_file = new File(filepath + "/" + str);
            old_chaptername = getFileNameNoEx(str);
            old_chaptercontent = readTxtToFile(old_file);
            edit_name.setText(old_chaptername);
            edit_content.setText(old_chaptercontent);
        }

        igbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoupdateMessage();
            }
        });
    }

    /**
     * 保存
     */
    private void saveChapter() {
        String chaptername = edit_name.getText().toString().trim();
        if (!chaptername.isEmpty()) {
            File newfile = new File(filepath + "/" + chaptername + ".txt");
            try {
                if (!newfile.exists()) {
                    newfile.createNewFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            writeTxtToFile(newfile);
        }
    }

    /**
     * 写入保存章节
     *
     * @param file
     */
    private void writeTxtToFile(File file) {
        //章节内容
        String chaptercontent = edit_content.getText().toString();
        System.out.println(chaptercontent);

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
     * 点击返回键时自动更新保存
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            autoupdateMessage();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 1.新建章节,情况分为：
     * 情况1.没有标题，没有内容，这种情况不做任何处理，即不创建章节
     * 情况2.没有标题，有内容，这种情况提示创建标题
     * <p>
     * 2.点击进入编辑页面，情况分为：
     * 情况1.没有标题，没有内容，即删除了标题和内容，这种情况直接删除掉章节
     * 情况2.没有标题，有内容，这种情况提示创建标题
     * 情况3.有标题，有内容：
     * 1.修改了内容或更新内容
     */
    public void autoupdateMessage() {
        String chaptername = edit_name.getText().toString().trim();
        String chaptercontent = edit_content.getText().toString();

        switch (openMode) {
            case 0://新建章节
            default:
                if (chaptername.length() == 0 && chaptercontent.length() == 0) {//没有标题，没有内容
                    return;
                } else if (chaptername.length() == 0 && chaptercontent.length() != 0) {
                    //弹出提示框添加标题
                    new AlertDialog.Builder(this)
                            .setMessage("标题不能为空")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //是标题输入框获取焦点
                                    edit_name.setFocusable(true);
                                    edit_name.setFocusableInTouchMode(true);
                                    edit_name.requestFocus();
                                    edit_name.findFocus();
                                }
                            })
                            .setNegativeButton("不保存",null)
                            .create()
                            .show();
                } else {
                    saveChapter();
                    intent.putExtra("chaptername",chaptername);
                    setResult(RESULT_OK, intent);
                }
                break;

            case 1://点击编辑
                if (chaptername.length() == 0 && chaptercontent.length() == 0) {//没有标题，没有内容
                    //删除章节
                    File file = old_file;
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }

                } else if (chaptername.length() == 0 && chaptercontent.length() != 0) {//没有标题，有内容
                    //弹出提示框添加标题
                    new AlertDialog.Builder(this)
                            .setMessage("标题不能为空")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //是标题输入框获取焦点
                                    edit_name.setFocusable(true);
                                    edit_name.setFocusableInTouchMode(true);
                                    edit_name.requestFocus();
                                    edit_name.findFocus();
                                }
                            })
                            .setNegativeButton("取消",null)
                            .create()
                            .show();

                } else {//有标题，有内容,
                    if (!chaptername.equals(old_chaptername) || !chaptercontent.equals(old_chaptercontent)) {//有修改标题或内容
                        if (chaptername.equals(old_chaptername)) {
                            saveChapter();
                        } else {
                            //删除章节
                            File file = old_file;
                            if (file.isFile() && file.exists()) {
                                file.delete();
                            }
                            saveChapter();
                            intent.putExtra("chaptername",chaptername);
                            setResult(RESULT_OK, intent);
                        }
                    }

                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
