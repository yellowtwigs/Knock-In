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
import android.widget.Toast;

public class AddNewContactActivity extends AppCompatActivity {

    private TextView add_new_contact_FirstName;
    private TextView add_new_contact_LastName;
    private TextView add_new_contact_PhoneNumber;
    private RoundedImageView add_new_contact_RoundedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contact);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow);
        actionbar.setTitle("Ajouter un nouveau contact");

        // Find View By Id
        add_new_contact_FirstName = findViewById(R.id.add_new_contact_first_name_id);
        add_new_contact_LastName = findViewById(R.id.add_new_contact_last_name_id);
        add_new_contact_PhoneNumber = findViewById(R.id.add_new_contact_phone_number_id);
        add_new_contact_RoundedImageView = findViewById(R.id.add_new_contact_rounded_image_view_id);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            case R.id.nav_validate:
                if (add_new_contact_FirstName.getText().toString().isEmpty() && add_new_contact_LastName.getText().toString().isEmpty() ) {
                    Toast.makeText(this, "Les champs nom et prénom ne peuvent pas être vide !", Toast.LENGTH_SHORT).show();
                } else {
                    // Query
                    Intent intent = new Intent(AddNewContactActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
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
