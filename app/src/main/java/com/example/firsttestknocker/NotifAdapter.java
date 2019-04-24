package com.example.firsttestknocker;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class NotifAdapter extends BaseAdapter {
    private Context context;
    private View view;
    private ArrayList<StatusBarParcelable> notifications;
    private WindowManager windowManager;
    private final String TAG = NotificationListener.class.getSimpleName();
    private final String FACEBOOK_PACKAGE = "com.facebook.katana";
    private final String MESSENGER_PACKAGE = "com.facebook.orca";
    private final String WATHSAPP_SERVICE = "com.whatsapp";
    private final String GMAIL_PACKAGE = "com.google.android.gm";
    private final String  MESSAGE_PACKAGE = "com.google.android.apps.messaging";

    public NotifAdapter(Context context, ArrayList<StatusBarParcelable> listNotification, WindowManager windowManager,View view){
        this.context=context;
        this.notifications= listNotification;
        this.windowManager=windowManager;
        this.view= view;
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
    public View getView(int position, View convertView, final ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_notification_adapter,parent,false);
        }
        final StatusBarParcelable sbp = getItem(position);
        TextView app= (TextView) convertView.findViewById(R.id.notification_plateformeTv);
        TextView contenue= (TextView) convertView.findViewById(R.id.notification_contenu);
        ImageView appImg= (ImageView) convertView.findViewById(R.id.notification_plateformeImg);
        ImageView expImg= (ImageView) convertView.findViewById(R.id.notification_expediteurImg);
        app.setText(convertPackageToString(sbp.getAppNotifier()));
        contenue.setText(sbp.getStatusBarNotificationInfo().get("android.title")+":"+sbp.getStatusBarNotificationInfo().get("android.text"));
        appImg.setImageResource(getApplicationNotifier(sbp));
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("click on constraint layout");
                String app=convertPackageToString(sbp.getAppNotifier());
                if(app.equals("Facebook")){
                    gotToFacebookPage("");
                }else if(app.equals("Messenger"))
                {
                    gotToFacebookPage("");
                }else if(app.equals("WhatsApp")){

                    Log.i(TAG,"test in whatsapp if");
                    onWhatsappClick();
                }else if(app.equals("gmail")){
                    openGmail();
                }else if(app.equals("messeage")) {
                    openSms();
                }

                windowManager.removeView(view);
                SharedPreferences sharedPreferences = context.getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit= sharedPreferences.edit();
                edit.putBoolean("view",false);
                edit.commit();
            }
        };

        contenue.setOnClickListener(listener);
        app.setOnClickListener(listener);
        app.setOnClickListener(listener);
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


/////****** code dupliqué faire attention trouvé un moyen de ne plus en avoir *******//////
    private void gotToFacebookPage(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id));
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id));
            context.startActivity(intent);
        }
    }


    private void onWhatsappClick(){
        //String url = "https://api.whatsapp.com/";
        // try{
            //PackageManager pm = context.getPackageManager();
            //pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            Intent i = context.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
            i.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        /*} catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }*/

    }
    private void openGmail(){
        Intent i = context.getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
        i.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
    private void openSms(){
        Intent i = context.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.messaging");
        i.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}

