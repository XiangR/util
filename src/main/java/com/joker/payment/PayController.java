package com.joker.payment;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joker.staticcommon.StringUtility;

@Controller
@RequestMapping("pay")
public class PayController {
	Logger logger = LogManager.getLogger(this.getClass().getSimpleName());

	private WxpayServiceImpl wxpayService;

	@RequestMapping("alipay")
	public String aliPay(HttpServletRequest request, @RequestParam(value = "id") Integer id) {
		String path = request.getContextPath();
		String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + path + "/";
		Map<String, String> sParaTemp = new HashMap<String, String>();

		Integer total_fee = null;
		String out_trade_no = null;

		// sParaTemp.put("total_fee", "0.01"); //金额
		sParaTemp.put("total_fee", total_fee + ""); // 金额
		sParaTemp.put("out_trade_no", out_trade_no);// 订单号

		// 调用支付宝接口
		String orderInfo = "";
		sParaTemp.put("service", AlipayConfig.service_web); // 服务接口
		sParaTemp.put("sign_type", AlipayConfig.sign_type); // 加密方式
		sParaTemp.put("partner", AlipayConfig.partner);// 合作身份者
		sParaTemp.put("seller_id", AlipayConfig.seller_id);// 支付宝账号 同合作身份者
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);// 字符串格式
		sParaTemp.put("payment_type", AlipayConfig.payment_type); // 支付类型
		sParaTemp.put("notify_url", basePath + "pay/alipay/success"); // 异步通知接口
		sParaTemp.put("return_url", basePath);// 支付时返回的页面
		sParaTemp.put("show_url", basePath);// 支付成功时跳转的页面
		sParaTemp.put("subject", "单车支付");// 标题
		sParaTemp.put("app_pay", "Y");// 是否打开APP
		sParaTemp.put("body", "单车支付");// 内容
		orderInfo = AlipaySubmit.buildRequest(sParaTemp, "get", "确认");
		request.setAttribute("orderInfo", orderInfo);
		return "pages/pay/alipay";
	}

	// alipay 支付结果回调检测
	@ResponseBody
	@RequestMapping("alipay/success")
	public String aliPaySuccess(HttpServletRequest request) {
		String refund_status = request.getParameter("trade_status");
		logger.info("aliPaySuccess : " + refund_status);
		if (!refund_status.equals("TRADE_SUCCESS")) { // 判断是否是支付成功状态
			return "";
		} else {
			// 接收所有数据
			Map<String, String> params = new HashMap<String, String>();
			@SuppressWarnings("unchecked")
			Enumeration<String> parameterNames = request.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				String parameterName = parameterNames.nextElement();
				params.put(parameterName, request.getParameter(parameterName));
			}
			// 判断是否是支付宝发来的数据,并且判断是否正确
			if (AlipayNotify.verify(params)) {
				String out_trade_no = request.getParameter("out_trade_no");// 订单号
				logger.info("out_trade_no : " + out_trade_no);

				return "success";
			} else {
				return "fail";
			}
		}
	}

	@ResponseBody
	@RequestMapping("weixinpay")
	public Object weixinPay(HttpServletRequest request) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		// 微信支付

		String path = request.getContextPath();
		String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + path + "/";

		HashMap<String, String> wxPayMap = null;
		String price = "";
		String orderNumber = "";
		String userOpenId = "";
		try {
			String notify_url = basePath + "pay/weixinpay/notify";
			logger.info("basePath: " + basePath);
			wxPayMap = wxpayService.wxPayReturn("单车支付" + price, price, notify_url, orderNumber, request.getRemoteAddr(), "JSAPI", userOpenId);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteApiException e) {
			e.printStackTrace();
		}

		map.put("paySign", wxPayMap.get("sign"));
		map.put("appId", wxPayMap.get("appid"));
		map.put("nonceStr", wxPayMap.get("nonce_str"));
		map.put("package", wxPayMap.get("package"));
		map.put("timeStamp", wxPayMap.get("timestamp"));
		map.put("signType", "MD5");
		logger.info("resultMap - > " + map);
		return map;
	}

	// weixinpay 支付结果回调检测
	@ResponseBody
	@RequestMapping("weixinpay/notify")
	public Object weixinPayNotify(HttpServletRequest request) {

		logger.info("进入微信异步回调");
		DataInputStream in = null;
		List<BasicNameValuePair> packageParams = new LinkedList<BasicNameValuePair>();
		try {
			in = new DataInputStream(request.getInputStream());
			byte[] dataOrigin = new byte[request.getContentLength()];
			in.readFully(dataOrigin);
			String wxNotifyXml = new String(dataOrigin); // 从字节数组中得到表示实体的字符串
			logger.info("wxNotifyXml: " + wxNotifyXml);
			HashMap<String, String> result = WxpayServiceImpl.decodeXml(wxNotifyXml);
			logger.info("result: " + result.toString());

			if (result.get("return_code").equals("SUCCESS") && result.get("result_code").equals("SUCCESS")) {
				packageParams.add(new BasicNameValuePair("return_code", "SUCCESS"));
				packageParams.add(new BasicNameValuePair("return_msg", "OK"));
				String orderNumber = result.get("out_trade_no");
				logger.info("orderNumber: " + orderNumber);
				// TODO 订单成功支付 -> 业务处理

			} else {
				packageParams.add(new BasicNameValuePair("return_code", "FAIL"));
				packageParams.add(new BasicNameValuePair("return_msg", "FAIL"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (RemoteApiException e) {
			e.printStackTrace();
			logger.error(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		String content = WxpayServiceImpl.toNotifyXml(packageParams);
		logger.info("返回给微信的结果：" + content);
		return content;
	}

	@RequestMapping("weixinpay/success")
	public Object weixinPaySuccess(HttpServletRequest request) {

		logger.info("支付成功后的订单检查");
		// 检查是否存在未支付订单
		try {
			/*
			 * TODO 使用request -> sessionUser
			 * 使用sessionUser 去查询用户未支付的orderId
			 */

			String orderId = "orderId";

			if (StringUtility.isNullOrEmpty(orderId)) {
				logger.info("后台不存在未支付订单，已在微信notify_url中做过处理");
			}
			/*
			 * 调取微信api 查询 orderId 是否真实支付
			 */
			HashMap<String, String> wxPayMap = wxpayService.wxPayCheck(orderId);
			logger.info("后台存在未支付订单，检查订单的结果：" + wxPayMap.toString());
			if (wxPayMap.get("trade_state").equals("SUCCESS") && wxPayMap.get("return_code").equals("SUCCESS") && wxPayMap.get("result_code").equals("SUCCESS")) {
				logger.info("订单检查成功，已修改为支付");
			}
		} catch (URISyntaxException | IOException | RemoteApiException e) {
			e.printStackTrace();
			logger.error(e);
		}

		// 未支付成功
		logger.info("未支付成功！");
		return "redirect:/pay";
	}
}