package com.example.android.eatitserver.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.android.eatitserver.Model.Request;
import com.example.android.eatitserver.OrderStatus;
import com.example.android.eatitserver.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class ListenOrder extends Service implements ChildEventListener {
    FirebaseDatabase database;
    DatabaseReference orders;

    @Override
    public void onCreate() {
        super.onCreate();

        database=FirebaseDatabase.getInstance();
        orders=database.getReference("Requests");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        orders.addChildEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public ListenOrder() {
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        //Trigger here
        Request request=dataSnapshot.getValue(Request.class);
        if(request.getStatus().equals("0"))
            showNotification(dataSnapshot.getKey(),request);
    }

    private void showNotification(String key, Request request) {
        Intent intent=new Intent(getBaseContext(), OrderStatus.class);
        //intent.putExtra("userPhone",request.getPhone());
        PendingIntent contentIntent=PendingIntent.getActivity(getBaseContext(),0,intent,0);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("Eat It")
                .setContentTitle("you have new order #"+key)
                .setContentIntent(contentIntent)
                .setContentInfo("New Order")
                .setSmallIcon(R.drawable.logo_eat_it);
        NotificationManager notificationManager=(NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //if you want to show multiple notification at same time, you need to give unique id for each notification
        int randomInt=new Random().nextInt(9999-1)+1;
        notificationManager.notify(randomInt,builder.build());
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
