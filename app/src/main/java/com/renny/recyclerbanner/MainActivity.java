package com.renny.recyclerbanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerViewBannerNew banner,banner2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        banner = findViewById(R.id.banner);
        banner2= findViewById(R.id.banner2);
        List<String> list = new ArrayList<>();
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510833032476&di=3998d3aaa2e006c1120c8534311e9a81&imgtype=0&src=http%3A%2F%2Fe.hiphotos.baidu.com%2Fzhidao%2Fpic%2Fitem%2F810a19d8bc3eb135275b10f1ae1ea8d3fc1f44df.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510833032475&di=8ceb2f76c9bed8a2ebaf5ba85efd4440&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2Fb%2F591953d9ce3cb.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510833032475&di=4cf3e1192a9e58529440d213de48a95a&imgtype=0&src=http%3A%2F%2Fimg.7xz.com%2Fimg%2Fpicimg%2F201607%2F20160715161145_327a1d30f651dab8932.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510833032474&di=3723ba473a9c92dc420e2538a9aa584e&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2Fc%2F57450b9a295f5.jpg");
        banner.initBannerImageView(list);
        banner2.initBannerImageView(list);

    }


}
