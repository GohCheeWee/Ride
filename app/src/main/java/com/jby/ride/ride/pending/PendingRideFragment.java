package com.jby.ride.ride.pending;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.jby.ride.ride.RideActivity;
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



public class PendingRideFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnScrollListener, CustomListView.OnDetectScrollListener,
        AdapterView.OnItemClickListener{
    View rootView;
    private CustomListView pendingRideFragmentListView;
    private RelativeLayout pendingRideFragmentNotFound;
    private ProgressBar pendingRideLoadingBar;

    private ArrayList<PendingRideObject> pendingRideObjectArrayList;

//list purpose
    private SwipeRefreshLayout pendingRideSwipeRefreshLayout;
    private View pendingRideFragmentListViewFooter;
    private PendingRideAdapter pendingRideAdapter;

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


    public PendingRideFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_pending_ride, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        page = 1;
        finishLoadAll = false;
    }

    private void objectInitialize() {
        pendingRideFragmentListView = (CustomListView)rootView.findViewById(R.id.fragment_pending_ride_list_view);
        pendingRideFragmentNotFound = (RelativeLayout)rootView.findViewById(R.id.fragment_pending_ride_not_found);
        pendingRideSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.fragment_pending_ride_swipe_refresh_layout);
        pendingRideLoadingBar = (ProgressBar)rootView.findViewById(R.id.fragment_pending_ride_progress_bar);

        pendingRideFragmentListViewFooter = ((LayoutInflater) Objects.requireNonNull(getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)))
                .inflate(R.layout.list_view_footer, null, false);

        pendingRideObjectArrayList = new ArrayList<>();
        handler = new Handler();
    }

    private void objectSetting() {
        pendingRideAdapter = new PendingRideAdapter(getContext(), pendingRideObjectArrayList);
        pendingRideFragmentListView.setAdapter(pendingRideAdapter);

        pendingRideSwipeRefreshLayout.setOnRefreshListener(this);
        pendingRideSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));

        pendingRideFragmentListView.setOnDetectScrollListener(this);
        pendingRideFragmentListView.setOnScrollListener(this);
        pendingRideFragmentListView.setOnItemClickListener(this);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getPendingRide();
            }
        },200);
    }

    private void getPendingRide(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("page", String.valueOf(page)));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().pendingRide,
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
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("pendingRide");
                        for(int i = 0; i < jsonArray.length(); i++){
                            pendingRideObjectArrayList.add(new PendingRideObject(
                                    jsonArray.getJSONObject(i).getString("pick_up_address"),
                                    jsonArray.getJSONObject(i).getString("drop_off_address"),
                                    jsonArray.getJSONObject(i).getString("payment_method"),
                                    jsonArray.getJSONObject(i).getString("date"),
                                    jsonArray.getJSONObject(i).getString("time"),
                                    jsonArray.getJSONObject(i).getString("note"),
                                    jsonArray.getJSONObject(i).getString("fare"),
                                    jsonArray.getJSONObject(i).getString("id")
                            ));
                        }
                        pendingRideAdapter.notifyDataSetChanged();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
//                        if array list containing value mean data is finished load.
                        if(pendingRideObjectArrayList.size() > 0)
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
        pendingRideFragmentListView.removeFooterView(pendingRideFragmentListViewFooter);
        setUpView();
    }

    private void setUpView() {
        pendingRideLoadingBar.setVisibility(View.GONE);
        pendingRideSwipeRefreshLayout.setVisibility(View.VISIBLE);

        if(pendingRideObjectArrayList.size() > 0)
        {
            pendingRideFragmentNotFound.setVisibility(View.GONE);
            pendingRideFragmentListView.setVisibility(View.VISIBLE);
        }
        else{
            pendingRideFragmentNotFound.setVisibility(View.VISIBLE);
            pendingRideFragmentListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        page = 1;
        finishLoadAll = false;

        pendingRideObjectArrayList.clear();
        pendingRideAdapter.notifyDataSetChanged();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pendingRideSwipeRefreshLayout.setRefreshing(false);
                getPendingRide();
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
                pendingRideFragmentListView.addFooterView(pendingRideFragmentListViewFooter);
                isLoading = true;
                page++;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPendingRide();
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
            case R.id.fragment_pending_ride_list_view:
                clickEffect(view);
                ((RideActivity)getActivity()).openPendingRideDetail(pendingRideObjectArrayList.get(i).getId());
                break;
        }
    }

    //click effect
    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }
}
