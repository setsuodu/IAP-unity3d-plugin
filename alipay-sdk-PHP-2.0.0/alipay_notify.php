<?php

error_reporting(E_ALL^E_NOTICE^E_WARNING);
require_once (__DIR__.'/AopSdk.php');

//PHP服务端验证异步通知信息参数示例
$aop = new AopClient;
$aop->alipayrsaPublicKey = '请填写支付宝公钥，一行字符串';
$flag = $aop->rsaCheckV1($_POST, NULL, "RSA2");

?>