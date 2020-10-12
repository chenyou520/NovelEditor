package com.chenyou.noveleditor.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chenyou.noveleditor.MyApplication;
import com.chenyou.noveleditor.R;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StorageActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int FILE_RESULT_CODE = 1;

    private Button btn_default;
    private Button btn_inside;
    private Button btn_sdcard;
    private ImageButton str_ibtn_back;
    private TextView changePath;

    private String rootPath;
    private String filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        initView();
        filepath = getSPrefs();
        if (filepath == null) {
            defaultFile();
        } else {
            changePath.setText(filepath);
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        btn_default = (Button) findViewById(R.id.btn_default);
        btn_inside = (Button) findViewById(R.id.btn_inside);
        btn_sdcard = (Button) findViewById(R.id.btn_sdcard);
        changePath = (TextView) findViewById(R.id.changePath);
        str_ibtn_back = (ImageButton) findViewById(R.id.str_ibtn_back);


        if (avaiableMedia()) {
            btn_sdcard.setEnabled(true);
        } else {
            btn_sdcard.setEnabled(false);
            Toast.makeText(StorageActivity.this, "Sdcard不存在", Toast.LENGTH_SHORT).show();
        }

        btn_default.setOnClickListener(this);
        btn_inside.setOnClickListener(this);
        btn_sdcard.setOnClickListener(this);
        str_ibtn_back.setOnClickListener(this);
    }

    /**
     * 设置监听动作
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_default://默认存储
            default:
                defaultFile();
                break;

            case R.id.btn_inside://打开手机文件夹
                openMemory();
                break;

            case R.id.btn_sdcard://打开Sdcard文件夹
                openSdcard();
                break;

            case R.id.str_ibtn_back://返回
                finish();
                break;
        }
    }

    /**
     * 获取保存的文件路径
     * @return
     */
    private String getSPrefs() {
        SharedPreferences pref = getSharedPreferences("bookpath", MODE_PRIVATE);
        String filepath = pref.getString("filepath", null);
        return filepath;
    }

    /**
     * 设置文件的保存路径
     * @param filepath
     */
    private void setSPrefs(String filepath) {
        SharedPreferences.Editor editor = getSharedPreferences("bookpath", MODE_PRIVATE).edit();
        editor.putString("filepath", filepath);
        editor.commit();
    }

    /**
     * 默认存储
     */
    private void defaultFile() {
        File file = getExternalFilesDir("");
        String defaultfilepath = file.toString();
        setSPrefs(defaultfilepath);
        changePath.setText(defaultfilepath);
    }

    /**
     * 打开手机文件夹
     */
    private void openMemory() {
        rootPath = System.getenv("SECONDARY_STORAGE");
        if (rootPath == null) {
            rootPath = Environment.getExternalStorageDirectory().toString();
        }
        if ((rootPath.equals(Environment.getExternalStorageDirectory().toString()))) {
            String filePath = rootPath + "/Android";
            Intent intent = new Intent(StorageActivity.this, FileBrowserActivity.class);
            //根目录
            intent.putExtra("rootPath", rootPath);
            //进去指定文件夹
            intent.putExtra("path", filePath);
            startActivityForResult(intent, FILE_RESULT_CODE);
        }
    }

    /**
     * 打开Sdcard文件夹
     */
    private void openSdcard() {
        rootPath = getSdcardPath();
        if (rootPath == null || rootPath.isEmpty()) {
            rootPath = Environment.getExternalStorageDirectory().toString();
        }
        Intent intent = new Intent(StorageActivity.this, FileBrowserActivity.class);
        intent.putExtra("rootPath", rootPath);
        intent.putExtra("path", rootPath);
        startActivityForResult(intent, FILE_RESULT_CODE);
    }

    /**
     * 获取SDcard路径
     *
     * @return
     */
    public String getSdcardPath() {
        String sdcardPath = "";
        String[] pathArr = null;
        StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        try {
            Method getVolumePaths = storageManager.getClass().getMethod("getVolumePaths");
            pathArr = (String[]) getVolumePaths.invoke(storageManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (pathArr != null && pathArr.length >= 3) {
            sdcardPath = pathArr[1];
        }
        return sdcardPath;
    }

    /**
     * 判断SD卡是否存在 返回true表示存在
     *
     * @return
     */
    public boolean avaiableMedia() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回的结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (FILE_RESULT_CODE == requestCode) {
            Bundle bundle = null;
            if (data != null && (bundle = data.getExtras()) != null) {
                String path = bundle.getString("file", "");
                if (!path.isEmpty()) {
                    changePath.setText(path);
                    setSPrefs(path);
                }
            }
        }
    }
}
