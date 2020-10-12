package com.chenyou.noveleditor.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chenyou.noveleditor.R;

public class SplashActivity extends AppCompatActivity {

    private Button splash_btn;
    int i = 5;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0 && i >= 0) {
                splash_btn.setText("跳过" + i + "s");
                i--;
            } else {
                startMainActivity();
            }
            removeMessages(0);
            sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //初始化布局
        initview();
    }

    private void initview() {
        splash_btn = (Button) findViewById(R.id.splash_btn);
        splash_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
        handler.sendEmptyMessage(0);
    }

    /**
     * 进入主页面
     */
    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        //关闭启动页面
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除所有消息
        handler.removeCallbacksAndMessages(null);
    }
}
