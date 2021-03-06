package com.pks.wechat.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pks.wechat.material.BasicMaterial;
import com.pks.wechat.material.NewsMaterial;
import com.pks.wechat.material.OtherMaterial;
import com.pks.wechat.message.resp.Article;
import com.pks.wechat.message.resp.Music;
import com.pks.wechat.pojo.AccessToken;
import com.pks.wechat.pojo.WeChatGroup;
import com.pks.wechat.pojo.WeChatMedia;
import com.pks.wechat.pojo.WeChatQRCode;
import com.pks.wechat.pojo.WeChatUserList;

public class AdvancedUtil {
	private static Logger log = LoggerFactory.getLogger(AdvancedUtil.class);
	
	/**
	 * @Description: 组装文本客服消息
	 * @param openId
	 *            消息发送对象
	 * @param content
	 *            文本消息内容
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String makeTextCustomMessage(String openId, String content) {
		// 对消息内容中的双引号进行转义
		content = content.replace("\"", "\\\"");
		String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"text\",\"text\":{\"content\":\"%s\"}}";
		return String.format(jsonMsg, openId, content);
	}

	/**
	 * @Description: 组装图片客服消息
	 * @param openId
	 *            消息发送对象
	 * @param mediaId
	 *            媒体文件id
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String makeImageCustomMessage(String openId, String mediaId) {
		String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"image\",\"image\":{\"media_id\":\"%s\"}}";
		return String.format(jsonMsg, openId, mediaId);
	}

	/**
	 * @Description: 组装语音客服消息
	 * @param openId
	 *            消息发送对象
	 * @param mediaId
	 *            媒体文件id
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String makeVoiceCustomMessage(String openId, String mediaId) {
		String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"voice\",\"voice\":{\"media_id\":\"%s\"}}";
		return String.format(jsonMsg, openId, mediaId);
	}

	/**
	 * @Description: 组装视频客服消息
	 * @param openId
	 *            消息发送对象
	 * @param mediaId
	 *            媒体文件id
	 * @param thumbMediaId
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String makeVideoCustomMessage(String openId, String mediaId,
			String thumbMediaId) {
		String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"video\",\"video\":{\"media_id\":\"%s\",\"thumb_media_id\":\"%s\"}}";
		return String.format(jsonMsg, openId, mediaId, thumbMediaId);
	}

	/**
	 * @Description: 组装音乐客服消息
	 * @param openId
	 *            消息发送对象
	 * @param music
	 *            音乐对象
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String makeMusicCustomMessage(String openId, Music music) {
		String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"music\",\"music\":%s}";
		jsonMsg = String.format(jsonMsg, openId, JSONObject.fromObject(music)
				.toString());
		// 将jsonMsg中的thumbmediaid替换为thumb_media_id
		jsonMsg = jsonMsg.replace("thumbmediaid", "thumb_media_id");
		return jsonMsg;
	}

	/**
	 * @Description: 组装图文客服消息
	 * @param openId
	 *            消息发送对象
	 * @param articleList
	 *            图文消息列表
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String makeNewsCustomMessage(String openId,
			List<Article> articleList) {
		String jsonMsg = "{\"touser\":\"%s\",\"msgtype\":\"news\",\"news\":{\"articles\":%s}}";
		jsonMsg = String.format(
				jsonMsg,
				openId,
				JSONArray.fromObject(articleList).toString()
						.replaceAll("\"", "\\\""));
		// 将jsonMsg中的picUrl替换为picurl
		jsonMsg = jsonMsg.replace("picUrl", "picurl");
		return jsonMsg;
	}

	/**
	 * @Description: 创建临时带参二维码
	 * @param accessToken
	 *            接口访问凭证
	 * @param expireSeconds
	 *            二维码有效时间，单位为秒，最大不超过1800
	 * @param sceneId
	 *            场景ID
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static WeChatQRCode createTemporaryQRCode(String accessToken,
			int expireSeconds, int sceneId) {
		WeChatQRCode weixinQRCode = null;
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=ACCESS_TOKEN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
		// 需要提交的json数据
		String jsonMsg = "{\"expire_seconds\": %d, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %d}}}";
		// 创建临时带参二维码
		JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "POST",
				String.format(jsonMsg, expireSeconds, sceneId));

		if (null != jsonObject) {
			try {
				weixinQRCode = new WeChatQRCode();
				weixinQRCode.setTicket(jsonObject.getString("ticket"));
				weixinQRCode.setExpireSeconds(jsonObject
						.getInt("expire_seconds"));
				log.info("创建临时带参二维码成功 ticket:{} expire_seconds:{}",
						weixinQRCode.getTicket(),
						weixinQRCode.getExpireSeconds());
			} catch (Exception e) {
				weixinQRCode = null;
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getString("errmsg");
				log.error("创建临时带参二维码失败 errcode:{} errmsg:{}", errorCode,
						errorMsg);
			}
		}
		return weixinQRCode;
	}

	/**
	 * @Description: 创建永久带参二维码
	 * @param accessToken
	 *            接口访问凭证
	 * @param sceneId
	 *            场景ID
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String createPermanentQRCode(String accessToken, int sceneId) {
		String ticket = null;
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=ACCESS_TOKEN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
		// 需要提交的json数据
		String jsonMsg = "{\"action_name\": \"QR_LIMIT_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %d}}}";
		// 创建永久带参二维码
		JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "POST",
				String.format(jsonMsg, sceneId));

		if (null != jsonObject) {
			try {
				ticket = jsonObject.getString("ticket");
				log.info("创建永久带参二维码成功 ticket:{}", ticket);
			} catch (Exception e) {
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getString("errmsg");
				log.error("创建永久带参二维码失败 errcode:{} errmsg:{}", errorCode,
						errorMsg);
			}
		}
		return ticket;
	}

	/**
	 * @Description: 根据ticket换取二维码
	 * @param ticket
	 *            二维码ticket
	 * @param savePath
	 *            保存路径
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String getQRCode(String ticket, String savePath) {
		String filePath = null;
		// 拼接请求地址
		String requestUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET";
		requestUrl = requestUrl.replace("TICKET",
				CommonUtil.urlEncodeUTF8(ticket));
		try {
			URL url = new URL(requestUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setRequestMethod("GET");

			if (!savePath.endsWith("/")) {
				savePath += "/";
			}
			// 将ticket作为文件名
			filePath = savePath + ticket + ".jpg";

			// 将微信服务器返回的输入流写入文件
			BufferedInputStream bis = new BufferedInputStream(
					conn.getInputStream());
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			byte[] buf = new byte[8096];
			int size = 0;
			while ((size = bis.read(buf)) != -1)
				fos.write(buf, 0, size);
			fos.close();
			bis.close();

			conn.disconnect();
			log.info("根据ticket换取二维码成功，filePath=" + filePath);
		} catch (Exception e) {
			filePath = null;
			log.error("根据ticket换取二维码失败：{}", e);
		}
		return filePath;
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
	public static WeChatUserList getUserList(String accessToken,
			String nextOpenId) {
		WeChatUserList weixinUserList = null;
		if (null == nextOpenId) {
			nextOpenId = "";
		}
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&next_openid=NEXT_OPENID";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace(
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

	/**
	 * @Description: 查询分组
	 * @param accessToken
	 *            调用接口凭证
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public static List<WeChatGroup> getGroups(String accessToken) {
		List<WeChatGroup> weixinGroupList = null;
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/groups/get?access_token=ACCESS_TOKEN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
		// 查询分组
		JSONObject jsonObject = CommonUtil
				.httpsRequest(requestUrl, "GET", null);

		if (null != jsonObject) {
			try {
				weixinGroupList = JSONArray.toList(
						jsonObject.getJSONArray("groups"), WeChatGroup.class);
			} catch (JSONException e) {
				weixinGroupList = null;
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getString("errmsg");
				log.error("查询分组失败 errcode:{} errmsg:{}", errorCode, errorMsg);
			}
		}
		return weixinGroupList;
	}

	/**
	 * @Description: 创建分组
	 * @param accessToken
	 *            接口访问凭证
	 * @param groupName
	 *            分组名称
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static WeChatGroup createGroup(String accessToken, String groupName) {
		WeChatGroup weixinGroup = null;
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/groups/create?access_token=ACCESS_TOKEN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
		// 需要提交的json数据
		String jsonData = "{\"group\" : {\"name\" : \"%s\"}}";
		// 创建分组
		JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "POST",
				String.format(jsonData, groupName));

		if (null != jsonObject) {
			try {
				weixinGroup = new WeChatGroup();
				weixinGroup.setId(jsonObject.getJSONObject("group")
						.getInt("id"));
				weixinGroup.setName(jsonObject.getJSONObject("group")
						.getString("name"));
			} catch (JSONException e) {
				weixinGroup = null;
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getString("errmsg");
				log.error("创建分组失败 errcode:{} errmsg:{}", errorCode, errorMsg);
			}
		}
		return weixinGroup;
	}

	/**
	 * @Description: 修改分组名
	 * @param accessToken
	 *            接口访问凭证
	 * @param groupId
	 *            分组id
	 * @param groupName
	 *            修改后的分组名
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static boolean updateGroup(String accessToken, int groupId,
			String groupName) {
		boolean result = false;
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/groups/update?access_token=ACCESS_TOKEN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
		// 需要提交的json数据
		String jsonData = "{\"group\": {\"id\": %d, \"name\": \"%s\"}}";
		// 修改分组名
		JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "POST",
				String.format(jsonData, groupId, groupName));
		if (null != jsonObject) {
			int errorCode = jsonObject.getInt("errcode");
			String errorMsg = jsonObject.getString("errmsg");
			if (0 == errorCode) {
				result = true;
				log.info("修改分组名成功 errcode:{} errmsg:{}", errorCode, errorMsg);
			} else {
				log.error("修改分组名失败 errcode:{} errmsg:{}", errorCode, errorMsg);
			}
		}
		return result;
	}

	/**
	 * @Description: 移动用户分组
	 * @param accessToken
	 *            接口访问凭证
	 * @param openId
	 *            用户标识
	 * @param groupId
	 *            分组id
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static boolean updateMemberGroup(String accessToken, String openId,
			int groupId) {
		boolean result = false;
		// 拼接请求地址
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/groups/members/update?access_token=ACCESS_TOKEN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken);
		// 需要提交的json数据
		String jsonData = "{\"openid\":\"%s\",\"to_groupid\":%d}";
		// 移动用户分组
		JSONObject jsonObject = CommonUtil.httpsRequest(requestUrl, "POST",
				String.format(jsonData, openId, groupId));

		if (null != jsonObject) {
			int errorCode = jsonObject.getInt("errcode");
			String errorMsg = jsonObject.getString("errmsg");
			if (0 == errorCode) {
				result = true;
				log.info("移动用户分组成功 errcode:{} errmsg:{}", errorCode, errorMsg);
			} else {
				log.error("移动用户分组失败 errcode:{} errmsg:{}", errorCode, errorMsg);
			}
		}
		return result;
	}

	/**
	 * @Description: 上传媒体文件
	 * @param accessToken
	 *            接口访问凭证
	 * @param type
	 *            媒体文件类型（image、voice、video和thumb）
	 * @param mediaFileUrl
	 *            媒体文件的url
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static WeChatMedia uploadMedia(String accessToken, String type,
			String mediaFileUrl) {
		WeChatMedia weixinMedia = null;
		// 拼装请求地址
		String uploadMediaUrl = "http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
		uploadMediaUrl = uploadMediaUrl.replace("ACCESS_TOKEN", accessToken)
				.replace("TYPE", type);

		// 定义数据分隔符
		String boundary = "------------7da2e536604c8";
		try {
			URL uploadUrl = new URL(uploadMediaUrl);
			HttpURLConnection uploadConn = (HttpURLConnection) uploadUrl
					.openConnection();
			uploadConn.setDoOutput(true);
			uploadConn.setDoInput(true);
			uploadConn.setRequestMethod("POST");
			// 设置请求头Content-Type
			uploadConn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			// 获取媒体文件上传的输出流（往微信服务器写数据）
			OutputStream outputStream = uploadConn.getOutputStream();

			URL mediaUrl = new URL(mediaFileUrl);
			HttpURLConnection meidaConn = (HttpURLConnection) mediaUrl
					.openConnection();
			meidaConn.setDoOutput(true);
			meidaConn.setRequestMethod("GET");

			// 从请求头中获取内容类型
			String contentType = meidaConn.getHeaderField("Content-Type");
			// 根据内容类型判断文件扩展名
			String fileExt = CommonUtil.getFileExt(contentType);
			// 请求体开始
			outputStream.write(("--" + boundary + "\r\n").getBytes());
			outputStream
					.write(String
							.format("Content-Disposition: form-data; name=\"media\"; filename=\"file1%s\"\r\n",
									fileExt).getBytes());
			outputStream.write(String.format("Content-Type: %s\r\n\r\n",
					contentType).getBytes());

			// 获取媒体文件的输入流（读取文件）
			BufferedInputStream bis = new BufferedInputStream(
					meidaConn.getInputStream());
			byte[] buf = new byte[8096];
			int size = 0;
			while ((size = bis.read(buf)) != -1) {
				// 将媒体文件写到输出流（往微信服务器写数据）
				outputStream.write(buf, 0, size);
			}
			// 请求体结束
			outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
			outputStream.close();
			bis.close();
			meidaConn.disconnect();

			// 获取媒体文件上传的输入流（从微信服务器读数据）
			InputStream inputStream = uploadConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			StringBuffer buffer = new StringBuffer();
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			uploadConn.disconnect();

			// 使用JSON-lib解析返回结果
			JSONObject jsonObject = JSONObject.fromObject(buffer.toString());
			weixinMedia = new WeChatMedia();
			weixinMedia.setType(jsonObject.getString("type"));
			// type等于thumb时的返回结果和其它类型不一样
			if ("thumb".equals(type))
				weixinMedia.setMediaId(jsonObject.getString("thumb_media_id"));
			else
				weixinMedia.setMediaId(jsonObject.getString("media_id"));
			weixinMedia.setCreatedAt(jsonObject.getInt("created_at"));
		} catch (Exception e) {
			weixinMedia = null;
			log.error("上传媒体文件失败：{}", e);
		}
		return weixinMedia;
	}

	/**
	 * @Description: 下载媒体文件
	 * @param accessToken
	 *            接口访问凭证
	 * @param mediaId
	 *            媒体文件标识
	 * @param savePath
	 *            文件在服务器上的存储路径
	 * @return
	 * @throws
	 * @author Administrator
	 * @date 2015-12-22
	 */
	public static String getMedia(String accessToken, String mediaId,
			String savePath) {
		String filePath = null;
		// 拼接请求地址
		String requestUrl = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace(
				"MEDIA_ID", mediaId);
		System.out.println(requestUrl);
		try {
			URL url = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setRequestMethod("GET");

			if (!savePath.endsWith("/")) {
				savePath += "/";
			}
			// 根据内容类型获取扩展名
			String fileExt = CommonUtil.getFileExt(conn
					.getHeaderField("Content-Type"));
			// 将mediaId作为文件名
			filePath = savePath + mediaId + fileExt;

			BufferedInputStream bis = new BufferedInputStream(
					conn.getInputStream());
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			byte[] buf = new byte[8096];
			int size = 0;
			while ((size = bis.read(buf)) != -1)
				fos.write(buf, 0, size);
			fos.close();
			bis.close();

			conn.disconnect();
			log.info("下载媒体文件成功，filePath=" + filePath);
		} catch (Exception e) {
			filePath = null;
			log.error("下载媒体文件失败：{}", e);
		}
		return filePath;
	}
	
