package com.example.chalkadoc.navigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chalkadoc.R;
import com.example.chalkadoc.home.HomeCameraActivity;
import com.example.chalkadoc.mypage.UserInformationModifyActivity;
import com.example.chalkadoc.popup.CustomPopupPartnership;
import com.example.chalkadoc.popup.CustomPopupPictureActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private PartnershipFragment partnershipFragment;
    private MyPageFragment myPageFragment;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("popup_prefs", MODE_PRIVATE);

        init();
        // Firebase 스토리지의 촬영 사진 폴더 안에 사진이 없으면 커스텀 다이얼로그 표시
        checkIfPhotosExist();

        // 첫 번째 로그인 시에만 CustomPopupPartnership를 표시
//        if (isFirstLogin() && FirebaseAuth.getInstance().getCurrentUser() != null) {
//            showCustomPopup();
//        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (getSelectedMenu(item)) {
                    case 0:
                        Log.d("Error", "시나리오에 없는 메뉴버튼을 눌렀음");
                        return false;

                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, homeFragment)
                                .commit();
                        return true;

                    case 2:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, partnershipFragment)
                                .commit();
                        return true;

                    case 3:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, myPageFragment)
                                .commit();
                        return true;
                }
                return false;
            }
        });
    }

    private int getSelectedMenu(@NonNull MenuItem item) {
        int result = 0;

        if (item.getItemId() == R.id.home)
            result = 1;
        if (item.getItemId() == R.id.partnership)
            result = 2;
        if (item.getItemId() == R.id.mypage)
            result = 3;

        return result;
    }

    private void init() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        homeFragment = new HomeFragment();
        partnershipFragment = new PartnershipFragment();
        myPageFragment = new MyPageFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, homeFragment)
                .commit();
    }

    private void checkIfPhotosExist() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference photosRef = storage.getReference().child("촬영사진");
        photosRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // Firebase storage의 /촬영사진 경로에 사진이 없는 경우 -> 커스텀 다이얼로그 표시
                if (listResult.getItems().isEmpty()) {
                    showCustomDialog();
                }
            }
        });
    }

    private void showCustomDialog() {
        CustomPopupPictureActivity customPopupActivity = new CustomPopupPictureActivity(this, "촬영하여 기록했던 사진이 없습니다.") {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                btn_yes.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, HomeCameraActivity.class);
                    startActivity(intent);
                    finish();
                    dismiss();
                });
            }
        };
        customPopupActivity.show();
    }

    private boolean isFirstLogin() {
        return sharedPreferences.getBoolean("first_login", true);
    }

    private void setFirstLoginDone() {
        sharedPreferences.edit().putBoolean("first_login", false).apply();
    }

    private void showCustomPopup() {
        CustomPopupPartnership customPopupPartnership = new CustomPopupPartnership(MainActivity.this, "") {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                // 설정 및 레이아웃 초기화
                btn_yes.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, UserInformationModifyActivity.class);
                    startActivity(intent);
                    finish();
                    // 팝업 닫기
                    dismiss();
                });
            }
        };
        customPopupPartnership.show();

        // 첫 번째 로그인 처리 완료로 설정
        setFirstLoginDone();
    }
}
