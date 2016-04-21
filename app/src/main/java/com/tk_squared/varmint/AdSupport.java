package com.tk_squared.varmint;

import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.smaato.soma.BannerView;
import com.smaato.soma.interstitial.Interstitial;
import com.smaato.soma.interstitial.InterstitialAdListener;


/**
 * Created by zengo on 4/19/2016.
 * This is the Smaato version of
 * this file!
 * Make sure you are using the
 * correct version!!
 */

class AdSupport implements InterstitialAdListener {

    private Interstitial interstitial;
    private final TkkActivity activity;

    public AdSupport(TkkActivity activity){
        this.activity = activity;
    }

    public void showInterstitial(){
        if(interstitial.isInterstitialReady()){
            interstitial.show();
        }
    }


    public void setupAdSupport(){
        //Set up interstitial ads
        setInterstitialAd();
        //Set up banner ads
        setupSmaato();
    }

    private void setInterstitialAd(){
        interstitial = new Interstitial(activity);
        interstitial.setInterstitialAdListener(this);
        interstitial.getAdSettings().setPublisherId(activity.getResources().getInteger(R.integer.smaato_pub_id));
        interstitial.getAdSettings().setAdspaceId(activity.getResources().getInteger(R.integer.smaato_inter_id));
    }

    private void interBannerLoad(){
        Runnable r = new Runnable(){
            @Override
            public void run(){
                interstitial.asyncLoadNewBanner();
            }
        };
        activity.getHandler().postDelayed(r,
                activity.getResources().getInteger(R.integer.smaato_interstitial_reload_delay));
    }

    //region Description:Callback methods for InterstitialListener
    @Override
    public void onReadyToShow(){
        //We'll let ya know
    }

    @Override
    public void onWillShow(){
        //Rejoice!
    }

    @Override
    public void onFailedToLoadAd(){
        //sh*t happens
        interBannerLoad();
    }

    @Override
    public void onWillClose(){
        interBannerLoad();
    }

    @Override
    public void onWillOpenLandingPage(){
        //$$!
    }
    //endregion


    private void setupSmaato(){
        BannerView bv = new BannerView(activity);
        bv.setAutoReloadEnabled(true);
        bv.setAutoReloadFrequency(activity.getResources().getInteger(R.integer.smaato_reload_delay));

        RelativeLayout relativeLayout = (RelativeLayout)activity.findViewById(R.id.ad_container);
        if (relativeLayout != null){
            relativeLayout.addView(bv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        bv.getAdSettings().setPublisherId(activity.getResources().getInteger(R.integer.smaato_pub_id));
        bv.getAdSettings().setAdspaceId(activity.getResources().getInteger(R.integer.smaato_ad_id));
        bv.asyncLoadNewBanner();
    }

    public void adCleanup(){
        interstitial.destroy();
    }

    public void loadInterstitial(){
        interstitial.asyncLoadNewBanner();
    }
    //endregion
}
