package com.example.chalkadoc.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chalkadoc.R;

public class HospitalInfoActivity extends AppCompatActivity {
    private Button btn_reservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_info);

        TextView nameTextView = findViewById(R.id.partnership_name);
        TextView addressTextView = findViewById(R.id.partnership_address);
        TextView phoneTextView = findViewById(R.id.hospital_phone);
        TextView hoursTextView = findViewById(R.id.hospital_hours);  // 이 부분이 이미 있음
        TextView categoryTextView = findViewById(R.id.hospital_category);
        ImageView partnershipImageView = findViewById(R.id.partnership_image);

        init();

        Intent intent = getIntent();
        String name = intent.getStringExtra("hospital_name");
        String address = intent.getStringExtra("hospital_address");
        String phone = intent.getStringExtra("hospital_phone");
        String hours = intent.getStringExtra("hospital_hours");  // 여기서 데이터를 받아옴
        String category = intent.getStringExtra("hospital_category");
        String imageUrl = intent.getStringExtra("hospital_image_url");

        nameTextView.setText(name);
        addressTextView.setText(address);
        phoneTextView.setText(phone);
        hoursTextView.setText(hours);  // 데이터 설정
        categoryTextView.setText(category);

        // Glide를 사용하여 이미지 로드
        Glide.with(this)
                .load(imageUrl)
                .into(partnershipImageView);

        btn_reservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HospitalInfoActivity.this, "에약되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init(){
        btn_reservation = findViewById(R.id.btn_reservation);
    }
}