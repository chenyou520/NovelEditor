package com.chenyou.noveleditor.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chenyou.noveleditor.R;
import com.chenyou.noveleditor.utils.PerformEdit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * 编辑页面
 */
public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int AUTO_SAVE = 1000;//自动保存标识
    private Intent getIntent;//从MainActivity获取消息
    private Intent intent = new Intent();//返回数据
    private int openMode = 0;//编辑页面的入口请求码，mode=0则新建章节页面，mode=1则编辑章节页面
    private String old_chaptercontent = "";//读取的内容
    private String old_chaptername = "";//读取的标题
    private String filepath;//章节保存路径
    private File old_file;//旧章节
    private PerformEdit performEdit;//用于撤销和恢复的类

    private ImageButton etitTopBarBack;//标题返回按钮
    private TextView editNumber;//字数统计
    private ImageButton editBtnPre;//撤销返回
    private ImageButton editBtnNext;//取消撤销
    private LinearLayout editTopBar;//顶部标题布局
    private EditText editName;//章节名称
    private EditText editContent;//章节内容
    private LinearLayout editBottomBar;//底部标题
    private ImageButton editBottomDelete;//删除章节内容
    private ImageButton editBottomSetting;//设置
    private ImageButton editBottomLocation;//定位到最底部
    private int intlength;//章节内容实时字数
    private String chaptercontent;//章节内容
    private int words;//获取的章节文件内容字数

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case AUTO_SAVE:
                    autoupdateMessage();
                    break;
            }
            removeMessages(AUTO_SAVE);
            sendEmptyMessageDelayed(AUTO_SAVE, 10000);//10秒自动保存一次
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        findViews();
        initData();
        handler.sendEmptyMessage(AUTO_SAVE);
    }

    /**
     * 初始化布局和动作监听
     */
    private void findViews() {
        etitTopBarBack = (ImageButton) findViewById(R.id.etit_top_bar_back);
        editNumber = (TextView) findViewById(R.id.edit_number);
        editBtnPre = (ImageButton) findViewById(R.id.edit_btn_pre);
        editBtnNext = (ImageButton) findViewById(R.id.edit_btn_next);
        editName = (EditText) findViewById(R.id.edit_name);
        editContent = (EditText) findViewById(R.id.edit_content);
        editTopBar = (LinearLayout) findViewById(R.id.edit_top_bar);
        editBottomBar = (LinearLayout) findViewById(R.id.edit_bottom_bar);
        editBottomDelete = (ImageButton) findViewById(R.id.edit_bottom_delete);
        editBottomSetting = (ImageButton) findViewById(R.id.edit_bottom_setting);
        editBottomLocation = (ImageButton) findViewById(R.id.edit_bottom_location);

        etitTopBarBack.setOnClickListener(this);
        editBtnPre.setOnClickListener(this);
        editBtnNext.setOnClickListener(this);
        editBottomDelete.setOnClickListener(this);
        editBottomSetting.setOnClickListener(this);
        editBottomLocation.setOnClickListener(this);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        getIntent = getIntent();
        openMode = getIntent.getIntExtra("mode", 0);
        filepath = getIntent.getExtras().getString("chapterpath");
        //文本发生改变,可以是用户输入或者是EditText.setText触发.(setDefaultText的时候不会回调)
        performEdit = new PerformEdit(editContent) {
            @Override
            protected void onTextChanged(Editable s) {
                super.onTextChanged(s);
            }
        };

        //打开存在的章节
        openChapter();

        //章节内容改变监听，内含自动计数和设置撤销操作动作
        editcontChanged();
    }

    /**
     * 事件点击监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == etitTopBarBack) {//返回按钮
            // Handle clicks for etitTopBarBack
            autoupdateMessage();//自动保存信息
            finish();
        } else if (v == editBtnPre) {//撤销
            // Handle clicks for editBtnPre
            undoAction();
        } else if (v == editBtnNext) {//恢复
            // Handle clicks for editBtnNext
            redoAction();
        } else if (v == editBottomDelete) {//删除内容
            // Handle clicks for editBottomDelete
        } else if (v == editBottomSetting) {//设置
            // Handle clicks for editBottomSetting
        } else if (v == editBottomLocation) {//定位到最底部
            // Handle clicks for editBottomLocation
            setLocation();
        }
    }

    /**
     * 撤销操作
     */
    private void undoAction() {
        if (performEdit.isUndoflag()) {//true为可撤销
            performEdit.undo();//撤销
            //变更恢复图片为可点击图片
            editBtnNext.setImageDrawable(getResources().getDrawable(R.drawable.edit_chapter_redo));
        }
        if (!performEdit.isUndoflag()) {
            //变更撤销图片为不可点击图片
            editBtnPre.setImageDrawable(getResources().getDrawable(R.drawable.edit_chapter_undo_dark));
        }
    }

    /**
     * 恢复操作
     */
    private void redoAction() {
        if (performEdit.isRedoflag()) {
            performEdit.redo();//恢复
            //变更撤销图片为不可点击图片
            editBtnPre.setImageDrawable(getResources().getDrawable(R.drawable.edit_chapter_undo));
        }
        if (!performEdit.isRedoflag()) {
            //变更恢复图片为不可点击图片
            editBtnNext.setImageDrawable(getResources().getDrawable(R.drawable.edit_chapter_redo_dark));
        }
    }

    /**
     * 清空章节内容
     */
    private void deleteEditcontent() {
        //弹出删除对话框
        new AlertDialog.Builder(this)
                .setTitle("是否清空小说内容？")
                .setPositiveButton("清空", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editContent.setText("");
                        editContent.requestFocusFromTouch();
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .create()
                .show();
    }

    /**
     * 将编辑框的光标定位到末尾
     */
    private void setLocation() {
        //章节内容编辑框获取焦点
        editContent.setFocusable(true);
        editContent.setFocusableInTouchMode(true);
        editContent.requestFocus();
        editContent.findFocus();
        //进行光标定位到内容末尾
        if (!chaptercontent.equals(old_chaptercontent)) {
            editContent.setSelection(intlength);
        } else {
            editContent.setSelection(words);
        }
    }


    /**
     * 章节内容改变监听
     * 1.自动计数
     * 2.撤销动作变更
     */
    private void editcontChanged() {
        editContent.addTextChangedListener(new TextWatcher() {
            /**
             * charSequence为在你按键之前显示的字符串
             * start为新字符串与charSequence开始出现差异的下标
             * count表示原字符串的count个字符
             * after表示将会被after个字符替换
             * @param s 输入框中改变前的字符串信息
             * @param start 输入框中改变前的字符串的起始位置
             * @param count 输入框中改变前后的字符串改变数量一般为0
             * @param after 输入框中改变后的字符串与起始位置的偏移量
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //这个方法是在Text改变之前被调用，它的意思就是说在原有的文本text中，从start开始的count个字符
                //将会被一个新的长度为after的文本替换，注意这里是将被替换，还没有被替换
            }

            /**
             * 按键之前字符串的start位置的before个字符已经被count个字符替换形成新字符串charSequence
             * @param s 输入框中改变后的字符串信息
             * @param start 输入框中改变后的字符串的起始位置
             * @param before 输入框中改变前的字符串的位置 默认为0
             * @param count 输入框中改变后的一共输入字符串的数量
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //这个方法是在Text改变过程中触发调用的，它的意思就是说在原有的文本text中，从start开始的count个字符
                //替换长度为before的旧文本，注意这里没有将要之类的字眼，也就是说一句执行了替换动作
            }

            /**
             * afterTextChanged中 editable为EditText显示的内容
             * @param s 输入结束呈现在输入框中的信息
             */
            @Override
            public void afterTextChanged(Editable s) {
                intlength = s.length();
                editNumber.setText("字数：" + intlength);//显示实时字数统计

                //变更撤销和恢复图片
                editBtnPre.setImageDrawable(getResources().getDrawable(R.drawable.edit_chapter_undo));
                if (!performEdit.isRedoflag()) {
                    editBtnNext.setImageDrawable(getResources().getDrawable(R.drawable.edit_chapter_redo_dark));
                }
            }
        });
    }

    /**
     * 打开存在的章节内容
     */
    private void openChapter() {
        if (openMode == 1) {//打开已存在的章节
            String str = getIntent.getExtras().getString("chaptername");//获取文件路径
            old_file = new File(filepath + "/" + str);//获取文件
            words = analysis(old_file);//获取总字数
            old_chaptername = getFileNameNoEx(str);//获取标题
            old_chaptercontent = readTxtToFile(old_file);//获取内容
            editName.setText(old_chaptername);//显示标题
            editContent.setText(old_chaptercontent);//显示内容
            editNumber.setText("字数：" + words);//显示字数
            performEdit.setDefultText(old_chaptercontent);//将内容设置为不可操作的初始值
        }
    }

    /**
     * 保存
     */
    private void saveChapter() {
        String chaptername = editName.getText().toString().trim();
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
        String chaptercontent = editContent.getText().toString();
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
        String chaptername = editName.getText().toString().trim();
        chaptercontent = editContent.getText().toString();

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
                                    editName.setFocusable(true);
                                    editName.setFocusableInTouchMode(true);
                                    editName.requestFocus();
                                    editName.findFocus();
                                }
                            })
                            .setNegativeButton("不保存", null)
                            .create()
                            .show();
                } else {
                    saveChapter();
                    intent.putExtra("chaptername", chaptername);
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
                                    editName.setFocusable(true);
                                    editName.setFocusableInTouchMode(true);
                                    editName.requestFocus();
                                    editName.findFocus();
                                }
                            })
                            .setNegativeButton("取消", null)
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
                            intent.putExtra("chaptername", chaptername);
                            setResult(RESULT_OK, intent);
                        }
                    }

                }
                break;
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        performEdit.clearHistory();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
