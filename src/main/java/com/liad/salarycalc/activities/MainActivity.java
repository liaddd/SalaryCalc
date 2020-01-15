package com.liad.salarycalc.activities;

import android.os.Bundle;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.liad.salarycalc.Config;
import com.liad.salarycalc.R;
import com.liad.salarycalc.fragments.MainFragment;

public class MainActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize MobileAds to whole app
        MobileAds.initialize(this);
        initInterstitialAd();
        changeFragment(MainFragment.newInstance(), false);
    }

    private void initInterstitialAd() {
        final InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(Config.STAGE.equals("production") ? R.string.ad_view_prod_interstitial_unit_id : R.string.ad_view_test_interstitial_unit_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (interstitialAd.isLoaded()) interstitialAd.show();
            }
        });
    }
}
