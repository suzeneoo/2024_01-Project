package com.example.chalkadoc.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chalkadoc.R;
import com.example.chalkadoc.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // 3초 후에 다른 화면으로 전환하기 위한 Handler 사용
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 전환할 화면의 Intent 생성
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent); // 다른 화면으로 전환
                finish(); // 현재 화면 종료
            }
        }, 3000); // 3초(3000밀리초) 후에 실행
    }
}