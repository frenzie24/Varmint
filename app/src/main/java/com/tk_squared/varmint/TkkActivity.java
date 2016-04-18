package com.tk_squared.varmint;

/**************************************************************
 * *********       Ad Support Section        ******** *******
 */
//Smaato
import android.widget.RelativeLayout;
import com.smaato.soma.BannerView;
import com.smaato.soma.interstitial.Interstitial;
import com.smaato.soma.interstitial.InterstitialAdListener;
/*************************************************************
 *    *********     **********             ******    ********
 */

//BEGIN COPY-PASTE HERE!!!


/*****************************************************************
 * ***************************************************************
 * ****************Instructions for Synchronizing ****************
 * *************This File Between Ad Support Versions*************
 * ***************************************************************
 * *** Copying/Pasting between version of this file but **********
 * *** but different ad support companies can be accomplished*****
 * *** as follows: The section to be copy/pasted starts with******
 * *** this comment block and continues down to the comment*******
 * *** block marking the beginning of the Ad Support code*********
 * *** section. DO NOT copy or past over the import code above****
 * *** or the Ad Support section below the comment block which****
 * *** marks it. For Smaato Ad Support, uncomment the following:**
 * *** In the class declaration statement, uncomment the**********
 * *** interface: --> /*, InterstitialAdListener*/   /************
 * *** For One by AOL Ad Support, comment that interface**********
 * *** declaration out including the comma! For Smaato Ad*********
 * *** Support, also uncomment the line in the method*************
 * *** onDataLoaded() ---> //interstitial.asyncLoadNewBanner();***
 * *** For One by AOL comment that line back out as noted*********
 * *** That should do it!*****************************************
 * ***************************************************************
 */



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.support.v7.widget.ShareActionProvider;


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
    private Handler handler = new Handler();
    private MusicIntentReceiver musicIntentReceiver;
    private boolean interstitialShowing = false;

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
        if (progBar != null){
            progBar.setVisibility(View.VISIBLE);
        }

        setupAdSupport();

        //Set up the headphone jack listener
        musicIntentReceiver = new MusicIntentReceiver(this);

        //Get data model
        tuxData = tkkDataMod.getInstance(this);

    }

    @Override
    public void onBackPressed() {
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment instanceof TkkWebViewFragment){
            if (((TkkWebViewFragment) fragment).getWebview().canGoBack()) {
                ((TkkWebViewFragment) fragment).getWebview().goBack();
            } else  {
                ((TkkWebViewFragment) fragment).getWebview().clearCache(true);
                ((TkkWebViewFragment) fragment).getWebview().destroy();
                if (fm.getBackStackEntryCount() > 1) {
                    fm.popBackStack();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        MenuInflater menuInflater = getMenuInflater();
        //ListView Menu - edit list, get new list, about
        if (fragment instanceof TkkListViewFragment) {
            menuInflater.inflate(R.menu.menu_tkk, menu);
            listEditEnabled = false;
            ((TkkListViewFragment) fragment)
                    .getListView()
                    .setRearrangeEnabled(false);
        } else if (fragment instanceof TkkWebViewFragment) {
            //WebView Menu - share button
            menuInflater.inflate(R.menu.menu_webview, menu);
            MenuItem item = menu.findItem(R.id.menu_item_share);
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(getString(R.string.app_icon_url)));
            shareIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, ((TkkWebViewFragment) fragment).getCurrentName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, ((TkkWebViewFragment) fragment).getCurrentUrl());
            mShareActionProvider.setShareIntent(shareIntent);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Get new list
            case R.id.action_fetch:
                progBar.setVisibility(View.VISIBLE);
                tuxData.repopulateStations();
                ((ArrayAdapter)((TkkListViewFragment)fm.findFragmentById(R.id.fragment_container))
                        .getListView().getAdapter()).notifyDataSetChanged();
                return true;
            //Edit list mode
            case R.id.action_edit:
                listEditEnabled = !listEditEnabled;
                item.setChecked(listEditEnabled);
                TkkListViewFragment fragment =
                        ((TkkListViewFragment) fm.findFragmentById(R.id.fragment_container));
                fragment.getListView()
                        .setRearrangeEnabled(listEditEnabled);
                setDeleteButtons(fragment);
                return true;
            //About screen
            case R.id.action_about:
                displayAbout();
                return true;
            //The fuck that happen?
            default:
                return super.onOptionsItemSelected(item);
        }
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
        tuxData.destroyInstance();
        //This doesn't really work
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        adCleanup();
        super.onDestroy();
    }

    @Override
    public void onStop(){
        super.onStop();
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
        if (!(fragment instanceof AboutFragment)) {
            fragment = new AboutFragment();
            fm.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack("About")
                    .commit();
        }
        //Auto-return from about screen.
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }
            }
        };
        handler.postDelayed(r, getResources().getInteger(R.integer.about_screen_delay));
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
            args.putInt("index", station.getIndex());
            fragment.setArguments(args);
            fm.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack("webView")
                    .commit();
        }
    }
    //endregion

    //region Description: Interface methods



    //Callback method for TuxedoActivityFragment.Callbacks
    @Override
    public void onStationSelected(tkkStation station) {
        showInterstitial();
        displayWebView(station);
    }

    //Callback method for tkkDataMod.Callbacks
    @Override
    public void onDataLoaded(ArrayList<tkkStation> stations) {
        progBar.setVisibility(View.GONE);
        displayListView();
        interstitial.asyncLoadNewBanner();
    }

    //callback method for TkkWebViewFragment.Callbacks
    @Override
    public void onIconReceived(Integer idx, Bitmap icon){
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
            builder.setContentIntent(pendingIntent);NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(1, builder.build());
        }
    }

    /******************************************
     * ****************************************
     * ****************************************
     * ****************************************
     *
     */

    //END COPY-PASTE HERE!!!


    /***************************************************
     * ******     Ad Support Section      *****  ******
     */
    private Interstitial interstitial;

    /***************************************************
     *            ****************         ***** ******
     */

    private void showInterstitial(){
        if(interstitial.isInterstitialReady()){
            interstitial.show();
        }
    }


    private void setupAdSupport(){
        //Set up interstitial ads
        setInterstitialAd();
        //Set up banner ads
        setupSmaato();
    }

    private void setInterstitialAd(){
        interstitial = new Interstitial(this);
        interstitial.setInterstitialAdListener(this);
        interstitial.getAdSettings().setPublisherId(getResources().getInteger(R.integer.smaato_pub_id));
        interstitial.getAdSettings().setAdspaceId(getResources().getInteger(R.integer.smaato_ad_id));
    }

    private void interBannerLoad(){
        Runnable r = new Runnable(){
            @Override
            public void run(){
                interstitial.asyncLoadNewBanner();
            }
        };
        handler.postDelayed(r,getResources().getInteger(R.integer.smaato_interstitial_reload_delay));
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
        BannerView bv = new BannerView(this);
        bv.setAutoReloadEnabled(true);
        bv.setAutoReloadFrequency(getResources().getInteger(R.integer.smaato_reload_delay));

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.ad_container);
        if (relativeLayout != null){
            relativeLayout.addView(bv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        bv.getAdSettings().setPublisherId(getResources().getInteger(R.integer.smaato_pub_id));
        bv.getAdSettings().setAdspaceId(getResources().getInteger(R.integer.smaato_ad_id));
        bv.asyncLoadNewBanner();
    }

    private void adCleanup(){
        interstitial.destroy();
    }
    //endregion
}