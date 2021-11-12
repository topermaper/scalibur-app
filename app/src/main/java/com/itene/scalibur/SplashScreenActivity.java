package com.itene.scalibur;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private long creation_ns;

    private static final long MINIMUM_TIME = 2000000000L; //  2 seconds in nanoseconds
    private static final String TAG = com.itene.scalibur.SplashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        creation_ns = System.nanoTime();
        super.onCreate(savedInstanceState);
        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        // HERE WE ARE TAKING THE REFERENCE OF OUR IMAGE
        // SO THAT WE CAN PERFORM ANIMATION USING THAT IMAGE
        //ImageView logo = (ImageView)findViewById(R.id.splash_scalibur_logo);
        //Animation slideAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        //logo.startAnimation(slideAnimation);
        autoLogin();
    }

    private void wait_a_bit() {
        long ns_difference = System.nanoTime() - creation_ns;
        if (ns_difference < MINIMUM_TIME) {
            try {
                Log.d(TAG, String.format("Waiting %d milliseconds", (MINIMUM_TIME - ns_difference) / 1000000));
                TimeUnit.NANOSECONDS.sleep(MINIMUM_TIME - ns_difference);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startLoginActivity() {
        Log.d(TAG, "startLoginActivity");
        wait_a_bit();
        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        finish();
    }

    private void startMainActivity(LoggedInUser user) {
        Log.d(TAG, "startMainActivity");
        wait_a_bit();
        Intent intent = new Intent(SplashScreenActivity.this, SelectRouteActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    public void autoLogin() {
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