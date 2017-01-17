package com.pks.wechat.configuration;

import com.pks.wechat.pojo.AccessToken;
import com.pks.wechat.pojo.JsApiTicket;
/**
 * @Description: 常量类
 * @author Administrator
 * @date 2015-12-23
 */
public class WeChatConfiguration {
	// 与开发模式接口配置信息中的Token保持一致
	public static String token = "sheng";
	// 用户标识֤
	public static String appId = "wx4beaf827cc97a96d";
	// 用户标识密钥
	public static String appSecret = "f7f6b851edb551403b9bf66e1f41297b";
	
	/**储存接口令牌 accessToken 由于accesstoken每天只能获取2000次，为了节省次数在项目启动的时候获取一次存储下来在即将失效的时候重新获取*/
	public static AccessToken accessToken ;
	/**储存jsapi_ticket 游湖获取jsapi_ticket 接口限制，进行保存*/
	public static JsApiTicket jsapi_ticket;
	
	public final static String MCH_ID = "12412412412";//商户号
	public final static String API_KEY = "4532452345";//API密钥
	public final static String SIGN_TYPE = "SHA1";//签名加密方式
	public final static String CERT_PATH = "D:/apiclient_cert.p12";//微信支付证书存放路径地址
	//微信支付统一接口的回调action
	public final static String NOTIFY_URL = "http://pweixin.tunnel.qydev.com/wechat/wxpay/notify.ajax";
	//真实域名
	public final static String DOMAIN_URL = "http://pweixin.tunnel.qydev.com/wechat";
	
	public final static String PAY_ACTION = "http://pweixin.tunnel.qydev.com/wechat/wxpay/topay.ajax";
	
}
