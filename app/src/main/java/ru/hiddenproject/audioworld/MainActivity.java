package ru.hiddenproject.audioworld;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.hiddenproject.audioworld.Fragments.MainFragment;
import ru.hiddenproject.audioworld.Fragments.NewFragment;
import ru.hiddenproject.audioworld.Fragments.SearchFragment;

import static android.view.MenuItem.*;
import static ru.hiddenproject.audioworld.Helper.API_VERSION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FragmentTransaction fTrans;
    Retrofit retrofit;
    API api;
    Toolbar toolbar;
    public Helper helper;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        helper = new Helper();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fTrans = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        Class fragmentClass = MainFragment.class;
        try {
            fragment = (Fragment)fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fTrans.replace(R.id.fragment, fragment);
        fTrans.commit();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://185.125.217.104")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(API.class);

        /*mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        fTrans = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        Class fragmentClass = null;
        if (id == R.id.nav_new) {
            fragmentClass = NewFragment.class;
        } else if (id == R.id.nav_library) {
            fragmentClass = MainFragment.class;
        } else if (id == R.id.nav_search){
            fragmentClass = SearchFragment.class;
        } else if (id == R.id.nav_archive){
            fragmentClass = ArchiveFragment.class;
        } else if (id == R.id.nav_vk){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/awbooks"));
            startActivity(browserIntent);
        }
        try {
            fragment = (Fragment)fragmentClass.newInstance();
            fTrans.replace(R.id.fragment, fragment);
            fTrans.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        toolbar.setTitle(item.getTitle());
        return true;
    }
}
