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

    private static final String TEST_REWARDED_ID =
            "ca-app-pub-3940256099942544/5224354917";

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
                TEST_REWARDED_ID,
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
