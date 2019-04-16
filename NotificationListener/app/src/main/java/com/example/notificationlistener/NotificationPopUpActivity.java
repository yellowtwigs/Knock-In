package com.example.notificationlistener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class NotificationPopUpActivity extends Activity {
    TextView expediteur;
    TextView message;
    ImageView imgExp;
    ImageView imgPlat;
    ConstraintLayout fond;
    public static final String FACEBOOK_PACKAGE = "com.facebook.katana";
    public static final String MESSENGER_PACKAGE = "com.facebook.orca";
    public static final String WATHSAPP_SERVICE = "com.whatsapp";
    public static final String GMAIL_PACKAGE = "com.google.android.gm";
    public static final String MESSAGE_PACKAGE = "";

    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onPopUp");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_notification_pop_up);
        Intent intent = getIntent();
        StatusBarParcelable sbp = intent.getParcelableExtra("statusBar");
        expediteur = (TextView) findViewById(R.id.expediteur2);
        message = (TextView) findViewById(R.id.txtView2);
        imgExp = (ImageView) findViewById(R.id.imageView);
        imgPlat = (ImageView) findViewById(R.id.imageView3);
        fond = (ConstraintLayout) findViewById(R.id.constraintLayout);
        expediteur.setText(sbp.getStatusBarNotificationInfo().get("android.title") + "");
        message.setText(sbp.getStatusBarNotificationInfo().get("android.text") + "");

        imgPlat.setImageResource(getApplicationNotifier(sbp));


        fond.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    private int getApplicationNotifier(StatusBarParcelable sbp) {

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
