package com.mier.mymirror;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.Map;

import demo.PayResult;

public class MyPluginClass extends Fragment
{
    private static final String TAG = "MyPlugin";
    private static MyPluginClass Instance = null;
    private String gameObjectName;

    public static MyPluginClass GetInstance(String gameObject)
    {
        if(Instance == null)
        {
            Instance = new MyPluginClass();
            Instance.gameObjectName = gameObject;
            UnityPlayer.currentActivity.getFragmentManager().beginTransaction().add(Instance, TAG).commit();
        }
        return Instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);  // 这一句很重要，保存对该Fragment的引用，防止在旋转屏幕等操作时时丢失引用（Fragment隶属于Activity）
    }
    //示例方法一：简单的向Unity回调
    public void SayHello()
    {
        UnityPlayer.UnitySendMessage(gameObjectName,"PluginCallBack","Hello Unity!");
    }
    //示例方法二：计算传入的参数并返回计算结果
    public int CalculateAdd(int one, int another)
    {
        return one + another;
    }

    ///
    private static final int SDK_PAY_FLAG = 1;
    private static final String RESULT_SUCCESS = "9000";
    private static final String TIP_PAY_SUCCESS = "支付成功";
    private static final String TIP_PAY_FAILED = "支付失败";

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
                        Toast.makeText(getActivity(), TIP_PAY_SUCCESS, Toast.LENGTH_SHORT).show();
                    } else
                    {
                        Toast.makeText(getActivity(), TIP_PAY_FAILED, Toast.LENGTH_SHORT).show();
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
                PayTask alipay = new PayTask(getActivity());
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

    public String AlipayClient(String _orderInfo) {
        final String orderInfo = _orderInfo;
        Runnable payRunnable = new Runnable()
        {

            @Override
            public void run()
            {
                PayTask alipay = new PayTask(getActivity());
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
        return orderInfo;
    }
}