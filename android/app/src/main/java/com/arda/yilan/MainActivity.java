package com.arda.yilan;

import android.os.Bundle;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import com.getcapacitor.BridgeActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.FullScreenContentCallback;

public class MainActivity extends BridgeActivity {

    private static final String REWARDED_AD_ID =
            "ca-app-pub-5076362961536868/2667866213";

    private RewardedAd rewardedAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, initializationStatus -> {
            loadRewardedAd();
        });

        getBridge().getWebView().addJavascriptInterface(
                new AdInterface(),
                "AdMob"
        );
    }

    private void loadRewardedAd() {

        AdRequest request = new AdRequest.Builder().build();

        RewardedAd.load(
                this,
                REWARDED_AD_ID,
                request,
                new RewardedAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;

                        rewardedAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {

                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        rewardedAd = null;
                                        loadRewardedAd();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            @NonNull LoadAdError error) {
                        rewardedAd = null;
                    }
                }
        );
    }

    private void showRewardedAd() {

        if (rewardedAd == null) {
            loadRewardedAd();

            runOnUiThread(() ->
                    getBridge().getWebView().evaluateJavascript(
                            "window.adRewardFailed && window.adRewardFailed()",
                            null
                    )
            );

            return;
        }

        rewardedAd.show(this, rewardItem -> {

            runOnUiThread(() ->
                    getBridge().getWebView().evaluateJavascript(
                            "window.adRewardEarned && window.adRewardEarned()",
                            null
                    )
            );

        });
    }

    public class AdInterface {

        @JavascriptInterface
        public void showRewarded() {
            runOnUiThread(() -> showRewardedAd());
        }
    }
}
