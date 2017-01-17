package com.pks.wechat.util;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pks.wechat.configuration.WeChatConfiguration;
import com.pks.wechat.configuration.WeChatUrlConfiguration;
import com.pks.wechat.pojo.SNSUserInfo;
import com.pks.wechat.pojo.WeChatOauth2Token;

public class Oauth2Util {

	private static final Logger log = LoggerFactory.getLogger(Oauth2Util.class);
	
	public static String oauthurl(String redirect_uri){
//		String redirect_uri = WeChatConfiguration.DOMAIN_URL+"/wxpay/oauth2.ajax";//  : -> %3A      / -> %2F
    	redirect_uri = redirect_uri.replaceAll(":", "%3A").replaceAll("/", "%2F");
    	String scope = "snsapi_userinfo";
    	String state = "1";
		String appid = WeChatConfiguration.appId;
		String url = WeChatUrlConfiguration.OAUTH2_LOAD_URL.replace("APPID", appid).replace("REDIRECT_URI", redirect_uri).replace("SCOPE", scope).replace("STATE", state);
		return url;
	}
	
	public static SNSUserInfo oauth2(String code){
		SNSUserInfo user = null;
		if (!"authdeny".equals(code)) {
			// 获取网页授权access_token
			WeChatOauth2Token wechatOauth2Token = getOauth2AccessToken(WeChatConfiguration.appId,WeChatConfiguration.appSecret, code);
			// 网页授权接口访问凭证
			String accessToken = wechatOauth2Token.getAccessToken();
			// 用户标识
			String openId = wechatOauth2Token.getOpenId();
			// 获取用户信息
			user = getSNSUserInfo(accessToken,openId);// [/align][align=left] // 设置要传递的参数
		}
		return user;
	}
	
	
	
	/**
	 * @Description: 获取网页授权凭证
	 * @param appId
	 *            公众账号的唯一标识
	 * @param appSecret
	 *            公众账号的密钥
	 * @param code
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static WeChatOauth2Token getOauth2AccessToken(String appId,
			String appSecret, String code) {
		WeChatOauth2Token wat = null;
		// 拼接请求地址
		String requestUrl = WeChatUrlConfiguration.OAUTH2_ACCESSTOKEN_URL;// "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
		requestUrl = requestUrl.replace("APPID", appId);
		requestUrl = requestUrl.replace("SECRET", appSecret);
		requestUrl = requestUrl.replace("CODE", code);

		// 获取网页授权凭证
		JSONObject jsonObject = CommonUtil
				.httpsRequest(requestUrl, "GET", null);
		if (null != jsonObject) {
			try {
				wat = new WeChatOauth2Token();
				wat.setAccessToken(jsonObject.getString("access_token"));
				wat.setExpiresIn(jsonObject.getInt("expires_in"));
				wat.setRefreshToken(jsonObject.getString("refresh_token"));
				wat.setOpenId(jsonObject.getString("openid"));
				wat.setScope(jsonObject.getString("scope"));
			} catch (Exception e) {
				wat = null;
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getString("errmsg");
				log.error("获取网页授权凭证失败 errcode:{} errmsg:{}", errorCode,
						errorMsg);
			}
		}
		return wat;
	}
	
	/**
	 * @Description: 通过网页授权获取用户信息
	 * @param accessToken
	 *            网页授权接口调用凭证
	 * @param openId
	 *            用户标识
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public static SNSUserInfo getSNSUserInfo(String accessToken, String openId) {
		SNSUserInfo snsUserInfo = null;
		// 拼接请求地址
		// access_token 是 调用接口凭证
		// openid 是 普通用户的标识，对当前公众号唯一
		// lang 否 返回国家地区语言版本，zh_CN 简体，zh_TW 繁体，en 英语
		String requestUrl = WeChatUrlConfiguration.SNS_USERINFO_URL; //"https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace(
				"OPENID", openId);
		// 通过网页授权获取用户信息
		JSONObject jsonObject = CommonUtil
				.httpsRequest(requestUrl, "GET", null);
		if (null != jsonObject) {
			try {
				snsUserInfo = new SNSUserInfo();
				// 用户的标识
				snsUserInfo.setOpenId(jsonObject.getString("openid"));
				// 昵称
				snsUserInfo.setNickname(jsonObject.getString("nickname"));
				// 性别（1是男性，2是女性，0是未知）
				snsUserInfo.setSex(jsonObject.getInt("sex"));
				// 用户所在国家
				snsUserInfo.setCountry(jsonObject.getString("country"));
				// 用户所在省份
				snsUserInfo.setProvince(jsonObject.getString("province"));
				// 用户所在城市
				snsUserInfo.setCity(jsonObject.getString("city"));
				// 用户头像
				snsUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
				//unionid (在公众号绑定到微信开放平台的时候才能获取到该值)
//				snsUserInfo.setUnionid(jsonObject.getString("unionid"));
				// 用户特权信息
				snsUserInfo.setPrivilegeList(JSONArray.toList(jsonObject.getJSONArray("privilege"), List.class));
			} catch (Exception e) {
				snsUserInfo = null;
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getString("errmsg");
				log.error("获取用户信息失败 errcode:{} errmsg:{}", errorCode, errorMsg);
			}
		}
		return snsUserInfo;
	}

	
}
