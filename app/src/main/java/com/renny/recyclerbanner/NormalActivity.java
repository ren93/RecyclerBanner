package com.renny.recyclerbanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.renny.recyclerbanner.banner.RecyclerViewBannerNormal;
import com.renny.recyclerbanner.banner.RecyclerViewBannerBase;

import java.util.ArrayList;
import java.util.List;

public class NormalActivity extends AppCompatActivity {
    RecyclerViewBannerNormal banner, banner2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        banner = (RecyclerViewBannerNormal) findViewById(R.id.banner);
        banner2 = (RecyclerViewBannerNormal) findViewById(R.id.banner2);
        List<String> list = new ArrayList<>();
        list.add("http://oo6pz0u05.bkt.clouddn.com/17-12-13/69427561.jpg");
        list.add("http://oo6pz0u05.bkt.clouddn.com/17-12-13/23738150.jpg");
        list.add("http://oo6pz0u05.bkt.clouddn.com/17-12-13/30127126.jpg");
        list.add("http://oo6pz0u05.bkt.clouddn.com/17-12-13/36125492.jpg");
        banner.initBannerImageView(list, new RecyclerViewBannerBase.OnBannerItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(NormalActivity.this, "clicked:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        banner2.initBannerImageView(list, new RecyclerViewBannerBase.OnBannerItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(NormalActivity.this, "clicked:" + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

}
