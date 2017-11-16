package co.hypersecure.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import co.hypersecure.Helpers.SPUtils;
import co.hypersecure.R;
import co.hypersecure.Utilities.Constants;
import co.hypersecure.Utilities.Dialogs;
import co.hypersecure.Utilities.Urls;
import co.hypersecure.Views.PrefixedEditText;
import co.hyperverge.hypersecuresdk.workflows.operation.HVOperationManager;


public class LoginActivity extends FDActivity {

    private AutoCompleteTextView mNameView;
    private PrefixedEditText mNumberView;

    private Button tvLogin;

    private String COUNTRY_CODE = "+91";

    public static void start(Context context) {
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mNameView = (AutoCompleteTextView) findViewById(R.id.email);

        mNumberView = (PrefixedEditText) findViewById(R.id.phoneNumber);
        mNumberView.setPrefix(COUNTRY_CODE);
        mNumberView.setPrefixTextColor(getResources().getColor(R.color.colorGrayText));
        mNumberView.setText("");

        tvLogin = (Button) findViewById(R.id.tvLogin);

        mNumberView.addTextChangedListener(phonetextWatcher);
        mNameView.addTextChangedListener(textWatcher);

        tvLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkForLogin();

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher phonetextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkForLogin();

        }

        @Override
        public void afterTextChanged(Editable s) {
            String number = s.toString();
            if (number.contains("+91")) {
                number = number.substring(3, number.length());
                mNumberView.setText(number);
            }

        }
    };

    public void checkForLogin() {
        String name = mNameView.getText().toString();
        String number = mNumberView.getText().toString();

        if(name.isEmpty() || !isNameValid(name) || number.isEmpty() || !isNumberValid(number)){
            tvLogin.setEnabled(false);
            tvLogin.setBackgroundTintList(getResources().getColorStateList(R.color.colorGray));
        }
        else{
            tvLogin.setEnabled(true);
            tvLogin.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        }
    }

    private void attemptLogin() {

        // Reset errors.
        mNameView.setError(null);
        mNumberView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString().trim();
        String number = mNumberView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(number) && !isNumberValid(number)) {
            mNumberView.setError("Invalid Number");
            focusView = mNumberView;
            cancel = true;
        }

        if (TextUtils.isEmpty(number)) {
            mNumberView.setError("Number cannot be empty");
            focusView = mNumberView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            if (isNameNotTooLong(name)) {
                mNameView.setError("Name too short");
            } else {
                mNameView.setError("Name too long");
            }
            focusView = mNameView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            number = COUNTRY_CODE + number;
            checkUserExist(number, name);

        }
    }

    private boolean isNameValid(String name) {
        //TODO: Replace this with your own logic
        return name.trim().length() > 3 && isNameNotTooLong(name);
    }

    private boolean isNameNotTooLong(String name) {
        //TODO: Replace this with your own logic
        return name.length() <= 18;
    }

    private boolean isNumberValid(String number) {
        //TODO: Replace this with your own logic
        return number.length() == 10;
    }

    public void checkUserExist(final String number, final String name){
        verifyUserExistAsync(number, new VerifyUserCallback() {
            @Override
            public void onUserVerificationResponse(int response) {
                switch (response){
                    case 0:
                        Dialogs.showFailureDialog(LoginActivity.this, "Could not connect to Server", "RETRY", 0, new Dialogs.DialogListener() {
                            @Override
                            public void OnButtonClick(AlertDialog dialog) {
                                checkUserExist(number, name);
                            }
                        });
                        break;
                    case 1:
                        SPUtils.setUserLoggedIn();
                        SPUtils.saveUserDetails(name, number);
                        LandingActivity.start(LoginActivity.this);
                        finish();
                        break;
                    case 2:
                        addUserToGroup(number, name);
                        break;
                    case 3:
                        Dialogs.showInformationDialog(LoginActivity.this, "User is already registered", "CONTINUE", 0, new Dialogs.DialogListener() {
                            @Override
                            public void OnButtonClick(AlertDialog dialog) {
                                SPUtils.setUserLoggedIn();
                                SPUtils.saveUserDetails(name, number);
                                SPUtils.setFaceRegistered();
                                LandingActivity.start(LoginActivity.this);
                                finish();
                            }
                        });
                        break;
                }
            }
        });
    }

    public void addUserToGroup(final String phone, final String name){
        addUserToGroupAsync(phone, new BooleanCallback() {
            @Override
            public void onBooleanResult(boolean isSuccess) {
                if(isSuccess){
                    AlertDialog dialog = Dialogs.showSuccessDialog(LoginActivity.this, "Successfully added User", name, 3000);
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            SPUtils.setUserLoggedIn();
                            SPUtils.saveUserDetails(name, phone);
                            SPUtils.setFaceRegistered();
                            LandingActivity.start(LoginActivity.this);
                        }
                    });
                }
                else{
                    Dialogs.showFailureDialog(LoginActivity.this, "Failed to add user to group", "RETRY", 0, new Dialogs.DialogListener() {
                        @Override
                        public void OnButtonClick(AlertDialog dialog) {
                            addUserToGroup(phone, name);
                        }
                    });
                }
            }
        });

    }

    private void addUserToGroupAsync(final String userPhone, final BooleanCallback callback) {
        final AlertDialog dialog = Dialogs.showProgressDialog(LoginActivity.this, "Adding user to group");
        JSONObject request = new JSONObject();
        try {
            request.put("username", userPhone);
            request.put("groupId", Constants.groupId);

            HVOperationManager.makeRequest(Urls.URL_GROUP_USER_ADD_RELATIVE, request, new HVOperationManager.HVOperationListener() {
                @Override
                public void onOperationComplete(JSONObject jsonObject) {
                    if (dialog != null && dialog.isShowing() && !LoginActivity.this.isFinishing())
                        dialog.dismiss();
                    callback.onBooleanResult(true);
                }

                @Override
                public void onError(int errCode, String errMsg) {
                    if (dialog != null && dialog.isShowing() && !LoginActivity.this.isFinishing())
                        dialog.dismiss();
                    callback.onBooleanResult(false);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            if (dialog != null && dialog.isShowing() && !LoginActivity.this.isFinishing())
                dialog.dismiss();
            callback.onBooleanResult(false);
        }
    }

    public void verifyUserExistAsync(final String userPhone, final VerifyUserCallback callback){

        final AlertDialog dialog = co.hyperverge.hypersecuresdk.common.utils.Utils.showProgressDialog(LoginActivity.this, "Verifying user");
        JSONObject request = new JSONObject();
        try {
            request.put("userId", userPhone);
            HVOperationManager.makeRequest(Urls.URL_USER_GET_RELATIVE, request, new HVOperationManager.HVOperationListener() {
                @Override
                public void onOperationComplete(JSONObject result) {
                    if (dialog != null && dialog.isShowing() && !LoginActivity.this.isFinishing())
                        dialog.dismiss();

                    int resultCode = checkUserPartOfGroup(result) ? 3: 2;
                    callback.onUserVerificationResponse(resultCode);
                }

                @Override
                public void onError(int errCode, String errMsg) {
                    if (dialog != null && dialog.isShowing() && !LoginActivity.this.isFinishing())
                        dialog.dismiss();
                    int resultCode = 0;
                    if(errCode == 615){
                        resultCode = 1;
                    }
                    callback.onUserVerificationResponse(resultCode);

                }
            });
            return;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callback.onUserVerificationResponse(0);
    }

    private boolean checkUserPartOfGroup(JSONObject result){
        try {
            JSONArray rolesArray = result.getJSONArray("groups");
            for(int i = 0; i < rolesArray.length(); i++){
                JSONObject groupRoleObj = rolesArray.getJSONObject(i);
                String groupId = groupRoleObj.getString("groupId");
                if(groupId.equals(Constants.groupId)){
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private interface BooleanCallback{
        public abstract void onBooleanResult(boolean isSuccess);
    }

    private interface VerifyUserCallback{
        public abstract void onUserVerificationResponse(int response);
    }
}

