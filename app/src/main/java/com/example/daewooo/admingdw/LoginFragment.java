package com.example.daewooo.admingdw;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LoginFragment extends Fragment {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle args) {

        View view = inflater.inflate(R.layout.login_page, container, false);
        return view;
    }


}
