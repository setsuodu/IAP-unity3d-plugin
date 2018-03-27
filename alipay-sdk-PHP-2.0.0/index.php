<?php
error_reporting(E_ALL^E_NOTICE^E_WARNING);
require_once (__DIR__.'/AopSdk.php');

if($_POST['out_trade_no'] != null)
{
	$test = new Alipay(); //初始化类实例
	//$test->haha();//调用类中的haha方法
	echo $test->unifiedorder($_POST['body'],$_POST['subject'],$_POST['out_trade_no'],$_POST['timeout_express'],$_POST['total_amount']);
}

class Alipay
{
    const APPID = '应用ID';
    const RSA_PRIVATE_KEY = '请填写开发者私钥去头去尾去回车，一行字符串';
    const ALIPAY_RSA_PUBLIC_KEY = '请填写支付宝公钥，一行字符串';
    
	//支付宝服务器主动通知商户服务器里指定的页面
    private $callback = "http://www.test.com/notify/alipay_notify.php";

    /**
     * 生成APP支付订单信息
     * @param string $orderId   商品订单ID
     * @param string $subject   支付商品的标题
     * @param string $body      支付商品描述
     * @param float $pre_price  商品总支付金额
     * @param int $expire       支付交易时间
     * @return bool|string  返回支付宝签名后订单信息，否则返回false
     */
    public function unifiedorder($body,$subject,$orderId,$expire,$pre_price)
	{
        try
		{
            $aop = new AopClient();
            //$aop->gatewayUrl = "https://openapi.alipay.com/gateway.do"; //正式
            $aop->gatewayUrl = "https://openapi.alipaydev.com/gateway.do"; //沙箱
            $aop->appId = self::APPID;
            $aop->rsaPrivateKey = self::RSA_PRIVATE_KEY;
            $aop->format = "json";
            $aop->charset = "UTF-8";
            $aop->signType = "RSA2";
            $aop->alipayrsaPublicKey = self::ALIPAY_RSA_PUBLIC_KEY;
            //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
            $request = new AlipayTradeAppPayRequest();
            //SDK已经封装掉了公共参数，这里只需要传入业务参数
            /*
            $bizcontent = "{\"body\":\"我是测试数据\","		//支付商品描述
				. "\"subject\": \"App支付测试\","			//支付商品的标题
				. "\"out_trade_no\": \"20170125test07\","	//商户网站唯一订单号
				. "\"timeout_express\": \"30m\","			//该笔订单允许的最晚付款时间，逾期将关闭交易
				. "\"total_amount\": \"0.01\","				//订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]
				. "\"product_code\":\"QUICK_MSECURITY_PAY\""
				. "}";
			*/
			//从POST传入
            $bizcontent = "{\"body\":\"" .$body. "\","
				. "\"subject\": \"" .$subject. "\","
				. "\"out_trade_no\": \"" .$orderId. "\","
				. "\"timeout_express\": \"" .$expire. "\","
				. "\"total_amount\": \"" .$pre_price. "\","
				. "\"product_code\":\"QUICK_MSECURITY_PAY\""
				. "}";
			
			$request->setNotifyUrl($this->callback); //商户外网可以访问的异步地址
			$request->setBizContent($bizcontent);
			
            //这里和普通的接口调用不同，使用的是sdkExecute
            $response = $aop->sdkExecute($request);
            
			//htmlspecialchars是为了输出到页面时防止被浏览器将关键参数html转义，实际打印到日志以及http传输不会有这个问题
			//return htmlspecialchars($response); //htmlspecialchars会把$转成$amp;
            return $response; //就是orderString 可以直接给客户端请求，无需再做处理。
        }
		catch (Exception $e)
		{
            return false;
        }
    }
}

?>
