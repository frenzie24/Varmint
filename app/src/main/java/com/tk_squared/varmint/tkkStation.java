package com.tk_squared.varmint;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

/**
 * Created by Kevin for tk^2 on 12/29/2015.
 * science bitches
 */
public class tkkStation {
    private long id;
    private Uri uri;
    private String name;
    private Uri iconUri;
    private BitmapDrawable icon;
    private int index;

    public tkkStation(){

    }

    //I DID THIS!! TIM!!
    public Uri getIconURI(){
        return Uri.parse("http://www.tshirthell.com/favicon.ico");
    }

    public tkkStation(long _id, String n, BitmapDrawable b, Uri u) {
        id = _id;
        uri = u;
        icon = b;
        name = n;
    }

    public tkkStation(int idx, long _id, String n, Uri u) {
        id = _id;
        index = idx;
        uri = u;
        name = n;
    }

    public tkkStation(int idx, String n, BitmapDrawable b, Uri u) {

        index = idx;
        uri = u;
        name = n;
        icon = b;
    }

    public tkkStation(int idx, long _id, Uri iU, String n, Uri u) {
        id = _id;
        index = idx;
        uri = u;
        name = n;
        iconUri = iU;
    }

    public BitmapDrawable getIcon() { return icon; }

    public void setIcon(BitmapDrawable i) { icon = i; }

    public Uri getIconUri(){
        return iconUri;
    }

    public void setIconUri(Uri iU){
        iconUri = iU;
    }

    public long getId() { return id; }

    public void setId(long _id) { id = _id; }

    public int getIndex(){
        return index;
    }

    public void setIndex(int idx) {
        index = idx;
    }

    public String getName(){
        return name;
    }

    public void setName(String n){
        name = n;
    }

    public Uri getUri(){
        return uri;
    }

    public void setUri(Uri u){
        uri = u;
    }

}