package com.itene.scalibur.custom;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyUtils {

    private static String TAG = "VolleyUtils";
    private ConnectivityManager.NetworkCallback mWifiNetworkCallback, mMobileNetworkCallback;
    private Network mWifiNetwork, mMobileNetwork;

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String response);
    }

    public interface VolleyJsonResponseListener {
        void onError(String message);
        void onResponse(JSONObject response);
    }

    /*public void initializeConnections(Context context){
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mWifiNetworkCallback == null){
            //Init only once
            mWifiNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(final Network network) {
                    try {
                        //Save this network for later use
                        mWifiNetwork = network;
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                }
            };
        }

        if(mMobileNetworkCallback == null){
            //Init only once
            mMobileNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(final Network network) {
                    try {
                        //Save this network for later use
                        mMobileNetwork = network;
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                }
            };
        }

        //Request networks
        NetworkRequest.Builder wifiBuilder;
        wifiBuilder = new NetworkRequest.Builder();
        //set the transport type do WIFI
        wifiBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        manager.requestNetwork(wifiBuilder.build(), mWifiNetworkCallback);

        NetworkRequest.Builder mobileNwBuilder;
        mobileNwBuilder = new NetworkRequest.Builder();
        //set the transport type do Cellular
        mobileNwBuilder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        manager.requestNetwork(mobileNwBuilder.build(), mMobileNetworkCallback);

    }

    public void makeHTTPRequest(final String httpUrl, final String payloadJson, final int timeout) {
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection conn = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                //conn = (HttpURLConnection) mWifiNetwork.openConnection(url);

                //Or use mMobileNetwork, if and when required
                conn = (HttpURLConnection) mMobileNetwork.openConnection(url);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setReadTimeout(timeout * 1000);
            conn.setConnectTimeout(timeout * 1000);

            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(payloadJson.getBytes());
            os.close();

            final int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                final String statusMessage = conn.getResponseMessage();
                //Log this
            }
        } catch (SocketException se){
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/



    public static void POST_JSON(Context context, String url, JSONObject postparams, final VolleyJsonResponseListener listener)
    {
        // Initialize a new StringRequest
        JsonObjectRequest jsonRequest = new JsonObjectRequest (
                Request.Method.POST,
                url,
                postparams,
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
                });

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