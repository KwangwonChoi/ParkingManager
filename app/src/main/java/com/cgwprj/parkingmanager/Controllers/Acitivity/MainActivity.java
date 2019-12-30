package com.cgwprj.parkingmanager.Controllers.Acitivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cgwprj.parkingmanager.Controllers.Fragments.CleanFragment;
import com.cgwprj.parkingmanager.Data.UserData;
import com.cgwprj.parkingmanager.Models.CarInfo;
import com.cgwprj.parkingmanager.R;
import com.cgwprj.parkingmanager.Controllers.Fragments.EnrollFragment;
import com.cgwprj.parkingmanager.Controllers.Fragments.InquiryFragment;
import com.cgwprj.parkingmanager.Controllers.Fragments.LookupFragment;
import com.cgwprj.parkingmanager.Controllers.Fragments.MainFragment;
import com.cgwprj.parkingmanager.Controllers.ResetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference mRef;
    List<CarInfo> carInfos;
    EditText search;
    ChildEventListener childEventListener = new ChildEventListener() {

        Fragment fragment;

        private boolean isMainFragment(){
            fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
            return fragment instanceof MainFragment;
        }

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            CarInfo carInfo = dataSnapshot.getValue(CarInfo.class);
            carInfos.add(carInfo);
            Collections.sort(carInfos);

            if (isMainFragment()){
                MainFragment mainFragment = (MainFragment) fragment;
                mainFragment.addCarInfoToList(carInfo);
            }

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            CarInfo carInfo = dataSnapshot.getValue(CarInfo.class);
            carInfos.remove(carInfo);

            if (isMainFragment()){
                MainFragment mainFragment = (MainFragment) fragment;
                mainFragment.deleteCarInfoFromList(carInfo);
            }
        }

        @Override   public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        @Override   public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        @Override   public void onCancelled(@NonNull DatabaseError databaseError) {}
    };

    public List<CarInfo> getCarInfos(){
        return this.carInfos;
    }
    public void ChangeFragmentToMain(){
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, new MainFragment()).commitNow();
        search.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        carInfos = new ArrayList<CarInfo>();

        // Tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Firebase
        mRef = FirebaseDatabase.getInstance().getReference().child(UserData.getInstance().getParkingLot());
        mRef.addChildEventListener(childEventListener);

        // Fragment
        replaceFragment(MainFragment.newInstance());

        search = findViewById(R.id.search_text);
        Button search_btn = findViewById(R.id.search_button);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String carNumber = search.getText().toString();
                FragmentChange(carNumber);
            }
        });

        ChangeFragmentToMain();
    }

    private void FragmentChange(String carNumber){

        CarInfo searchedCar = getCarInfo(carNumber);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);

        if (fragment instanceof MainFragment
        || fragment instanceof EnrollFragment
        || fragment instanceof InquiryFragment
        ){
            if (searchedCar != null){
                // 정산
                replaceFragment(InquiryFragment.newInstance(searchedCar));
            }
            else{
                // 등록
                replaceFragment(EnrollFragment.newInstance(carNumber));
            }
        }

        else if (
                fragment instanceof LookupFragment
        ){
            replaceFragment(LookupFragment.newInstance(carNumber));
        }
    }

    private CarInfo getCarInfo(String carNumber){
        if(carInfos == null)
            return null;

        for (CarInfo c : carInfos){
            if(c.getCarNumber().equals(carNumber))
                return c;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            replaceFragment(CleanFragment.newInstance(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSearchBar(String Number){
        search.setText(Number);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            ChangeFragmentToMain();
        } else if (id == R.id.nav_inquiry) {
            replaceFragment(LookupFragment.newInstance(""));
        } else if (id == R.id.nav_reset){

        final ResetDialog builder = new ResetDialog(this);
        builder.show();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Fragment 변환을 해주기 위한 부분, Fragment의 Instance를 받아서 변경
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, fragment).commit();
    }
}
