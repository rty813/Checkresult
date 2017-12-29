package com.rty813.zhang.checkresult;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.AsyncRequestExecutor;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.rest.SimpleResponseListener;
import com.yanzhenjie.nohttp.rest.StringRequest;
import com.yanzhenjie.nohttp.rest.SyncRequestExecutor;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyReceiver extends BroadcastReceiver {
    public static final String ACTION_GET_DATA = "com.rty813.zhang.checkresult.ACTION_GET_DATA";
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        NoHttp.initialize(context);
        StringRequest request = new StringRequest("http://139.199.37.92", RequestMethod.GET);
        AsyncRequestExecutor.INSTANCE.execute(0, request, new SimpleResponseListener<String>() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSucceed(int what, Response<String> response) {
                try{
                    int num = Integer.parseInt(response.get().split("zhang:")[1].trim());
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
                    if (num > sharedPreferences.getInt("sum", 0)){
                        sharedPreferences.edit().putInt("sum", num).apply();
                        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
                                new Intent(mContext, MainActivity.class), 0);
                        Notification notification = new Notification.Builder(mContext)
                                .setContentTitle("出成绩啦！")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentIntent(pendingIntent)
                                .setVisibility(Notification.VISIBILITY_PUBLIC)
                                .build();
                        notification.flags |= Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(0, notification);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                super.onSucceed(what, response);
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                response.getException().printStackTrace();
                super.onFailed(what, response);
            }
        });
    }
}
