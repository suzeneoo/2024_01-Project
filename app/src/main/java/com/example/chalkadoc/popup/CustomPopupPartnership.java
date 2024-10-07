package com.example.chalkadoc.popup;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.chalkadoc.R;
import com.example.chalkadoc.mypage.UserInformationModifyActivity;

public class CustomPopupPartnership extends Dialog {

    public TextView txt_contents;
    public Button btn_yes;
    public Button btn_no;
    private String contents;

    public CustomPopupPartnership(@NonNull Context context, String contents) {
        super(context);
        this.contents = contents;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_custom_popup_partnership, null);
        setContentView(dialogView);

        txt_contents = dialogView.findViewById(R.id.txt_contents);
        btn_yes = dialogView.findViewById(R.id.btn_yes);
        btn_no = dialogView.findViewById(R.id.btn_no);

        txt_contents.setText(contents);

        btn_yes.setOnClickListener(v -> dismiss());
        btn_no.setOnClickListener(v -> dismiss());
    }

    public void setYesButtonClickListener(View.OnClickListener onClickListener) {
        if (btn_yes != null) {
            btn_yes.setOnClickListener(onClickListener);
        }
    }

    @Override
    public void show() {
        super.show();
    }
}