	public static void main(String args[]) {
		String type = "image";
		AccessToken accessToken = BaseUtil.getAccessToken();
		JSONObject jsonObject = MaterialUtil.getMaterailListJson(accessToken.getAccess_token(), type, 0, 50);
		if(jsonObject!=null && jsonObject.containsKey("item")){
			List<BasicMaterial> list = new ArrayList<BasicMaterial>();
			//List<String> itemList = new Gson().fromJson(jsonObject.getString("item"), new TypeToken<List<String>>(){}.getType());
			if("news".equals(type)){
				list = new Gson().fromJson(jsonObject.getString("item"),  new TypeToken<List<NewsMaterial>>(){}.getType());
			}else{
				list = new Gson().fromJson(jsonObject.getString("item"),  new TypeToken<List<OtherMaterial>>(){}.getType());
			}
			System.out.println(list.size());
		}
//			List<String> itemList = new Gson().fromJson(jsonObject.getString("item"), new TypeToken<List<String>>(){}.getType());
//			List<BasicMaterial> list = new ArrayList<BasicMaterial>();
//			for (String itemStr : itemList) {
//				JSONObject itemJson = JSONObject.fromObject(itemStr);
//				if(itemJson.containsKey("content")){
//					String newsJson = itemJson.getJSONObject("content").getString("news_item");
//					List<MaterialNews> news = new Gson().fromJson(newsJson, new TypeToken<List<MaterialNews>>(){}.getType());
//					ImageMaterial im = new ImageMaterial();
//					im.setNews_item(news);
//					im.setMedia_id(itemJson.getString("media_id"));
//					im.setUpdate_time(itemJson.getString("update_time"));
//					list.add(im);
//				}else{
//					OtherMaterial om = new OtherMaterial();
//					om.setMedia_id(itemJson.getString("media_id"));
//					om.setUpdate_time(itemJson.getString("update_time"));
//					om.setName(itemJson.getString("name"));
//					om.setUrl(itemJson.getString("url"));
//					list.add(om);
//				}
//			}
//		}
		
		//		File file = new File("E:/images/404.jpg");
//		WeChatMaterail materail = uploadMaterail(accessToken.getAccess_token(), file, "标题", "描述");
//		System.out.println(materail.getMedia_id()); 
//		
//		// 获取接口访问凭证
//		String accessToken = CommonUtil.getToken("APPID", "APPSECRET")
//				.getAccessToken();
//
//		/**
//		 * 发送客服消息（文本消息）
//		 */
//		// 组装文本客服消息
//		String jsonTextMsg = makeTextCustomMessage(
//				"oEdzejiHCDqafJbz4WNJtWTMbDcE",
//				"点击查看<a href=\"http://blog.csdn.net/lyq8479\">柳峰的博客</a>");
//		// 发送客服消息
//		sendCustomMessage(accessToken, jsonTextMsg);
//
//		/**
//		 * 发送客服消息（图文消息）
//		 */
//		Article article1 = new Article();
//		article1.setTitle("微信上也能斗地主");
//		article1.setDescription("");
//		article1.setPicUrl("http://www.egouji.com/xiaoq/game/doudizhu_big.png");
//		article1.setUrl("http://resource.duopao.com/duopao/games/small_games/weixingame/Doudizhu/doudizhu.htm");
//		Article article2 = new Article();
//		article2.setTitle("傲气雄鹰\n80后不得不玩的经典游戏");
//		article2.setDescription("");
//		article2.setPicUrl("http://www.egouji.com/xiaoq/game/aoqixiongying.png");
//		article2.setUrl("http://resource.duopao.com/duopao/games/small_games/weixingame/Plane/aoqixiongying.html");
//		List<Article> list = new ArrayList<Article>();
//		list.add(article1);
//		list.add(article2);
//		// 组装图文客服消息
//		String jsonNewsMsg = makeNewsCustomMessage(
//				"oEdzejiHCDqafJbz4WNJtWTMbDcE", list);
//		// 发送客服消息
//		sendCustomMessage(accessToken, jsonNewsMsg);
//
//		/**
//		 * 创建临时二维码
//		 */
//		WeChatQRCode weixinQRCode = createTemporaryQRCode(accessToken, 900,
//				111111);
//		// 临时二维码的ticket
//		System.out.println(weixinQRCode.getTicket());
//		// 临时二维码的有效时间
//		System.out.println(weixinQRCode.getExpireSeconds());
//
//		/**
//		 * 根据ticket换取二维码
//		 */
//		String ticket = "gQEg7zoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2lIVVJ3VmJsTzFsQ0ZuQ0Y1bG5WAAIEW35+UgMEAAAAAA==";
//		String savePath = "G:/download";
//		// 根据ticket换取二维码
//		getQRCode(ticket, savePath);
//
//		/**
//		 * 获取用户信息
//		 */
//		WeChatUserInfo user = getUserInfo(accessToken,
//				"oEdzejiHCDqafJbz4WNJtWTMbDcE");
//		System.out.println("OpenID：" + user.getOpenId());
//		System.out.println("关注状态：" + user.getSubscribe());
//		System.out.println("关注时间：" + user.getSubscribeTime());
//		System.out.println("昵称：" + user.getNickname());
//		System.out.println("性别：" + user.getSex());
//		System.out.println("国家：" + user.getCountry());
//		System.out.println("省份：" + user.getProvince());
//		System.out.println("城市：" + user.getCity());
//		System.out.println("语言：" + user.getLanguage());
//		System.out.println("头像：" + user.getHeadImgUrl());
//
//		/**
//		 * 获取关注者列表
//		 */
//		WeChatUserList weixinUserList = getUserList(accessToken, "");
//		System.out.println("总关注用户数：" + weixinUserList.getTotal());
//		System.out.println("本次获取用户数：" + weixinUserList.getCount());
//		System.out.println("OpenID列表："
//				+ weixinUserList.getOpenIdList().toString());
//		System.out.println("next_openid：" + weixinUserList.getNextOpenId());
//
//		/**
//		 * 查询分组
//		 */
//		List<WeChatGroup> groupList = getGroups(accessToken);
//		// 循环输出各分组信息
//		for (WeChatGroup group : groupList) {
//			System.out.println(String.format("ID：%d 名称：%s 用户数：%d",
//					group.getId(), group.getName(), group.getCount()));
//		}
//
//		/**
//		 * 创建分组
//		 */
//		WeChatGroup group = createGroup(accessToken, "公司员工");
//		System.out.println(String.format("成功创建分组：%s id：%d", group.getName(),
//				group.getId()));
//
//		/**
//		 * 修改分组名
//		 */
//		updateGroup(accessToken, 100, "同事");
//
//		/**
//		 * 移动用户分组
//		 */
//		updateMemberGroup(accessToken, "oEdzejiHCDqafJbz4WNJtWTMbDcE", 100);
//
//		/**
//		 * 上传多媒体文件
//		 */
//		WeChatMedia weixinMedia = uploadMedia(accessToken, "voice",
//				"http://localhost:8080/WeChatmpapi/test.mp3");
//		System.out.println(weixinMedia.getMediaId());
//		System.out.println(weixinMedia.getType());
//		System.out.println(weixinMedia.getCreatedAt());
//
//		/**
//		 * 下载多媒体文件
//		 */
//		getMedia(
//				accessToken,
//				"N7xWhOGYSLWUMPzVcGnxKFbhXeD_lLT5sXxyxDGEsCzWIB2CcUijSeQOYjWLMpcn",
//				"G:/download");
	}
	
	
	
}