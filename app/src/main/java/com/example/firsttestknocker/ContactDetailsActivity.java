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

    private TextView contactFirstName_TextView;
    private TextView contactLastName_TextView;
    private TextView contactPhoneNumber_TextView;
    private RoundedImageView contactImage_RoundedImageView;
    private FloatingActionButton floating_button_contact_details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        Intent intent = getIntent();
        String contactFirstName = intent.getStringExtra("ContactFirstName");
        String contactLastName = intent.getStringExtra("ContactLastName");
        String contactPhoneNumber = intent.getStringExtra("ContactPhoneNumber");
        int contactImage = intent.getIntExtra("ContactImage", 1);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow);
        actionbar.setTitle("Détails du contact " + contactFirstName);

        contactFirstName_TextView = (TextView)findViewById(R.id.contact_details_user_firstname);
        contactLastName_TextView = (TextView)findViewById(R.id.contact_details_user_lastname);
        contactPhoneNumber_TextView = (TextView)findViewById(R.id.contact_details_phone_number);
        contactImage_RoundedImageView = (RoundedImageView)findViewById(R.id.contact_details_RoundedImageView);
        floating_button_contact_details = findViewById(R.id.floating_button_contact_details);

        contactFirstName_TextView.setText(contactFirstName);
        contactLastName_TextView.setText(contactLastName);
        contactPhoneNumber_TextView.setText(contactPhoneNumber);
        contactImage_RoundedImageView.setImageResource(contactImage);

        floating_button_contact_details.setOnClickListener(new View.OnClickListener() {
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
                Intent loginIntent = new Intent(ContactDetailsActivity.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
