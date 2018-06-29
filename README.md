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
