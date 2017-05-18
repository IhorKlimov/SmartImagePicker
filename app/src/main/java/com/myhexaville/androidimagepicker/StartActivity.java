package com.myhexaville.androidimagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.databinding.DataBindingUtil;

import com.myhexaville.androidimagepicker.activity_example.ActivityExample;
import com.myhexaville.androidimagepicker.databinding.ActivityStartBinding;
import com.myhexaville.androidimagepicker.fragment_example.FragmentExample;


public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStartBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_start);
        setSupportActionBar(binding.toolbar);
    }

    public void fragmentExample(View view) {
        startActivity(new Intent(this, FragmentExample.class));
    }

    public void activityExample(View view) {
        startActivity(new Intent(this, ActivityExample.class));
    }
}