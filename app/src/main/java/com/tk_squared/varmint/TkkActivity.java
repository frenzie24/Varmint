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

//Millennial Media Ad Support
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;


//Facebook
import com.facebook.FacebookSdk;
import com.smaato.soma.BannerView;
import com.smaato.soma.interstitial.Interstitial;
import com.smaato.soma.interstitial.InterstitialAdListener;

/**
 * Created by zengo on 1/30/2016.
 * You know it Babe!
 */
public class TkkActivity extends AppCompatActivity
        implements TkkListViewFragment.Callbacks, tkkDataMod.Callbacks,
        LoginFragment.Callbacks, TkkWebViewFragment.Callbacks, InterstitialAdListener {

    //region Description: Variables and Accessors
    private tkkDataMod tuxData;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    public tkkDataMod getData() {
        return tuxData;
    }
    public void setData(tkkDataMod data) {
        tuxData = data;
    }
    //private ArrayList<tkkStation> tkkData;
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
    private CallbackManager callbackManager;
    public CallbackManager getCallbackManager() {return callbackManager;}
    private MusicIntentReceiver musicIntentReceiver;
    public boolean isLoggedIn(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }
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
        progBar = (ProgressBar) findViewById(R.id.progress_bar);
        progBar.setVisibility(View.VISIBLE);
        fm = getFragmentManager();
        displaySplashFragment();

        //TODO Fix interstitials
        setInterstitialAd();
        interstitial.asyncLoadNewBanner();

        musicIntentReceiver = new MusicIntentReceiver(this);

        //Set up ad support
        setupSmaato();
        //Initialize Facebook
        setupFacebook();

        //Get data model
        tuxData = tkkDataMod.getInstance(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

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
        }
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
            case R.id.action_facebook_share:
                ((TkkWebViewFragment)fm.findFragmentById(R.id.fragment_container)).onShareStation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "@string/web_page_title",
                Uri.parse("@string/web_page_url"),
                Uri.parse(getString(R.string.deep_url))
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "@string/web_page_title",
                Uri.parse("@string/web_page_url"),
                Uri.parse("android-app://com.tk_squared.varmint/http/www.tk-squared.com/Varmint")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onPause(){
        unregisterReceiver(musicIntentReceiver);
        sendNotification();
        super.onPause();
    }

    @Override
    public void onResume(){
        if (fm.findFragmentById(R.id.fragment_container) instanceof SplashFragment){
            interstitial.asyncLoadNewBanner();
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(musicIntentReceiver, filter);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        super.onResume();
    }
    //endregion

    //region Description: Fragment handling
    private void displayLoginFragment(){
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (!(fragment instanceof LoginFragment)){
            fragment = new LoginFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

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
        interstitial.show();
    }

    @Override
    public void onWillShow(){
        //Rejoice!
    }

    @Override
    public void onFailedToLoadAd(){
        interstitial.destroy();
    }

    @Override
    public void onWillClose(){
        interstitial.destroy();
    }

    @Override
    public void onWillOpenLandingPage(){
        interstitial.destroy();
    }
    //endregion

    //Callback method for LoginFragment.Callbacks
    @Override
    public void onLoginFinish() {
        displayListView();
    }

    //Callback method for TuxedoActivityFragment.Callbacks
    @Override
    public void onStationSelected(tkkStation station) {
        displayWebView(station);
    }

    //Callback method for tkkDataMod.Callbacks
    @Override
    public void onDataLoaded(ArrayList<tkkStation> stations) {
        //Set data and switch to Facebook login fragment
        progBar.setVisibility(View.GONE);
        if (isLoggedIn()){
            onLoginFinish();
        } else {
            displayLoginFragment();
        }
    }

    //callback method for TkkWebViewFragment.Callbacks
    @Override
    public void onIconReceived(Integer index, Bitmap icon){
        Log.i("Icon Callback", "New icon received");
        tuxData.saveIcon(index, icon);
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
        interstitial.getAdSettings().setPublisherId(1100018452);
        interstitial.getAdSettings().setAdspaceId(130097262);
    }

    private void setupFacebook(){
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.tk_squared.popcorn",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.i("KeyHash:", something);
                Log.i("KeyHash:", something);
                Log.i("KeyHash:", something);
                Log.i("KeyHash:", something);
                Log.i("KeyHash:", something);
                Log.i("KeyHash:", something);
                Log.i("KeyHash:", something);
                Log.i("KeyHash:", something);
                Log.i("KeyHash:", something);

            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }
    //endregion

    private void setupSmaato(){
        BannerView bv = new BannerView(this);
        bv.setAutoReloadEnabled(true);
        bv.setAutoReloadFrequency(15);

        RelativeLayout mll = (RelativeLayout)findViewById(R.id.ad_container);
        mll.addView(bv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        bv.getAdSettings().setPublisherId(1100018452);
        bv.getAdSettings().setAdspaceId(130087931);
        bv.asyncLoadNewBanner();
    }
}