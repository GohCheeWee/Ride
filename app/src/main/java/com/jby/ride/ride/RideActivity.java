package com.jby.ride.ride;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.ride.R;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.ride.comfirm.ConfirmRideFragment;
import com.jby.ride.ride.past.PastRideFragment;
import com.jby.ride.ride.pending.PendingRideFragment;
import com.jby.ride.ride.pending.pendingDetail.PendingRideDetailActivity;

import java.util.ArrayList;
import java.util.List;

import static com.jby.ride.home.MainActivity.UPDATE_COUNTER_REQUEST;

public class RideActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener,
        View.OnClickListener{
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon, actionBarLogout;
    private TextView actionBarTitle;

    private FrameLayout rideActivityFrameLayout;
    private TabLayout rideActivityTabLayout;
    private LinearLayout rideActivityLinearLayout;
//    fragment
    private PendingRideFragment pendingRideFragment;
    private PastRideFragment pastRideFragment;
    private ConfirmRideFragment confirmRideFragment;
    private Fragment fragment2;
//
    public static int DELETE_PENDING_RIDE = 302;
    public static int EDIT_PENDING_RIDE = 303;
    public int requestCode;

    FragmentTransaction ft;
    FragmentManager fm;
    List<Fragment> activeCenterFragments = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarMenuIcon = (SquareHeightLinearLayout)findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = (SquareHeightLinearLayout)findViewById(R.id.actionbar_close);
        actionBarLogout = (SquareHeightLinearLayout)findViewById(R.id.actionbar_logout);
        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);

        rideActivityFrameLayout = (FrameLayout)findViewById(R.id.activity_ride_frame_layout);
        rideActivityTabLayout = (TabLayout)findViewById(R.id.activity_ride_tab_layout);
        rideActivityLinearLayout = (LinearLayout)findViewById(R.id.activity_ride_main_layout);

        pendingRideFragment = new PendingRideFragment();
        pastRideFragment = new PastRideFragment();
        confirmRideFragment = new ConfirmRideFragment();

    }

    private void objectSetting() {
        //        actionBar
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarTitle.setText(R.string.fragment_pending_trip_title);

        actionBarCloseIcon.setOnClickListener(this);

        rideActivityTabLayout.addOnTabSelectedListener(this);
        setupTabLayout();

        Intent intent = getIntent();
        if(intent != null){
            if(intent.getStringExtra("accept") != null){
                TabLayout.Tab tab = rideActivityTabLayout.getTabAt(0);
                assert tab != null;
                tab.select();
            }
        }
    }
    private void setupTabLayout(){
        rideActivityTabLayout.removeAllTabs();
        rideActivityTabLayout.addTab(rideActivityTabLayout.newTab().setText("Comfirm"));
        rideActivityTabLayout.addTab(rideActivityTabLayout.newTab().setText("Pending"), true);
        rideActivityTabLayout.addTab(rideActivityTabLayout.newTab().setText("Past"));

        fragment2 = pendingRideFragment;
        pendingRideFragment = (PendingRideFragment)fragment2;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
            setCurrentTabFragment(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private void setCurrentTabFragment(int tabPosition) {
        switch (tabPosition) {
            case 0:
                replaceFragment(confirmRideFragment);
                break;
            case 1:
                replaceFragment(pendingRideFragment);
                break;
            case 2:
                replaceFragment(pastRideFragment);
                break;
        }
    }

    public void replaceFragment(Fragment fragment) {
        fm = getSupportFragmentManager();
        activeCenterFragments.add(fragment);

        ft = fm.beginTransaction();
        ft.replace(R.id.activity_ride_frame_layout, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.actionbar_close:
                onBackPressed();
                break;
        }
    }

    public void openPendingRideDetail(String ride_id){
        Bundle bundle = new Bundle();
        bundle.putString("ride_id", ride_id);

        Intent intent = new Intent(this, PendingRideDetailActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, DELETE_PENDING_RIDE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == DELETE_PENDING_RIDE){
            pendingRideFragment.onRefresh();
            showSnackBar("Delete Successful");
            this.requestCode = UPDATE_COUNTER_REQUEST;
        }
    }

    //    snackBar setting
    private void showSnackBar(String message){
        final Snackbar snackbar = Snackbar.make(rideActivityFrameLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(requestCode, intent);
        super.onBackPressed();
    }
}
