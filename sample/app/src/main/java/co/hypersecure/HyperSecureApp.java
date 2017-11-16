package co.hypersecure;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import co.hypersecure.Helpers.SPUtils;
import co.hypersecure.Utilities.Constants;
import co.hyperverge.hypersecuresdk.HyperSecureSDK;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class HyperSecureApp extends Application {

    private volatile static HyperSecureApp instance = null;

    public static synchronized HyperSecureApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SPUtils.init(this);
        Fabric.with(this, new Crashlytics());
        instance = this;

        if(Constants.tenantId.isEmpty() || Constants.tenantKey.isEmpty() || Constants.adminToken.isEmpty() || Constants.groupId.isEmpty()){
            Log.e("HyperSecureApp", "This sample code needs tenantId, tenantKey, adminToken and groupId to work. Contact HyperVerge at contact@hyperverge.co for getting these credentials");
        }
        HyperSecureSDK.init(this.getApplicationContext(), Constants.tenantId, Constants.tenantKey, Constants.adminToken);
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("Roboto_regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()
        );

    }
}
