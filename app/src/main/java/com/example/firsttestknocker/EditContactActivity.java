package com.example.firsttestknocker;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class EditContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow);
        actionbar.setTitle("Editer le contact");

        // Create the Intent, and get the data from the GridView
        Intent intent = getIntent();
        String contact_details_first_name = intent.getStringExtra("ContactFirstName");
        String contact_details_last_name = intent.getStringExtra("ContactLastName");
        String contact_details_phone_number = intent.getStringExtra("ContactPhoneNumber");
        int contactImage = intent.getIntExtra("ContactImage", 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent loginIntent = new Intent(EditContactActivity.this, ContactDetailsActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
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
