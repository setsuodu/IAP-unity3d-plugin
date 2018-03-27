package com.mier.mymirror;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View;

import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.app.EnvUtils;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.util.Log;

import demo.BadgeUtil;
import demo.PayResult;
import demo.util.OrderInfoUtil2_0;

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
    public void SayHello() {
        UnityPlayer.UnitySendMessage(gameObjectName,"PluginCallBack","Hello Unity!");
    }

    //unity打印
    public void UnityDebug(String log){
        UnityPlayer.UnitySendMessage(gameObjectName,"PluginCallBack", log);
    }

    //示例方法二：计算传入的参数并返回计算结果
    public int CalculateAdd(int one, int another)
    {
        return one + another;
    }

    //支付
    public static final String APPID = "填写应用APPID"; //沙箱版
    public static final String RSA_PRIVATE = "";
    public static final String RSA2_PRIVATE = "填写RSA2应用私钥";
    private static final int SDK_PAY_FLAG = 1;
    public String productid;

    private static final String RESULT_SUCCESS = "9000";
    private static final String TIP_PAY_SUCCESS = "支付成功";
    private static final String TIP_PAY_FAILED = "支付失败";

    // 支付回调
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")

                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    //对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                    String resultInfo = payResult.getResult(); // 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus(); //9000成功 //6001用户中途取消

                    UnityDebug("resultInfo: " + resultInfo);
                    UnityDebug("resultStatus: " + resultStatus);

                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, RESULT_SUCCESS))
                    {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(getActivity(), TIP_PAY_SUCCESS, Toast.LENGTH_SHORT).show();
                        //PayResultToUnity(productid);
                    }
                    else
                    {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(getActivity(), TIP_PAY_FAILED, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    // Unity中调用，服务端php合成orderStr
    public String Pay(final String info) {
        final String orderInfo = info;

        Runnable payRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX); //沙箱环境
                PayTask alipay = new PayTask(getActivity());
                Map<String, String> result = alipay.payV2(orderInfo, true);

                UnityDebug("result: " + result.toString());
                UnityDebug("orderStr: " + orderInfo);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        //必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();

        return orderInfo;
    }

    //支付宝支付业务，插件中合成orderStr
    public String Alipay(String name,String price,String productid) {
        this.productid = productid;
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID,name,price,productid,rsa2);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);

        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
        final String orderInfo = orderParam + "&" + sign;

        Runnable payRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX); //沙箱环境
                PayTask alipay = new PayTask(getActivity());
                Map<String, String> result = alipay.payV2(orderInfo, true);

                UnityDebug("result: " + result.toString());
                UnityDebug("orderStr: " + orderInfo);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        //必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();

        return orderInfo;
    }

    //返回给Unity
    public void PayResultToUnity(String productid) {
        //物体名字，   方法名字    方法的参数
        UnityPlayer.UnitySendMessage("Canvas","PayResult",productid);
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
    public String ShortCut() {
        if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi")){
            //小米
            return "小米";
        }else if(Build.MANUFACTURER.equalsIgnoreCase("samsung")){
            //三星
            return "三星";
        }else {//其他原生系统手机
            return "其他原生系统手机";
        }
    }

    public void SendBadge(){
        BadgeUtil.setBadgeCount(getActivity().getApplicationContext(), 1);
    }

    public void CleanBadge(){
        BadgeUtil.resetBadgeCount(getActivity().getApplicationContext());
    }

    //淘宝
    public void taobao(String str) {
        Intent intent = new Intent();
        intent.setAction("Android.intent.action.VIEW");
        Uri uri = Uri.parse(str); // 商品地址
        intent.setData(uri);
        intent.setClassName("com.taobao.taobao", "com.taobao.tao.detail.activity.DetailActivity");
        startActivity(intent);
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

        UnityDebug("onClickPaste: " + result);

        return result;
    }

    //拍照
    //用于展示选择的图片
    private ImageView mImageView;
    private static final int CAMERA_CODE = 1;
    private static final int GALLERY_CODE = 2;
    private static final int CROP_CODE = 3;
    private static final int VIDEO = 4;

    /**
     * 拍照选择图片
     */
    public void chooseFromCamera() {
        //构建隐式Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //调用系统相机
        startActivityForResult(intent, CAMERA_CODE);
    }

    /*
     * 从相册选择图片
     */
    public void chooseFromGallery() {
        //构建一个内容选择的Intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //设置选择类型为图片类型
        intent.setType("image/*");
        //打开图片选择
        startActivityForResult(intent, GALLERY_CODE);
    }

    /*
    * 拍视频
    */
    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setAction("android.media.action.VIDEO_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");

        String destDir = getActivity().getExternalFilesDir(null).toString() + "/" + "video.mp4";
        File file = new File(destDir);
        if(file.exists()){
            file.delete();
        }
        Uri uri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CAMERA_CODE:
                //用户点击了取消
                if(data == null){
                    return;
                }else{
                    Bundle extras = data.getExtras();
                    if (extras != null){
                        //获得拍的照片
                        Bitmap bm = extras.getParcelable("data");
                        //将Bitmap转化为uri
                        Uri uri = saveBitmap(bm, "temp");
                        //启动图像裁剪
                        //startImageZoom(uri);
                        String destDir = getActivity().getExternalFilesDir(null).toString();
                        UnityPlayer.UnitySendMessage(gameObjectName,"CameraCallBack", destDir);
                    }
                }
                break;
            case GALLERY_CODE:
                if (data == null){
                    return;
                }else{
                    //用户从图库选择图片后会返回所选图片的Uri
                    Uri uri;
                    //获取到用户所选图片的Uri
                    uri = data.getData();
                    //返回的Uri为content类型的Uri,不能进行复制等操作,需要转换为文件Uri
                    uri = convertUri(uri);
                    //启动图像裁剪
                    //startImageZoom(uri);
                    String destDir = getActivity().getExternalFilesDir(null).toString();
                    UnityPlayer.UnitySendMessage(gameObjectName,"GalleryCallBack",destDir);
                }
                break;
            case CROP_CODE:
                if (data == null){
                    return;
                }else{
                    Bundle extras = data.getExtras();
                    if (extras != null){
                        //获取到裁剪后的图像
                        Bitmap bm = extras.getParcelable("data");
                        String destDir = getActivity().getExternalFilesDir(null).toString();
                        Bitmap bitmap = getLoacalBitmap(destDir); //从本地取图片(在cdcard中获取)  //
                        mImageView.setImageBitmap(bitmap); //设置Bitmap
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将content类型的Uri转化为文件类型的Uri
     * @param uri
     * @return
     */
    private Uri convertUri(Uri uri){
        InputStream is;
        try {
            //Uri ----> InputStream
            is = getActivity().getContentResolver().openInputStream(uri);
            //InputStream ----> Bitmap
            Bitmap bm = BitmapFactory.decodeStream(is);
            //关闭流
            is.close();
            return saveBitmap(bm, "temp");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将Bitmap写入SD卡中的一个文件中,并返回写入文件的Uri
     * @param bm
     * @param dirPath
     * @return
     */
    private Uri saveBitmap(Bitmap bm, String dirPath) {
        //新建文件夹用于存放裁剪后的图片
        File tmpDir = new File( getActivity().getExternalFilesDir(null).toString() + "/" + dirPath); //Environment.getExternalStorageDirectory()
        if (!tmpDir.exists()){
            tmpDir.mkdir();
        }

        //新建文件存储裁剪后的图片
        File img = new File( tmpDir + "/shot.jpg"); //tmpDir.getAbsolutePath()
        try {
            //打开文件输出流
            FileOutputStream fos = new FileOutputStream(img);
            //将bitmap压缩后写入输出流(参数依次为图片格式、图片质量和输出流)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fos);
            //刷新输出流
            fos.flush();
            //关闭输出流
            fos.close();
            //返回File类型的Uri
            return Uri.fromFile(img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过Uri传递图像信息以供裁剪
     * @param uri
     */
    public void startImageZoom(Uri uri){
        //构建隐式Intent来启动裁剪程序
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置数据uri和类型为图片类型
        intent.setDataAndType(uri, "image/*");
        //显示View为可裁剪的
        intent.putExtra("crop", true);
        //裁剪的宽高的比例为1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //输出图片的宽高均为150
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        //裁剪之后的数据是通过Intent返回
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_CODE);
    }

    //6.音乐列表
    public String getAllMediaList() {
        Cursor cursor = null;
        String output = "";
        try
        {
            cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.SIZE},
                    null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
            if(cursor == null) {
                Log.d(TAG, "The getMediaList cursor is null.");
                return "";
            }
            int count = cursor.getCount();
            if(count <= 0) {
                Log.d(TAG, "The getMediaList cursor count is 0.");
                return "";
            }
            MediaEntity mediaEntity = null;
            while (cursor.moveToNext()) {
                mediaEntity = new MediaEntity();
                mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                mediaEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                mediaEntity.display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if(!mediaEntity.path.contains("/storage/emulated/0/")){
                    continue;
                }
                output += "\"" + mediaEntity.path + "\",";
            }
        }
        catch (Exception e) {}
        finally
        {
            if(cursor != null) {
                cursor.close();
            }
        }
        return output;
    }

    /** 复制单个文件
    * @param oldPath String 原文件路径 如：c:/fqf.txt
    * @param newPath String 复制后路径 如：f:/fqf.txt
    * @return boolean
    */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
            UnityDebug("复制成功!");
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        }
    }
}