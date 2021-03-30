package com.chenyou.noveleditor.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenyou.noveleditor.R;
import com.chenyou.noveleditor.pager.SetPopuWindow;
import com.chenyou.noveleditor.utils.PerformEdit;
import com.chenyou.noveleditor.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

    private LinearLayout llPlace;//替换布局
    private EditText edSearch;//搜索框
    private Button btnReplace;//替换按钮
    private EditText edReplace;//替换框
    private Button btnAllreplace;//替换所有按钮
    private Button btnExc;//取消按钮
    private LinearLayout editMainll;
    private ImageButton etitTopBarBack;//标题返回按钮
    private TextView editNumber;//字数统计
    private ImageButton editBtnPre;//撤销返回
    private ImageButton editBtnNext;//取消撤销
    private LinearLayout editTopBar;//顶部标题布局
    private EditText editName;//章节名称
    private EditText editContent;//章节内容
    private Button findAndReplace;//查找/替换
    private ScrollView editScrollview;//包裹章节标题和内容的可滚动布局
    private LinearLayout editBottomBar;//底部标题
    private ImageButton editBottomTypeset;//排版
    private ImageButton editBottomDelete;//删除章节内容
    private ImageButton editBottomSetting;//设置
    private ImageButton editBottomLocation;//定位到最底部
    private int intlength;//章节内容实时字数（去除了特殊字符）
    private String chaptercontent;//章节内容
    private int endLength;//章节内容更改后的总字数
    private int height;//底部标题栏高度
    private int mScreenWidth;//屏幕宽

    private SharedPreferences shared = null;

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
            sendEmptyMessageDelayed(AUTO_SAVE, 20000);//20秒自动保存一次
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        shared = getSharedPreferences("setdata", MODE_PRIVATE);
        findViews();
        initData();
        measured();
        //自动保存
        handler.sendEmptyMessage(AUTO_SAVE);
    }

    /**
     * 测量
     */
    private void measured() {
        //获取屏幕宽高
        WindowManager manager = this.getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;//屏幕宽

        //获取底部标题栏的高度
        editBottomBar.post(new Runnable() {
            @Override
            public void run() {
                height = editBottomBar.getMeasuredHeight();
            }
        });
    }

    /**
     * 初始化布局和动作监听
     */
    private void findViews() {
        editMainll = (LinearLayout) findViewById(R.id.edit_main_ll);
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
        editScrollview = (ScrollView) findViewById(R.id.edit_scrollview);
        findAndReplace = (Button) findViewById(R.id.find_and_replace);
        editBottomTypeset = (ImageButton) findViewById(R.id.edit_bottom_typeset);
        llPlace = (LinearLayout) findViewById(R.id.ll_place);
        edSearch = (EditText) findViewById(R.id.ed_search);
        btnReplace = (Button) findViewById(R.id.btn_replace);
        edReplace = (EditText) findViewById(R.id.ed_replace);
        btnAllreplace = (Button) findViewById(R.id.btn_allreplace);
        btnExc = (Button) findViewById(R.id.btn_exc);

        btnReplace.setOnClickListener(this);
        btnAllreplace.setOnClickListener(this);
        btnExc.setOnClickListener(this);
        etitTopBarBack.setOnClickListener(this);
        editBtnPre.setOnClickListener(this);
        editBtnNext.setOnClickListener(this);
        editBottomDelete.setOnClickListener(this);
        editBottomSetting.setOnClickListener(this);
        editBottomLocation.setOnClickListener(this);
        findAndReplace.setOnClickListener(this);
        editBottomTypeset.setOnClickListener(this);
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

        //初始化字体大小
        int fontSize = shared.getInt("fonesize", R.id.default_size);
        setFontsize(fontSize);
        //初始化背景颜色
        int mNowPick = shared.getInt("mNowPick", R.id.edit_set_rb_whitle);
        setChangeBackgound(mNowPick);
        //打开存在的章节
        openChapter();
        //章节内容改变监听，内含自动计数和设置撤销操作动作
        editcontChanged();
        //enter键自动换行，并空自动空二格，实现文字自动排版
        enterKeyAutotype();

    }

    /**
     * enter键自动换行，并空自动空二格，实现文字自动排版
     */
    private void enterKeyAutotype() {
        editContent.setOnKeyListener(new MyOnKeyListener());
        autoTypeset();
        typeSetting();
    }

    /**
     * 事件点击监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == etitTopBarBack) {//返回按钮
            autoupdateMessage();//自动保存信息
            finish();
        } else if (v == findAndReplace) {//替换
            llPlace.setVisibility(View.VISIBLE);
            editTopBar.setVisibility(View.GONE);
        } else if (v == editBtnPre) {//撤销
            undoAction();
        } else if (v == editBtnNext) {//恢复
            redoAction();
        } else if (v == editBottomTypeset) {//排版
            typeSetting();
        } else if (v == editBottomDelete) {//删除内容
            deleteEditcontent();
        } else if (v == editBottomSetting) {//设置
            settings();
        } else if (v == editBottomLocation) {//定位到最底部
            setLocation();
        } else if (v == btnReplace) {//替换
            replace();
        } else if (v == btnAllreplace) {//替换所有
            allReplace();
        } else if (v == btnExc) {//取消
            editTopBar.setVisibility(View.VISIBLE);
            llPlace.setVisibility(View.GONE);
        }
    }

    /**
     * 全部替换
     */
    private void allReplace() {
        String keyword = edSearch.getText().toString();//关键字
        String replaceword = edReplace.getText().toString();
        String s = editContent.getText().toString();
        s = s.replaceAll(keyword, replaceword);
        editContent.setText(s);
    }

    /**
     * 替换
     */
    private void replace() {
        String keyword = edSearch.getText().toString();//关键字
        String replaceword = edReplace.getText().toString();
        String s = editContent.getText().toString();
        int index = s.indexOf(keyword);
        s = s.replaceFirst(keyword, replaceword);
        if (index != 0) {
            int end = index + keyword.length();
            editContent.setSelection(index, end);
        }
        editContent.setText(s);
    }

    /**
     * 排版
     *
     * @param
     */
    private void typeSetting() {
        //去除字符串中的所有空格
        String str = editContent.getText().toString();
        String str2 = str.replaceAll(" ", "");
//        str2 = str2.replaceAll("(?m)^\\s*$(\\n|\\r\\n)", "");
        str2 = str2.replaceAll("\t", "");
        str2 = str2.replaceAll("\t", "");
        str2 = str2.replaceAll("\n", "\n    ");
        editContent.setText("    " + str2);
    }

    /**
     * 设置功能
     */
    private void settings() {
        SetPopuWindow popuWindow = new SetPopuWindow(this, mScreenWidth - 10, WindowManager.LayoutParams.WRAP_CONTENT);
        popuWindow.showAtLocation(editMainll, Gravity.BOTTOM | Gravity.CENTER, 0, height);
        popuWindow.setCallback(new SetPopuWindow.CallBack() {
            @Override
            public void setFontSize(int font) {
                setFontsize(font);
            }

            @Override
            public void changeBackgound(int id) {
                setChangeBackgound(id);
            }
        });

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
        //获取内容长度
        int length = editContent.getText().length();
        //进行光标定位到内容末尾
        editContent.setSelection(length);

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
             * @param start 输框中改变前的字符串的起始位置入
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
                String s1 = editContent.getText().toString();
                String str = stringFilter(s1); //过滤特殊字符
                endLength = s1.length();//总字数
                intlength = s.length() - (s.length() - str.length());
                editNumber.setText("字数：" + intlength);//显示实时字数统计

                //变更撤销图片
                editBtnPre.setImageDrawable(getResources().getDrawable(R.drawable.edit_chapter_undo));
                //判断是否可恢复，不可恢复变更图片为白色
                if (!performEdit.isRedoflag()) {
                    editBtnNext.setImageDrawable(getResources().getDrawable(R.drawable.edit_chapter_redo_dark));
                }
            }
        });
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
     * 打开存在的章节内容
     */
    private void openChapter() {
        if (openMode == 1) {//打开已存在的章节
            String str = getIntent.getExtras().getString("chaptername");//获取文件路径
            old_file = new File(filepath + "/" + str);//获取文件
            int words = analysis(old_file);//获取总字数
            old_chaptername = getFileNameNoEx(str);//获取标题
            old_chaptercontent = readTxtToFile(old_file);//获取内容并转码
            editName.setText(old_chaptername);//显示标题
            editContent.setText(old_chaptercontent);//显示内容
            editNumber.setText("字数：" + words);//显示字数
            performEdit.setDefultText(old_chaptercontent);//将内容设置为不可操作的初始值
            //排除写入的错版
            autoTypeset();
            typeSetting();//排版
        }
    }

    /**
     * 替换空格，回车自动空4格
     */
    private void autoTypeset() {
        String text = editContent.getText().toString().trim();
        text = text.replaceAll(" ", "");
//        text = text.replaceAll("\t", "");
//        text = text.replaceAll("\n", "\n    ");
        editContent.setText("    " + text);
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
     * 读取章节文件并转码
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
                    text = text + str+"\n";
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
                if (chaptercontent.length() == 0) {//没内容
                    return;
                } else if (chaptername.length() == 0 && chaptercontent.length() != 0) {//有内容没标题
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


    /**
     * 设置字体大小
     *
     * @param fontid
     */
    public void setFontsize(int fontid) {
        switch (fontid) {
            case R.id.small_size:
                editName.setTextSize(16);
                editContent.setTextSize(16);
                break;
            case R.id.medium_size:
                editName.setTextSize(19);
                editContent.setTextSize(19);
                break;
            case R.id.default_size:
                editName.setTextSize(22);
                editContent.setTextSize(22);
                break;
            case R.id.big_size:
                editName.setTextSize(24);
                editContent.setTextSize(24);
                break;
//            case R.id.super_size:
//                editName.setTextSize(26);
//                editContent.setTextSize(26);
//                break;
        }

    }

    /**
     * 设置背景颜色
     *
     * @param id
     */
    public void setChangeBackgound(int id) {
        switch (id) {
            case R.id.edit_set_rb_whitle:
                setBackgroundColor(getResources().getColor(R.color.rg_white), getResources().getColor(R.color.rg_white), false);
                break;
            case R.id.edit_set_rb_yellow:
                setBackgroundColor(getResources().getColor(R.color.rg_yellow), getResources().getColor(R.color.rg_yellow_less), false);
                break;
            case R.id.edit_set_rb_green:
                setBackgroundColor(getResources().getColor(R.color.rg_green), getResources().getColor(R.color.rg_green_less), false);
                break;
            case R.id.edit_set_rb_blue:
                setBackgroundColor(getResources().getColor(R.color.rg_blue), getResources().getColor(R.color.rg_blue_less), false);
                break;
            case R.id.edit_set_rb_black:
                setBackgroundColor(getResources().getColor(R.color.rg_black), getResources().getColor(R.color.rg_black_less), true);
                break;
        }
    }

    /**
     * 设置背景颜色
     *
     * @param mianColor
     * @param titleColor
     * @param black
     */
    private void setBackgroundColor(int mianColor, int titleColor, boolean black) {
        if (black) {//黑色模式
            editScrollview.setBackgroundColor(mianColor);
            editName.setTextColor(getResources().getColor(R.color.white));
            editContent.setTextColor(getResources().getColor(R.color.white));
        } else {
            editScrollview.setBackgroundColor(mianColor);
            editName.setTextColor(getResources().getColor(R.color.black));
            editContent.setTextColor(getResources().getColor(R.color.black));
        }

    }

    /**
     * enter键自动换行，并空自动空二格
     */
    private class MyOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if ((event.getAction() == KeyEvent.ACTION_UP)) {
                    int end = editContent.getSelectionEnd();
                    Editable editableText = editContent.getEditableText();
                    editableText.insert(end, "\r\n    ");
                }
                return true;
            }
            return false;
        }
    }

}
