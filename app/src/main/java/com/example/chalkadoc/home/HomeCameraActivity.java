package com.example.chalkadoc.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chalkadoc.R;

public class HomeCameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_camera);

        Button cameraCheckbtn = findViewById(R.id.cameraCheckbtn);

        cameraCheckbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // 이동 페이지 추후 추가 예정
                Intent intent = new Intent(HomeCameraActivity.this, HomeCameraTakePictureActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
