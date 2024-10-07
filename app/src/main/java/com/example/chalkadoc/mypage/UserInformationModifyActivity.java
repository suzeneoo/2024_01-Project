package com.example.chalkadoc.mypage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chalkadoc.R;

public class UserInformationModifyActivity extends AppCompatActivity {
    Button btn_info_modify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_imformation_modify);

        init();

        btn_info_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserInformationModifyActivity.this, "변경되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init(){
        btn_info_modify = findViewById(R.id.btn_info_modify);
    }
}