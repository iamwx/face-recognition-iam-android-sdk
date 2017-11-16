package co.hypersecure.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import co.hypersecure.R;

/**
 * Created by sanchit on 06/09/17.
 */

public class Dialogs {

    public static AlertDialog showSuccessDialog(final Context context, String namePrefixText, String username, long timeToClose){
        if(((Activity) context).isFinishing())
            return null;
        final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_result_success, null, false);

        TextView namePrefixTV = (TextView) view.findViewById(R.id.tv_dialog_name_prefix);
        TextView nameTV = (TextView) view.findViewById(R.id.tv_dialog_name);

        namePrefixTV.setText(namePrefixText);
        nameTV.setText(username);

        builder.setView(view);
        final AlertDialog dialog = builder.show();

        if(timeToClose > 0) {
            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (dialog != null && dialog.isShowing() && !((Activity) context).isFinishing())
                        dialog.cancel();
                }
            }, timeToClose);
        }

        return dialog;
    }

    public static AlertDialog showFailureDialog(final Context context, String message, String buttonText, long timeToClose, final DialogListener listener){
        if(((Activity) context).isFinishing())
            return null;
        final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_result_failure, null, false);

        TextView messageTV = (TextView) view.findViewById(R.id.tv_dialog_message);
        TextView retryTV = (TextView) view.findViewById(R.id.tv_dialog_retry);
        retryTV.setText(buttonText);

        messageTV.setText(message);

        builder.setView(view);
        final AlertDialog dialog = builder.show();

        if(timeToClose > 0) {
            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (dialog != null && dialog.isShowing() && !((Activity) context).isFinishing())
                        dialog.cancel();
                }
            }, timeToClose);
        }

        if(listener != null){
            retryTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.OnButtonClick(dialog);
                    if (dialog != null && dialog.isShowing() && !((Activity) context).isFinishing())
                        dialog.dismiss();
                }
            });
        }
        else {
            //cancel on button click
            retryTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
        }

        return dialog;
    }

    public static AlertDialog showInformationDialog(final Context context, String message, String buttonText, long timeToClose, final DialogListener listener){
        if(((Activity) context).isFinishing())
            return null;
        final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_inform, null, false);

        TextView messageTV = (TextView) view.findViewById(R.id.tv_dialog_message);
        TextView retryTV = (TextView) view.findViewById(R.id.tv_dialog_retry);
        retryTV.setText(buttonText);

        messageTV.setText(message);

        builder.setView(view);
        final AlertDialog dialog = builder.show();

        if(timeToClose > 0) {
            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (dialog != null && dialog.isShowing() && !((Activity) context).isFinishing())
                        dialog.cancel();
                }
            }, timeToClose);
        }

        if(listener != null){
            retryTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.OnButtonClick(dialog);
                    if (dialog != null && dialog.isShowing() && !((Activity) context).isFinishing())
                        dialog.dismiss();
                }
            });
        }
        else {
            //cancel on button click
            retryTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
        }

        return dialog;
    }

    public static AlertDialog showProgressDialog(Context context, String message){
        if(((Activity) context).isFinishing())
            return null;
        final AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress_basic, null, false);
        TextView messageTV = (TextView) view.findViewById(R.id.dialog_message);

        messageTV.setText(message);

        builder.setView(view);
        builder.setCancelable(false);

        return builder.show();
    }

    public interface DialogListener{
        public abstract void OnButtonClick(AlertDialog dialog);
    }
}
