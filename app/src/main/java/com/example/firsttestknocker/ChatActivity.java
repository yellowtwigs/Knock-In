package com.example.firsttestknocker;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {

    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
    ImageView messenger;
    ImageView instagram;

    private final static int SEND_SMS_PERMISSION_REQUEST_CODE = 111;
    private Button sendMessage;
    private Button phoneCallValidate;
    private EditText phoneNumber;
    private EditText message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messenger = findViewById(R.id.messenger);
        instagram = findViewById(R.id.instagram);

        sendMessage = findViewById(R.id.smsValidate);
        sendMessage.setEnabled(false);
        phoneCallValidate = findViewById(R.id.phoneCallValidate);
        phoneNumber = findViewById(R.id.phoneNumber);
        message = findViewById(R.id.sms);

        messenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotToFacebookPage("");
            }
        });

        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotToWhatsapp();
                gotToInstagramPage();
            }
        });

        if (checkPermission(Manifest.permission.SEND_SMS)) {
            sendMessage.setEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                String phoneNumb = phoneNumber.getText().toString();

                if (!TextUtils.isEmpty(msg) && !TextUtils.isEmpty(phoneNumb)) {
                    if (checkPermission(Manifest.permission.SEND_SMS)) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNumb, null, msg, null, null);
                    } else {
                        Toast.makeText(ChatActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Enter a message and a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        phoneCallValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumb = phoneNumber.getText().toString();

                if(!TextUtils.isEmpty(phoneNumb)){
                    String dial = "tel:" + phoneNumb;
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
                }else {
                    Toast.makeText(ChatActivity.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void gotToFacebookPage(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id));
            startActivity(intent);
        }
    }

    private void gotToWhatsapp() {
        Uri uri = Uri.parse("smsto: " + "12345");
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage("com.whatsapp");
        startActivity(i);
    }

    private void gotToInstagramPage() {
        Uri uri = Uri.parse("http://instagram.com/_u/therock/");
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/")));
        }
    }

    private Boolean checkPermission(String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case SEND_SMS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    sendMessage.setEnabled(true);
                }

                break;
        }
    }
}
