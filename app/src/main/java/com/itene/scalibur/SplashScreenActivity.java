package com.itene.scalibur;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.itene.scalibur.config.Config;
import com.itene.scalibur.custom.VolleyUtils;
import com.itene.scalibur.data.model.LoggedInUser;
import com.itene.scalibur.ui.login.LoginActivity;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int MINIMUM_TIME = 3000; // 3 seconds in milliseconds
    private static final String TAG = com.itene.scalibur.SplashScreenActivity.class.getSimpleName();
    private boolean autoLoginInProgress;
    private long creation_ns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        creation_ns = System.nanoTime();
        autoLoginInProgress = false;

        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Set activity layout
        setContentView(R.layout.splash_screen);

        // Start animation
        ImageView logo = (ImageView)findViewById(R.id.splash_scalibur_logo);
        Animation slideAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        logo.startAnimation(slideAnimation);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        autoLogin();        // Attempt autologin
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        outState.putBoolean("autoLoginInProgress", autoLoginInProgress);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
        autoLoginInProgress = savedInstanceState.getBoolean("autoLoginInProgress", false);
    }

    private int calculate_waiting_time() {
        int ns_difference = (int)((System.nanoTime() - creation_ns))/1000000;
        if (ns_difference < MINIMUM_TIME) {
            return (MINIMUM_TIME - ns_difference);
        } else {
            return 0;
        }
    }

    private void startLoginActivity() {
        Log.d(TAG, "startLoginActivity");
        long waiting_time = calculate_waiting_time();
        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                finish();
            }
        }, waiting_time); // 3000 is the delayed time in milliseconds.
    }

    private void startMainActivity(LoggedInUser user) {
        Log.d(TAG, "startMainActivity");
        long waiting_time = calculate_waiting_time();
        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, SelectRouteActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                finish();
            }
        }, waiting_time); // 3000 is the delayed time in milliseconds.
    }

    public void autoLogin() {
        Log.d(TAG, "autoLogin");
        if (autoLoginInProgress) {
            Log.d(TAG, "autoLogin already launched, skipping");
            return;
        } else {
            autoLoginInProgress = true;
        }

        SharedPreferences sharedPreferences = this.getSharedPreferences(
                "com.itene.scalibur", Context.MODE_PRIVATE);
        String api_token = sharedPreferences.getString("API_TOKEN", null);

        if (!sharedPreferences.contains("API_TOKEN")) {
            startLoginActivity();
            return;
        }

        try {
            VolleyUtils.GET_JSON(this, Config.API_GET_ME, api_token, new VolleyUtils.VolleyJsonResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "Auto login error" + message);
                    startLoginActivity();
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("API", "Auto Login successful: " + response.toString());
                        LoggedInUser loggedInUser =
                                new LoggedInUser(
                                        response.getInt("id"),
                                        response.getString("name"),
                                        response.getString("email"));

                        startMainActivity(loggedInUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                        startLoginActivity();
                    }
                }
            });
        } catch (Exception e) {
            startLoginActivity();
        }
    }
}