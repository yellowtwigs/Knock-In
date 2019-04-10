package com.example.notificationlistener;

import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StatusBarParcelable implements Parcelable {
    private int id;
    private String appNotifier;
    private int tailleList;
    private ArrayList<String> key = new ArrayList<String>();
    public static String TAG = StatusBarParcelable.class.getSimpleName();

    public int getId() {
        return id;
    }

    public String getAppNotifier() {
        return appNotifier;
    }

    public int getTailleList() {
        return tailleList;
    }

    public ArrayList<String> getKey() {
        return key;
    }

    public HashMap<String, Object> getStatusBarNotificationInfo() {
        return statusBarNotificationInfo;
    }

    private HashMap<String,Object> statusBarNotificationInfo = new HashMap<String,Object>();

    public StatusBarParcelable(StatusBarNotification sbn) {
        id = sbn.getId();
        tailleList=sbn.getNotification().extras.keySet().size();
        appNotifier = sbn.getPackageName();
        for (String keySbn : sbn.getNotification().extras.keySet()) {
            key.add(keySbn);

            statusBarNotificationInfo.put(keySbn, sbn.getNotification().extras.get(keySbn));
        }
        //Log.i(TAG, "ID:" + sbn.getId());
        //Log.i(TAG, "Posted by:" + sbn.getPackageName());


    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(appNotifier);
        dest.writeInt(tailleList);

        Set<Map.Entry<String, Object>> entree = statusBarNotificationInfo.entrySet();

        for(Map.Entry<String,Object> e:entree){

                dest.writeString(e.getKey());
                dest.writeString(" "+e.getValue());

        }
        dest.writeString(appNotifier);
        //dest.writeList(key);
        //dest.writeMap(statusBarNotificationInfo);
    }

    public static Creator<StatusBarParcelable> CREATOR = new Creator<StatusBarParcelable>() {
        @Override
        public StatusBarParcelable createFromParcel(Parcel in) {
            return new StatusBarParcelable(in);
        }
        @Override
        public StatusBarParcelable[] newArray(int size) {
            return new StatusBarParcelable[size];
        }
    };

    private StatusBarParcelable(Parcel in) {
        id=in.readInt();
        appNotifier=in.readString();
        tailleList=in.readInt();

        Log.i(TAG,"posted by:"+appNotifier+" taille list"+tailleList);
        for (int i=0;i<tailleList;i++){
            String keysbn= in.readString();
            String value= in.readString();
            key.add(keysbn);
            statusBarNotificationInfo.put(keysbn,value);
                if (key != null) {
                    Log.i(TAG, keysbn + "=" + this.getStatusBarNotificationInfo().get(keysbn));
                }
            }
        }
        //key = in.readArrayList(String.class.getClassLoader());
        //statusBarNotificationInfo=in.readHashMap(String.class.getClassLoader());

}