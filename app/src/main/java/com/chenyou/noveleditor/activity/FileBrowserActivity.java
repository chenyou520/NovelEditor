package com.chenyou.noveleditor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chenyou.noveleditor.R;
import com.chenyou.noveleditor.adapter.StorageAdapter;

import java.io.File;

public class FileBrowserActivity extends Activity implements View.OnClickListener, StorageAdapter.FileSelectListener {


    private static final String TAG = FileBrowserActivity.class.getSimpleName();
    //根目录
    private String rootPath = "";
    //初始化进入的目录，默认目录
    private String filePath = "";
    private StorageAdapter listAdapter;

    //当前目录路径显示
    private TextView curPathTextView;
    //取消
    private Button btnCancel;
    //确定
    private Button btnSure;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        initView();

        //根目录
        rootPath = getIntent().getStringExtra("rootPath");
        Log.i(TAG, rootPath);
        //指定文件夹
        filePath = getIntent().getStringExtra("path");

        curPathTextView.setText(filePath);
        filePath = filePath.isEmpty() ? rootPath : filePath;
        View layoutFileSelectList = findViewById(R.id.layoutFileSelectList);
        //设置适配器
        listAdapter = new StorageAdapter(layoutFileSelectList, rootPath, filePath);
        listAdapter.setOnFileSelectListener(this);
    }

    /**
     * 初始化布局
     */
    private void initView() {
        curPathTextView = (TextView) findViewById(R.id.curPath);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSure = (Button) findViewById(R.id.btnSure);

        btnCancel.setOnClickListener(this);
        btnSure.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSure:
                finish();
                break;

            case R.id.btnCancel:
                filePath = "";
                finish();
                break;

            default:
                break;
        }
    }

    @Override
    public void onFileSelect(File selectedFile) {
        filePath = selectedFile.getPath();
    }

    @Override
    public void onDirSelect(File selectedDir) {
        filePath = selectedDir.getPath();
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("file", filePath);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
