package com.pilju.slidingdrawersample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.pilju.slidingdrawersample.lib.SlidingDrawer;

public class MainActivity extends AppCompatActivity {

    private SlidingDrawer mSlidingDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        mSlidingDrawer.setOnDrawerStateListener(new SlidingDrawer.OnDrawerStateListener() {
            @Override
            public void onDrawerClosed() {
                Toast.makeText(getApplicationContext(), "Sliding Drawer Closed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDrawerOpened() {
                Toast.makeText(getApplicationContext(), "Sliding Drawer Opened.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
