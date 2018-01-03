package com.joker.payment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.joker.fileupload.ConfigParser;

public class WxpayServiceImpl {
    Logger logger = LogManager.getLogger(this.getClass().getName().toString());

    String APPID = "";
    String MCHID = "";
    String KEY = "";
    String APPSECRET = "";

    public HashMap<String, String> wxPayReturn(String body, String total_fee, String notify_url, String orderId, String ip, String tradeType, String openId)
            throws URISyntaxException, IOException, RemoteApiException {

        URI uri = new URIBuilder("https://api.mch.weixin.qq.com/pay/unifiedorder").build();
        logger.info(String.format("body:%s, fee:%s, url:%s, order:%s, ip:%s", body, total_fee, notify_url, orderId, ip));
        String entity = genProductArgs(body, total_fee, notify_url, orderId, ip, tradeType, openId); // 把数据加密转换成xml字符串
        logger.info(String.format("body:%s, fee:%s, url:%s, order:%s, ip:%s", body, total_fee, notify_url, orderId, ip));
        String content = doPost(uri, entity); // 请求微信服务器,信息是否正确
        logger.info("请求微信服务器的结果：content -> " + content);
        HashMap<String, String> result = decodeXml(content); // xml字符串转换成map,
        addPaymentSign(result); // 生成sign加密字符串
        return result;
    }

    // public static void main(String[] args) throws URISyntaxException,
    // IOException, RemoteApiException {
    // WxpayServiceImpl test = new WxpayServiceImpl();
    // HashMap<String, String> wxPayMap = test.wxPayCheck("201706021019472271");
    // if (wxPayMap.get("trade_state").equals("SUCCESS") &&
    // wxPayMap.get("return_code").equals("SUCCESS") &&
    // wxPayMap.get("result_code").equals("SUCCESS")) {
    // System.out.println("SUCCESS");
    // }
    // }

    /**
     * 查询订单是否真实支付
     *
     * @param orderId 订单的id
     * @return
     */

    public HashMap<String, String> wxPayCheck(String orderId) throws URISyntaxException, IOException, RemoteApiException {

        URI uri = new URIBuilder("https://api.mch.weixin.qq.com/pay/orderquery").build();
        logger.info(String.format("检查订单的订单号 -> orderId： %s", orderId));
        String entity = genProductArgsCheck(orderId); // 把数据加密转换成xml字符串
        String content = doPost(uri, entity); // 请求微信服务器,信息是否正确
        HashMap<String, String> result = decodeXml(content); // xml字符串转换成map
        return result;
    }

    private void addPaymentSign(HashMap<String, String> result) {
        result.put("timestamp", "" + new Date().getTime() / 1000);
        List<BasicNameValuePair> signParams = new LinkedList<>();
        signParams.add(new BasicNameValuePair("appId", result.get("appid")));
        signParams.add(new BasicNameValuePair("nonceStr", result.get("nonce_str")));
        result.put("package", "prepay_id=" + result.get("prepay_id"));
        signParams.add(new BasicNameValuePair("package", result.get("package")));
        signParams.add(new BasicNameValuePair("signType", "MD5"));
        signParams.add(new BasicNameValuePair("timeStamp", result.get("timestamp")));
        result.put("sign", genPackageSign(signParams, KEY));
    }

    /**
     * 查询订单是否真实支付
     *
     * @param orderId 订单的id
     * @return
     */
    public String genProductArgsCheck(String orderId) {
        try {
            List<BasicNameValuePair> packageParams = new LinkedList<BasicNameValuePair>();
            packageParams.add(new BasicNameValuePair("appid", APPID));
            packageParams.add(new BasicNameValuePair("mch_id", MCHID));
            packageParams.add(new BasicNameValuePair("nonce_str", genNonceStr()));
            packageParams.add(new BasicNameValuePair("out_trade_no", orderId));
            String sign = genPackageSign(packageParams, KEY);
            packageParams.add(new BasicNameValuePair("sign", sign));
            String xmlstring = toXml(packageParams);
            return xmlstring;
        } catch (Exception e) {
            return null;
        }
    }

    public String genProductArgs(String body, String total_fee, String notify_url, String orderId, String ip, String tradeType, String openId) {
        try {
            List<BasicNameValuePair> packageParams = new LinkedList<BasicNameValuePair>();
            packageParams.add(new BasicNameValuePair("appid", APPID));
            packageParams.add(new BasicNameValuePair("body", body));
            packageParams.add(new BasicNameValuePair("mch_id", MCHID));
            packageParams.add(new BasicNameValuePair("nonce_str", genNonceStr()));
            packageParams.add(new BasicNameValuePair("notify_url", notify_url));
            packageParams.add(new BasicNameValuePair("openid", openId));
            packageParams.add(new BasicNameValuePair("out_trade_no", orderId));
            packageParams.add(new BasicNameValuePair("spbill_create_ip", ip));
            packageParams.add(new BasicNameValuePair("total_fee", total_fee));
            packageParams.add(new BasicNameValuePair("trade_type", tradeType));
            String sign = genPackageSign(packageParams, KEY);
            packageParams.add(new BasicNameValuePair("sign", sign));
            String xmlstring = toXml(packageParams);
            return xmlstring;
        } catch (Exception e) {
            return null;
        }
    }

