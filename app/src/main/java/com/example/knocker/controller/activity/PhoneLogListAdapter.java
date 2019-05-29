package com.example.knocker.controller.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.knocker.R;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;
import com.example.knocker.model.PhoneLog;

import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;

public class PhoneLogListAdapter extends BaseAdapter {
    private List<PhoneLog> listPhoneLogInfos;
    private LayoutInflater layoutInflater;
    private Context context;
    private Integer len;

    public PhoneLogListAdapter(Context context, List<PhoneLog> listPhoneLogInfos) {
        this.context = context;
        this.listPhoneLogInfos = listPhoneLogInfos;

        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listPhoneLogInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return listPhoneLogInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listview = convertView;
        ViewHolder holder;

        if (listview == null) {
            listview = layoutInflater.inflate(R.layout.list_phone_calls_log_item_layout, null);

            holder = new ViewHolder();
            holder.phoneLogRoundedImage = listview.findViewById(R.id.phone_log_calls_image_view);
            holder.phoneLogName = listview.findViewById(R.id.phone_log_calls_name);
            holder.phoneLogNumber = listview.findViewById(R.id.phone_log_calls_phone_number);
            holder.phoneLogDate = listview.findViewById(R.id.phone_log_calls_phone_date);
            holder.phoneLogDuration = listview.findViewById(R.id.phone_log_calls_phone_duration);
            holder.phoneLogType = listview.findViewById(R.id.phone_log_calls_phone_type);

            listview.setTag(holder);
        } else {
            holder = (ViewHolder) listview.getTag();
        }

        holder.phoneLogName.setText(listPhoneLogInfos.get(position).getName());
        holder.phoneLogNumber.setText(listPhoneLogInfos.get(position).getNum());
        holder.phoneLogDate.setText(listPhoneLogInfos.get(position).getDate());
        holder.phoneLogDuration.setText(listPhoneLogInfos.get(position).getDuration());
        holder.phoneLogType.setText(listPhoneLogInfos.get(position).getCall_type());

        return listview;
    }

    static class ViewHolder {
        ImageView phoneLogRoundedImage;
        TextView phoneLogName;
        TextView phoneLogNumber;
        TextView phoneLogDate;
        TextView phoneLogDuration;
        TextView phoneLogType;
    }
}
