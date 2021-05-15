package com.example.android.eatitserver.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.android.eatitserver.Model.Request;
import com.example.android.eatitserver.Model.User;
import com.example.android.eatitserver.Remote.IGeoCoordinates;
import com.example.android.eatitserver.Remote.RetrofitClient;

public class Common {
    public static User currentUser;
    public static Request currentRequest;

    public static final String UPDATE="Update";
    public static final String DELETE="Delete";

    public static final int PICK_IMAGE_REQUEST =71 ;
    public static final String baseUrl="https://maps.googleapis.com";

    public static final String fcmUrl="https://fcm.googleapis.com/";

    public static String convertCodeToStatus(String code) {
        String status="";
        if(code.equals("0"))
            status="Placed";
        else if(code.equals("1"))
            status="On Your Way";
        else if(code.equals("2"))
            status="Shipped";
        return status;
    }

    public static IGeoCoordinates getGeoCodeService(){
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,int newWidth,int newHeight){
        Bitmap scaledBitmap=Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);
        float scaleX=newWidth/(float)bitmap.getWidth();
        float scaleY=newHeight/(float)bitmap.getHeight();

        float pivotX=0,pivotY=0;

        Matrix scaleMatrix=new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas=new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null){
            NetworkInfo[] info=connectivityManager.getAllNetworkInfo();
            if(info!=null){
                for (int i=0;i<info.length;i++){
                    if(info[i].getState()==NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

}
