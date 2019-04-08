package com.example.firsttestknocker;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.URI;

public class ContactDetailsActivity extends AppCompatActivity {

    private TextView contact_details_FirstName;
    private TextView contact_details_LastName;
    private TextView contact_details_PhoneNumber;
    private RoundedImageView contact_details_RoundedImageView;
    private ImageView contactImage_BackgroundImage;
    private FloatingActionButton contact_details_FloatingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        // Create the Intent, and get the data from the GridView
        Intent intent = getIntent();
        String contact_details_first_name = intent.getStringExtra("ContactFirstName");
        String contact_details_last_name = intent.getStringExtra("ContactLastName");
        String contact_details_phone_number = intent.getStringExtra("ContactPhoneNumber");
        int contactImage = intent.getIntExtra("ContactImage", 1);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow);
        actionbar.setTitle("Détails du contact " + contact_details_first_name);

        // Find View By Id
        contact_details_FirstName = (TextView)findViewById(R.id.contact_details_first_name_id);
        contact_details_LastName = (TextView)findViewById(R.id.contact_details_last_name_id);
        contact_details_PhoneNumber = (TextView)findViewById(R.id.contact_details_phone_number_text_id);
        contact_details_RoundedImageView = (RoundedImageView)findViewById(R.id.contact_details_rounded_image_view_id);
        contactImage_BackgroundImage = findViewById(R.id.contact_details_background_image_id);
        contact_details_FloatingButton = findViewById(R.id.contact_details_floating_button_id);

        // Set Resources from MainActivity to ContactDetailsActivity
        contact_details_FirstName.setText(contact_details_first_name);
        contact_details_LastName.setText(contact_details_last_name);
        contact_details_PhoneNumber.setText(contact_details_phone_number);
        contact_details_RoundedImageView.setImageResource(contactImage);
        contactImage_BackgroundImage.setImageResource(contactImage);

        contact_details_FloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ContactDetailsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
