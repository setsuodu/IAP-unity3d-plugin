package com.mier.mymirror;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity {

    Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    //Unity中会调用这个方法，用于区分打开摄像机，开始本地相册
    public void TakePhoto(String str)
    {
        Intent intent = new Intent(mContext,WebViewActivity.class);
        intent.putExtra("type", str);
        this.startActivity(intent);
    }
}
