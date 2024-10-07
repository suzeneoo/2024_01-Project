package com.example.chalkadoc.navigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.chalkadoc.R;
import com.example.chalkadoc.home.HospitalInfoActivity;
import com.example.chalkadoc.listview.EyesData;
import com.example.chalkadoc.listview.HospitalAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
public class PartnershipFragment extends Fragment {
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_partnership, container, false);

        listView = view.findViewById(R.id.listview);
        ArrayList<EyesData> eyesDataList = new ArrayList<>();

        // res/raw 폴더에서 JSON 파일을 읽습니다.
        String jsonData = loadJSONFromRaw(getContext(), R.raw.jhospitals);

        try {
            // JSON 배열을 파싱합니다.
            JSONArray jsonArray = new JSONArray(jsonData);

            // JSON 배열의 각 항목을 처리합니다.
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // EyesData 객체를 생성하고 JSON 데이터를 사용하여 초기화합니다.
                EyesData eyesData = new EyesData();
                eyesData.set이름(jsonObject.getString("이름"));
                eyesData.set카테고리(jsonObject.getString("카테고리"));
                eyesData.set주소(jsonObject.getString("주소"));
                eyesData.set일반전화(jsonObject.getString("일반전화"));
                eyesData.set영업시간(jsonObject.getString("영업시간"));
                eyesData.set방문자_리뷰수(jsonObject.optString("방문자_리뷰수"));
                eyesData.setPartnered(jsonObject.optBoolean("제휴병원", true));

                // 리스트에 추가합니다.
                eyesDataList.add(eyesData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HospitalAdapter hospitalAdapter = new HospitalAdapter(getContext(), eyesDataList);
        listView.setAdapter(hospitalAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EyesData clickedItem = eyesDataList.get(position);
                String clickedItemName = clickedItem.get이름();
                String clickedItemCategory = clickedItem.get카테고리();
                String clickedItemAddress = clickedItem.get주소();
                String clickedItemPhone = clickedItem.get일반전화();
                String clickedItemhours = clickedItem.get영업시간();
                boolean isPartnered = clickedItem.isPartnered();

                Log.d("Check", "Name: " + clickedItemName);

                // HospitalInfoActivity로 이동
                Intent intent = new Intent(getActivity(), HospitalInfoActivity.class);
                intent.putExtra("hospital_name", clickedItemName);
                intent.putExtra("hospital_category", clickedItemCategory);
                intent.putExtra("hospital_address", clickedItemAddress);
                intent.putExtra("is_partnered", isPartnered);
                intent.putExtra("hospital_phone", clickedItemPhone);
                intent.putExtra("hospital_hours", clickedItemhours);
                startActivity(intent);
            }
        });

        return view;
    }

    // res/raw 폴더에서 JSON 파일을 읽는 메소드입니다.
    private String loadJSONFromRaw(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }
}


