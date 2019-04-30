package com.example.firsttestknocker;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.Resource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class NotifAdapter extends BaseAdapter {
    private Context context;
    private View view;
    private ArrayList<StatusBarParcelable> notifications;
    private WindowManager windowManager;
    private final String TAG = NotificationListener.class.getSimpleName();
    private  ContactsRoomDatabase notification_listener_ContactsDatabase ;
    private  DbWorkerThread notification_listener_mDbWorkerThread ;
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
        notification_listener_mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
        notification_listener_mDbWorkerThread.start();
        //on get la base de données
        //notification_listener_ContactsDatabase = ContactsRoomDatabase.getDatabase(context);
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
        final Button buttonResponse = (Button) convertView.findViewById(R.id.notification_adapter_button_response);
        final Button buttonSend = (Button) convertView.findViewById(R.id.notification_adapter_button_send);
        final EditText editText = (EditText) convertView.findViewById(R.id.notification_adapter_editText);

        app.setText(convertPackageToString(sbp.getAppNotifier()));
        contenue.setText(sbp.getStatusBarNotificationInfo().get("android.title")+":"+sbp.getStatusBarNotificationInfo().get("android.text"));
        //appImg.setImageResource(getApplicationNotifier(sbp));

        String pckg= sbp.getAppNotifier();
        if(sbp.getStatusBarNotificationInfo().get("android.icon")!=null) {
            int iconID = Integer.parseInt(sbp.getStatusBarNotificationInfo().get("android.icon").toString());
        }
        try {
            PackageManager pckManager = context.getPackageManager();
            Drawable icon = pckManager.getApplicationIcon(sbp.getAppNotifier());
            appImg.setImageDrawable(icon);
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(!sbp.getStatusBarNotificationInfo().get("android.largeIcon").equals("")) {//image de l'expediteur provenant l'application source
            System.out.println("bitmap :"+sbp.getStatusBarNotificationInfo().get("android.largeIcon"));
            Bitmap bitmap = (Bitmap) sbp.getStatusBarNotificationInfo().get("android.largeIcon");
            expImg.setImageBitmap(bitmap);
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("click on constraint layout");
                String app = convertPackageToString(sbp.getAppNotifier());
                if (v.getId() == R.id.notification_adapter_button_response) {
                    System.out.println("click on button response");
                    buttonResponse.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    editText.requestFocus();
                    InputMethodManager inputMM= (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMM.showSoftInput(editText,InputMethodManager.SHOW_IMPLICIT);
                } else if (v.getId() == R.id.notification_adapter_button_send){
                    System.out.println("click on button send");

                }else{

                    if (app.equals("Facebook")) {
                        gotToFacebookPage("");
                        closeNotification();
                    } else if (app.equals("Messenger")) {
                        gotToFacebookPage("");
                        closeNotification();
                    } else if (app.equals("WhatsApp")) {
                        onWhatsappClick();
                        closeNotification();
                    } else if (app.equals("gmail")) {
                        openGmail();
                        closeNotification();
                    } else if (app.equals("message")) {
                        openSms();
                        closeNotification();
                    }
                }


            }
        };

        contenue.setOnClickListener(listener);
        app.setOnClickListener(listener);
        app.setOnClickListener(listener);
        buttonResponse.setOnClickListener(listener);
        buttonSend.setOnClickListener(listener);
        return convertView;
    }
    private void closeNotification(){
        windowManager.removeView(view);
        SharedPreferences sharedPreferences = context.getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit= sharedPreferences.edit();
        edit.putBoolean("view",false);
        edit.commit();
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

