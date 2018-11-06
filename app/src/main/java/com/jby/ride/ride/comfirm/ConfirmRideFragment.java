package com.jby.ride.ride.comfirm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.others.CustomListView;
import com.jby.ride.ride.comfirm.dialog.ViewOtherRiderDialog;
import com.jby.ride.ride.comfirm.object.OtherRiderInCarObject;
import com.jby.ride.ride.comfirm.startRoute.StartRouteActivity;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConfirmRideFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnScrollListener, CustomListView.OnDetectScrollListener,
        AdapterView.OnItemClickListener, MatchRideAdapter.MatchRideAdapterCallBack {
    View rootView;
    RelativeLayout confirmRideFragmentNotFound;
    SwipeRefreshLayout confirmRideSwipeRefreshLayout;
    ProgressBar confirmRideFragmentProgressBar;

    CustomListView confirmRideFragmentListView;
    MatchRideAdapter matchRideAdapter;
    ArrayList<ConfirmRideObject> confirmRideObjectArrayList;
    View confirmRideFragmentListViewFooter;
    private int page = 1;
    //    check background process is running or not
    private boolean isLoading = true;
    //    detect is scroll down or not
    private boolean isScroll = false;
    //    if finish load then become true
    boolean finishLoadAll = false;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    public static int UPDATE_CONFIRMED_RIDE_REQUEST = 400;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_comfirm_ride, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        confirmRideFragmentNotFound = (RelativeLayout)rootView.findViewById(R.id.fragment_confirm_ride_not_found);
        confirmRideFragmentListView = (CustomListView)rootView.findViewById(R.id.fragment_confirm_ride_list_view);
        confirmRideFragmentProgressBar = (ProgressBar)rootView.findViewById(R.id.fragment_confirm_ride_progress_bar);
        confirmRideSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.fragment_confirm_ride_swipe_refresh_layout);

        confirmRideFragmentListViewFooter = ((LayoutInflater) Objects.requireNonNull(getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)))
                .inflate(R.layout.list_view_footer, null, false);

        confirmRideObjectArrayList = new ArrayList<>();
        matchRideAdapter = new MatchRideAdapter(getActivity(), confirmRideObjectArrayList, this);
        handler = new Handler();


    }

    private void objectSetting() {
        confirmRideFragmentListView.setAdapter(matchRideAdapter);

        confirmRideSwipeRefreshLayout.setOnRefreshListener(this);
        confirmRideSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));

        confirmRideFragmentListView.setOnDetectScrollListener(this);
        confirmRideFragmentListView.setOnScrollListener(this);
        confirmRideFragmentListView.setOnItemClickListener(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getConfirmRide();
            }
        },200);
    }

    private void getConfirmRide(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("rider_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("page", String.valueOf(page)));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().confirmRide,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("matchRide");
                        for(int i = 0; i < jsonArray.length(); i++){
                            confirmRideObjectArrayList.add(new ConfirmRideObject(
                                    jsonArray.getJSONObject(i).getString("pick_up_address"),
                                    jsonArray.getJSONObject(i).getString("drop_off_address"),
                                    jsonArray.getJSONObject(i).getString("payment_method"),
                                    jsonArray.getJSONObject(i).getString("date"),
                                    jsonArray.getJSONObject(i).getString("time"),
                                    jsonArray.getJSONObject(i).getString("note"),
                                    jsonArray.getJSONObject(i).getString("fare"),
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("status"),
                                    jsonArray.getJSONObject(i).getString("driver_name"),
                                    jsonArray.getJSONObject(i).getString("driver_profile_pic"),
                                    jsonArray.getJSONObject(i).getString("driver_gender"),
                                    jsonArray.getJSONObject(i).getString("driver_car_plate"),
                                    jsonArray.getJSONObject(i).getString("driver_car_brand"),
                                    jsonArray.getJSONObject(i).getString("driver_car_model"),
                                    jsonArray.getJSONObject(i).getString("driver_ride_id"),
                                    setUpOtherRiderArray(jsonArray.getJSONObject(i).getJSONObject("other_rider"))
                            ));
                        }
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
//                        if array list containing value mean data is finished load.
                        if(confirmRideObjectArrayList.size() > 0)
                            finishLoadAll = true;
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        isLoading = false;
        confirmRideFragmentListView.removeFooterView(confirmRideFragmentListViewFooter);
        setUpView();
        matchRideAdapter.notifyDataSetChanged();
    }

    private ArrayList<OtherRiderInCarObject> setUpOtherRiderArray(JSONObject jsonObject){
        ArrayList<OtherRiderInCarObject> otherRiderInCarObjectArrayList = new ArrayList<>();
        try {
            if(jsonObject.getString("status").equals("1")){
                JSONArray jsonArray = jsonObject.getJSONArray("value");
                for(int i = 0; i < jsonArray.length(); i++){
                    otherRiderInCarObjectArrayList.add(new OtherRiderInCarObject(
                            jsonArray.getJSONObject(i).getString("username"),
                            jsonArray.getJSONObject(i).getString("gender"),
                            jsonArray.getJSONObject(i).getString("user_id"),
                            jsonArray.getJSONObject(i).getString("profile_picture")
                    ));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return otherRiderInCarObjectArrayList;
    }

    private void setUpView() {
        confirmRideFragmentProgressBar.setVisibility(View.GONE);
        if(confirmRideObjectArrayList.size() > 0)
        {
            confirmRideFragmentNotFound.setVisibility(View.GONE);
            confirmRideFragmentListView.setVisibility(View.VISIBLE);
            confirmRideSwipeRefreshLayout.setVisibility(View.VISIBLE);
        }
        else{
            confirmRideFragmentNotFound.setVisibility(View.VISIBLE);
            confirmRideFragmentListView.setVisibility(View.GONE);
            confirmRideSwipeRefreshLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        page = 1;
        finishLoadAll = false;

        confirmRideObjectArrayList.clear();
        matchRideAdapter.notifyDataSetChanged();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                confirmRideSwipeRefreshLayout.setRefreshing(false);
                getConfirmRide();
            }
        },50);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {

        final int position = firstVisibleItem+visibleItemCount;

        if(!finishLoadAll){
            // Check if bottom has been reached
            if (position >= totalItemCount && totalItemCount > 0 && !isLoading && isScroll) {
                confirmRideFragmentListView.addFooterView(confirmRideFragmentListViewFooter);
                isLoading = true;
                page++;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getConfirmRide();
                    }
                },200);
            }
        }
    }
    @Override
    public void onUpScrolling() {
        isScroll = false;
    }

    @Override
    public void onDownScrolling() {
        isScroll = true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()){
            case R.id.fragment_confirm_ride_list_view:
                clickEffect(view);
                break;
        }
    }

    //click effect
    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    @Override
    public void trackMyRoute(int position) {
        Intent intent = new Intent(getActivity(), StartRouteActivity.class);
        Bundle bundle = new Bundle();
        this.position = position;

        bundle.putString("match_ride_id", confirmRideObjectArrayList.get(position).getId());
        bundle.putString("driver_ride_id", confirmRideObjectArrayList.get(position).getDriver_ride_id());
        intent.putExtras(bundle);
        startActivityForResult(intent, UPDATE_CONFIRMED_RIDE_REQUEST);
    }

    @Override
    public void viewAllRiderDialog(ArrayList<OtherRiderInCarObject> otherRiderInCarObjectArrayList) {
        Bundle bundle = new Bundle();
        DialogFragment dialogFragment = new ViewOtherRiderDialog();
        FragmentManager fm = Objects.requireNonNull(getActivity()).getSupportFragmentManager();

        bundle.putParcelableArrayList("OtherRiderInCarArrayList", otherRiderInCarObjectArrayList);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }
}
