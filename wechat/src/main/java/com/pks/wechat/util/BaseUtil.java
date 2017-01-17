package com.pks.wechat.util;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pks.wechat.configuration.WeChatConfiguration;
import com.pks.wechat.configuration.WeChatUrlConfiguration;
import com.pks.wechat.pojo.AccessToken;
import com.pks.wechat.pojo.JsApiTicket;
import com.pks.wechat.pojo.WeChatUserInfo;
import com.pks.wechat.pojo.WeChatUserList;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class BaseUtil {
	
	private static final Logger log = LoggerFactory.getLogger(BaseUtil.class);
	
	/**
	 * 获取access_token
	 * 
	 * @param appid
	 *            凭证
	 * @param appsecret
	 *            密钥
	 * @return
	 */
	public static AccessToken getAccessToken(String appid, String appsecret) {
		System.out.println("accesstoken过期，重启获取accesstoken");
		AccessToken accessToken = null;
		String requestUrl = WeChatUrlConfiguration.TOKEN_URL.replace("APPID",appid).replace("APPSECRET", appsecret);
		JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);
		// 如果请求成功
		if (null != jsonObject) {
			try {
				accessToken = new AccessToken();
				accessToken.setAccess_token(jsonObject.getString("access_token"));
				int expires_in = jsonObject.getInt("expires_in");
				long endtime = new Date().getTime()+expires_in*1000-500*1000;
				accessToken.setExpires_in(expires_in);
				accessToken.setEndtime(endtime);
			} catch (JSONException e) {
				accessToken = null;
				// 获取token失败
				System.out.println("获取token失败 errcode:{} errmsg:{}"
						+ jsonObject.getInt("errcode")
						+ jsonObject.getString("errmsg"));
				log.error("获取token失败 errcode:{} errmsg:{}",
						jsonObject.getInt("errcode"),
						jsonObject.getString("errmsg"));
			}
		}
		return accessToken;
	}
	/**
	 * 获取保存的accesstoken 如果accesstoken为null 或者过去重新获取accesstoken
	 * @return
	 */
	public static AccessToken getAccessToken(){
		AccessToken accessToken = WeChatConfiguration.accessToken;
		if(accessToken==null || accessToken.getEndtime()<new Date().getTime()){
			accessToken = getAccessToken(WeChatConfiguration.appId, WeChatConfiguration.appSecret);
			WeChatConfiguration.accessToken = accessToken;
		}
		return accessToken;
	}
	
	
	 public static JsApiTicket getJsApiTicket(String accessToken) {  
       JsApiTicket jsApiTicket = null;  
       String requestUrl = WeChatUrlConfiguration.JSAPI_TICKET_URL.replace("ACCESS_TOKEN", accessToken);  
       JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "GET", null);  
       // 如果请求成功  
       if (null != jsonObject) {  
           try {  
               jsApiTicket = new JsApiTicket();  
               jsApiTicket.setTicket(jsonObject.getString("ticket")); 
               int expires_in = jsonObject.getInt("expires_in");
               long endtime = new Date().getTime()+expires_in*1000 - 500*1000;
               jsApiTicket.setExpires_in(expires_in);
               jsApiTicket.setEndtime(endtime);
           } catch (JSONException e) {  
               accessToken = null;  
               // 获取jsApiTicket失败  
               log.error("获取jsApiTicket失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));  
           }  
       }  
       return jsApiTicket;  
   }
	
	/**
	 * 获取jsapiticket 如果为空或者过期 重新发起请求获取
	 * @return
	 */
	public static JsApiTicket getJsApiTicket(){
		JsApiTicket jsApiTicket = WeChatConfiguration.jsapi_ticket;
		if(jsApiTicket==null || jsApiTicket.getEndtime()<new Date().getTime()){
			AccessToken accessToken = getAccessToken();
			jsApiTicket = getJsApiTicket(accessToken.getAccess_token());
			WeChatConfiguration.jsapi_ticket = jsApiTicket;
		}
		return jsApiTicket;
	}
	
	
	/**
	 * @Description: 获取用户信息
	 * @param accessToken
	 *            接口访问凭证
	 * @param openId
	 *            用户标识
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static WeChatUserInfo getUserInfo(String openId) {
		AccessToken accessToken = BaseUtil.getAccessToken();
		WeChatUserInfo weixinUserInfo = null;
		// 拼接请求地址
		String requestUrl = WeChatUrlConfiguration.USER_INFO_URL; // "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken.getAccess_token()).replace(
				"OPENID", openId);
		// 获取用户信息
		JSONObject jsonObject = CommonUtil
				.httpsRequest(requestUrl, "GET", null);

		if (null != jsonObject) {
			try {
				weixinUserInfo = new WeChatUserInfo();
				// 用户的标识
				weixinUserInfo.setOpenId(jsonObject.getString("openid"));
				// 关注状态（1是关注，0是未关注），未关注时获取不到其余信息
				weixinUserInfo.setSubscribe(jsonObject.getInt("subscribe"));
				// 用户关注时间
				weixinUserInfo.setSubscribeTime(jsonObject.getString("subscribe_time"));
				// 昵称
				weixinUserInfo.setNickname(jsonObject.getString("nickname"));
				// 用户的性别（1是男性，2是女性，0是未知）
				weixinUserInfo.setSex(jsonObject.getInt("sex"));
				// 用户所在国家
				weixinUserInfo.setCountry(jsonObject.getString("country"));
				// 用户所在省份
				weixinUserInfo.setProvince(jsonObject.getString("province"));
				// 用户所在城市
				weixinUserInfo.setCity(jsonObject.getString("city"));
				// 用户的语言，简体中文为zh_CN
				weixinUserInfo.setLanguage(jsonObject.getString("language"));
				// 用户头像
				weixinUserInfo.setHeadImgUrl(jsonObject.getString("headimgurl"));
			} catch (Exception e) {
				if (0 == weixinUserInfo.getSubscribe()) {
					log.error("用户{}已取消关注", weixinUserInfo.getOpenId());
				} else {
					int errorCode = jsonObject.getInt("errcode");
					String errorMsg = jsonObject.getString("errmsg");
					log.error("获取用户信息失败 errcode:{} errmsg:{}", errorCode,
							errorMsg);
				}
			}
		}
		return weixinUserInfo;
	}

	/**
	 * @Description: 获取关注者列表
	 * @param accessToken
	 *            调用接口凭证
	 * @param nextOpenId
	 *            第一个拉取的openId，不填默认从头开始拉取
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public static WeChatUserList getUserList(String nextOpenId) {
		AccessToken accessToken = BaseUtil.getAccessToken();
		WeChatUserList weixinUserList = null;
		if (null == nextOpenId) {
			nextOpenId = "";
		}
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&next_openid=NEXT_OPENID";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken.getAccess_token()).replace(
				"NEXT_OPENID", nextOpenId);
		// 获取关注者列表
		JSONObject jsonObject = CommonUtil
				.httpsRequest(requestUrl, "GET", null);
		// 如果请求成功
		if (null != jsonObject) {
			try {
				weixinUserList = new WeChatUserList();
				weixinUserList.setTotal(jsonObject.getInt("total"));
				weixinUserList.setCount(jsonObject.getInt("count"));
				weixinUserList.setNextOpenId(jsonObject
						.getString("next_openid"));
				JSONObject dataObject = (JSONObject) jsonObject.get("data");
				weixinUserList.setOpenIdList(JSONArray.toList(
						dataObject.getJSONArray("openid"), List.class));
			} catch (JSONException e) {
				weixinUserList = null;
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getString("errmsg");
				log.error("获取关注者列表失败 errcode:{} errmsg:{}", errorCode, errorMsg);
			}
		}
		return weixinUserList;
	}
	
	
	
	
}
