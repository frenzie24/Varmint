package com.tk_squared.varmint;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

/**
 * Created by Tim on 1/4/2016.
 * 'Cuz Tim rocks.
 */

@SuppressLint("SetJavaScriptEnabled")
public class TkkWebViewFragment extends Fragment{

    //region Description: Variables and Constructor and Callbacks
    private WebView webview; public WebView getWebview(){ return webview;}
    private ShareDialog shareDialog;
    private ShareLinkContent linkContent;
    private String currentUrl;
    private String currentName; public String getCurrentName() {return currentName;}
    private Integer currentIndex;

    public interface Callbacks {
        void onIconReceived(Integer index, Bitmap icon);
    }
    public TkkWebViewFragment(){}

    //endregion

    //region Description: Lifecycle and Super Overrides
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        //Make a toolbar
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.webview_toolbar);
        toolbar.setSubtitle(R.string.subtitle);
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.setSupportActionBar(toolbar);
        //Initialize the Facebook Share Dialog
        shareDialog = new ShareDialog(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        setupWebView();
        //Called to set up share dialog
        prepShareDialog();
    }

    @Override
    public void onDestroy(){
        webview.clearCache(true);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        ((TkkActivity)getActivity()).getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }
    //endregion

    //region Description: private methods for setups
    private void setupWebView(){
        //Setup the WebView
        if (webview == null) {
            webview = (WebView) getView().findViewById(R.id.webview_view);
            webview.getSettings().setJavaScriptEnabled(true);
            currentName = getArguments().getString("name");
            currentUrl = getArguments().getString("uri");
            currentIndex = getArguments().getInt("index");
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return false;
                }
            });
            webview.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedIcon(WebView webView, Bitmap icon){
                    ((TkkWebViewFragment.Callbacks)getActivity()).onIconReceived(currentIndex, icon);
                }
            });

            webview.loadUrl(currentUrl);
            Log.i("URL", currentUrl);
        } else {
            Log.i("WebView: ", "webview isn't null, bro");
        }
    }

    public void onShareStation(){
        shareDialog.show(linkContent);
    }

    public void prepShareDialog(){
        final TkkActivity tkkActivity = (TkkActivity)getActivity();
        try {
            if (ShareDialog.canShow(ShareLinkContent.class)){
                //create the post

                String description = "Listen to " + currentName + " on " + getString(R.string.app_name) + "!";
                linkContent = new ShareLinkContent.Builder()
                        .setContentTitle(currentName)
                        .setContentDescription(description)
                        .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=" + getString(R.string.app_id_string)))
                        .setImageUrl(Uri.parse(getString(R.string.app_icon_url)))
                        .build();
                //Sharing callbacks
                shareDialog.registerCallback(tkkActivity.getCallbackManager(), new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result shareResult) {
                        Log.i("Share Success", "Shared to facebook");
                    }

                    @Override
                    public void onCancel() {
                        Log.i("Cancel", "Canceled");
                        try {
                            Log.i("Webview Title: ", webview.getTitle());
                        } catch(Exception e) {
                            Log.i("webview.getTitle(): ", e.toString());
                        }
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.i("Error", "Error");
                    }
                });

            }
        } catch (Exception e) {
            Log.e("ShareDialogError: ", e.toString());
        }
    }
    //endregion
}