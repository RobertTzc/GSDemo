package com.dji.GSDemo.PathPlanning;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dji.GSDemo.GoogleMap.R;

public class MainActivity extends AppCompatActivity {

    private View.OnClickListener clickListener = v -> {
        switch (v.getId()) {
            case R.id.btn_waypoint:
                startActivity(MainActivity.this, Waypoint1Activity.class);
                break;
            case R.id.btn_video:
                startActivity(MainActivity.this, FpvActivity.class);
                break;
            case R.id.btn_cust_waypoint:
                startActivity(MainActivity.this, SelfPathPlanning.class);
                break;
            case R.id.btn_tapFly:
                startActivity(MainActivity.this, TapFly.class);
                break;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_waypoint).setOnClickListener(clickListener);
        findViewById(R.id.btn_video).setOnClickListener(clickListener);
        findViewById(R.id.btn_cust_waypoint).setOnClickListener(clickListener);
        findViewById(R.id.btn_tapFly).setOnClickListener(clickListener);
    }

    public static void startActivity(Context context, Class activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
    }
}
