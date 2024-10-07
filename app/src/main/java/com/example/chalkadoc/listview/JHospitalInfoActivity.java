package com.example.chalkadoc.listview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chalkadoc.R;
import com.example.chalkadoc.home.HospitalInfoActivity;

public class JHospitalInfoActivity extends AppCompatActivity {
    private TextView partnershipName;
    private TextView partnershipAddress;
    private TextView partnershipInfo;
    private TextView partnershipDetailsInfo;
    private String name;
    private String address;
    private String info;
    private String apply;
    private String phone;
    private String hours;  // 영업시간 변수 추가
    private String category;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partnership_hospital_info);

        partnershipName = findViewById(R.id.partnership_name);
        partnershipAddress = findViewById(R.id.partnership_address);
        partnershipInfo = findViewById(R.id.partnership_info);
        partnershipDetailsInfo = findViewById(R.id.partnership_details_info);
        Button bottomButton = findViewById(R.id.bottom_button);

        Intent intent = getIntent();
        name = intent.getStringExtra("hospital_name");
        address = intent.getStringExtra("hospital_address");
        info = intent.getStringExtra("partnership_info");
        apply = intent.getStringExtra("partnership_apply");
        phone = intent.getStringExtra("hospital_phone");
        hours = intent.getStringExtra("hospital_hours");  // 영업시간 정보 받기
        category = intent.getStringExtra("hospital_category");
        imageUrl = intent.getStringExtra("hospital_image_url");

        partnershipName.setText(name);
        partnershipAddress.setText(address);
        partnershipInfo.setText(info);
        partnershipDetailsInfo.setText(apply);

        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent hospitalInfoIntent = new Intent(JHospitalInfoActivity.this, HospitalInfoActivity.class);
                hospitalInfoIntent.putExtra("hospital_name", name);
                hospitalInfoIntent.putExtra("hospital_address", address);
                hospitalInfoIntent.putExtra("hospital_phone", phone);
                hospitalInfoIntent.putExtra("hospital_hours", hours);  // 영업시간 정보 추가
                hospitalInfoIntent.putExtra("hospital_category", category);
                hospitalInfoIntent.putExtra("hospital_image_url", imageUrl);
                hospitalInfoIntent.putExtra("partnership_info", info);
                hospitalInfoIntent.putExtra("partnership_apply", apply);
                startActivity(hospitalInfoIntent);
            }
        });
    }

}
