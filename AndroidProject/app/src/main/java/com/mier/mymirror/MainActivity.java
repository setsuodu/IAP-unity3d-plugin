package com.mier.mymirror;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;

import demo.*;
import com.alipay.sdk.app.PayTask;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.Map;

public class MainActivity extends UnityPlayerActivity {
    private static final int SDK_PAY_FLAG = 1;
    private static final String RESULT_SUCCESS = "9000";
    private static final String TIP_PAY_SUCCESS = "支付成功";
    private static final String TIP_PAY_FAILED = "支付失败";

    Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    // 支付结果回调，仅作参考，以服务端确认为准!
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SDK_PAY_FLAG:
                {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    String resultInfo = payResult.getResult();
                    String resultStatus = payResult.getResultStatus();
                    if (TextUtils.equals(resultStatus, RESULT_SUCCESS))
                    {
                        Toast.makeText(MainActivity.this, TIP_PAY_SUCCESS, Toast.LENGTH_SHORT).show();
                    } else
                    {
                        Toast.makeText(MainActivity.this, TIP_PAY_FAILED, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                default:
                    break;
            }
        };
    };

    // Unity中调用
    public void Pay(String _orderInfo) {
        final String orderInfo = _orderInfo;
        Runnable payRunnable = new Runnable()
        {

            @Override
            public void run()
            {
                PayTask alipay = new PayTask(MainActivity.this);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    //示例方法一：简单的向Unity回调
    public void SayHello() {
        UnityPlayer.UnitySendMessage("Main Camera", "PluginCallBack", "Hello Unity!");
    }

    //Unity中会调用这个方法，用于区分打开摄像机，开始本地相册
    public void TakePhoto(String str)
    {
        Intent intent = new Intent(mContext,WebViewActivity.class);
        intent.putExtra("type", str);
        this.startActivity(intent);
    }
}
