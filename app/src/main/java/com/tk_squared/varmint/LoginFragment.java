package com.tk_squared.varmint;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


public class LoginFragment extends Fragment {
    private LoginButton loginButton;
    private Button skipButton;
    public Callbacks callbacks;

    private TextView info;

    public interface Callbacks{
        void onLoginFinish();
    }
    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final TkkActivity tkkActivity = (TkkActivity)getActivity();
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        skipButton = (Button) view.findViewById(R.id.skip_button);
        if(skipButton == null) skipButton = new Button(getActivity());
        info = (TextView)view.findViewById(R.id.info);
        if(info == null) info = new TextView(getActivity());
        info.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorInfoText));

        loginButton = (LoginButton)view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.setFragment(this);
        callbacks = tkkActivity;

        skipButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callbacks = (Callbacks) getActivity();
                callbacks.onLoginFinish();
            }
        });
        loginButton.registerCallback(tkkActivity.getCallbackManager(), new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("LOGIN", "Facebook logged in");
                callbacks.onLoginFinish();
                info.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()
                );
            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {

                info.setText("Login attempt failed.");
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        ((TkkActivity)getActivity()).getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }
}
