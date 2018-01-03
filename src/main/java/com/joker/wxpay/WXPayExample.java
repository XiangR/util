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
import com.joker.staticcommon.CommonRunTimeConfig;
import com.joker.staticcommon.StringUtility;

public class WXPayExample {
	static Logger logger = LogManager.getLogger(WXPayConfigImpl.class.getSimpleName());

	private static WXPayConfigImpl config = WXPayConfigImpl.getInstance();
	private static WXPay wxpay = new WXPay(config);
	private static String notifyUrl = CommonRunTimeConfig.serverPath + "/user_payment/wxpay_notify";

	public Map<String, String> wxPayApp(HttpServletRequest request, Integer paymentId, Double money) {
		String bookingNumber = "";
		Integer totalFee = Double.valueOf(money * 100).intValue();
		totalFee = 1;// 测试
		Map<String, String> wxResponse = this.wxPayUnifiedOrder(request.getRemoteAddr(), bookingNumber, totalFee, notifyUrl, "APP", null);
		Map<String, String> paymentSign = this.getAppSign(wxResponse);
		logger.info(paymentSign); // 签名后的结果
		return paymentSign;
	}

	public Map<String, String> wxPayJsapi(HttpServletRequest request, Integer paymentId, Double money, String openid) {
		String bookingNumber = "";
		Integer totalFee = Double.valueOf(money * 100).intValue();
		totalFee = 1;// 测试
		Map<String, String> wxResponse = this.wxPayUnifiedOrder(request.getRemoteAddr(), bookingNumber, totalFee, notifyUrl, "JSAPI", openid);
		Map<String, String> paymentSign = this.getJsapiSign(wxResponse);
		logger.info(paymentSign); // 签名后的结果
		return paymentSign;
	}

	public Map<String, String> wxPayUnifiedOrder(String spbillCreateIp, String bookingNumber, Integer totalFee, String notifyUrl, String tradeType, String openid) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("body", "候鸟旅居-支付");
		data.put("out_trade_no", bookingNumber);
		data.put("fee_type", "CNY");
		data.put("total_fee", totalFee.toString());// 单位为分
		data.put("spbill_create_ip", StringUtility.isNullOrEmpty(spbillCreateIp) ? "123.12.12.123" : spbillCreateIp);
		data.put("notify_url", notifyUrl);
		data.put("trade_type", tradeType); // 默认为APP支付
		if (!StringUtility.isNullOrEmpty(openid) && tradeType.equals("JSAPI")) {// 在调用网页支付的时候需要openid
			data.put("openid", openid);
		}
		try {
			Map<String, String> response = wxpay.unifiedOrder(data);
			logger.info(response);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String wxNotify(HttpServletRequest request) {// 微信支付的回调检测
		Map<String, String> result = new HashMap<String, String>();
		DataInputStream in = null;
		try {
			in = new DataInputStream(request.getInputStream());
			byte[] dataOrigin = new byte[request.getContentLength()];
			in.readFully(dataOrigin);
			String wxNotifyXml = new String(dataOrigin);
			Map<String, String> notifyMap = WXPayUtil.xmlToMap(wxNotifyXml);
			logger.info("wxNotify notifyMap -> " + notifyMap);
			if (wxpay.isPayResultNotifySignatureValid(notifyMap) && notifyMap.get("return_code").equals("SUCCESS") && notifyMap.get("result_code").equals("SUCCESS")) {
				logger.info("wxNotify 支付结果校验成功");
				result.put("return_code", "SUCCESS");
				result.put("return_msg", "OK");
				String bookingNumber = notifyMap.get("out_trade_no");
				logger.info(bookingNumber);
			} else { // 签名错误，如果数据里没有sign字段，也认为是签名错误
				logger.info("wxNotify 支付结果校验失败");
				result.put("return_code", "FAIL");
				result.put("return_msg", "FAIL");
			}
			return WXPayUtil.mapToXml(result);
		} catch (Exception e) {
			e.printStackTrace();
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

	public Boolean wxPayRefund(String bookingNumber, Double refoundMoney) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("out_trade_no", bookingNumber);
		data.put("out_refund_no", bookingNumber);
		data.put("total_fee", refoundMoney.toString());
		data.put("refund_fee", refoundMoney.toString());
		data.put("refund_fee_type", "CNY");
		data.put("op_user_id", config.getMchID());
		data.put("refund_account", "REFUND_SOURCE_UNSETTLED_FUNDS");
		try {
			Map<String, String> response = wxpay.refund(data);
			if (response.get("return_code").equals("SUCCESS") && response.get("result_code").equals("SUCCESS") && response.get("return_msg").equals("OK")) {
				return true;
			} else {
				// 若未结算资金退款失败使用可用余额退款
				data.put("refund_account", "REFUND_SOURCE_RECHARGE_FUNDS");
				response = wxpay.refund(data);
				if (response.get("return_code").equals("SUCCESS") && response.get("result_code").equals("SUCCESS") && response.get("return_msg").equals("OK")) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Map<String, String> wxPayRefundQuery(String refundNumber) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("out_refund_no", refundNumber);
		try {
			Map<String, String> response = wxpay.refundQuery(data);
			logger.info("wxPayRefundQuery -> bookingNumber: " + refundNumber + ". response: " + response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Boolean wxPayOrderQuery(String orderId) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("out_trade_no", orderId);
		try {
			Map<String, String> wxPayMap = wxpay.orderQuery(data);
			logger.info(wxPayMap);
			if (wxPayMap.get("trade_state").equals("SUCCESS") && wxPayMap.get("return_code").equals("SUCCESS") && wxPayMap.get("result_code").equals("SUCCESS")) {
				logger.info("SUCCESS");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Map<String, String> getAppSign(Map<String, String> data) {
		try { // 检测通过
			Map<String, String> result = new HashMap<String, String>();
			Map<String, String> requestMap = new HashMap<String, String>();
			result.put("appid", data.get("appid"));
			result.put("partnerid", data.get("mch_id"));
			result.put("prepayid", data.get("prepay_id"));
			result.put("package", "Sign=WXPay");
			result.put("noncestr", WXPayUtil.generateNonceStr());
			result.put("timestamp", "" + new Date().getTime() / 1000);
			result.put("noncestr", WXPayUtil.generateNonceStr());
			requestMap.putAll(result);
			logger.info(requestMap);
			result.put("sign", WXPayUtil.generateSignature(requestMap, config.getKey()));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, String> getJsapiSign(Map<String, String> data) {
		try { // TODO 待检测
			Map<String, String> result = new HashMap<String, String>();
			Map<String, String> requestMap = new HashMap<String, String>();
			result.put("appId", data.get("appid"));
			result.put("timeStamp", "" + new Date().getTime() / 1000);
			result.put("noncestr", WXPayUtil.generateNonceStr());
			result.put("package", "prepay_id=" + result.get("prepay_id"));
			result.put("signType", "MD5");
			requestMap.putAll(result);
			logger.info(requestMap);
			result.put("sign", WXPayUtil.generateSignature(requestMap, config.getKey()));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
