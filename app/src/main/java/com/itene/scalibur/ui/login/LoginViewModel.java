package com.itene.scalibur.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;

import com.itene.scalibur.config.Config;
import com.itene.scalibur.custom.VolleyUtils;
import com.itene.scalibur.data.Credential;
import com.itene.scalibur.data.model.LoggedInUser;
import com.itene.scalibur.R;

import org.json.JSONObject;

public class LoginViewModel extends ViewModel {


    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private Credential credential;
    private SharedPreferences sharedPreferences;
    private Context context;


    LoginViewModel(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(
                "com.itene.scalibur", Context.MODE_PRIVATE);
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }
    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }


    public void login(String email, String password) {
        credential = new Credential(context, email, password);

        try {
            JSONObject post_params = new JSONObject();
            post_params.put("email", email);
            post_params.put("password", password);

            Context context = LoginActivity.getAppContext();

            VolleyUtils.POST_JSON(context, Config.API_POST_LOGIN, post_params, null, new VolleyUtils.VolleyJsonResponseListener() {
                @Override
                public void onError(String message) {
                    Log.e("API", "Login error" + message);
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                }

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("API", "Login successful: " + response.toString());

                        JSONObject user = response.getJSONObject("user");

                        LoggedInUser loggedInUser =
                                new LoggedInUser(
                                        user.getInt("id"),
                                        user.getString("name"),
                                        user.getString("email"));

                        // Store API token
                        String api_token = response.getString("api_token");
                        sharedPreferences.edit().putString("API_TOKEN", api_token).apply();

                        loginResult.setValue(new LoginResult(loggedInUser));

                    } catch (Exception e) {
                        e.printStackTrace();
                        loginResult.setValue(new LoginResult(R.string.login_failed));
                    }
                }
            });
        } catch (Exception e) {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() >= 6 && password.trim().length() <= 30;
    }
}