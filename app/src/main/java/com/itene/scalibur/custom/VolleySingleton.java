package com.itene.scalibur.custom;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

class VolleySingleton {
        private static com.itene.scalibur.custom.VolleySingleton mInstance;
        private RequestQueue mRequestQueue;
        private static Context mCtx;

    private VolleySingleton(Context context)
        {
            mCtx = context;
            mRequestQueue = getRequestQueue();
        }

        public static synchronized com.itene.scalibur.custom.VolleySingleton getInstance(Context context)
        {
            if (mInstance == null)
            {
                mInstance = new com.itene.scalibur.custom.VolleySingleton(context);
            }
            return mInstance;
        }

        public RequestQueue getRequestQueue()
        {
            if (mRequestQueue == null)
            {
                mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            }
            return mRequestQueue;
        }

        public <T> void addToRequestQueue(Request<T> req)
        {
            getRequestQueue().add(req);
        }

}