    public String doPost(URI uri, String request) throws IOException {
        return doPost(uri, new StringEntity(request, StandardCharsets.UTF_8));
    }

    public String doPost(URI uri, HttpEntity request) throws IOException {
        try {
            HttpPost post = new HttpPost(uri);
            post.setEntity(request);
            try (CloseableHttpResponse response = HttpClientBuilder.create().build().execute(post)) {
                HttpEntity entity = response.getEntity();
                String result = loadEntity(entity);
                logger.info("微信请求结果：" + result);
                return result;
            }
        } catch (IOException e) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(stream));
            throw e;
        } finally {
        }
    }

    public static String genNonceStr() {
        try {
            Random random = new Random();
            return MD5Util.MD5(String.valueOf(random.nextInt(10000)).getBytes());
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    /**
     * 生成签名
     */
    protected String genPackageSign(List<BasicNameValuePair> params, String key) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        if (key != null) {
            sb.append("key=");
            sb.append(key);
        }

        try {
            logger.info("签名串:" + sb.toString());
            return MD5Util.MD5(sb.toString().getBytes("UTF-8")).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String toXml(List<BasicNameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<" + params.get(i).getName() + ">");

            sb.append(StringEscapeUtils.escapeXml(params.get(i).getValue()));
            sb.append("</" + params.get(i).getName() + ">");
        }
        sb.append("</xml>");

        return sb.toString();
    }

    public static String toNotifyXml(List<BasicNameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<" + params.get(i).getName() + ">");
            sb.append("<![CDATA[");
            sb.append(StringEscapeUtils.escapeXml(params.get(i).getValue()));
            sb.append("]]>");
            sb.append("</" + params.get(i).getName() + ">");
        }
        sb.append("</xml>");

        return sb.toString();
    }

    public static HashMap<String, String> decodeXml(String content) throws RemoteApiException {
        try {

            HashMap<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:
                        if ("xml".equals(nodeName) == false) {
                            // 实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            throw new RemoteApiException(e);
        }
    }

    private String loadEntity(HttpEntity entity) throws IOException {
        byte[] bytes = EntityUtils.toByteArray(entity);
        return new String(bytes, "UTF-8");
    }

    public String queryBookingWxApp(String orderId) {
        StringBuffer xml = new StringBuffer();

        try {
            xml.append("</xml>");
            List<BasicNameValuePair> packageParams = new LinkedList<BasicNameValuePair>();
            packageParams.add(new BasicNameValuePair("appid", ConfigParser.getPaymentProperty("wxPaymentAppid")));
            packageParams.add(new BasicNameValuePair("mch_id", ConfigParser.getPaymentProperty("wxPaymentPrepayid")));
            packageParams.add(new BasicNameValuePair("nonce_str", genNonceStr()));
            // packageParams.add(new BasicNameValuePair("openid",
            // "o0plcwELk4hq8PcLJuETAZrCzw_M"));
            packageParams.add(new BasicNameValuePair("out_trade_no", orderId));
            packageParams.add(new BasicNameValuePair("sign_type", "MD5"));

            String sign = genPackageSign(packageParams, ConfigParser.getPaymentProperty("wxPaymentApikey"));
            System.out.println(sign);
            packageParams.add(new BasicNameValuePair("sign", sign));
            logger.info(packageParams);
            String xmlstring = toXml(packageParams);

            return xmlstring;
        } catch (Exception e) {
            return null;
        }
    }

    public String queryBookingWx(String orderId) {
        StringBuffer xml = new StringBuffer();

        try {

            xml.append("</xml>");
            List<BasicNameValuePair> packageParams = new LinkedList<BasicNameValuePair>();
            packageParams.add(new BasicNameValuePair("appid", ConfigParser.getPaymentProperty("wxPaymentAppidWeb")));
            packageParams.add(new BasicNameValuePair("mch_id", ConfigParser.getPaymentProperty("wxPaymentPrepayidWeb")));
            packageParams.add(new BasicNameValuePair("nonce_str", genNonceStr()));
            packageParams.add(new BasicNameValuePair("out_trade_no", orderId));
            packageParams.add(new BasicNameValuePair("sign_type", "MD5"));

            String sign = genPackageSign(packageParams, ConfigParser.getPaymentProperty("wxPaymentApikey"));
            System.out.println(sign);
            packageParams.add(new BasicNameValuePair("sign", sign));
            logger.info(packageParams);
            String xmlstring = toXml(packageParams);

            return xmlstring;
        } catch (Exception e) {
            return null;
        }
    }

    public HashMap<String, String> checkPay(String orderId, boolean isApp) throws URISyntaxException, IOException, RemoteApiException {
        URI uri = new URIBuilder("https://api.mch.weixin.qq.com/pay/orderquery").build();
        logger.info(String.format("order:%s", orderId));
        String entity = "";
        if (isApp) {
            entity = queryBookingWxApp(orderId);
        } else {
            entity = queryBookingWx(orderId);
        }
        logger.info(String.format("order:%s", orderId));
        String content = doPost(uri, entity);
        HashMap<String, String> result = decodeXml(content);
        addPaymentSign(result);
        logger.info(result);
        return result;
    }

}
