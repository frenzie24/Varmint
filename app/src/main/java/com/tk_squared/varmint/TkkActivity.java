package com.tk_squared.varmint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.support.v7.widget.ShareActionProvider;

//Smaato
import com.smaato.soma.BannerView;
import com.smaato.soma.interstitial.Interstitial;
import com.smaato.soma.interstitial.InterstitialAdListener;

/**
 * Created by zengo on 1/30/2016.
 * You know it Babe!
 */
public class TkkActivity extends AppCompatActivity
        implements TkkListViewFragment.Callbacks, tkkDataMod.Callbacks,
        TkkWebViewFragment.Callbacks, InterstitialAdListener {

    //region Description: Variables and Accessors
    private tkkDataMod tuxData;
    public tkkDataMod getData() {
        return tuxData;
    }
    public void setData(tkkDataMod data) {
        tuxData = data;
    }
    public ArrayList<tkkStation> getTkkData() {
        return tuxData.getStations();
    }
    private FragmentManager fm;
    private ProgressBar progBar;
    private boolean listEditEnabled = false;
    public boolean getListEditEnabled() {
        return listEditEnabled;
    }
    public void setEditEnabled(boolean enableEdit) {
        listEditEnabled = enableEdit;
    }
    private Handler handler = new Handler();
    private ShareActionProvider mShareActionProvider;
    private MusicIntentReceiver musicIntentReceiver;
    private Interstitial interstitial;
    //endregion

    public TkkActivity() {
    }

    //region Description: Lifecycle and Super Overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tkk);

        //show Splashscreen and progress indicator
        fm = getFragmentManager();
        displaySplashFragment();
        progBar = (ProgressBar) findViewById(R.id.progress_bar);
        progBar.setVisibility(View.VISIBLE);

        //TODO Fix interstitials
        setInterstitialAd();

        //Set up the headphone jack listener
        musicIntentReceiver = new MusicIntentReceiver(this);
        //Set up ad support
        //setupSmaato();

        //Get data model
        tuxData = tkkDataMod.getInstance(this);

    }

    @Override
    public void onBackPressed() {
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment instanceof TkkWebViewFragment &&
                ((TkkWebViewFragment) fragment).getWebview().canGoBack()) {
            ((TkkWebViewFragment) fragment).getWebview().goBack();
        } else if (fm.getBackStackEntryCount() > 1) {
            if (fragment instanceof TkkWebViewFragment){
                ((TkkWebViewFragment) fragment).getWebview().destroy();
            }
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        MenuInflater menuInflater = getMenuInflater();
        if (fragment instanceof TkkListViewFragment) {
            menuInflater.inflate(R.menu.menu_tkk, menu);
            listEditEnabled = false;
            ((TkkListViewFragment) fragment)
                    .getListView()
                    .setRearrangeEnabled(false);
        } else if (fragment instanceof TkkWebViewFragment) {
            menuInflater.inflate(R.menu.menu_webview, menu);
            MenuItem item = menu.findItem(R.id.menu_item_share);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(getString(R.string.app_icon_url)));
            shareIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, ((TkkWebViewFragment) fragment).getCurrentName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, ((TkkWebViewFragment) fragment).getCurrentUrl());
            mShareActionProvider.setShareIntent(shareIntent);
        }
        // Inflate menu resource file.

        // Locate MenuItem with ShareActionProvider



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fetch:
                progBar.setVisibility(View.VISIBLE);
                tuxData.repopulateStations();
                return true;
            case R.id.action_edit:
                listEditEnabled = !listEditEnabled;
                if (listEditEnabled) {
                    item.setChecked(true);
                } else {
                    item.setChecked(false);
                }
                TkkListViewFragment fragment =
                        ((TkkListViewFragment) fm.findFragmentById(R.id.fragment_container));
                fragment.getListView()
                        .setRearrangeEnabled(listEditEnabled);
                setDeleteButtons(fragment);
                return true;
            case R.id.action_about:
                displayAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause(){
        unregisterReceiver(musicIntentReceiver);
        sendNotification();
        super.onPause();
    }

    @Override
    public void onResume(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(musicIntentReceiver, filter);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        super.onResume();
    }

    @Override
    public void onDestroy(){
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        super.onDestroy();
    }
    //endregion

    //region Description: Fragment handling

    private void displaySplashFragment(){
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new SplashFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }


    //Displays the About screen
    private void displayAbout() {
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (!(fragment instanceof SplashFragment)) {
            fragment = new SplashFragment();
            fm.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack("About")
                    .commit();
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }
            }
        };
        handler.postDelayed(r, 8000);
    }

    private void displayListView(){
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (!(fragment instanceof TkkListViewFragment)) {
            fragment = new TkkListViewFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack("ListView")
                    .commit();
        }
    }

    private void displayWebView(tkkStation station){
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (!(fragment instanceof TkkWebViewFragment)) {
            fragment = new TkkWebViewFragment();
            Bundle args = new Bundle();
            args.putString("uri", station.getUri().toString());
            args.putString("name", station.getName());
            //TODO ts code, remove
                Log.i("icon index#############", "index put to args is " + station.getIndex());
            args.putInt("index", station.getIndex());
            fragment.setArguments(args);
            fm.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack("webView")
                    .commit();
        }
    }
    //endregion

    //region Description: Interface methods

    //region Description:Callback methods for InterstitualListener
    //TODO Check this code
    @Override
    public void onReadyToShow(){
        if(interstitial.isInterstitialReady()){
            Log.i("SMAATO_RES", "Ad available");
            interstitial.show();
        }
        Log.i("Smaato Interstitial", "Ready to show it says");
    }

    @Override
    public void onWillShow(){
        Log.i("Smaato Interstitial", "Will Show it says.");
        //Rejoice!
    }

    @Override
    public void onFailedToLoadAd(){
        Log.i("Smaato Interstitial", "Failed to load it says");
        //interstitial.destroy();
        //displayListView();
    }

    @Override
    public void onWillClose(){
        Log.i("Smaato Interstitial", "Will Close it says");
        //interstitial.destroy();
        //displayListView();
    }

    @Override
    public void onWillOpenLandingPage(){
        interstitial.destroy();
    }
    //endregion

    //Callback method for TuxedoActivityFragment.Callbacks
    @Override
    public void onStationSelected(tkkStation station) {
        displayWebView(station);
    }

    //Callback method for tkkDataMod.Callbacks
    @Override
    public void onDataLoaded(ArrayList<tkkStation> stations) {
        progBar.setVisibility(View.GONE);
        Log.i("Smaato Interstitial", "Calling asyncLoad now...");
        displayListView();
        interstitial.asyncLoadNewBanner();
    }

    //callback method for TkkWebViewFragment.Callbacks
    @Override
    public void onIconReceived(Integer idx, Bitmap icon){
        Log.i("Icon Callback", "New icon received");
        tuxData.saveIcon(idx, icon);
    }
    //endregion

    //region Description: private methods for utility
    //Method for setting visibility for delete buttons
    //Seeing as how I can't seem to make them work in the edit mode
    private void setDeleteButtons(TkkListViewFragment fragment){

        ListView listView = fragment.getListView();
        ((TkkListViewFragment.StationAdapter)(listView.getAdapter())).setShowDelete(!listEditEnabled);

        for( int i = 0; i < listView.getCount(); i++) {
            View row = listView.getChildAt(i);
            if (row != null) {
                if (listEditEnabled) {
                    row.findViewById(R.id.delete_button).setVisibility(View.GONE);
                } else {
                    row.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void sendNotification(){
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment instanceof TkkWebViewFragment){
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentText(getString(R.string.app_name))
                    .setContentText(((TkkWebViewFragment) fragment).getCurrentName());
            Intent intent = new Intent(this, TkkActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this)
                    .addParentStack(TkkActivity.class)
                    .addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(1, builder.build());
        }
    }

    //TODO Fix this code
        private void setInterstitialAd(){
            interstitial = new Interstitial(this);
            interstitial.setInterstitialAdListener(this);
            interstitial.getAdSettings().setPublisherId(0); // TODO replace with getResources().getInteger(R.integer.smaato_pub_id)
            interstitial.getAdSettings().setAdspaceId(0);  //TODO replace with getResources().getInteger(R.integer.smaato_ad_id)
        }


    private void setupSmaato(){
        BannerView bv = new BannerView(this);
        bv.setAutoReloadEnabled(true);
        bv.setAutoReloadFrequency(15);

        RelativeLayout mll = (RelativeLayout)findViewById(R.id.ad_container);
        mll.addView(bv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        bv.getAdSettings().setPublisherId(getResources().getInteger(R.integer.smaato_pub_id));
        bv.getAdSettings().setAdspaceId(getResources().getInteger(R.integer.smaato_ad_id));
        bv.asyncLoadNewBanner();
    }
}