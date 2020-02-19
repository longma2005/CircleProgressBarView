package com.ryoma.circlebar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.ryoma.circleprogressbar.CircleProgressView;

public class MainActivity extends AppCompatActivity {

    private CircleProgressView circleProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleProgressView = findViewById(R.id.circleProgressView);
    }

    public void onButton1Click(View view) {
        circleProgressView.setProgress(30, true, 1000);
    }

    public void onButton2Click(View view) {
        circleProgressView.setProgress(60, true, 1000);
    }

    public void onButton3Click(View view) {
        circleProgressView.setProgress(80, true, 1000);
    }

}
