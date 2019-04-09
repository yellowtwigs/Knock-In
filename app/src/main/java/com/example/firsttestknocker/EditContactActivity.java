package com.example.firsttestknocker;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class EditContactActivity extends AppCompatActivity {

    private TextView edit_contact_FirstName;
    private TextView edit_contact_LastName;
    private TextView edit_contact_PhoneNumber;
    private RoundedImageView edit_contact_RoundedImageView;
    private String edit_contact_first_name;
    private String edit_contact_last_name;
    private String edit_contact_phone_number;
    private int edit_contact_rounded_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        // Create the Intent, and get the data from the GridView
        Intent intent = getIntent();
        edit_contact_first_name = intent.getStringExtra("ContactFirstName");
        edit_contact_last_name = intent.getStringExtra("ContactLastName");
        edit_contact_phone_number = intent.getStringExtra("ContactPhoneNumber");
        edit_contact_rounded_image = intent.getIntExtra("ContactImage", 1);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_cross);
        actionbar.setTitle("Editer le contact");

        // Find View By Id
        edit_contact_FirstName = findViewById(R.id.edit_contact_first_name_id);
        edit_contact_LastName = findViewById(R.id.edit_contact_last_name_id);
        edit_contact_PhoneNumber = findViewById(R.id.edit_contact_phone_number_id);
        edit_contact_RoundedImageView = findViewById(R.id.edit_contact_rounded_image_view_id);

        // Set Resources from MainActivity to ContactDetailsActivity
        edit_contact_FirstName.setText(edit_contact_first_name);
        edit_contact_LastName.setText(edit_contact_last_name);
        edit_contact_PhoneNumber.setText(edit_contact_phone_number);
        edit_contact_RoundedImageView.setImageResource(edit_contact_rounded_image);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            case R.id.nav_validate:
                // Update
                Intent intent = new Intent(EditContactActivity.this, ContactDetailsActivity.class);

                intent.putExtra("ContactFirstName", edit_contact_first_name);
                intent.putExtra("ContactLastName", edit_contact_last_name);
                intent.putExtra("ContactPhoneNumber", edit_contact_phone_number);
                intent.putExtra("ContactImage", edit_contact_rounded_image);

                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_contact, menu);
        return true;
    }
}
