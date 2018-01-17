package com.renny.recyclerbanner;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.SeekBar;

import com.renny.recyclerbanner.banner.adapter.DataAdapter;
import com.renny.recyclerbanner.banner.layoutmanager.CenterScrollListener;
import com.renny.recyclerbanner.banner.layoutmanager.OverFlyingLayoutManager;

//未封装版
public class OverFlyingActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    OverFlyingLayoutManager mOverFlyingLayoutManager;
    Handler mHandler;
    Runnable mRunnable;
    int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_flying);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_banner);
        mOverFlyingLayoutManager = new OverFlyingLayoutManager(0.75f, 385, OverFlyingLayoutManager.HORIZONTAL);

        recyclerView.setAdapter(new DataAdapter());
        recyclerView.setLayoutManager(mOverFlyingLayoutManager);

        recyclerView.addOnScrollListener(new CenterScrollListener());
        mOverFlyingLayoutManager.setOnPageChangeListener(new OverFlyingLayoutManager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                currentPosition++;
                Log.d("recyclerBanner", currentPosition + " ");
               mOverFlyingLayoutManager.scrollToPosition(currentPosition);
                //  recyclerView.smoothScrollToPosition(currentPosition);
                mHandler.postDelayed(this, 3000);
            }
        };
        mHandler.postDelayed(mRunnable, 3000);

        SeekBar seekBar = (SeekBar) findViewById(R.id.progress);
        seekBar.setProgress(385);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mOverFlyingLayoutManager.setItemSpace(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
