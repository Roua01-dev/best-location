package com.example.bestlocation.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.bestlocation.Model.User;
import com.example.bestlocation.View.LoginActivity;

import java.security.Key;

//hedha bch yra9eb  user fel app
public class SessionManager {
    private static  final String SHARED_PREF_Name="userTaken";
    private  static final String KEY_NAME="name";
    private  static final String KEY_EMAIL="email";
    private  static final String KEY_TOKEN="token";
    private  static final String KEY_ID="user_id";
    private static SessionManager mInstance;
    private static Context mCtx;

    public SessionManager(Context context){
        mCtx=context;
    }
    public static synchronized  SessionManager getInstance(Context context){
        if(mInstance==null)
        {
            mInstance=new SessionManager(context);
        }
        return  mInstance;
    }


    public void UserloginOut(){

        //delete les info de user connecté
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_Name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.apply();
        mCtx.startActivity(new Intent(mCtx, LoginActivity.class));

    }
    public void userLogin(User user ){
        //ysajel les info mta3 user k ya3ml login
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_Name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(KEY_ID, user.getId());
        editor.putString(KEY_NAME,user.getName());
        editor.putString(KEY_EMAIL,user.getEmail());
        editor.putString(KEY_TOKEN,user.getToken());
        editor.apply();
    }

    public Boolean  isLoggedIn(){
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_Name,Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN,null)!=null;
    }

    public  User getToken(){
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_Name,Context.MODE_PRIVATE);
        return new User(
                sharedPreferences.getString(KEY_TOKEN,null)
        ) ;

    }




    public User getUser(User user ) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_Name, Context.MODE_PRIVATE);

        return new User(
                //tjib id mta3 user ken famech yraja3 -1
                sharedPreferences.getInt(KEY_ID, -1),
                sharedPreferences.getString(KEY_NAME, null),
                sharedPreferences.getString(KEY_EMAIL, null),
                sharedPreferences.getString(KEY_TOKEN  , null)


        );
    }
}
