package com.example.chalkadoc.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chalkadoc.R;
import com.example.chalkadoc.mypage.HospitalRecordActivity;
import com.example.chalkadoc.mypage.UserInformationModifyActivity;

public class MyPageFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_my_page,container,false);


        TextView textView = v.findViewById(R.id.hospitalrecord);

        textView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity().getApplicationContext(), HospitalRecordActivity.class);
                startActivity(intent);
            }
        });

        TextView textView2 = v.findViewById(R.id.myimformation);

        textView2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity().getApplicationContext(), UserInformationModifyActivity.class);
                startActivity(intent);
            }
        });

        return v;

    }

}