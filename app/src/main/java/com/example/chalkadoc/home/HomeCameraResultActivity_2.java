package com.example.chalkadoc.home;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chalkadoc.R;
import com.example.chalkadoc.partnership.PartnershipActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

public class HomeCameraResultActivity_2 extends AppCompatActivity {
    private ImageView imageView;
    private TextView resultTextView;
    private Interpreter tflite;
    private TextView tv_detailPage;
    private TextView tv_goToMap;
    public String resultToNextPage;
    private static final String TAG = "HomeCameraResultAcitivy_2";
    private String eyeDiseaseLabel;
    private float eyeDiseaseConfidence;
    private String skinDiseaseLabel;
    private float skinDiseaseConfidence;
    private String dentalDiseaseLabel;
    private float dentalDiseaseConfidence;
    private int classIndex = -1;
    private float maxClassProbability = -1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_camera_result_2);

        imageView = findViewById(R.id.iv_camera);
        resultTextView = findViewById(R.id.tv_teeth_result);
        tv_detailPage = findViewById(R.id.tv_detailResult);
        tv_goToMap = findViewById(R.id.tv_goToMap);

        tv_goToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 예측된 구강 질환이 있으면 startValue 3을 PartnershipActivity에 전달하고 화면이 전환
                if(!dentalDiseaseLabel.equals("질병 없음")){
                    Intent intent = new Intent(HomeCameraResultActivity_2.this, PartnershipActivity.class);
                    intent.putExtra("startValue", 3);
                    startActivity(intent);
                    finish();
                }
                // 예측된 질병이 없으면 토스트 메시지 출력
                else{
                    Toast.makeText(HomeCameraResultActivity_2.this, "예측된 질환이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_detailPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImageToAllResultActivity();
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

                        tflite = new Interpreter(loadModelFile());
                        Log.d(TAG, "TensorFlow Lite 모델 로드 성공");

                        String result = analyzeImage(bitmap);
                        resultTextView.setText(result);
                        resultToNextPage = result;
                    } else {
                        throw new Exception("InputStream is null");
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

        eyeDiseaseLabel = getIntent().getStringExtra("eyeDiseaseLabel");
        eyeDiseaseConfidence = getIntent().getFloatExtra("eyeDiseaseConfidence", 0);
        skinDiseaseLabel = getIntent().getStringExtra("skinDiseaseLabel");
        skinDiseaseConfidence = getIntent().getFloatExtra("skinDiseaseConfidence", 0);
    }

    private MappedByteBuffer loadModelFile() throws Exception {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("dentalbest-fp16.tflite");
        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private String analyzeImage(Bitmap bitmap) {
        try {
            // 이미지 리사이즈
            int inputImageWidth = 640;
            int inputImageHeight = 640;
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true);

            // 입력 ByteBuffer 생성
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * inputImageWidth * inputImageHeight * 3);
            inputBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[inputImageWidth * inputImageHeight];
            resizedBitmap.getPixels(intValues, 0, inputImageWidth, 0, 0, inputImageWidth, inputImageHeight);

            for (int i = 0; i < intValues.length; ++i) {
                int val = intValues[i];
                inputBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
                inputBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
                inputBuffer.putFloat((val & 0xFF) / 255.0f);
            }

            // 모델의 출력 텐서 형상에 맞게 출력 배열 생성
            float[][][] output = new float[1][25200][10];

            // 모델 실행
            tflite.run(inputBuffer, output);
            Log.d(TAG, "TensorFlow Lite 모델 실행 성공");

            // Bitmap을 Canvas에 그리기 위한 준비
            Bitmap mutableBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(24);

            String[] labels = {"치은염", "치석", "충치", "부정교합", "치아착색"};

            // 결과를 저장할 변수
            String result = "분석 실패";

            // 가장 높은 확률의 객체 찾기
            int maxIndex = -1;
            float maxProbability = -1.0f;

            for (int i = 0; i < 25200; i++) {
                float objectProbability = output[0][i][4]; // 객체 확률
                if (objectProbability > maxProbability) {
                    maxProbability = objectProbability;
                    maxIndex = i;
                }
            }

            // 임계값 설정
            float threshold = 0.5f;

            if (maxIndex != -1 && maxProbability >= threshold) {
                // 가장 높은 확률을 가진 객체의 클래스 정보 추출
                for (int j = 5; j < 10; j++) {
                    float classProbability = output[0][maxIndex][j];
                    if (classProbability > maxClassProbability) {
                        maxClassProbability = classProbability;
                        classIndex = j - 5; // 클래스 인덱스를 맞추기 위해 5를 뺍니다.
                    }
                }

                if (classIndex != -1) {
                    // 경계 상자 좌표 추출
                    float centerX = output[0][maxIndex][0] * inputImageWidth;
                    float centerY = output[0][maxIndex][1] * inputImageHeight;
                    float width = output[0][maxIndex][2] * inputImageWidth;
                    float height = output[0][maxIndex][3] * inputImageHeight;

                    float left = centerX - (width / 2);
                    float top = centerY - (height / 2);
                    float right = centerX + (width / 2);
                    float bottom = centerY + (height / 2);

                    // 경계 상자 그리기
                    canvas.drawRect(left, top, right, bottom, paint);

                    // 클래스 이름 그리기
                    canvas.drawText(labels[classIndex], left, top - 10, textPaint);
                    dentalDiseaseLabel = labels[classIndex];
                    dentalDiseaseConfidence = maxClassProbability * 100;  // float 값을 퍼센트로 변환
                }
            } else {
                // 임계값 이상의 객체가 없는 경우
                dentalDiseaseLabel = "결과 없음.";
                dentalDiseaseConfidence = -0.1f;  // 결과가 없는 경우 confidence를 0으로 설정
            }

            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            result = dentalDiseaseLabel + " (" + decimalFormat.format(dentalDiseaseConfidence) + "%)";

            // 결과 반환
            imageView.setImageBitmap(mutableBitmap);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "TensorFlow Lite 모델 실행 실패", e);
            return "분석 실패";
        }
    }

    private void sendImageToAllResultActivity() {
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);

            // DentalActivity로 전달할 Intent 생성
            Intent intent = new Intent(HomeCameraResultActivity_2.this, HomeCameraDetailResultActivity.class);
            intent.putExtra("imageUri", imageUri.toString());
            intent.putExtra("eyeDiseaseLabel", eyeDiseaseLabel);
            intent.putExtra("eyeDiseaseConfidence", eyeDiseaseConfidence);
            intent.putExtra("skinDiseaseLabel", skinDiseaseLabel);
            intent.putExtra("skinDiseaseConfidence", skinDiseaseConfidence);
            intent.putExtra("dentalDiseaseLabel", dentalDiseaseLabel);
            intent.putExtra("dentalDiseaseConfidence", dentalDiseaseConfidence);

            // DentalActivity 실행
            startActivity(intent);
            finish();
        } else {
            // 이미지 URI가 없는 경우 처리
            Toast.makeText(HomeCameraResultActivity_2.this, "이미지 데이터가 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}
