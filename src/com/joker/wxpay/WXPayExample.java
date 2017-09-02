package com.joker.wxpay;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.joker.staticcommon.StringUtility;

public class WXPayExample {
	static Logger logger = LogManager.getLogger(WXPayExample.class.getName());

	private static MyConfig config = MyConfig.getInstance();
	private static WXPay wxpay = new WXPay(config);
	private static String wxNotifyUrl = "";

	public static void main(String[] args) {
		wxPayOrderQuery("201706021019472271");
	}

	public static Object wxPayUnifiedOrder(HttpServletRequest request, String orderId, Integer totalFee, String tradeType) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("body", "候鸟旅居-支付");
		data.put("out_trade_no", orderId);
		data.put("fee_type", "CNY");
		data.put("total_fee", totalFee.toString());// 单位为分
		data.put("spbill_create_ip", request != null ? request.getRemoteAddr() : "123.12.12.123");
		data.put("notify_url", wxNotifyUrl);
		data.put("trade_type", "APP"); // 指定为APP支付
		try {
			Map<String, String> response = wxpay.unifiedOrder(data);
			logger.info(response);
			Map<String, String> paymentSign = getPaymentSign(response);
			logger.info(paymentSign);
			return paymentSign;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, String> getPaymentSign(Map<String, String> data) {
		try {
			Map<String, String> result = new HashMap<String, String>();
			result.put("appid", data.get("appid"));
			result.put("partnerid", data.get("mch_id"));
			result.put("prepayid", data.get("prepay_id"));
			result.put("package", "Sign=WXPay");
			result.put("noncestr", WXPayUtil.generateNonceStr());
			result.put("timestamp", "" + new Date().getTime() / 1000);
			result.put("noncestr", WXPayUtil.generateNonceStr());
			result.put("sign", WXPayUtil.generateSignature(result, config.getKey()));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void wxPayOrderQuery(String orderId) {
		Map<String, String> data = new HashMap<String, String>();
		// data.put("out_trade_no", "201706021019472271");
		data.put("out_trade_no", orderId);
		try {
			Map<String, String> wxPayMap = wxpay.orderQuery(data);
			logger.info(wxPayMap);
			if (wxPayMap.get("trade_state").equals("SUCCESS") && wxPayMap.get("return_code").equals("SUCCESS") && wxPayMap.get("result_code").equals("SUCCESS")) {
				logger.info("SUCCESS");
				// TODO 业务处理
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object wxPayNotify(HttpServletRequest request) { // 微信支付的回调检测
		Map<String, String> result = new HashMap<String, String>();
		String notifyData = getWXNotifyString(request);
		try {
			if (StringUtility.isNullOrEmpty(notifyData)) {
				logger.info("异步回调失败");
			} else {
				Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyData);
				if (wxpay.isPayResultNotifySignatureValid(notifyMap)) {
					result.put("return_code", "SUCCESS");
					result.put("return_msg", "OK");
					/*
					 * 签名正确
					 * TODO 业务处理
					 */
				} else {
					result.put("return_code", "FAIL");
					result.put("return_msg", "FAIL");
					// 签名错误，如果数据里没有sign字段，也认为是签名错误
				}
			}
			return WXPayUtil.mapToXml(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void wxPayRefund(HashMap<String, String> data) {// 请求退款
		data.put("out_trade_no", "");
		data.put("out_refund_no", "");
		data.put("total_fee", "");
		data.put("refund_fee", "");
		data.put("refund_fee_type", "CNY");
		// data.put("op_user_id", config.getMchID());

		try {
			Map<String, String> result = wxpay.refund(data);
			logger.info(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void wxPayRefundQuery(String refundNumber) {// 插叙退款
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("out_refund_no", refundNumber);
		try {
			Map<String, String> result = wxpay.refundQuery(data);
			logger.info(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getWXNotifyString(HttpServletRequest request) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(request.getInputStream());
			byte[] dataOrigin = new byte[request.getContentLength()];
			in.readFully(dataOrigin);
			String wxNotifyXml = new String(dataOrigin);
			// logger.info("wxNotifyXml: " + wxNotifyXml);
			return wxNotifyXml;
		} catch (IOException e) {
			logger.error("异步回调失败", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
