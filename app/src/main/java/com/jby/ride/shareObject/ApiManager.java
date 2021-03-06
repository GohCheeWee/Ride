package com.jby.ride.shareObject;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ApiManager {
    private String domain = "http://188.166.186.198/~cheewee";
    public String register = domain + "/ride/frontend/user/registration/register.php";
    public String login = domain + "/ride/frontend/user/registration/login.php";
    public String registerToken = domain + "/ride/frontend/user/registration/rider_token.php";
// user profile
    public String userProfile = domain + "/ride/frontend/user/profile/user_profile.php";
//    route api
    public String userCreateRide = domain + "/ride/frontend/user/home/matching/user_matching.php";
//    pending ride
    public String pendingRide = domain + "/ride/frontend/user/ride/pending/pending_ride.php";
    public String numPendingRide = domain + "/ride/frontend/user/ride/pending/pending_ride_counter.php";
//    confirm ride
    public String confirmRide = domain + "/ride/frontend/user/ride/confirm/confirm_ride.php";
    // rating
    public String rating = domain + "/ride/frontend/user/rating/rating.php";
    //past ride
    public String pastRide = domain + "/ride/frontend/user/ride/past/past_ride.php";
    //wallet
    public String bankCard = domain + "/ride/frontend/user/wallet/bank_card/bank_card.php";
    public String transaction = domain + "/ride/frontend/user/wallet/transaction/transaction.php";
    //chat
    public String chat = domain + "/ride/frontend/user/chat/chat.php";
    //driver path
    public static String profile_prefix = "http://188.166.186.198/~cheewee/ride/frontend/driver/profile/driver_profile_picture/";
    public static String car_prefix = "http://188.166.186.198/~cheewee/ride/frontend/driver/profile/driver_car/";

    public String setData(ArrayList<ApiDataObject> apiDataObjectArrayList){
        String apiDataPost = "";
        String anApiDataPost = "";

        for (int position = 0 ; position < apiDataObjectArrayList.size() ; position++) {
            if (apiDataObjectArrayList.size() > 0){
                try {
                    anApiDataPost = URLEncoder.encode(apiDataObjectArrayList.get(position).getDataKey(), "UTF-8")
                                    + "=" +
                                    URLEncoder.encode(apiDataObjectArrayList.get(position).getDataContent(), "UTF-8");

                    apiDataPost += anApiDataPost;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {
                    if (position != (apiDataObjectArrayList.size() - 1))
                        apiDataPost += "&";
                }
            }
        }

        return apiDataPost;
    }

    /*
    *       Set Data <key>=<data> OR Model <model-name>[<key>]=<data>
    * */
    public String setModel(ArrayList<ApiModelObject> apiModelObjectArrayList){
        String apiModelPost = "";
        String anApiDataPost = "";

        /*
        *
        *       Build Post Data In Model Format
        *
        * */
        for (int position = 0 ; position < apiModelObjectArrayList.size() ; position++) {
            if (apiModelObjectArrayList.size() > 0){
                try {
                    anApiDataPost = URLEncoder.encode(apiModelObjectArrayList.get(position).getModelName(), "UTF-8")
                                    + URLEncoder.encode("[", "UTF-8")
                                    + URLEncoder.encode(apiModelObjectArrayList.get(position).getApiDataObject().getDataKey(), "UTF-8")
                                    + URLEncoder.encode("]", "UTF-8")
                                    + "="
                                    + URLEncoder.encode(apiModelObjectArrayList.get(position).getApiDataObject().getDataContent(), "UTF-8");

                    apiModelPost += anApiDataPost;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {
                    if (position != (apiModelObjectArrayList.size() - 1))
                        apiModelPost += "&";
                }
            }
        }

        return apiModelPost;
    }

    /*
    *       List Data Or Model
    * */
    public String setListModel(String Model, ArrayList<ArrayList<ApiDataObject>> apiListModelObjectArrayList){
        String apiListModelPost = "";

        String anApiModelPost = "";

        /*
        *
        *       Build Post Data In List Model Format
        *
        * */
        for (int position = 0 ; position < apiListModelObjectArrayList.size() ; position++) {
            if (apiListModelObjectArrayList.size() > 0){
                try {
                    for (int innerPosition = 0 ; innerPosition < apiListModelObjectArrayList.get(position).size() ; innerPosition++){
                        anApiModelPost = URLEncoder.encode(Model, "UTF-8")
                                        + URLEncoder.encode("[", "UTF-8")
                                        + position
                                        + URLEncoder.encode("]", "UTF-8")
                                        + URLEncoder.encode("[", "UTF-8")
                                        + URLEncoder.encode(apiListModelObjectArrayList.get(position).get(innerPosition).getDataKey(), "UTF-8")
                                        + URLEncoder.encode("]", "UTF-8")
                                        + "="
                                        + URLEncoder.encode(apiListModelObjectArrayList.get(position).get(innerPosition).getDataContent(), "UTF-8");

                        Log.i("Each Api", anApiModelPost);

                        apiListModelPost += anApiModelPost;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {
                    if (position != (apiListModelObjectArrayList.size() - 1))
                        apiListModelPost += "&";
                }
            }
        }

        return apiListModelPost;
    }

    /*
    *       Data OR Model OR List Model Joining
    * */
    public String getResultParameter(String data, String model, String listModel){

        if ((!data.equals("")) && (!model.equals("")) && (!listModel.equals("")))
            return data + "&" + model + "&" + listModel;

        else if ((!data.equals("")) && (!model.equals("")) && (listModel.equals("")))
            return data + "&" + model;
        else if ((!data.equals("")) && (model.equals("")) && (!listModel.equals("")))
            return data + "&" + listModel;
        else if ((data.equals("")) && (!model.equals("")) && (!listModel.equals("")))
            return model + "&" + listModel;

        else if ((!data.equals("")) && (model.equals("")) && (listModel.equals("")))
            return data;
        else if ((data.equals("")) && (!model.equals("")) && (listModel.equals("")))
            return model;
        else if ((data.equals("")) && (model.equals("")) && (!listModel.equals("")))
            return listModel;

        else
            return "";
    }
}
