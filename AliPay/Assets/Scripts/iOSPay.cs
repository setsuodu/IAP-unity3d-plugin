using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class iOSPay : MonoBehaviour
{
    public delegate void AliPayDelegate(string status); //监听
    public static AliPayDelegate AliPayEvent; //回调
    public static void OnAliPayEvent(string status) //调用函数
    {
        if (AliPayEvent != null)
        {
            AliPayEvent(status);
        }
    }

	public Button m_clientPayButton;
    public Button m_serverPayButton;

	void Awake()
	{
		m_clientPayButton.onClick.AddListener(ClientPay);
		m_serverPayButton.onClick.AddListener(ServerPay);
	}

	void OnDestroy()
	{
		m_clientPayButton.onClick.AddListener(ClientPay);
		m_serverPayButton.onClick.RemoveListener(ServerPay);
	}

	void ClientPay()
	{
        /*
        string orderInfo = "==0XSFASA418URHJ113H9RUIF2NN";
        string result = HookBridge.doAPPay(orderInfo);
        Debug.Log("支付结果: " + result);
        */
	}

	void ServerPay()
	{
        AliPayEvent += AliPayLog;

		PayInfo payInfo = new PayInfo();
		payInfo.body = "AR会员";
		payInfo.subject = "蜜迩科技";
		payInfo.out_trade_no = System.DateTime.Now.ToString("yyyyMMddhhmmss") + "test";
		payInfo.total_amount = "1";

		StartCoroutine(OnServerSign(payInfo));
	}

	IEnumerator OnServerSign(PayInfo payInfo)
	{
		// 添加委托方法
        AliPayEvent += AliPayLog;

		WWWForm form = new WWWForm();
		form.AddField("body", payInfo.body);
		form.AddField("subject", payInfo.subject);
		form.AddField("out_trade_no", payInfo.out_trade_no);
		form.AddField("timeout_express", "30m");
		form.AddField("total_amount", payInfo.total_amount);
		string url = "http://122.112.233.193:9090"; //在开发平台绑定服务端接口
		WWW www = new WWW(url, form);
		yield return www;
		if (!string.IsNullOrEmpty(www.error))
		{
			Debug.Log(www.error);
		}
		Debug.Log(www.text); //服务器返回加签后的订单

        string orderInfo = www.text;
        HookBridge.doAPPay(orderInfo);
	}

    // 委托的方法
    void AliPayLog(string log)
    {
        Debug.Log("SDK回调 ==>> " + log);
        switch (log)
        {
            case "9000":
				m_serverPayButton.image.color = Color.green;
                break;
            default:
				m_serverPayButton.image.color = Color.red;
                break;
        }
        AliPayEvent -= AliPayLog; //释放委托
    }
   
    //sdk支付结果回调 UnitySendMessage("Object", "StatusCallback", "param");
    public void StatusCallback(string log)
    {
        Debug.Log("StatusCallback ==>> " + log);
        OnAliPayEvent(log);
    }
}
