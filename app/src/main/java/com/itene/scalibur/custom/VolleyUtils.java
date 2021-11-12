package com.itene.scalibur.custom;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyUtils {

    private static String TAG = "VolleyUtils";

    public interface VolleyResponseListener {
        void onError(String message);
        void onResponse(String response);
    }
    public interface VolleyJsonResponseListener {
        void onError(String message);
        void onResponse(JSONObject response);
    }
    public interface VolleyJsonArrayResponseListener {
        void onError(String message);
        void onResponse(JSONArray response);
    }

    public static void POST_JSON(Context context, String url, JSONObject post_params, String ACCESS_TOKEN, final VolleyJsonResponseListener listener)
    {
        // Initialize a new StringRequest
        JsonObjectRequest jsonRequest = new JsonObjectRequest (
                Request.Method.POST,
                url,
                post_params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());

                    }
                }) {
                //Passing some request headers
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    if (ACCESS_TOKEN != null) {
                        headers.put("api-token", ACCESS_TOKEN);
                    }
                    return headers;
                }
        };

        // Access the RequestQueue through singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonRequest);
    }

    public static void GET_JSON(Context context, String url, String ACCESS_TOKEN, final VolleyJsonResponseListener listener)
    {
        // Initialize a new StringRequest
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());
                    }
                })
            {
                //Passing some request headers
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    if (ACCESS_TOKEN != null) {
                        headers.put("api-token", ACCESS_TOKEN);
                    }
                    return headers;
            }
        };

        // Access the RequestQueue through singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonRequest);
    }

    public static void GET_JSON_ARRAY(Context context, String url, String ACCESS_TOKEN, final VolleyJsonArrayResponseListener listener)
    {
        // Initialize a new StringRequest
        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        listener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());
                    }
                })
        {
            //Passing some request headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("api-token", ACCESS_TOKEN);
                return headers;
            }
        };

        // Access the RequestQueue through singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonRequest);
    }

    public static void GET_METHOD(Context context, String url, final VolleyResponseListener listener)
    {
        // Initialize a new StringRequest
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onResponse(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());

                    }
                })
            {
                //Passing some request headers
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    //headers.put("api-token", ACCESS_TOKEN);
                    return headers;
            }
        };

        // Access the RequestQueue through singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    public static void POST_METHOD(Context context, String url, final Map<String, String> getParams, final VolleyResponseListener listener)
    {
        // Initialize a new StringRequest
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onResponse(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());

                    }
                })
        {
             //Passing some request headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                getParams.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        // Access the RequestQueue through singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

}