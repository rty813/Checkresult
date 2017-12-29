package com.rty813.zhang.checkresult;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    public static final String ACTION_GET_DATA = "com.rty813.zhang.checkresult.ACTION_GET_DATA";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Receiver!", Toast.LENGTH_SHORT).show();
    }
}
