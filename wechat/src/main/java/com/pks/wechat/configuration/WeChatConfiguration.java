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
	public static String token ;
	// 用户标识֤
	public static String appId ;
	// 用户标识密钥
	public static String appSecret ;
	
	/**储存接口令牌 accessToken 由于accesstoken每天只能获取2000次，为了节省次数在项目启动的时候获取一次存储下来在即将失效的时候重新获取*/
	public static AccessToken accessToken ;
	/**储存jsapi_ticket 游湖获取jsapi_ticket 接口限制，进行保存*/
	public static JsApiTicket jsapi_ticket;
	
	public static String MCH_ID ;//商户号
	public static String API_KEY ;//API密钥
	public static String SIGN_TYPE ;//签名加密方式
	public static String CERT_PATH ;//微信支付证书存放路径地址
	//微信支付统一接口的回调action
	public static String NOTIFY_URL ;
	//真实域名
	public static String DOMAIN_URL ;
	
	public static String PAY_ACTION ;

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		WeChatConfiguration.token = token;
	}

	public static String getAppId() {
		return appId;
	}

	public static void setAppId(String appId) {
		WeChatConfiguration.appId = appId;
	}

	public static String getAppSecret() {
		return appSecret;
	}

	public static void setAppSecret(String appSecret) {
		WeChatConfiguration.appSecret = appSecret;
	}

	public static String getMCH_ID() {
		return MCH_ID;
	}

	public static void setMCH_ID(String mCH_ID) {
		MCH_ID = mCH_ID;
	}

	public static String getAPI_KEY() {
		return API_KEY;
	}

	public static void setAPI_KEY(String aPI_KEY) {
		API_KEY = aPI_KEY;
	}

	public static String getSIGN_TYPE() {
		return SIGN_TYPE;
	}

	public static void setSIGN_TYPE(String sIGN_TYPE) {
		SIGN_TYPE = sIGN_TYPE;
	}

	public static String getCERT_PATH() {
		return CERT_PATH;
	}

	public static void setCERT_PATH(String cERT_PATH) {
		CERT_PATH = cERT_PATH;
	}

	public static String getNOTIFY_URL() {
		return NOTIFY_URL;
	}

	public static void setNOTIFY_URL(String nOTIFY_URL) {
		NOTIFY_URL = nOTIFY_URL;
	}

	public static String getDOMAIN_URL() {
		return DOMAIN_URL;
	}

	public static void setDOMAIN_URL(String dOMAIN_URL) {
		DOMAIN_URL = dOMAIN_URL;
	}

	public static String getPAY_ACTION() {
		return PAY_ACTION;
	}

	public static void setPAY_ACTION(String pAY_ACTION) {
		PAY_ACTION = pAY_ACTION;
	}
	
}
