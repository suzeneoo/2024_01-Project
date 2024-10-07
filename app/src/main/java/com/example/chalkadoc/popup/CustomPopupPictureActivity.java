package com.example.chalkadoc.popup;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.chalkadoc.R;

public class CustomPopupPictureActivity extends Dialog {
    public TextView txt_contents;
    public Button btn_yes;
    public Button btn_no;
    private String contents;

    public CustomPopupPictureActivity(@NonNull Context context, String contents) {
        super(context);
        this.contents = contents;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_custom_popup_picture, null);
        setContentView(dialogView);

        txt_contents = dialogView.findViewById(R.id.txt_contents);
        btn_yes = dialogView.findViewById(R.id.btn_yes);
        btn_no = dialogView.findViewById(R.id.btn_no);

        txt_contents.setText(contents);

        btn_yes.setOnClickListener(v -> dismiss());
        btn_no.setOnClickListener(v -> dismiss());
    }

    public void show() {
        super.show();
    }
}
