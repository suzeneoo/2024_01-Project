package com.example.chalkadoc.navigation;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.chalkadoc.R;
import com.example.chalkadoc.home.HomeCameraActivity;
import com.example.chalkadoc.partnership.PartnershipActivity;

public class HomeFragment extends Fragment {
    private ImageView iv_camera;
    private ImageView iv_partnership;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        iv_camera = view.findViewById(R.id.iv_camera);
        iv_partnership = view.findViewById(R.id.iv_partnership);

        // 사진 찍기 버튼 클릭
        iv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HomeCameraActivity.class);
                startActivity(intent);
            }
        });

        // 제휴 기업 버튼 클릭
        iv_partnership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PartnershipActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    // test test
}