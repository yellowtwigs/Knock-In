package com.example.knocker.controller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.example.knocker.R;

public class ChatActivity extends AppCompatActivity {

    ImageView messenger;
    ImageView instagram;

    private final static int SEND_SMS_PERMISSION_REQUEST_CODE = 111;
    private Button sendMessage;
    private Button phoneCallValidate;
    private EditText phoneNumber;
    private EditText message;

    private Switch changeTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messenger = findViewById(R.id.messenger);
        instagram = findViewById(R.id.instagram);

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

    public void restartActivity()
    {
        Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
        startActivity(intent);
        finish();
    }
}
