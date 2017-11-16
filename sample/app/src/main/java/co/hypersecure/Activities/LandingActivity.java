package co.hypersecure.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.hypersecure.Helpers.SPUtils;
import co.hypersecure.R;
import co.hypersecure.Utilities.ConnectivityUtil;

public class LandingActivity extends FDActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    //private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SPUtils.isUserLoggedIn()) {
            GetStartedPage.start(this);
            finish();
            return;
        }

        if (!SPUtils.isFaceRegistered()) {
            SelfieActivity.start(this);
            finish();
            return;
        }

        setContentView(R.layout.activity_landing);

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LandingActivity.this.registerReceiver(networkChangeMessageReceiver, new IntentFilter("network_status_changed"));

        onInternetConnectivityChanged(ConnectivityUtil.isConnected(LandingActivity.this));

    }

    @Override
    protected void onPause() {
        LandingActivity.this.unregisterReceiver(networkChangeMessageReceiver);
        super.onPause();
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    public static void start(Context context) {
        Intent i = new Intent(context, LandingActivity.class);
        context.startActivity(i);
    }

    public static Intent getLauncherIntent(Context mContext) {
        Intent intent = new Intent(mContext, LandingActivity.class);
        return intent;
    }

    public static void networkStatusChanged(Context context, boolean isConnected){
        Intent intent = new Intent("network_status_changed");

        //put whatever data you want to send, if any
        intent.putExtra("newStatus", isConnected);

        //send broadcast
        context.sendBroadcast(intent);
    }

    public void onEnterRoomButtonClick(View view) {
        if(ConnectivityUtil.isConnected(LandingActivity.this)) {
            AuthActivity.start(LandingActivity.this);
        }
    }

    public void onInternetConnectivityChanged(boolean isConnected){
        if(isConnected){
            LinearLayout btnLL = (LinearLayout) findViewById(R.id.ll_btn_enter_room);
            btnLL.setBackground(getDrawable(R.drawable.shape_circle_enter_room));

            ImageView doorImage = (ImageView) findViewById(R.id.iv_enter_room_info);
            doorImage.setImageDrawable(getDrawable(R.drawable.ic_enterroom_opendoor));

            TextView textView = (TextView) findViewById(R.id.tv_enter_room);
            textView.setText("ENTER ROOM");
        }
        else{

            LinearLayout btnLL = (LinearLayout) findViewById(R.id.ll_btn_enter_room);
            btnLL.setBackground(getDrawable(R.drawable.shape_circle_no_internet));

            ImageView doorImage = (ImageView) findViewById(R.id.iv_enter_room_info);
            doorImage.setImageDrawable(getDrawable(R.drawable.ic_error_internet_alert));

            TextView textView = (TextView) findViewById(R.id.tv_enter_room);
            textView.setText("NO INTERNET");
        }
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver networkChangeMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
            boolean newStatus = intent.getBooleanExtra("newStatus", true);
            onInternetConnectivityChanged(newStatus);

            //do other stuff here
        }
    };
}
