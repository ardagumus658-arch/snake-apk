package com.arda.yilan;

import android.app.Activity;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

@CapacitorPlugin(name = "AdMobRewarded")
public class AdMobRewardedPlugin extends Plugin {

    private RewardedInterstitialAd rewardedAd;
    private boolean loading = false;

    private static final String AD_UNIT_ID =
            "ca-app-pub-5076362961536868/2667866213";

    @PluginMethod
    public void load(PluginCall call) {
        Activity activity = getActivity();

        if (loading) {
            call.resolve();
            return;
        }

        loading = true;

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedInterstitialAd.load(
                activity,
                AD_UNIT_ID,
                adRequest,
                new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        loading = false;
                        rewardedAd = ad;
                        call.resolve();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        loading = false;
                        rewardedAd = null;

                        JSObject result = new JSObject();
                        result.put("error", error.getMessage());
                        call.reject(error.getMessage());
                    }
                }
        );
    }

    @PluginMethod
    public void show(PluginCall call) {
        Activity activity = getActivity();

        if (rewardedAd == null) {
            call.reject("Reklam henüz hazır değil.");
            return;
        }

        RewardedInterstitialAd ad = rewardedAd;
        rewardedAd = null;

        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(
                    com.google.android.gms.ads.AdError adError) {
                loadAd();
            }
        });

        ad.show(activity, rewardItem -> {
            JSObject result = new JSObject();
            result.put("type", rewardItem.getType());
            result.put("amount", rewardItem.getAmount());
            call.resolve(result);
        });
    }

    private void loadAd() {
        Activity activity = getActivity();

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedInterstitialAd.load(
                activity,
                AD_UNIT_ID,
                adRequest,
                new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        rewardedAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        rewardedAd = null;
                    }
                }
        );
    }
}
