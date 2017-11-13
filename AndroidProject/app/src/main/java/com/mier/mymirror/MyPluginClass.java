package com.mier.mymirror;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;
import android.view.View;

import com.alipay.sdk.app.PayTask;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.Map;

import demo.BadgeUtil;
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

    //GPS
    LocationManager locationManager;
    public boolean checkGPSIsOpen() {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        return isOpen;
    }

    public void openGPSSetting() {
        //1、提示用户打开定位服务；2、跳转到设置界面
        Toast.makeText(getActivity(), "无法定位，请打开定位服务", Toast.LENGTH_SHORT).show();
        Intent i = new Intent();
        i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
    }

    //Badge
    public String ShortCut()
    {
        if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi")){
            //小米
            //xiaoMiShortCut(context, clazz, num);
            return "小米";
        }else if(Build.MANUFACTURER.equalsIgnoreCase("samsung")){
            //三星
            //samsungShortCut(context, num);
            return "三星";
        }else {//其他原生系统手机
            //installRawShortCut(context, MainActivity.class, isShowNum, num, isStroke);
            return "其他原生系统手机";
        }
    }

    public void SendBadge(){
        BadgeUtil.setBadgeCount(getActivity().getApplicationContext(), 1);
    }

    public void CleanBadge(){
        BadgeUtil.resetBadgeCount(getActivity().getApplicationContext());
    }

    //拷贝String到剪贴板
    public void onClickCopy(String str) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", str); //Label是任意文字标签
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }

    //粘贴
    public String onClickPaste(){
        ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        String result = "";
        ClipData clipData = cm.getPrimaryClip();
        //result = cm.toString(); //ClipData{ text/plain "Label"{T:"str"}}; //取出的是ClipData
        //result = cm.getText().toString(); //"str" //方法deprecated
        ClipData.Item item = clipData.getItemAt(0); //这里获取第一条，也可以用遍历获取任意条
        CharSequence charSequence = item.coerceToText(getActivity().getApplicationContext());
        return result;
    }
}