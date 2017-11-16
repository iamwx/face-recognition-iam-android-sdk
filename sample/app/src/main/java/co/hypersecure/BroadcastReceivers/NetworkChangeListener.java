package co.hypersecure.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import co.hypersecure.Activities.LandingActivity;
import co.hypersecure.Utilities.ConnectivityUtil;

public class NetworkChangeListener extends BroadcastReceiver {
    private static final String LOG_TAG = "NetworkChange";

    @Override
    public void onReceive(Context context, Intent intent) {
        LandingActivity.networkStatusChanged(context, ConnectivityUtil.isConnected(context));
    }
}