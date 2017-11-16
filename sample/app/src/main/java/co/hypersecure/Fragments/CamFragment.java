package co.hypersecure.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import co.hypersecure.Activities.FDActivity;
import co.hypersecure.Helpers.SPUtils;
import co.hypersecure.Utilities.Dialogs;
import co.hypersecure.R;
import co.hypersecure.Utilities.Constants;
import co.hypersecure.Utilities.Slog;
import co.hyperverge.hypersecuresdk.workflows.fr.Utilities.HVFRError;
import co.hyperverge.hypersecuresdk.workflows.fr.Views.HVFrCamera;
import co.hyperverge.hypersecuresdk.workflows.fr.enums.FRMode;

/**
 * Created by Awanish Raj on 13/01/16.
 */
public class CamFragment extends Fragment implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    private static final String LOG_TAG = "FDCameraFragment";

    private ImageView btCapture;

    private HVFrCamera hvFrCamera;
    private View vFlash;
    private AtomicBoolean cameraFree;
    private Boolean autoCaptureEnabled = false;
    private Toolbar myToolbar;
    Menu menu;

    public CamFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CamFragment newInstance() {
        return new CamFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera_new, container, false);

        cameraFree = new AtomicBoolean(true);
        setupCamera(rootView);

        rootView.findViewById(R.id.cancel_button).setOnClickListener(this);

        myToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((FDActivity)getActivity()).setSupportActionBar(myToolbar);

        myToolbar.setOnMenuItemClickListener(CamFragment.this);

        return rootView;
    }

    private void setupCamera(View rootView) {

        btCapture = (ImageView) rootView.findViewById(R.id.btCapture);
        btCapture.setOnClickListener(this);

        vFlash = rootView.findViewById(R.id.v_flash);
        vFlash.setVisibility(View.GONE);

        hvFrCamera = (HVFrCamera) rootView.findViewById(R.id.cameraFL);
        hvFrCamera.startCamera(getUserData(), FRMode.VERIFICATION, 0, autoCaptureEnabled, true, new HVFrCamera.HVFrCameraListener() {
            @Override
            public void onFaceRecognitionResult(FRMode frMode, JSONObject result) {
                cameraFree.set(true);
                hvFrCamera.pauseFR();
                String name;
                ArrayList<String> imageUriList = new ArrayList<>();
                try {
                    name = result.getString("userId");
                    if(result.has("userInfo")){
                        try {
                            JSONObject info = new JSONObject(result.getString("userInfo"));
                            name = info.getString("userName");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(result.has("imageUri")) {
                        JSONArray imageUris = result.getJSONArray("imageUri");
                        for(int i = 0; i < imageUris.length(); i++){
                            String imageUri = imageUris.getString(i);
                            imageUriList.add(imageUri);
                            new File(imageUri).delete();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    onError(HVFRError.Error.ERROR_FACE_RECOGNITION.getErrCode(), "Face could not be Recognized", result);
                    return;
                }

                final AlertDialog dialog = Dialogs.showSuccessDialog(getActivity(), "Welcome", name, 1500);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        hvFrCamera.resumeFR();
                    }
                });
            }

            @Override
            public void onError(int errCode, String errMsg, JSONObject info) {
                hvFrCamera.pauseFR();
                cameraFree.set(true);
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

                if(errCode == 1){
                    final AlertDialog dialog = Dialogs.showFailureDialog(getActivity(), "Initialization Error", "GO BACK", 0, new Dialogs.DialogListener() {
                        @Override
                        public void OnButtonClick(AlertDialog dialog) {
                            dialog.cancel();
                            getActivity().finish();
                        }
                    });
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            hvFrCamera.resumeFR();
                        }
                    });
                    return;
                }

                if(errCode == 2){
                    final AlertDialog dialog = Dialogs.showFailureDialog(getActivity(), "Face could not be Detected", "GO BACK", 0, new Dialogs.DialogListener() {
                        @Override
                        public void OnButtonClick(AlertDialog dialog) {
                            dialog.cancel();
                            getActivity().finish();
                        }
                    });
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            hvFrCamera.resumeFR();
                        }
                    });
                    return;
                }

                final AlertDialog dialog = Dialogs.showFailureDialog(getActivity(), "Failed to Authenticate", "RETRY", 0, new Dialogs.DialogListener() {
                    @Override
                    public void OnButtonClick(AlertDialog dialog) {
                        hvFrCamera.resumeFR();
                    }
                });
            }

            @Override
            public void onCaptureCallback(boolean b, int i, String s) {

            }
        });
    }

    private void updateAutoCapture(){
        hvFrCamera.pauseFR();
        hvFrCamera.setAutoCaptureEnabled(autoCaptureEnabled);
        hvFrCamera.resumeFR();
    }

    private JSONObject getUserData(){
        JSONObject userData = new JSONObject();
        try {
            userData.put("tenantId", Constants.tenantId);
            userData.put("groupId", Constants.groupId);
            userData.put("userId", SPUtils.getNumber());
            JSONObject info = new JSONObject();
            info.put("userName", SPUtils.getName());
            userData.put("userInfo", info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userData;
    }


    private void safeCapture() {
        if (cameraFree.get() && !autoCaptureEnabled) {
            cameraFree.set(false);
            flashScreen();
            hvFrCamera.capture();
        }
    }

    public void setMenu(Menu menu){
        this.menu = menu;
        if(autoCaptureEnabled){
            menu.findItem(R.id.action_enable_auto_capture).setEnabled(false);
            menu.findItem(R.id.action_enable_auto_capture).setChecked(true);
            menu.findItem(R.id.action_disable_auto_capture).setEnabled(true);
            menu.findItem(R.id.action_disable_auto_capture).setChecked(false);
        }
        else {
            menu.findItem(R.id.action_enable_auto_capture).setEnabled(true);
            menu.findItem(R.id.action_enable_auto_capture).setChecked(false);
            menu.findItem(R.id.action_disable_auto_capture).setEnabled(false);
            menu.findItem(R.id.action_disable_auto_capture).setChecked(true);
        }
    }

    private void flashScreen() {
        AlphaAnimation animation = new AlphaAnimation(0.6f, 0.0f);
        animation.setDuration(300);
        animation.setInterpolator(new DecelerateInterpolator(2));
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                vFlash.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vFlash.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        vFlash.startAnimation(animation);
    }

    private void autoCaptureEnableDisbleBtnClicked(MenuItem menuItem){
        if(menuItem.getItemId() == R.id.action_enable_auto_capture){
            if(!autoCaptureEnabled && menuItem.isEnabled()) {
                autoCaptureEnabled = true;
                updateAutoCapture();
                menuItem.setChecked(true);
                menuItem.setEnabled(false);
                menu.findItem(R.id.action_disable_auto_capture).setEnabled(true);
                menu.findItem(R.id.action_disable_auto_capture).setChecked(false);
            }
        }
        else{
            if(autoCaptureEnabled && menuItem.isEnabled()) {
                autoCaptureEnabled = false;
                updateAutoCapture();
                menuItem.setChecked(true);
                menuItem.setEnabled(false);
                menu.findItem(R.id.action_enable_auto_capture).setEnabled(true);
                menu.findItem(R.id.action_enable_auto_capture).setChecked(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hvFrCamera.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        hvFrCamera.pause();
    }

    public void onCancelClick() {
        getActivity().finish();
    }

    public void onVolumeKeyPressed() {
        safeCapture();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btCapture:
                safeCapture();
                break;
            case R.id.cancel_button:
                onCancelClick();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_enable_auto_capture:
            case R.id.action_disable_auto_capture:
                autoCaptureEnableDisbleBtnClicked(item);
                break;
        }
        return false;
    }
}
