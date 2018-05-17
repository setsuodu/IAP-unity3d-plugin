//
//  ViewController.m
//  AliSDKDemo
//
//  Created by antfin on 17-10-24.
//  Copyright (c) 2017年 AntFin. All rights reserved.
//

#import "APViewController.h"
#import <AlipaySDK/AlipaySDK.h>

#import "APAuthInfo.h"
#import "APOrderInfo.h"
#import "APRSASigner.h"

@implementation APViewController

#pragma mark -
#pragma mark   ==============产生随机订单号==============

+ (NSString *)generateTradeNO
{
    static int kNumber = 15;
    NSString *sourceStr = @"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    NSMutableString *resultStr = [[NSMutableString alloc] init];
    srand((unsigned)time(0));
    for (int i = 0; i < kNumber; i++)
    {
        unsigned index = rand() % [sourceStr length];
        NSString *oneStr = [sourceStr substringWithRange:NSMakeRange(index, 1)];
        [resultStr appendString:oneStr];
    }
    return resultStr;
}

@end


#ifdef __cplusplus
extern "C" {
#endif
    
    #pragma mark -
    #pragma mark   ==============点击订单模拟支付行为==============
    //
    // 选中商品调用支付宝极简支付
    //
    const char* doAPPay(const char *info)
    {
        // 重要说明
        // 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
        // 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
        // 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
        /*============================================================================*/
        /*=======================需要填写商户app申请的===================================*/
        /*============================================================================*/
        NSString *appID = @"2017110209682458";
        
        // 如下私钥，rsa2PrivateKey 或者 rsaPrivateKey 只需要填入一个
        // 如果商户两个都设置了，优先使用 rsa2PrivateKey
        // rsa2PrivateKey 可以保证商户交易在更加安全的环境下进行，建议使用 rsa2PrivateKey
        // 获取 rsa2PrivateKey，建议使用支付宝提供的公私钥生成工具生成，
        // 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1
        NSString *rsa2PrivateKey = @"MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDJ6xWC2b7CgasnkYUsywSUf5t+E+gXCQ57ARyU/JiM4ImZ4Mb+leKNSwm7LsrJomX3YpZuusS3Tt8Jk9XPmQU+TnKz35vMvTpfZxOtPDBrmOywBpwwqGPPHKSztJPdrhEw3iHQynJRCZnp2LlTAiQuo3amw238Yvm972MD/jY9I2nef5p56ATz/R14Bi4r/6VSn/MPo/cOiX9wg/bGiNErf6iX6Hqw5PMn12TBp+Ih4TBIB7hsRSvLUPiaudIvqrPmZEE2UFJembfYjIz5L7ve7EfFs8GJWaJHvCMy5WTZUpwATAcEx+M0xANpL9vdNCMycwdHraM/onr/yojORGSHAgMBAAECggEBAI9oSaTfpZ5bngXCUsWXgy882mDXHpWNtx0hkv010drsdo41mN+j8H3Nd6Hb/+bpUmY7PQBz4dcXS01wFAEXeyPfb5PVEsB8/QJZIR5+ix1ARYpXh4L/dbPZchi3/fUJ9eexPG8f+cUYfpoZdLZB5RTCwDh8wJbqbNm7NaXWXQtZ4l1WBd2GevM7BETGcgO1rIecOu2Z/Vg1DoR5aHtug75zADern+v/nxx7SPnaLPwXrlDCpmVNmuCSpu2xy5v1rcxYVoNG1p9GYJbwdD2d4+RPb8b9yJLya6fMlkzOnrGQ09jOdhBgalgS0BZdqDavC7eQzAJGqbSNhAWufTJknFECgYEA7CtwET9ciw5lsGottQ00oyRDFk5blMjQ1UU/rOxaa/7t+uC/7MZwnN3QX8+UtXvij7WPJCg5w7HMrnG1XtvvCpRmBRV5/q9OSfHjCkkws47EmlZblhTbIK6D7TfM/YlcWlyIucylQ692DBoj9FjYFhv3BezTUYkVAyxht/a8ZlsCgYEA2t9mJ0b+qgirl20+WX6ramMGpHKVqeynNwZidLQBlhXRNc1bZeOuWCL29mXD5Mu4dPG5fnmuD4y7ZAp/8Fpb4DhzXicqa+UUGccb2QDKIDQnK8bwVVarBPgBDpUniskxs/POqOM5f7en1YheRKl8+4dXGmJ7p4cD9JzeiBV7ykUCgYEAqpSVVV/Wj/nOKSYH7YWkR1GhwzLFoymI7QIfg98TUH3JljNPbM3H+QWdxBZC6vKYKgSRZyRG1LpPhJBM2D8y4Qlexr1EaFhmy7ISsxM6QfcUugU2EajuvSRP1b9AKMBWHE5J0ymlFMZfFU6sOYkSSqXYPkdYOsQwkSI1UpK8cZECgYBfcP84rWqrdrBsjcScffH9qETjPNwqhvm7stsIDN6MNeNO1MfAPHJwGH8cnu+3O2aqCCZdEm5mdXXZgAKeKqjZs4QLUITdG72SKBLqo7+AH33dRxjR/cMBnavIUh42FNnJoM1DNCKEoO9c2+RPiKDphfDjnoJRYTzGVAltygB7CQKBgQCkDmZvUeww5F+Ip4sc4a0KZJIjeGhGZjaDOPs+hn0rR/FeXGLiz5Cq+XlFvRDwLJm7ay0j9rOt00ifuH6xW3rNBXb3xlVeEprqy6tB3mrHlXUpEzWV8V0ILcsdx5+KZU5E3Zg71w5D+yU4LudxZyefAJcfY8L3S2Nwb5qd1P8UrQ==";
        NSString *rsaPrivateKey = @"";
        /*============================================================================*/
        /*============================================================================*/
        /*============================================================================*/
        
        //partner和seller获取失败,提示
        if ([appID length] == 0 ||
            ([rsa2PrivateKey length] == 0 && [rsaPrivateKey length] == 0))
        {
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"提示"
                                                                           message:@"缺少appId或者私钥,请检查参数设置"
                                                                    preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction *action = [UIAlertAction actionWithTitle:@"知道了"
                                                             style:UIAlertActionStyleDefault
                                                           handler:^(UIAlertAction *action){
                                                               
                                                           }];
            [alert addAction:action];
            //[self presentViewController:alert animated:YES completion:^{ }];
            return strdup([@"seller获取失败" UTF8String]);
        }
        
        /*
         *生成订单信息及签名
         */
        //将商品信息赋予AlixPayOrder的成员变量
        APOrderInfo* order = [APOrderInfo new];
        
        // NOTE: app_id设置
        order.app_id = appID;
        
        // NOTE: 支付接口名称
        order.method = @"alipay.trade.app.pay";
        
        // NOTE: 参数编码格式
        order.charset = @"utf-8";
        
        // NOTE: 当前时间点
        NSDateFormatter* formatter = [NSDateFormatter new];
        [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        order.timestamp = [formatter stringFromDate:[NSDate date]];
        
        // NOTE: 支付版本
        order.version = @"1.0";
        
        // NOTE: sign_type 根据商户设置的私钥来决定
        order.sign_type = (rsa2PrivateKey.length > 1)?@"RSA2":@"RSA";
        
        // NOTE: 商品数据
        order.biz_content = [APBizContent new];
        order.biz_content.body = @"我是测试数据";
        order.biz_content.subject = @"1";
        order.biz_content.out_trade_no = [APViewController generateTradeNO]; //订单ID（由商家自行制定）
        order.biz_content.timeout_express = @"30m"; //超时时间设置
        order.biz_content.total_amount = [NSString stringWithFormat:@"%.2f", 0.01]; //商品价格
        
        //将商品信息拼接成字符串
        NSString *orderInfo = [order orderInfoEncoded:NO];
        NSString *orderInfoEncoded = [order orderInfoEncoded:YES];
        NSLog(@"orderSpec = %@",orderInfo);
        
        // NOTE: 获取私钥并将商户信息签名，外部商户的加签过程请务必放在服务端，防止公私钥数据泄露；
        //       需要遵循RSA签名规范，并将签名字符串base64编码和UrlEncode
        NSString *signedString = nil;
        APRSASigner* signer = [[APRSASigner alloc] initWithPrivateKey:((rsa2PrivateKey.length > 1)?rsa2PrivateKey:rsaPrivateKey)];
        if ((rsa2PrivateKey.length > 1)) {
            signedString = [signer signString:orderInfo withRSA2:YES];
        } else {
            signedString = [signer signString:orderInfo withRSA2:NO];
        }
        
        // NOTE: 如果加签成功，则继续执行支付
        if (signedString != nil) {
            //应用注册scheme,在AliSDKDemo-Info.plist定义URL types
            NSString *appScheme = @"alisdkdemo";
            
            // NOTE: 将签名成功字符串格式化为订单字符串,请严格按照该格式
            NSString *orderString = [NSString stringWithFormat:@"%@&sign=%@",
                                     orderInfoEncoded, signedString];
            
            // NOTE: 调用支付结果开始支付（唤起支付宝页面）
            [[AlipaySDK defaultService] payOrder:orderString fromScheme:appScheme callback:^(NSDictionary *resultDic) {
                NSLog(@"reslut = %@",resultDic);
            }];
        }
        
        NSLog(@"传入的加签订单: %s", info);
        
        NSString * strPath = @"返回值: haha";
        const char * uLog = [strPath UTF8String];
        //return uLog; //malloc: *** (lldb) 会报内存分配为空的问题，使用strdup()
        return strdup(uLog);
    }
    
#ifdef __cplusplus
}
#endif
