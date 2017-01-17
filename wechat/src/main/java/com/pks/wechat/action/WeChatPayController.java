package com.pks.wechat.action;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.Gson;
import com.pks.wechat.common.Configuration;
import com.pks.wechat.configuration.WeChatConfiguration;
import com.pks.wechat.pojo.JsApiTicket;
import com.pks.wechat.pojo.SNSUserInfo;
import com.pks.wechat.pojo.WeChatPayResult;
import com.pks.wechat.util.BaseUtil;
import com.pks.wechat.util.CommonUtil;
import com.pks.wechat.util.Oauth2Util;
import com.pks.wechat.util.PayUtil;
import com.pks.wechat.util.RequestHandler;
import com.pks.wechat.util.SDKRuntimeException;
import com.pks.wechat.util.SignUtil;
import com.pks.wechat.util.XMLUtil;
import com.zcj.util.UtilString;
import com.zcj.web.dto.ServiceResult;

@Controller
@RequestMapping(value = "/wxpay")
public class WeChatPayController {
	
//	@Autowired
//	private OrderService orderService;
	
	@RequestMapping("/test/index")
	public String testIndex(Model model){
		model.addAttribute("orderNo", UtilString.getLongUUID());
		return "/wxpay/testindex.jsp";
	}
	
	@RequestMapping(value="/oauth")
	public void oauth(HttpServletRequest request,HttpServletResponse response) throws IOException{
		String redirect_uri = WeChatConfiguration.DOMAIN_URL+"/wxpay/oauth2.ajax";//  : -> %3A      / -> %2F
    	String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+WeChatConfiguration.appId+"&redirect_uri="+redirect_uri.replaceAll(":", "%3A").replaceAll("/", "%2F")+"&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect";
		response.sendRedirect(url); 
	}
	
	@RequestMapping(value = "/oauth2")
	public String oauth2(Model model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");// [/align][align=left] //
												// 用户同意授权后，能获取到code
		String code = request.getParameter("code");// [/align][align=left] //
		SNSUserInfo snsUserInfo = Oauth2Util.oauth2(code);
		model.addAttribute("snsUserInfo", snsUserInfo);
		model.addAttribute("basePath", Configuration.getContextPath());
		return "/www/zone.jsp";
	}
	
	// userId 用户id
	// orderNo 订单编号
	// descr 商品描述
	// money 金额，double类型
	@RequestMapping("/paymain")
	public void paymain(HttpServletRequest request,
			HttpServletResponse response, Long userId, Long orderNo,
			String descr, Double money, PrintWriter out) throws IOException {
		String backUri = WeChatConfiguration.PAY_ACTION;
		if (orderNo == null) {
			out.write(ServiceResult.initErrorJson("订单号不能为空!"));
			return;
		}
		if(StringUtils.isBlank(descr)){
			out.write(ServiceResult.initErrorJson("商品描述不能为空！"));
			return ;
		}
		if (money == null) {
			out.write(ServiceResult.initErrorJson("金额不能为空！"));
			return;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("orderNo", orderNo);
		params.put("descr", descr);
		params.put("money", money);
		String url = PayUtil.payurl(backUri, params);
		response.sendRedirect(url);
	}

	@RequestMapping(value = "/topay")
	public String topay(HttpServletRequest request,
			HttpServletResponse response, String userId, String orderNo,
			String money, String descr, String code, Model model) throws UnsupportedEncodingException {
		RequestHandler reqHandler = new RequestHandler(request, response);
		String ip = CommonUtil.getRemortIP(request);
		String url = PayUtil.topay(reqHandler, orderNo, money, code, userId, descr, ip,"/wxpay/pay.jsp");
		if(StringUtils.isNotBlank(url)){
			return "redirect:"+url;
		}else{
			return "/wxpay/payerror.jsp";
		}
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping("/notify")
	public void notify(Model model, HttpServletRequest request,
			HttpServletResponse response, String appid) throws Exception {
		String inputLine;
		String notityXml = "";
		String resXml = "";
		try {
			while ((inputLine = request.getReader().readLine()) != null) {
				notityXml += inputLine;
			}
			request.getReader().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("接收到的报文：" + notityXml);
		Map m = XMLUtil.doXMLParse(notityXml);
		WeChatPayResult wpr = new WeChatPayResult();
		wpr.setAppid(m.get("appid").toString());
		wpr.setBankType(m.get("bank_type").toString());
		wpr.setCashFee(Integer.parseInt(m.get("cash_fee").toString()));
		wpr.setFeeType(m.get("fee_type").toString());
		wpr.setIsSubscribe(m.get("is_subscribe").toString());
		wpr.setMchId(m.get("mch_id").toString());
		wpr.setNonceStr(m.get("nonce_str").toString());
		wpr.setOpenid(m.get("openid").toString());
		wpr.setOutTradeNo(m.get("out_trade_no").toString());
		wpr.setResultCode(m.get("result_code").toString());
		wpr.setReturnCode(m.get("return_code").toString());
		wpr.setSign(m.get("sign").toString());
		wpr.setTimeEnd(m.get("time_end").toString());
		wpr.setTotalFee(Integer.parseInt(m.get("total_fee").toString()));
		wpr.setTradeType(m.get("trade_type").toString());
		wpr.setTransactionId(m.get("transaction_id").toString());
		if ("SUCCESS".equals(wpr.getResultCode())) {
			//orderService.userPaySuccess(Long.parseLong(wpr.getOutTradeNo()), 3, wpr.getOutTradeNo());
			// 支付成功
			resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
						+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
		} else {
			resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
					+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
		}
//		System.out.println("微信支付回调数据结束");
		BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
		out.write(resXml.getBytes());
		out.flush();
		out.close();
	}

	@SuppressWarnings("unused")
	@RequestMapping("refund")
	public void refund(HttpServletResponse response,String out_trade_no,Double total_fee1,Double refund_fee1) throws SDKRuntimeException {
		boolean bool = PayUtil.refund(response, out_trade_no, total_fee1, refund_fee1);
	}
	
	@RequestMapping("/jsapidticketsign")
	public void jsapidticketsign(HttpServletRequest request,String url,PrintWriter out){
		if(StringUtils.isBlank(url)){
			out.write(ServiceResult.initErrorJson("url不能为空！"));
			return ;
		}
		JsApiTicket jsapi_ticket = BaseUtil.getJsApiTicket();
		Map<String, String> map =  SignUtil.jsApiTicketSign(jsapi_ticket.getTicket(), url);
		out.write(ServiceResult.initSuccessJson(new Gson().toJson(map)));
	}
	
}
