package com.example.firsttestknocker;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class ContactDetailsActivity extends AppCompatActivity {

    private TextView contact_details_FirstName;
    private TextView contact_details_LastName;
    private TextView contact_details_PhoneNumber;
    private RoundedImageView contact_details_RoundedImageView;
    private ImageView contactImage_BackgroundImage;
    private FloatingActionButton contact_details_FloatingButton;
    private String pathToFile;
    private String contact_details_first_name;
    private String contact_details_last_name;
    private String contact_details_phone_number;
    private int contact_details_rounded_image;
    private RelativeLayout contact_details_phone_number_RelativeLayout;
    private RelativeLayout contact_details_messenger_RelativeLayout;
    private RelativeLayout contact_details_whatsapp_RelativeLayout;
    private RelativeLayout contact_details_instagram_RelativeLayout;
    private RelativeLayout contact_details_mail_RelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        // Create the Intent, and get the data from the GridView
        Intent intent = getIntent();
        contact_details_first_name = intent.getStringExtra("ContactFirstName");
        contact_details_last_name = intent.getStringExtra("ContactLastName");
        contact_details_phone_number = intent.getStringExtra("ContactPhoneNumber");
        contact_details_rounded_image = intent.getIntExtra("ContactImage", 1);

        if(!contact_details_phone_number.isEmpty())
        {
            contact_details_phone_number_RelativeLayout.setVisibility(View.VISIBLE);
        }

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow);
        actionbar.setTitle("Détails du contact " + contact_details_first_name);

        // Find View By Id
        contact_details_FirstName = findViewById(R.id.contact_details_first_name_id);
        contact_details_LastName = findViewById(R.id.contact_details_last_name_id);
        contact_details_PhoneNumber = findViewById(R.id.contact_details_phone_number_text_id);
        contact_details_RoundedImageView = findViewById(R.id.contact_details_rounded_image_view_id);
        contactImage_BackgroundImage = findViewById(R.id.contact_details_background_image_id);
        contact_details_FloatingButton = findViewById(R.id.contact_details_floating_button_id);


        contact_details_phone_number_RelativeLayout = findViewById(R.id.contact_details_phone_number_relative_layout_id);
        contact_details_messenger_RelativeLayout = findViewById(R.id.contact_details_messenger_relative_layout_id);
        contact_details_whatsapp_RelativeLayout = findViewById(R.id.contact_details_whatsapp_relative_layout_id);
        contact_details_instagram_RelativeLayout = findViewById(R.id.contact_details_instagram_relative_layout_id);
//        contact_details_mail_RelativeLayout = findViewById(R.id.contact_details_phone_number_relative_layout_id);


        // Set Resources from MainActivity to ContactDetailsActivity
        contact_details_FirstName.setText(contact_details_first_name);
        contact_details_LastName.setText(contact_details_last_name);
        contact_details_PhoneNumber.setText(contact_details_phone_number);
        contact_details_RoundedImageView.setImageResource(contact_details_rounded_image);

//        if(Build.VERSION.SDK_INT >= 23){
//            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
//        }

        contact_details_FloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactDetailsActivity.this, EditContactActivity.class);

                intent.putExtra("ContactFirstName", contact_details_first_name);
                intent.putExtra("ContactLastName", contact_details_last_name);
                intent.putExtra("ContactPhoneNumber", contact_details_phone_number);
                intent.putExtra("ContactImage", contact_details_rounded_image);

                startActivity(intent);
            }
        });

//        contactImage_BackgroundImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dispatchPictureTakerAction();
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent loginIntent = new Intent(ContactDetailsActivity.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
                contactImage_BackgroundImage.setImageBitmap(bitmap);
            }
        }
    }

    public void dispatchPictureTakerAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createPhotoFile();

            if (photoFile != null) {
                String pathToFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(ContactDetailsActivity.this, "com.example.firsttestknocker.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, 1);
            }
        }
    }

    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;

        try {
            image = File.createTempFile(name, ".jpg", storageDir);
        } catch (IOException e) {
            Log.d("mylog", "Excep : " + e.toString());
        }

        return image;
    }
}
