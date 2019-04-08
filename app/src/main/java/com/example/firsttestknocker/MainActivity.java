package com.example.firsttestknocker;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private DrawerLayout drawerLayout;
    private CoordinatorLayout main_layout;
    private Dialog add_contact_popup;
    private ImageView add_contact_popup_close;
    private FloatingActionButton fab_open;
    private FloatingActionButton fab_add;
    private FloatingActionButton fab_close;
    private FloatingActionButton fab_compose;
    private Animation FabOpen, FabClose, Fab_R_ClockWiser, Fab_R_anticlockwiser;
    boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_layout = findViewById(R.id.main_layout);

        add_contact_popup = new Dialog(this);

        // Floating Button
        fab_open = findViewById(R.id.fab_open);
        fab_add = findViewById(R.id.fab_add);
        fab_compose = findViewById(R.id.fab_compose);
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        Fab_R_ClockWiser = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwiser);
        Fab_R_anticlockwiser = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anticlockwiser);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer);

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();

                int id = menuItem.getItemId();

                if (id == R.id.nav_home) {
                    Intent loginIntent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                } else if (id == R.id.nav_settings) {
                    Intent loginIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(loginIntent);
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Grid View
        gridView = findViewById(R.id.myGridView1);
        List<Contact> contactList = getContactList();

        ContactAdapter contactAdapter = new ContactAdapter(this, contactList);
        gridView.setAdapter(contactAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = gridView.getItemAtPosition(position);
                Contact contact = (Contact) o;

                Intent intent = new Intent(MainActivity.this, ContactDetailsActivity.class);
                intent.putExtra("ContactName", contact.getContactName());
                intent.putExtra("ContactPhoneNumber", contact.getContactPhoneNumber());
                intent.putExtra("ContactImage", contact.getContactImage());
                startActivity(intent);
//                finish();
            }
        });

        // Drag n Drop
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

//                finish();
                return false;
            }
        });

        fab_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    onFloatingClickBack();
                    isOpen = false;
                } else {
                    onFloatingClick();
                    isOpen = true;
                }
            }
        });

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MainActivity.this, AddNewContactActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
                case R.id.nav_category:
                    return true;
            case R.id.nav_filter:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onFloatingClickBack() {
        fab_add.startAnimation(FabClose);
        fab_compose.startAnimation(FabClose);
        fab_open.startAnimation(Fab_R_anticlockwiser);

        fab_add.setClickable(false);
        fab_compose.setClickable(false);
    }

    public void onFloatingClick() {
        fab_add.startAnimation(FabOpen);
        fab_compose.startAnimation(FabOpen);
        fab_open.startAnimation(Fab_R_ClockWiser);

        fab_add.setClickable(true);
        fab_compose.setClickable(true);
    }

    private List<Contact> getContactList() {
        List<Contact> list = new ArrayList<Contact>();
        Contact Michel = new Contact("Michel", "06 51 74 09 03", R.drawable.michel);
        Contact Jean_Luc = new Contact("Jean Luc", "06 66 93 32 49", R.drawable.jl);
        Contact Jean_Francois = new Contact("Jean Francois", " 07 78 03 65 54", R.drawable.jf);
        Contact Ryan = new Contact("Ryan", "07 04 51 42 37", R.drawable.ryan);
        Contact Florian = new Contact("Florian", "06 96 32 09 28", R.drawable.img_avatar);

        list.add(Michel);
        list.add(Jean_Luc);
        list.add(Jean_Francois);
        list.add(Ryan);
        list.add(Florian);

        return list;
    }
}
