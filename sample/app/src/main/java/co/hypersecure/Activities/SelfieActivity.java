package co.hypersecure.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import co.hypersecure.Utilities.Dialogs;
import co.hypersecure.Helpers.SPUtils;
import co.hypersecure.R;
import co.hypersecure.Utilities.Constants;
import co.hypersecure.Utilities.Slog;
import co.hyperverge.hypersecuresdk.workflows.fr.Views.HVFrCamera;
import co.hyperverge.hypersecuresdk.workflows.fr.enums.FRMode;

public class SelfieActivity extends FDActivity {

    private static final String LOG_TAG = "SelfieActivity";

    private ImageView btCapture;
    private AtomicBoolean cameraFree;
    private ImageView btnSubmit;

    private HVFrCamera hvFrCamera;

    private int imageClickedCount = 0;

    public static void start(Context context) {
        Intent i = new Intent(context, SelfieActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie);

        cameraFree = new AtomicBoolean(true);

        btnSubmit = (ImageView) findViewById(R.id.btSubmit);
        btCapture = (ImageView) findViewById(R.id.btCapture);

        hvFrCamera = (HVFrCamera) findViewById(R.id.cameraFL);
        hvFrCamera.startCamera(getUserData(), FRMode.REGISTER, 0, false, true, new HVFrCamera.HVFrCameraListener() {
            @Override
            public void onFaceRecognitionResult(FRMode frMode, JSONObject result) {
                cameraFree.set(true);
                AlertDialog dialog;
                dialog = Dialogs.showSuccessDialog(SelfieActivity.this, "Successfully registered", SPUtils.getName(), 1000);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        SPUtils.setFaceRegistered();
                        LandingActivity.start(SelfieActivity.this);
                        finish();
                    }
                });
                Slog.i(LOG_TAG, result.toString());

                try {
                    if(result.has("imageUri")) {
                        JSONArray imageUris = result.getJSONArray("imageUri");
                        for(int i = 0; i < imageUris.length(); i++){
                            String imageUri = imageUris.getString(i);
                            new File(imageUri).delete();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(int errCode, String errMsg, JSONObject info) {
                hvFrCamera.pauseFR();
                cameraFree.set(true);
                AlertDialog dialog;
                dialog = Dialogs.showFailureDialog(SelfieActivity.this, "User Registration Failed", "RETRY", 2000, null);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        hvFrCamera.resumeFR();
                    }
                });
                Slog.i(LOG_TAG, errCode + " " + errMsg);
                try {
                    if (info.has("imageUri")) {
                        JSONArray imageUris = info.getJSONArray("imageUri");
                        for (int i = 0; i < imageUris.length(); i++) {
                            String imageUri = imageUris.getString(i);
                            new File(imageUri).delete();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCaptureCallback(boolean isSuccess, int errCode, String errMsg) {
                cameraFree.set(true);
                if(isSuccess){
                    imageClickedCount += 1;
                    findViewById(R.id.tv_camera_clicked_count).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.tv_camera_clicked_count)).setText(imageClickedCount + "");
                    btnSubmit.setVisibility(View.VISIBLE);
                }
                else{
                    Slog.i(LOG_TAG, "Error in capture: " + errCode + "::" + errMsg);
                    AlertDialog dialog;
                    if(errCode == 4){
                        dialog = Dialogs.showFailureDialog(SelfieActivity.this, "Face could not be detected. Try Again", "OK", 2000, null);
                    }
                    else if(errCode != 3){
                        //errCode 3 means that camera was not free. Trying again should definitely solve the problem
                        dialog = Dialogs.showFailureDialog(SelfieActivity.this, "Unable to capture. Try again later", "OK", 2000, null);
                    }
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hvFrCamera.submit();
            }
        });

        btCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageClickedCount == 5){
                    Toast.makeText(SelfieActivity.this, "You can only add upto 5 faces", Toast.LENGTH_LONG).show();
                    return;
                }
                if (cameraFree.get()) {
                    cameraFree.set(false);
                    flashScreen();
                    hvFrCamera.capture();
                }
            }
        });

        hideSystem();
    }

    private JSONObject getUserData(){
        JSONObject userData = new JSONObject();
        try {
            userData.put("userId", SPUtils.getNumber());
            userData.put("tenantId", Constants.tenantId);
            userData.put("groupId", Constants.groupId);

            JSONObject userDetails = new JSONObject();
            userDetails.put("userName", SPUtils.getName());

            userData.put("userInfo", userDetails.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userData;
    }

    private void hideSystem() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    @Override
    protected void onResume() {
        super.onResume();
        hvFrCamera.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hvFrCamera.pause();
    }

    private void flashScreen() {
        AlphaAnimation animation = new AlphaAnimation(0.6f, 0.0f);
        animation.setDuration(300);
        animation.setInterpolator(new DecelerateInterpolator(2));
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//                vFlash.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                vFlash.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}
