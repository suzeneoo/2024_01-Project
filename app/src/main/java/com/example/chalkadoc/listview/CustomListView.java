package com.example.chalkadoc.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chalkadoc.R;

import java.util.ArrayList;

public class CustomListView extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<ListData> listViewData;
    private int count;
    private Context context;

    public CustomListView(ArrayList<ListData> listData, Context context) {
        this.listViewData = listData;
        this.count = listViewData.size();
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return listViewData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.customlistview, parent, false);
        }

        TextView title = convertView.findViewById(R.id.partnership_name);
        TextView body_1 = convertView.findViewById(R.id.partnership_address);
        TextView body_2 = convertView.findViewById(R.id.partnership_info);


        ListData listData = listViewData.get(position);

        title.setText(listData.title);
        body_1.setText(listData.body_1);
        body_2.setText(listData.body_2);

        // Glide를 사용하여 이미지 로드
//        Glide.with(context)
//                .load(listData.imageUrl)
//                .placeholder(R.drawable.ic_run_24) // 이미지가 로드되는 동안 표시할 플레이스홀더 이미지
//                .error(R.drawable.ic_error_outline_24) // 이미지 로드 실패 시 표시할 이미지
//                .into(mainImage);

        return convertView;
    }
}
