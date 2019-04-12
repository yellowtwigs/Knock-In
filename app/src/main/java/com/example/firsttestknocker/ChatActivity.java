package com.example.firsttestknocker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {

    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
    ImageView messenger;
    ImageView instagram;

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
}
