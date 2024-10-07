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

public class HomeCameraResultActivity_1 extends AppCompatActivity {

    private ImageView imageView;
    private TextView resultTextView;
    private TextView rTview;
    private Interpreter tflite;
    private TextView tv_goToMap;

    private static final String TAG = "HomeCameraResultActivity_1";

    private String highestConfidenceEyeDiseaseLabel;
    private float highestConfidenceEyeDiseasePercent;
    private String highestConfidenceSkinDiseaseLabel;
    private float highestConfidenceSkinDiseasePercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_camera_result_1);

        imageView = findViewById(R.id.iv_camera);
        resultTextView = findViewById(R.id.tv_eyes_result);
        rTview = findViewById(R.id.tv_skin_result);
        tv_goToMap = findViewById(R.id.tv_goToMap);

        tv_goToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeCameraResultActivity_1.this, PartnershipActivity.class);
                // 예측된 안구 질환이 있으면 PartnershipActivity에 startValue 1을 전달하고, 화면을 전환
                if(!highestConfidenceEyeDiseaseLabel.equals("결과 없음")){
                    intent.putExtra("startValue", 1);
                    startActivity(intent);
                    finish();
                }
                // 예측된 피부 질환이 있으면 PartnershipActivity에 startValue 2을 전달하고, 화면을 전환
                else if(!highestConfidenceSkinDiseaseLabel.equals("결과 없음")){
                    intent.putExtra("startValue", 2);
                    startActivity(intent);
                    finish();
                }
                // 예측된 질환이 없으면, 토스트 메시지 출력
                else{
                    Toast.makeText(HomeCameraResultActivity_1.this, "예측된 질환이 없습니다.", Toast.LENGTH_SHORT).show();
                }
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

                        analyzeImage(bitmap);
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

        TextView nextButton = findViewById(R.id.tv_detailResult);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사진을 DentalActivity로 전달하는 메서드 호출
                sendImageToDentalActivity();
            }
        });
    }

    // 사진을 DentalActivity로 전달하는 메서드
    private void sendImageToDentalActivity() {
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);

            // DentalActivity로 전달할 Intent 생성
            Intent intent = new Intent(HomeCameraResultActivity_1.this, HomeCameraResultActivity_2.class);
            intent.putExtra("imageUri", imageUri.toString());
            intent.putExtra("eyeDiseaseLabel", highestConfidenceEyeDiseaseLabel);
            intent.putExtra("eyeDiseaseConfidence", highestConfidenceEyeDiseasePercent);
            intent.putExtra("skinDiseaseLabel", highestConfidenceSkinDiseaseLabel);
            intent.putExtra("skinDiseaseConfidence", highestConfidenceSkinDiseasePercent);

            // DentalActivity 실행
            startActivity(intent);
        } else {
            // 이미지 URI가 없는 경우 처리
            Toast.makeText(HomeCameraResultActivity_1.this, "이미지 데이터가 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private MappedByteBuffer loadModelFile() throws Exception {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("best-fp16.tflite");
        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private void analyzeImage(Bitmap bitmap) {
        try {
            // 이미지 리사이즈
            int inputImageWidth = 320;
            int inputImageHeight = 320;
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
            float[][][] output = new float[1][6300][14];

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

            String[] eyeDiseaseLabels = {"백내장", "포도막염", "익상편", "다래끼"};
            String[] skinDiseaseLabels = {"블랙헤드", "면포성여드름", "습진", "농포성여드름", "주사"};

            // 최종 결과 저장할 변수
            highestConfidenceEyeDiseaseLabel = null;
            highestConfidenceEyeDiseasePercent = -0.1f;
            float[] highestConfidenceEyeDiseaseBox = null;

            highestConfidenceSkinDiseaseLabel = null;
            highestConfidenceSkinDiseasePercent = -0.1f;
            float[] highestConfidenceSkinDiseaseBox = null;

            // 임계값 설정
            float threshold = 0.3f;

            // 결과 표시용 문자열 생성
            for (int i = 0; i < 6300; i++) {
                float objectProbability = output[0][i][4]; // 객체 확률
                if (objectProbability >= threshold) {
                    // 클래스 확률과 경계 상자 좌표 추출
                    int classIndex = -1;
                    float maxClassProbability = -1.0f;
                    for (int j = 5; j < 14; j++) {
                        float classProbability = output[0][i][j];
                        if (classProbability > maxClassProbability) {
                            maxClassProbability = classProbability;
                            classIndex = j - 5; // 클래스 인덱스 조정
                        }
                    }

                    // 결과에 추가
                    if (classIndex != -1) {
                        float confidencePercent = (float) (objectProbability * 100);
                        if (classIndex < 4) { // Eye disease
                            if (confidencePercent > highestConfidenceEyeDiseasePercent) {
                                highestConfidenceEyeDiseasePercent = confidencePercent;
                                highestConfidenceEyeDiseaseLabel = eyeDiseaseLabels[classIndex];
                                highestConfidenceEyeDiseaseBox = new float[]{
                                        output[0][i][0] * inputImageWidth,
                                        output[0][i][1] * inputImageHeight,
                                        output[0][i][2] * inputImageWidth,
                                        output[0][i][3] * inputImageHeight
                                };
                            }
                        } else { // Skin disease
                            if (confidencePercent > highestConfidenceSkinDiseasePercent) {
                                highestConfidenceSkinDiseasePercent = confidencePercent;
                                highestConfidenceSkinDiseaseLabel = skinDiseaseLabels[classIndex - 4];
                                highestConfidenceSkinDiseaseBox = new float[]{
                                        output[0][i][0] * inputImageWidth,
                                        output[0][i][1] * inputImageHeight,
                                        output[0][i][2] * inputImageWidth,
                                        output[0][i][3] * inputImageHeight
                                };
                            }
                        }
                    }
                }
            }

            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            // 최종 결과 그리기
            if (highestConfidenceEyeDiseaseLabel != null) {
                float centerX = highestConfidenceEyeDiseaseBox[0];
                float centerY = highestConfidenceEyeDiseaseBox[1];
                float width = highestConfidenceEyeDiseaseBox[2];
                float height = highestConfidenceEyeDiseaseBox[3];

                float left = centerX - (width / 2);
                float top = centerY - (height / 2);
                float right = centerX + (width / 2);
                float bottom = centerY + (height / 2);

                // 경계 상자와 클래스 이름 그리기
                canvas.drawRect(left, top, right, bottom, paint);
                canvas.drawText(highestConfidenceEyeDiseaseLabel, left, top - 10, textPaint);
            } else {
                highestConfidenceEyeDiseaseLabel = "결과 없음";
            }

            if (highestConfidenceSkinDiseaseLabel != null) {
                float centerX = highestConfidenceSkinDiseaseBox[0];
                float centerY = highestConfidenceSkinDiseaseBox[1];
                float width = highestConfidenceSkinDiseaseBox[2];
                float height = highestConfidenceSkinDiseaseBox[3];

                float left = centerX - (width / 2);
                float top = centerY - (height / 2);
                float right = centerX + (width / 2);
                float bottom = centerY + (width / 2);

                // 경계 상자와 클래스 이름 그리기
                canvas.drawRect(left, top, right, bottom, paint);
                canvas.drawText(highestConfidenceSkinDiseaseLabel, left, top - 10, textPaint);
            } else {
                highestConfidenceSkinDiseaseLabel = "결과 없음";
            }

            imageView.setImageBitmap(mutableBitmap);
            resultTextView.setText(highestConfidenceEyeDiseaseLabel + " (정확도: " + decimalFormat.format(highestConfidenceEyeDiseasePercent) + "%)");
            rTview.setText(highestConfidenceSkinDiseaseLabel + " (정확도: " + decimalFormat.format(highestConfidenceSkinDiseasePercent) + "%)");
        } catch (Exception e) {
            Log.e(TAG, "TensorFlow Lite 모델 실행 실패", e);
            resultTextView.setText("분석 실패");
            rTview.setText("분석 실패");
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
