using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using LitJson;

[System.Serializable]
public class PayInfo
{
    public string subject;  // 显示在按钮上的内容,跟支付无关系  
    public float money;     // 商品价钱  
    public string title;    // 商品描述  
}

public class AlipayUI : MonoBehaviour
{
    [SerializeField] private Text logText, resultText, clipText;
    [SerializeField] private InputField m_clipInputField;
    [SerializeField] private Image mainImage;

    public string className = "com.mier.mymirror.MyPluginClass";
    public List<Button> buttons = null;
    public List<PayInfo> payInfos = null;

    private Texture texture;
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

    //设置Badge角标为1
    public void OnSetBadge()
    {
        //jo.Call("SetBadge", 3); //3作为object对象，要与java函数中类型对应
        jo.Call("SendBadge"); //3作为object对象，要与java函数中类型对应
    }

    //清除Badge角标
    public void OnResetBadge()
    {
        jo.Call("CleanBadge");
    }

    //淘宝
    //string url = "https://item.taobao.com/item.htm?spm=a230r.1.14.41.5a4e6353yVPUB4&id=538143998548&ns=1&abbucket=6#detail";
    string url = "https://item.taobao.com/item.htm?id=538143998548"; //最短引用，填商品id即可
    public void OnTaobao()
    {
        jo.Call("taobao", url); //void taobao()没有返回类型
    }

    //剪贴板
    public void OnCopy()
    {
        jo.Call("onClickCopy", m_clipInputField.text);
    }

    public void OnPaste()
    {
        string str = jo.Call<string>("onClickPaste");
        clipText.text = str;
        Debug.Log(str);
    }

    //拍照
    public void OnChooseFromCamera()
    {
        jo.Call("chooseFromCamera");
    }

    public void OnChooseFromGallery()
    {
        jo.Call("chooseFromGallery");
    }

    public void OnChooseVideo()
    {
        jo.Call("chooseVideo");
    }

    public void CameraCallBack(string log)
    {
        Debug.Log("[拍照路径回调]" + log);
        //log = log.Substring(8, log.Length - 8);
        LoadByIO();
    }

    public void GalleryCallBack(string log)
    {
        Debug.Log("[相册路径回调]" + log);
        //log = log.Substring(8, log.Length - 8);
        LoadByIO();
    }

    /// <summary>
    /// 以IO方式进行加载
    /// </summary>
    private void LoadByIO()
    {
        //filePath = "file://" + filePath;
        //Debug.Log("[IO]" + filePath);
        string filePath = Application.persistentDataPath + "/temp/shot.jpg";
        Debug.Log("[IO]" + filePath);

        double startTime = (double)Time.time;
        //创建文件读取流
        FileStream fileStream = new FileStream(filePath, FileMode.Open, FileAccess.Read);
        fileStream.Seek(0, SeekOrigin.Begin);
        //创建文件长度缓冲区
        byte[] bytes = new byte[fileStream.Length];
        //读取文件
        fileStream.Read(bytes, 0, (int)fileStream.Length);
        //释放文件读取流
        fileStream.Close();
        fileStream.Dispose();
        fileStream = null;

        //创建Texture
        int width = 300;
        int height = 372;
        Texture2D t2d = new Texture2D(width, height);
        t2d.LoadImage(bytes);

        //创建Sprite
        Sprite sprite = Sprite.Create(t2d, new Rect(0, 0, t2d.width, t2d.height), new Vector2(0.5f, 0.5f));
        mainImage.sprite = sprite;
        mainImage.preserveAspect = true;

        startTime = (double)Time.time - startTime;
        Debug.Log("IO加载用时:" + startTime);
    }

    List<string> musicList = new List<string>();
    //本地音乐
    public void OnGetMusic()
    {
        string json = jo.Call<string>("getAllMediaList"); // "/storage/emulated/0/netease/cloudmusic/Music/井口裕香 - Hey World.mp3",
        json = "[" + json.Substring(0, json.Length - 1) + "]";
        Debug.Log(json);
        JsonData jd = JsonMapper.ToObject(json);
        //Debug.Log(jd.Count);
        for (int i = 0; i < jd.Count; i++)
        {
            string filePath = jd[i].ToString();
            var array = filePath.Split('/');
            string fileName = array[array.Length - 1];
            Debug.Log("曲目" + i + fileName);
            musicList.Add(filePath);
        }
        CopyFile(musicList[0]);
    }

    public void CopyFile(string oldPath)
    {
        string newPath = Application.persistentDataPath + "/temp.mp3";
        Debug.Log(oldPath + " -> " + newPath);
        jo.Call("copyFile", oldPath, newPath);
    }
}