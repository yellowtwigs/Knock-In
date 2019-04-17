package com.example.firsttestknocker;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NotifAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<StatusBarParcelable> notifications;
    private final String TAG = NotificationListener.class.getSimpleName();
    private final String FACEBOOK_PACKAGE = "com.facebook.katana";
    private final String MESSENGER_PACKAGE = "com.facebook.orca";
    private final String WATHSAPP_SERVICE = "com.whatsapp";
    private final String GMAIL_PACKAGE = "com.google.android.gm";
    private final String  MESSAGE_PACKAGE = "";
    public NotifAdapter(Context context, ArrayList<StatusBarParcelable> listNotification){
        this.context=context;
        this.notifications= listNotification;
    }
    public int getCount() {
        return notifications.size();
    }

    @Override
    public StatusBarParcelable getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_notification_adapter,parent,false);

        }
        StatusBarParcelable sbp = getItem(position);
        TextView app= (TextView) convertView.findViewById(R.id.notification_plateformeTv);
        TextView contenue= (TextView) convertView.findViewById(R.id.notification_contenu);
        ImageView appImg= (ImageView) convertView.findViewById(R.id.notification_plateformeImg);
        ImageView expImg= (ImageView) convertView.findViewById(R.id.notification_expediteurImg);
        app.setText(convertPackageToString(sbp.getAppNotifier()));
        contenue.setText(sbp.getStatusBarNotificationInfo().get("android.title")+":"+sbp.getStatusBarNotificationInfo().get("android.text"));
        appImg.setImageResource(getApplicationNotifier(sbp));

        return convertView;
    }
    private String  convertPackageToString(String packageName){
        if(packageName.equals(FACEBOOK_PACKAGE)){
            return "Facebook";
        }else if(packageName.equals(MESSENGER_PACKAGE)){
            return "Messenger";
        }else if(packageName.equals(WATHSAPP_SERVICE)){
            return "WhatsApp";
        }else if(packageName.equals(GMAIL_PACKAGE)){
            return "gmail";
        }else if(packageName.equals(MESSAGE_PACKAGE)){
            return "message";
        }
        return "";
    }
    private int getApplicationNotifier(StatusBarParcelable sbp ){

        if (sbp.getAppNotifier().equals(FACEBOOK_PACKAGE) || sbp.getAppNotifier().equals(MESSENGER_PACKAGE)) {
            return R.drawable.facebook;
        } else if (sbp.getAppNotifier().equals(GMAIL_PACKAGE)) {
            return R.drawable.gmail;
        } else if (sbp.getAppNotifier().equals(WATHSAPP_SERVICE)) {
            return R.drawable.download;
        }
        return R.drawable.sms;
    }

}

