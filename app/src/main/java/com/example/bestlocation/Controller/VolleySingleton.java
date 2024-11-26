package com.example.bestlocation.Controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    private static VolleySingleton mInstance;
    //file d'attente pour gérer les requêtes HTTP
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    public VolleySingleton(Context context) {
        this.mCtx = context  ;
        mRequestQueue =getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if(mRequestQueue==null)
        {
            mRequestQueue= Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return  mRequestQueue;
    }

    public static synchronized  VolleySingleton getInstance(Context context){
        if(mInstance==null)
        {
            mInstance=new VolleySingleton(context);
        }
        return  mInstance;
    }
//demande  eli mawjoud f tlf wel internet
    public <T> void addToRequestQueue(Request<T> req)
    {
        //Une requête HTTP est ajoutée à la file d'attente à l'aide de :
       //VolleySingleton.getInstance(context).addToRequestQueue(request);

        getRequestQueue().add(req);
    }


}
