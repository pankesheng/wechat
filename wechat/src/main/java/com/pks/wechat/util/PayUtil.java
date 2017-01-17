package com.pks.wechat.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pks.wechat.configuration.WeChatConfiguration;
import com.pks.wechat.configuration.WeChatUrlConfiguration;
import com.zcj.util.UtilString;

public class PayUtil {
	
	private static final Logger log = LoggerFactory.getLogger(PayUtil.class);
	
	public static String payurl(String backUri,Map<String, Object> params) throws UnsupportedEncodingException{
		String paramsurl = CommonUtil.paramstourl(params);
		backUri = backUri + "?"+paramsurl;
		// URLEncoder.encode 后可以在backUri 的url里面获取传递的所有参数
		backUri = URLEncoder.encode(backUri,"UTF-8");
		String scope = "snsapi_userinfo";
    	String state = "1";
		String appid = WeChatConfiguration.appId;
		String url = WeChatUrlConfiguration.OAUTH2_LOAD_URL.replace("APPID", appid).replace("REDIRECT_URI", backUri).replace("SCOPE", scope).replace("STATE", state);
		return url;
	}

	@SuppressWarnings({ "static-access", "unused" })
	public static String topay(RequestHandler reqHandler,String orderNo,String money,String code,String userId,String descr,String ip,String url){
		float sessionmoney = Float.parseFloat(money);
		String finalmoney = String.format("%.2f", sessionmoney);
		finalmoney = finalmoney.replace(".", "");
		// 商户相关资料
		String appid = WeChatConfiguration.appId;
		String appsecret = WeChatConfiguration.appSecret;
		String mch_id = WeChatConfiguration.MCH_ID;
		String api_key = WeChatConfiguration.API_KEY;

		String openId = "";
		String URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
				+ appid + "&secret=" + appsecret + "&code=" + code
				+ "&grant_type=authorization_code";
		HttpResponse temp = HttpConnect.getInstance().doGetStr(URL);
		String tempValue = "";
		if (temp == null) {
			return "";
		} else {
			try {
				tempValue = temp.getStringResult();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject jsonObj = JSONObject.fromObject(tempValue);
			if (jsonObj.containsKey("errcode")) {
				return "";
			}
			openId = jsonObj.getString("openid");
		}

		// 获取openId后调用统一支付接口https://api.mch.weixin.qq.com/pay/unifiedorder
		String currTime = CommonUtil.getCurrTime();
		// 8位日期
		String strTime = currTime.substring(8, currTime.length());
		// 四位随机数
		String strRandom = CommonUtil.buildRandom(4) + "";
		// 10位序列号,可以自行调整。
		String strReq = strTime + strRandom;
		// 子商户号 非必输
		// String sub_mch_id="";
		// 设备号 非必输
		//String device_info = "";
		// 随机数
		String nonce_str = strReq;
		// 商品描述
		// String body = describe;

		// 商品描述根据情况修改
		String body = descr;
		// 附加数据
		String attach = userId;
		// 商户订单号
		String out_trade_no = orderNo;
		int intMoney = Integer.parseInt(finalmoney);

		// 总金额以分为单位，不带小数点
		int total_fee = intMoney;
		// 订 单 生 成 时 间 非必输
		// String time_start ="";
		// 订单失效时间 非必输
		// String time_expire = "";
		// 商品标记 非必输
		// String goods_tag = "";

		// 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
		String notify_url = WeChatConfiguration.NOTIFY_URL;
		String trade_type = "JSAPI";
		String openid = openId;
		// 非必输
		// String product_id = "";
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("attach", attach);
		packageParams.put("out_trade_no", out_trade_no);
		// 这里写的金额为1 分到时修改
		packageParams.put("total_fee", String.valueOf(total_fee));
		// packageParams.put("total_fee", "finalmoney");
		packageParams.put("spbill_create_ip", ip);
		packageParams.put("notify_url", notify_url);
		packageParams.put("trade_type", trade_type);
		packageParams.put("openid", openid);

		reqHandler.init(appid, appsecret, api_key);
		
		String sign = reqHandler.createSign(packageParams);
		String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>"
				+ mch_id + "</mch_id>" + "<nonce_str>" + nonce_str
				+ "</nonce_str>" + "<sign>" + sign + "</sign>"
				+ "<body>" + body + "</body>" + "<attach>" + attach
				+ "</attach>" + "<out_trade_no>" + out_trade_no
				+ "</out_trade_no>" + "<attach>"
				+ attach
				+ "</attach>"
				+
				// 金额，这里写的1 分到时修改
				"<total_fee>"
				+ total_fee
				+ "</total_fee>"
				+
				// "<total_fee>"+finalmoney+"</total_fee>"+
				"<spbill_create_ip>" + ip + "</spbill_create_ip>"
				+ "<notify_url>" + notify_url + "</notify_url>"
				+ "<trade_type>" + trade_type + "</trade_type>" + "<openid>"
				+ openid + "</openid>" + "</xml>";
//		System.out.println(xml);
		String allParameters = "";
		try {
			allParameters = reqHandler.genPackage(packageParams);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String createOrderURL = WeChatUrlConfiguration.UNIFIED_ORDER_URL;//"https://api.mch.weixin.qq.com/pay/unifiedorder";
		String prepay_id = "";
		try {
			prepay_id = new GetWxOrderno().getPayNo(createOrderURL, xml);
			System.out.println("prepay_id:"+prepay_id);
			if (prepay_id.equals("")) {
				log.error("统一支付接口获取预支付订单出错！");
				return "";
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		SortedMap<String, String> finalpackage = new TreeMap<String, String>();
		String appid2 = appid;
		String timestamp = CommonUtil.getTimeStamp();
		String nonceStr2 = nonce_str;
		String prepay_id2 = "prepay_id=" + prepay_id;
		String packages = prepay_id2;
		finalpackage.put("appId", appid2);
		finalpackage.put("timeStamp", timestamp);
		finalpackage.put("nonceStr", nonceStr2);
		finalpackage.put("package", packages);
		finalpackage.put("signType", "MD5");
		String finalsign = reqHandler.createSign(finalpackage);
		return url+"?appid=" + appid2 + "&timeStamp=" + timestamp + "&nonceStr=" + nonceStr2 + "&package=" + packages + "&sign=" + finalsign+"&orderNo="+orderNo;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	@RequestMapping("refund")
	public static boolean refund(HttpServletResponse response,String out_trade_no,Double total_fee1,Double refund_fee1) throws SDKRuntimeException {
		String out_refund_no = UtilString.getUUID();// 退款单号，随机生成 ，但长度应该跟文档一样（32位）(卖家信息校验不一致，请核实后再试)
		int total_fee = (int) (total_fee1*100);//订单的总金额,以分为单位（填错了貌似提示：同一个out_refund_no退款金额要一致）
		int refund_fee = (int) (refund_fee1*100);;// 退款金额，以分为单位（填错了貌似提示：同一个out_refund_no退款金额要一致）
		String nonce_str = CommonUtil.buildRandom(4) + "";// 随机字符串
		//微信公众平台文档：“基本配置”--》“开发者ID”
		String appid = WeChatConfiguration.appId;
		//微信公众平台文档：“基本配置”--》“开发者ID”
		String appsecret = WeChatConfiguration.appSecret;
		//商户号
		//微信公众平台文档：“微信支付”--》“商户信息”--》“商户号”，将该值赋值给partner
		String mch_id = WeChatConfiguration.MCH_ID;
		String op_user_id = mch_id;//就是MCHID
		//微信公众平台："微信支付"--》“商户信息”--》“微信支付商户平台”（登录）--》“API安全”--》“API密钥”--“设置密钥”（设置之后的那个值就是partnerkey，32位）
		String partnerkey = WeChatConfiguration.API_KEY;
		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.init(appid, appsecret, partnerkey);
		String xml = ClientCustomSSL.RefundNativePackage(out_trade_no, out_refund_no, String.valueOf(total_fee), String.valueOf(refund_fee), nonce_str,reqHandler);
		String createOrderURL = WeChatUrlConfiguration.REFUND_URL; //"https://api.mch.weixin.qq.com/secapi/pay/refund";
		try {
			String refundResult= ClientCustomSSL.doRefund(createOrderURL, xml);
			Map m = XMLUtil.doXMLParse(refundResult);
			log.info("退款结果XML：" + refundResult);
			String return_code = (String) m.get("return_code");
			if ("SUCCESS".equals(return_code)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
