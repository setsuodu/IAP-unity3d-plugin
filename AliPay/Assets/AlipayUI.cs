using UnityEngine;
using UnityEngine.UI;
using System.Collections.Generic;

[System.Serializable]
public class PayInfo
{
    public string subject;  // 显示在按钮上的内容,跟支付无关系  
    public float money;     // 商品价钱  
    public string title;    // 商品描述  
}

public class AlipayUI : MonoBehaviour
{
    public Text logText, resultText;

    public string className = "com.mier.mymirror.MyPluginClass";
    public List<Button> buttons = null;
    public List<PayInfo> payInfos = null;
    private AndroidJavaObject jo = null;

    void Start()
    {
        // Init UI
        for (int i = 0; i < buttons.Count; i++)
        {
            var payInfo = payInfos[i];
            buttons[i].GetComponentInChildren<Text>().text = payInfos[i].subject;
#if UNITY_ANDROID && !UNITY_EDITOR
            buttons[i].onClick.AddListener(() =>
            {
                Alipay(payInfo);
            });
#endif
        }
        // 固定写法
        //AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        //jo = jc.GetStatic<AndroidJavaObject>("currentActivity");
        //jo.Call("SayHello");

        AndroidJavaClass jc = new AndroidJavaClass(className);
        jo = jc.CallStatic<AndroidJavaObject>("GetInstance", gameObject.name); //Main Camera
        jo.Call("SayHello"); //void SayHello()，没有返回类型
        resultText.text = jo.Call<int>("CalculateAdd", 12, 34).ToString(); //int CalculateAdd()，有返回类型
    }

    // AlipayClient是Android里的方法名字，写死.
    // payInfo.money是要付的钱，只能精确分.
    // payInfo.title是商品描述信息，注意不能有空格.
    //jo.Call("AlipayClient", payInfo.money, payInfo.title, "");
    public void Alipay(PayInfo payInfo)
    {
        //jo.Call("Pay", "商品,详情,1.0元"); //void Pay()，没有返回类型
        string res = jo.Call<string>("AlipayClient", "商品,详情,1.0元"); //void Pay()，没有返回类型
        Debug.Log("[res]" + res);
    }

    public void PluginCallBack(string text)
    {
        logText.text = text;
    }

    //检查GPS是否打开
    public void OnCheckGPS()
    {
        logText.text = "";

        bool res = jo.Call<bool>("checkGPSIsOpen");
        logText.text = "[GPS是否开启]" + res;
    }

    //跳转GPS系统设置页
    public void OnOpenGPSSettings()
    {
        jo.Call("openGPSSetting"); //void openGPSSetting()没有返回类型
    }


    //角标
    public void OnCheckOEM()
    {
        string res = jo.Call<string>("OnCheckOEM"); //void OnCheckOEM()没有返回类型
        Debug.Log(res);
    }

    public void OnSetBadge()
    {
        jo.Call("SetBadge", 3); //3作为object对象，要与java函数中类型对应
    }

    public void OnResetBadge()
    {
        jo.Call("ResetBadge"); //void OnResetBadge()没有返回类型
    }

    string url = "https://item.taobao.com/item.htm?id=560384010422&ali_refid=a3_430406_1007:1150235186:N:7597500987074971615_0_100:ce6b98fb592c27b3a4befb5a73e7d620&ali_trackid=1_ce6b98fb592c27b3a4befb5a73e7d620&spm=a21bo.2017.201874-sales.15";
    public void OnTaobao()
    {
        jo.Call("taobao", url); //void taobao()没有返回类型
    }


}