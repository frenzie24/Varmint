package com.tk_squared.varmint;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ZengoTim on 3/29/16.
 * Rockin your world one bit at a time.
 */
public class MusicIntentReceiver extends BroadcastReceiver {
    private Activity activity;

    public MusicIntentReceiver(){}

    public MusicIntentReceiver(Activity activity){
        this.activity = activity;
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            FragmentManager fm = activity.getFragmentManager();
            switch (state) {
                case 0:
                    //Headset was unplugged, back off the music
                    if (fm.getBackStackEntryCount() > 1) {
                        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
                        if (fragment instanceof TkkWebViewFragment) {
                            ((TkkWebViewFragment) fragment).getWebview().destroy();
                        }
                        fm.popBackStack();
                    }
                    break;
                case 1:
                    //Headset was plugged in, who cares?
                    break;
                default:
                    //If ya don't know what to do, then don't do it.
            }
        }
    }
}
