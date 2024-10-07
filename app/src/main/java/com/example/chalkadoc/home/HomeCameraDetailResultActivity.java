package com.example.chalkadoc.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chalkadoc.R;
import com.example.chalkadoc.partnership.PartnershipActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeCameraDetailResultActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView tv_goToMap;
    private TextView resultTextView;
    private TextView eyeResultTextView;
    private TextView skinResultTextView;
    private TextView teethResultTextView;
    private TextView symptomResultTextView;

    private String eyeDiseaseLabel;
    private float eyeDiseaseConfidence;
    private String skinDiseaseLabel;
    private float skinDiseaseConfidence;
    private String dentalDiseaseLabel;
    private float dentalDiseaseConfidence;

    private static final String TAG = "AllResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_camera_detail_result);

        imageView = findViewById(R.id.iv_camera);
        eyeResultTextView = findViewById(R.id.tv_eyes_result);
        skinResultTextView = findViewById(R.id.tv_skin_result);
        teethResultTextView = findViewById(R.id.tv_teeth_result);
        symptomResultTextView = findViewById(R.id.tv_symptom);

        init();

        // "예측 결과 관련 병원보기" 클릭
        tv_goToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeCameraDetailResultActivity.this, PartnershipActivity.class);
                startActivity(intent);
                finish();
            }
        });

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                Log.d(TAG, "Received imageUri: " + imageUri.toString());

                try (InputStream is = getContentResolver().openInputStream(imageUri)) {
                    if (is != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageView.setImageBitmap(bitmap);
                        Log.d(TAG, "Image received and set successfully");

                    } else {
                        throw new Exception("InputStream이 null입니다.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultTextView.setText("이미지 로드 실패");
                Log.e(TAG, "이미지 로드 실패", e);
            }
        } else {
            resultTextView.setText("이미지 데이터가 없습니다");
            Log.e(TAG, "이미지 URI가 없습니다");
        }

        // 진단 결과 수신
        eyeDiseaseLabel = getIntent().getStringExtra("eyeDiseaseLabel");
        eyeDiseaseConfidence = getIntent().getFloatExtra("eyeDiseaseConfidence", 0);
        skinDiseaseLabel = getIntent().getStringExtra("skinDiseaseLabel");
        skinDiseaseConfidence = getIntent().getFloatExtra("skinDiseaseConfidence", 0);
        dentalDiseaseLabel = getIntent().getStringExtra("dentalDiseaseLabel");
        dentalDiseaseConfidence = getIntent().getFloatExtra("dentalDiseaseConfidence", 0);

        // 수신한 데이터 로그 출력
        Log.d(TAG, "Eye Disease: " + eyeDiseaseLabel + " (Confidence: " + eyeDiseaseConfidence + "%)");
        Log.d(TAG, "Skin Disease: " + skinDiseaseLabel + " (Confidence: " + skinDiseaseConfidence + "%)");
        Log.d(TAG, "Dental Disease: " + dentalDiseaseLabel + " (Confidence: " + dentalDiseaseConfidence + "%)");

        // DecimalFormat을 사용하여 소수점 둘째 자리까지 표시
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        // 진단 결과 설정
        eyeResultTextView.setText(String.format("%s (정확도: %s%%)", eyeDiseaseLabel, decimalFormat.format(eyeDiseaseConfidence)));
        skinResultTextView.setText(String.format("%s (정확도: %s%%)", skinDiseaseLabel, decimalFormat.format(skinDiseaseConfidence)));
        teethResultTextView.setText(String.format("%s (정확도: %s%%)", dentalDiseaseLabel, decimalFormat.format(dentalDiseaseConfidence)));

        // JSON 로드 및 파싱
        String json = loadJSONFromAsset();
        List<Disease> diseases = parseDiseasesJSON(json);

        // 진단된 질병에 대한 증상 찾기 및 표시
        StringBuilder symptomText = new StringBuilder();
        for (Disease disease : diseases) {
            if (disease.getName().equals(eyeDiseaseLabel)) {
                symptomText.append("안구 질환: ").append(eyeDiseaseLabel).append("\n증상: ").append(disease.getSymptom()).append("\n\n");
            }
            if (disease.getName().equals(skinDiseaseLabel)) {
                symptomText.append("피부 질환: ").append(skinDiseaseLabel).append("\n증상: ").append(disease.getSymptom()).append("\n\n");
            }
            if (disease.getName().equals(dentalDiseaseLabel)) {
                symptomText.append("구강 질환: ").append(dentalDiseaseLabel).append("\n증상: ").append(disease.getSymptom()).append("\n\n");
            }
        }
        symptomResultTextView.setText(symptomText.toString());
    }

    // 파일 또는 assets에서 JSON 로드하는 메소드
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.disease_info); // 파일 이름이 정확한지 확인
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            Log.d(TAG, "JSON 로드 성공: " + json); // JSON 로드 성공 여부 로그 출력
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "JSON 로드 실패", ex); // JSON 로드 실패 로그 출력
            return null;
        }
        return json;
    }

    // JSON을 Disease 객체 리스트로 파싱하는 메소드
    private List<Disease> parseDiseasesJSON(String json) {
        List<Disease> diseases = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("질병명");
                String symptom = obj.getString("증상");
                diseases.add(new Disease(name, symptom));
                Log.d(TAG, "질병명: " + name + ", 증상: " + symptom); // 파싱된 데이터 로그 출력
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "JSON 파싱 실패", e); // JSON 파싱 실패 로그 출력
        }
        return diseases;
    }

    // Disease 클래스 정의
    private static class Disease {
        private final String name;
        private final String symptom;

        public Disease(String name, String symptom) {
            this.name = name;
            this.symptom = symptom;
        }

        public String getName() {
            return name;
        }

        public String getSymptom() {
            return symptom;
        }
    }

    // 초기화 메소드
    private void init() {
        tv_goToMap = findViewById(R.id.tv_goToMap);
    }
}
