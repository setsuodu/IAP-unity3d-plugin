# IAP-unity3d-plugin
式例项目，Android/iOS插件集成

### Android接口

1. void SayHello() - 测试接口，向Unity回调SendMessage
2. int CalculateAdd(int,int) - 接收Unity传入，计算返回结果
3. void Pay(String) - 支付宝支付接口
4. bool checkGPSIsOpen() - 检查GPS是否开启
5. void openGPSSetting() - 打开系统GPS设置页
6. String ShortCut() - 返回当前手机生产商
7. void SendBadge() - 添加桌面角标
8. void CleanBadge() - 清除桌面角标
9. void TakePhoto(String) - 调用本地相册
10. void Copy(String)- 将文字放入系统剪贴板
11. String Paste()- 从系统剪贴板中获取文字

### iOS接口

1. string doAPPay(string orderInfo) - 支付宝支付接口

[集成文档](https://docs.open.alipay.com/204/105295/)

### 常见错误及解决方案
1. [iOS] Xcode编译，提示'openssl/asn1.h' file not found

openssl是用来在客户端生成订单的。
如果由服务器生成订单，可删除相关包。
如果要在客户端生成订单，需要在"Build Settings -> Search Paths -> Header Search Paths"中配置添加头文件搜索路径$(SRCROOT)/Libraries/Plugins/iOS。

2. [iOS] 完成支付，无法回调。设置PList。
```
// 设置Alipay回调app名称
var urltypes = plistDict.CreateArray("CFBundleURLTypes");
var item0 = urltypes.AddDict();
var urlschemes = item0.CreateArray("URL Schemes");
urlschemes.AddString("app_name");
```
