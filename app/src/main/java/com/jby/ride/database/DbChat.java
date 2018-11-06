package com.jby.ride.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jby.ride.chat.ChatObject;
import com.jby.ride.chat.chatRoom.ChatRoomGroupObject;
import com.jby.ride.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by user on 3/11/2018.
 */
public class DbChat extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database";
    private static final int DATABASE_VERSION = 1;

    private static final String TB_CHAT = "tb_chat";
    private static final String CHAT_ID = "id";
    private static final String DRIVER_RIDE_ID = "driver_ride_id";
    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";

    private static final String TB_CHAT_DETAIL = "tb_chat_detail";
    private static final String CHAT_DETAIL_ID = "id";
    private static final String MESSAGE = "message";
    private static final String USERNAME = "username";
    private static final String SENDER_PROFILE_PICTURE = "profile_picture";
    private static final String SENDER_ID = "sender_id";
    private static final String USER_ID = "user_id";
    //read or not read
    private static final String STATUS = "status";
    //send date
    private static final String SEND_DATE = "send_date";
    //send from driver or user
    private static final String SEND_FROM = "sendFrom";
    //message type
    private static final String TYPE = "type";

    private static final String CREATE_TABLE_CHAT = "CREATE TABLE "+ TB_CHAT +
            "(" + CHAT_ID + " INTEGER PRIMARY KEY, " +
            DRIVER_RIDE_ID + " INTEGER, " +
            USER_ID + " Text, " +
            CREATED_AT + " Text, " +
            UPDATED_AT + " Text)";

    private static final String CREATE_TABLE_CHAT_DETAIL = "CREATE TABLE " + TB_CHAT_DETAIL +
            "(" + CHAT_DETAIL_ID + " INTEGER PRIMARY KEY, " +
            DRIVER_RIDE_ID +" INTEGER, " +
            MESSAGE + " Text, " +
            USERNAME+ " Text, " +
            SENDER_PROFILE_PICTURE + " Text, " +
            SENDER_ID + " Text, " +
            SEND_FROM + " Text, " +
            STATUS + " Text, " +
            TYPE +  " Text, " +
            SEND_DATE + " Text, " +
            CREATED_AT + " Text, " +
            UPDATED_AT + "Text)";

    private String timeStamp;
    private Context context;
    public DbChat(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_CHAT);
        sqLiteDatabase.execSQL(CREATE_TABLE_CHAT_DETAIL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_CHAT);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_CHAT_DETAIL);
        onCreate(sqLiteDatabase);
    }
    /*------------------------------------------------------------------------------store chat---------------------------------------------------------*/
    //store chat from firebase
    public boolean saveChat(String driver_ride_id, String message, String username, String user_id, String profile_picture, String type, String send_date, String send_from, String status)
    {
        //create chat
        createChat(driver_ride_id);
        //store chat detail
        return storeChatDetail(driver_ride_id, message, username, user_id, profile_picture, type, send_date, send_from, timeStamp, status) != -1;
    }
    //create new chat list
    public void createChat(String driver_ride_id){
        boolean chatRoomExisted = checkChatRoomExisted(driver_ride_id);
        SQLiteDatabase db = this.getWritableDatabase();
        timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        //         new record
        if(!chatRoomExisted) {
            String userID = SharedPreferenceManager.getUserID(context);

            ContentValues contentValues = new ContentValues();
            contentValues.put(DRIVER_RIDE_ID, driver_ride_id);
            contentValues.put(USER_ID, userID);
            contentValues.put(CREATED_AT, timeStamp);
            db.insert(TB_CHAT, null, contentValues);
        }
    }
    //store chat detail into chat room
    public long storeChatDetail(String driver_ride_id, String message, String username, String sender_id, String profile_picture, String type, String send_date, String send_from, String created_at, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DRIVER_RIDE_ID, driver_ride_id);
        contentValues.put(MESSAGE, message);
        contentValues.put(USERNAME, username);
        contentValues.put(SENDER_PROFILE_PICTURE, profile_picture);
        contentValues.put(SENDER_ID, sender_id);
        contentValues.put(TYPE, type);
        contentValues.put(SEND_DATE, send_date);
        contentValues.put(SEND_FROM, send_from);
        contentValues.put(STATUS, status);
        contentValues.put(CREATED_AT, created_at);
        return db.insert(TB_CHAT_DETAIL, null, contentValues);

    }

    private boolean checkChatRoomExisted(String driver_ride_id){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + CHAT_ID + " FROM " +TB_CHAT+ " WHERE " + DRIVER_RIDE_ID + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] { driver_ride_id });
        boolean status = false;

        if(cursor.getCount() > 0)
            status = true;
        cursor.close();
        return status;
    }
    /*----------------------------------------------------------------------------end of store chat--------------------------------------------------------*/

    public JSONObject fetchAllChatRoom() {
        SQLiteDatabase db = this.getReadableDatabase();
        String userID = SharedPreferenceManager.getUserID(context);
        //for return json purpose
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = null;
        String json ="";

        String sql = "SELECT a.driver_ride_id, b.send_date, b.message, b.username, b.profile_picture, b.status, b.sendFrom"+
                " FROM "+ TB_CHAT + " as a" +
                " INNER JOIN "+ TB_CHAT_DETAIL + " as b" +
                " ON a.driver_ride_id = b.driver_ride_id" +
                " WHERE a.user_id=" + userID +
                " GROUP BY a.driver_ride_id" +
                " ORDER BY b.id DESC";

        Cursor crs = db.rawQuery(sql, null);
        while (crs.moveToNext()) {
            ChatObject object = new ChatObject(crs.getString(crs.getColumnIndex("driver_ride_id")),
                    crs.getString(crs.getColumnIndex("message")),
                    crs.getString(crs.getColumnIndex("username")),
                    crs.getString(crs.getColumnIndex("send_date")),
                    crs.getString(crs.getColumnIndex("profile_picture")),
                    crs.getString(crs.getColumnIndex("status")),
                    crs.getString(crs.getColumnIndex("sendFrom"))
            );
            sb.append(object).append(",");
        }
        //remove the "," from result
        if (!sb.toString().equals(""))
            json = sb.substring(0, sb.length()-1);
        //adding the format of json to result
        json = "{\"ChatObject\":[" + json + "]}";
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
        crs.close();
        return jsonObject;
    }

    public JSONObject fetchAllChat(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        //for return json purpose
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = null;
        String json ="";

        String sql = "SELECT b.id, b.sender_id, b.send_date, b.message, b.username, b.profile_picture, b.status, b.sendFrom"+
                " FROM "+ TB_CHAT + " as a" +
                " INNER JOIN "+ TB_CHAT_DETAIL + " as b" +
                " ON a.driver_ride_id = b.driver_ride_id" +
                " WHERE a.driver_ride_id =" + id +
                " ORDER BY b.id ASC";

        Cursor crs = db.rawQuery(sql, null);
        while (crs.moveToNext()) {
            ChatRoomGroupObject object = new ChatRoomGroupObject(
                    crs.getString(crs.getColumnIndex("id")),
                    crs.getString(crs.getColumnIndex("sender_id")),
                    crs.getString(crs.getColumnIndex("username")),
                    crs.getString(crs.getColumnIndex("profile_picture")),
                    crs.getString(crs.getColumnIndex("message")),
                    crs.getString(crs.getColumnIndex("status")),
                    crs.getString(crs.getColumnIndex("sendFrom")),
                    crs.getString(crs.getColumnIndex("send_date"))
            );
            sb.append(object).append(",");
        }
        //remove the "," from result
        if (!sb.toString().equals(""))
            json = sb.substring(0, sb.length()-1);
        //adding the format of json to result
        json = "{\"ChatRoomObject\":[" + json + "]}";
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
        crs.close();
        return jsonObject;
    }

    public JSONObject fetchNewAddedChat(String driverRideID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userId = SharedPreferenceManager.getUserID(context);
        //for return json purpose
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = null;
        String json ="";

        String sql = "SELECT b.id, b.sender_id, b.send_date, b.message, b.username, b.profile_picture, b.status, b.sendFrom"+
                " FROM "+ TB_CHAT + " as a" +
                " INNER JOIN "+ TB_CHAT_DETAIL + " as b" +
                " ON a.driver_ride_id = b.driver_ride_id" +
                " WHERE a.driver_ride_id = " + driverRideID +
                " ORDER BY b.id DESC" +
                " LIMIT 1";

        Cursor crs = db.rawQuery(sql, null);
        while (crs.moveToNext()) {
            ChatRoomGroupObject object = new ChatRoomGroupObject(
                    crs.getString(crs.getColumnIndex("id")),
                    crs.getString(crs.getColumnIndex("sender_id")),
                    crs.getString(crs.getColumnIndex("username")),
                    crs.getString(crs.getColumnIndex("profile_picture")),
                    crs.getString(crs.getColumnIndex("message")),
                    crs.getString(crs.getColumnIndex("status")),
                    crs.getString(crs.getColumnIndex("sendFrom")),
                    crs.getString(crs.getColumnIndex("send_date"))
            );
            sb.append(object).append(",");
        }
        //remove the "," from result
        if (!sb.toString().equals(""))
            json = sb.substring(0, sb.length()-1);
        //adding the format of json to result
        json = "{\"ChatRoomObject\":[" + json + "]}";
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
        crs.close();
        return jsonObject;
    }

    public boolean updateChatStatusWhenClick(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", "1");
        return db.update(TB_CHAT_DETAIL, contentValues, "driver_ride_id = ?", new String[] { id}) != -1;
    }
}
