package com.example.synerzip.myapplication3;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by synerzip on 23/9/16.
 */
public class AppController {

    private static AppController INSTANCE;
    private RequestQueue requestQueue;
    private Context context;

    private AppController(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }
    public static synchronized AppController getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppController(context);
        }
        return INSTANCE;
    }
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }
    public <T> void addToRequestQueue(StringRequest req) {
        getRequestQueue().add(req);
    }
}
