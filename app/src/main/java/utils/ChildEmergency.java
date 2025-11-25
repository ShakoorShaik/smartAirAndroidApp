package utils;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.example.smartair.child.emergency.Emergency;

public class ChildEmergency {
    public static void emergencyPrompt(Activity activity){
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle("ARE YOU HAVING TROUBLE BREATHING?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivity(new Intent(activity, Emergency.class));
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

}


