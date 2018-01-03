package com.joker.staticcommon;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

/**
 * @author xiangR
 * @date 2017年7月31日上午10:17:56
 */
public class UrlDownloader {
    static Logger logger = LogManager.getLogger(UrlDownloader.class.getName());
    static int TRY_COUNT = 6;

    static void getBankImgError(String filePath, String bankName) {
        FileOutputStream output = null;
        InputStream input = null;
        try {
            File fp = new File(filePath);
            if (!fp.exists()) { // 由路径创建文件夹
                fp.mkdir();
            }
            HttpClient client = new HttpClient();
            String url = "https://apimg.alipay.com/combo.png?d=cashier&t=" + bankName;
            GetMethod method = new GetMethod(url);
            int statusCode = client.executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                File storeFile = new File(filePath + "/" + bankName + ".png");
                if (storeFile.exists()) {
                    storeFile.delete();
                }
                output = new FileOutputStream(storeFile);
                input = method.getResponseBodyAsStream(); // 输出到文件
                int tempByte = -1;
                while ((tempByte = input.read()) > 0) {
                    output.write(tempByte);
                }
                output.write(tempByte);
            } else {
                logger.info(bankName + "下载失败");
            }
        } catch (Exception ex) {
            logger.error(bankName + "下载失败", ex);
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] getByteArr(String url) {
        HttpClient client = new HttpClient();
        GetMethod method = null;
        try {
            method = new GetMethod(url);
            int statusCode = client.executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                return method.getResponseBody();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return null;
    }

    public static JSONObject getInfo(String url, Map<String, String> params) {
        String bodystr = getContent(url, params);
        try {
            if (!StringUtility.isNullOrEmpty(bodystr))
                return (JSONObject) JSONObject.parse(bodystr);
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    public static JSONObject getInfoOkHttp(String url, String params) {
        String bodystr = getContentOkHttp(url, params);
        try {
            if (!StringUtility.isNullOrEmpty(bodystr))
                return JSONObject.parseObject(bodystr);
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    public static String getContentOkHttp(String url, String params) {
        return post(url, params);
    }

    private static final okhttp3.MediaType CONTENT_TYPE_FORM = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
    // 分别设置Http的连接,写入,读取的超时时间
    private static okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static String post(String url, String params) {
        okhttp3.RequestBody body = okhttp3.RequestBody.create(CONTENT_TYPE_FORM, params);
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).post(body).build();
        return exec(request);
    }

    private static String exec(okhttp3.Request request) {
        try {
            okhttp3.Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful())
                throw new RuntimeException("Unexpected code " + response);
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getContent(String url, Map<String, String> params) {
        HttpClient client = new HttpClient();
        HttpMethod method = null;
        method = new UTF8PostMethod(url);
        method.getParams().setContentCharset("utf-8");
        if (params != null) {
            for (String key : params.keySet()) {
                ((PostMethod) method).setParameter(key, params.get(key));
            }
        }
        try {
            client.executeMethod(method);
            return method.getResponseBodyAsString();// 返回结果
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } // 执行请求
        return null;
    }

    public static JSONObject getInfoIndex(String url, Map<String, String> params) {

        try {
            String bodystr = getContentIndex(url, params);
            if (!StringUtility.isNullOrEmpty(bodystr))
                return (JSONObject) JSONObject.parse(bodystr);
        } catch (Exception ex) {
            logger.error(ex);
            return null;
        }
        return null;
    }

    public static String getContentIndex(String url, Map<String, String> params) {
        HttpClient client = new HttpClient();
        HttpMethod method = null;
        method = new UTF8PostMethod(url);
        method.getParams().setContentCharset("utf-8");
        client.getHttpConnectionManager().getParams().setConnectionTimeout(10000); // 连接超时
        client.getHttpConnectionManager().getParams().setSoTimeout(10000); // 数据返回超时
        if (params != null) {
            for (String key : params.keySet()) {
                ((PostMethod) method).setParameter(key, params.get(key));
            }
        }
        try {
            client.executeMethod(method);
            return method.getResponseBodyAsString();// 返回结果
        } catch (HttpException e) {
            logger.error(url + ":\n" + JSONObject.toJSONString(params), e);
        } catch (IOException e) {
            logger.error(url + ":\n" + JSONObject.toJSONString(params), e);
        } finally {
            method.releaseConnection();
        } // 执行请求
        return null;
    }

    static void getImg(InputStream inStream) {
        writeToDisk(readStream(inStream), "111");
    }

    private static void writeToDisk(byte[] img, String fileName) {
        try {
            File file = new File("f:/test/" + fileName);
            FileOutputStream fops = new FileOutputStream(file);
            fops.write(img);
            fops.flush();
            fops.close();
            System.out.println("图片已经写入到 D 盘");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] readStream(InputStream inStream) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            return outStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void Download(String realUrl, String destFilePath, String encoding, Map<String, String> cookies)
            throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException, IOException {
        Download(realUrl, destFilePath, false, encoding, cookies);
    }

    public static void Download(String realUrl, String destFilePath, int tryCount, String encoding, Map<String, String> cookies)
            throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException, IOException {
        do {
            Download(realUrl, destFilePath, false, encoding, cookies);
        } while (--tryCount > 0);
    }

    public static void DownloadBinnary(String realUrl, String destFilePath) {
        DownloadBinnary(realUrl, destFilePath, false);
    }

    public static void DownloadForGet(String realUrl, Map<String, String> params, String destFilePath, boolean overwrite, String encoding, Map<String, String> cookies) {
        StringBuffer url = new StringBuffer(realUrl + "?");
        try {
            for (String key : params.keySet()) {
                url.append(key + "=" + URLEncoder.encode(params.get(key).toString(), "utf-8") + "&");
            }
        } catch (UnsupportedEncodingException e) {
        }
        url.setLength(url.length() - 1);
        Download(url.toString(), null, destFilePath, overwrite, encoding, cookies);
    }

    public static String Download(String realUrl, String destFilePath, boolean overwrite, String encoding, Map<String, String> cookies) {
        return Download(realUrl, null, destFilePath, overwrite, encoding, cookies);
    }

    public static String Download(String realUrl, String destFilePath, boolean overwrite, String encoding, Map<String, String> cookies, String contentType) {
        return Download(realUrl, null, destFilePath, overwrite, encoding, cookies, contentType, TRY_COUNT);
    }

    public static String Download(String realUrl, Map<String, String> params, String destFilePath, boolean overwrite, String encoding, Map<String, String> cookies) {
        return Download(realUrl, params, destFilePath, overwrite, encoding, cookies, TRY_COUNT);
    }

    ;

    public static String Download(String realUrl, Map<String, String> params, String destFilePath, boolean overwrite, String encoding, Map<String, String> cookies, String contentType) {
        return Download(realUrl, params, destFilePath, overwrite, encoding, cookies, contentType, TRY_COUNT);
    }

    public static String Download(String realUrl, Map<String, String> params, String destFilePath, boolean overwrite, String encoding, Map<String, String> cookies, int tryTime) {
        return Download(realUrl, params, destFilePath, overwrite, encoding, cookies, null, tryTime);
    }

    public static String Download(String realUrl, Map<String, String> params, String destFilePath, boolean overwrite, String encoding, Map<String, String> cookies, String contentType, int tryTime) {
        return Download(null, null, realUrl, params, destFilePath, overwrite, encoding, cookies, contentType, tryTime);
    }

    public static String Download(String proxy, Integer port, String realUrl, String encoding, Map<String, String> cookies) {
        return Download(null, null, realUrl, null, null, false, encoding, cookies, null, TRY_COUNT);
    }

    public static String Download(String proxy, Integer port, String realUrl, Map<String, String> params, String destFilePath, boolean overwrite, String encoding, Map<String, String> cookies,
                                  String contentType, int tryTime) {
        System.out.println("url:" + realUrl);
        HttpURLConnection conn = null;
        try {
            if (destFilePath != null) {
                File destFile = new File(destFilePath);
                if (!destFile.exists()) {
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                } else {
                    if (overwrite) {
                        destFile.delete();
                    } else {
                        return null;
                    }
                }
            }
            setGlobalIgnoreCert();
            URL url = new URL(realUrl);
            if (!StringUtility.isNullOrEmpty(proxy)) {
                Proxy proxyInfo = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy, port));
                conn = (HttpURLConnection) url.openConnection(proxyInfo);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            initConnectionProperty(encoding, cookies, contentType, conn);

            if (params != null && params.size() > 0) {
                String textParams = getPostString(params);
                OutputStream out = conn.getOutputStream();
                DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
                outStream.write(textParams.getBytes());
                outStream.flush();
                out.flush();
                out.close();
            } else {
                conn.setRequestMethod("GET");
            }
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String redirectUrl = conn.getHeaderField("Location");
                if (!StringUtility.isNullOrEmpty(redirectUrl)) {
                    if (realUrl.startsWith("https://")) {
                        redirectUrl = redirectUrl.replace("http://", "https://");
                    }
                    if (redirectUrl.startsWith("/")) {
                        redirectUrl = url.getProtocol() + "://" + url.getHost() + redirectUrl;
                    }
                    if (!redirectUrl.startsWith("https://") && !redirectUrl.startsWith("http://")) {
                        redirectUrl = url.getProtocol() + "://" + url.getHost() + "/" + redirectUrl;
                    }
                    readCookies(cookies, conn);
                    conn.disconnect();
                    System.out.println("Redirect:" + redirectUrl);
                    if (redirectUrl.equalsIgnoreCase(realUrl)) {
                        // throw new
                        // SealNetworkException(ExceptionCodes.ProductLineNetWorkCycleRedirect);
                    }
                    // if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    // if (redirectUrl.startsWith("https://")) {
                    // redirectUrl = redirectUrl.replace("https://", "http://");
                    // } else if (redirectUrl.startsWith("http://")) {
                    // redirectUrl = redirectUrl.replace("http://", "https://");
                    // }
                    // Download(redirectUrl, params, destFilePath, overwrite,
                    // encoding, cookies);
                    // return;
                    // }
                    if (redirectUrl.endsWith("start.do")) {
                        redirectUrl = redirectUrl.replace("https://", "http://");
                        return Download(redirectUrl, null, destFilePath, overwrite, encoding, cookies);
                    } else if (responseCode == 303) {
                        return Download(redirectUrl, null, destFilePath, overwrite, encoding, cookies);
                    } else {
                        return Download(redirectUrl, params, destFilePath, overwrite, encoding, cookies);
                    }
                }
                System.out.println("response code:" + responseCode + ", url:" + realUrl);
            }
            readCookies(cookies, conn);
            if (destFilePath != null) {
                InputStream inputStream = conn.getInputStream();
                if ("gzip".equals(conn.getContentEncoding())) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                FileOutputStream fileout = new FileOutputStream(destFilePath);
                fileout.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
                OutputStreamWriter writer = new OutputStreamWriter(fileout, "utf-8");
                String line = null;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.write("\r\n"); // 补上换行符
                }
                writer.flush();
                reader.close();
                writer.close();
            }
            // Thread.sleep(5000);
        } catch (java.net.SocketException | javax.net.ssl.SSLHandshakeException | java.net.SocketTimeoutException e) {
            if (--tryTime > 0) {
                logger.error(String.format("Connection timeout! real Url:%s, dest File Path:%s, encoding:%s, try:%d.", realUrl, destFilePath, encoding, tryTime) + e.getMessage());
                Download(realUrl, params, destFilePath, overwrite, encoding, cookies, tryTime);
                return null;
            } else {
                logger.error(String.format("Connection timeout! real Url:%s, dest File Path:%s, encoding:%s, try:%d.", realUrl, destFilePath, encoding, tryTime), e);
            }
        } catch (Exception e) {
            logger.error(String.format("real Url:%s, dest File Path:%s, encoding:%s", realUrl, destFilePath, encoding), e);
            System.out.println(String.format("C--Url:%s", realUrl));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return realUrl;
    }

    public static String DownloadContent(String realUrl, Map<String, String> params, String encoding, Map<String, String> cookies) {
        System.out.println("url:" + realUrl);
        HttpURLConnection conn = null;
        try {
            setGlobalIgnoreCert();
            URL url = new URL(realUrl);
            conn = (HttpURLConnection) url.openConnection();
            initConnectionProperty(encoding, cookies, null, conn);

            if (params != null && params.size() > 0) {
                String textParams = getPostString(params);
                OutputStream out = conn.getOutputStream();
                DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
                outStream.write(textParams.getBytes());
                outStream.flush();
                out.flush();
                out.close();
            } else {
                conn.setRequestMethod("GET");
            }
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String redirectUrl = conn.getHeaderField("Location");
                if (!StringUtility.isNullOrEmpty(redirectUrl)) {
                    if (realUrl.startsWith("https://")) {
                        redirectUrl = redirectUrl.replace("http://", "https://");
                    }
                    if (redirectUrl.startsWith("/")) {
                        redirectUrl = url.getProtocol() + "://" + url.getHost() + redirectUrl;
                    }
                    if (!redirectUrl.startsWith("https://") && !redirectUrl.startsWith("http://")) {
                        redirectUrl = url.getProtocol() + "://" + url.getHost() + "/" + redirectUrl;
                    }
                    readCookies(cookies, conn);
                    conn.disconnect();
                    System.out.println("Redirect:" + redirectUrl);
                    if (redirectUrl.equalsIgnoreCase(realUrl)) {
                        // throw new
                        // SealNetworkException(ExceptionCodes.ProductLineNetWorkCycleRedirect);
                    }
                    if (redirectUrl.endsWith("start.do")) {
                        redirectUrl = redirectUrl.replace("https://", "http://");
                        return DownloadContent(redirectUrl, null, encoding, cookies);
                    } else if (responseCode == 303) {
                        return DownloadContent(redirectUrl, null, encoding, cookies);
                    } else {
                        return DownloadContent(redirectUrl, params, encoding, cookies);
                    }
                }
                System.out.println("response code:" + responseCode + ", url:" + realUrl);
            }
            readCookies(cookies, conn);
            InputStream inputStream = conn.getInputStream();
            if ("gzip".equals(conn.getContentEncoding())) {
                inputStream = new GZIPInputStream(inputStream);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            StringBuffer result = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                result.append("\r\n"); // 补上换行符
            }
            reader.close();
            return result.toString();
            // Thread.sleep(5000);
        } catch (java.net.SocketException | javax.net.ssl.SSLHandshakeException | java.net.SocketTimeoutException e) {
            logger.error(String.format("Connection timeout! real Url:%s, encoding:%s.", realUrl, encoding), e);

        } catch (Exception e) {
            logger.error(String.format("real Url:%s, encoding:%s", realUrl, encoding), e);
            System.out.println(String.format("C--Url:%s", realUrl));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private static void initConnectionProperty(String encoding, Map<String, String> cookies, String contentType, HttpURLConnection conn) throws ProtocolException {
        conn.setRequestProperty("Accept-Charset", encoding);
        conn.setRequestProperty("contentType", encoding);
        if (!StringUtility.isNullOrEmpty(contentType)) {
            conn.setRequestProperty("Content-Type", contentType);
        }
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(200000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");

        // conn.setRequestProperty("Origin",
        // "http://www.espresso.cruisingpower.com/");
        // conn.setRequestProperty("Referer",
        // "http://www.espresso.cruisingpower.com/protected/start.do");
        if (cookies != null && cookies.size() > 0) {
            conn.setRequestProperty("Cookie", generateCookies(cookies));
        }
        conn.setInstanceFollowRedirects(false);
    }

    private static String generateCookies(Map<String, String> cookies) {
        StringBuilder results = new StringBuilder();
        for (String key : cookies.keySet()) {
            results.append(key);
            results.append("=");
            results.append(cookies.get(key));
            results.append(";");
        }
        return results.toString();
    }

    private static void readCookies(Map<String, String> cookies, HttpURLConnection conn) {
        String key = null;
        // 取cookie
        for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
            if (key.contains("Set-Cookie")) {
                List<String> cookieList = conn.getHeaderFields().get(key);
                mergeCookies(cookies, cookieList);
                break;
            }
        }
        // for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
        // if (key.contains("Cookie")) {
        // List<String> cookieList = conn.getHeaderFields().get(key);
        // mergeCookies(cookies, cookieList);
        // break;
        // }
        // }
    }

    private static void mergeCookies(Map<String, String> cookies, String newCookie) {
        if (cookies == null)
            return;
        String[] splits = newCookie.split(";");
        for (String single : splits) {
            if (!StringUtility.isNullOrEmpty(single) && single.contains("=")) {
                String key = single.substring(0, single.indexOf("=")).trim();
                if (!key.equalsIgnoreCase("expires") && !key.equalsIgnoreCase("domain") && !key.equalsIgnoreCase("path")) {
                    cookies.put(key, single.substring(single.indexOf("=") + 1));
                }
            }
        }

    }

    private static void mergeCookies(Map<String, String> cookies, List<String> cookieList) {
        StringBuilder cookieStr = new StringBuilder();
        for (String newCookie : cookieList) {
            cookieStr.append(newCookie);
            cookieStr.append(";");
        }
        mergeCookies(cookies, cookieStr.toString());
    }

    private static String getPostString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder textParam = new StringBuilder();
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (key.indexOf("#") >= 0) {
                key = key.substring(0, key.indexOf("#"));
            }
            textParam.append(java.net.URLEncoder.encode(key, "utf-8"));
            textParam.append("=");
            if (value != null) {
                textParam.append(java.net.URLEncoder.encode(value, "utf-8"));
            }
            textParam.append("&");
        }
        if (textParam.length() > 0) {
            textParam.setLength(textParam.length() - 1);
        }
        String ss = textParam.toString();
        return ss;
    }

    private static void setGlobalIgnoreCert() throws NoSuchAlgorithmException, KeyManagementException {
        HttpURLConnection.setFollowRedirects(true);
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // HttpURLConnection.setFollowRedirects(true);
    }

    public static String getCookie1() {
        // 登陆 Url
        String loginUrl = "https://www.ncl.com/cas/login";

        // 需登陆后访问的 Url
        String dataUrl = "https://www.ncl.com/bge/my-account";

        HttpClient httpClient = new HttpClient();

        // 模拟登陆，按实际服务器端要求选用 Post 或 Get 请求方式
        PostMethod postMethod = new PostMethod(loginUrl);

        // 设置登陆时要求的信息，一般就用户名和密码，验证码自己处理了
        NameValuePair[] data = {new NameValuePair("username", "uwojia"), new NameValuePair("password", "1qaz2wsxE"), new NameValuePair("code", "anyany")};
        postMethod.setRequestBody(data);
        String tmpcookies = "";

        try {
            // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            httpClient.executeMethod(postMethod);

            // 获得登陆后的 Cookie
            Cookie[] cookies = httpClient.getState().getCookies();
            for (Cookie c : cookies) {
                tmpcookies += c.toString() + ";";
            }

            // 进行登陆后的操作
            GetMethod getMethod = new GetMethod(dataUrl);

            // 每次访问需授权的网址时需带上前面的 cookie 作为通行证
            getMethod.setRequestHeader("cookie", tmpcookies);

            // 你还可以通过 PostMethod/GetMethod 设置更多的请求后数据
            // 例如，referer 从哪里来的，UA 像搜索引擎都会表名自己是谁，无良搜索引擎除外
            postMethod.setRequestHeader("Referer", "http://unmi.cc");
            postMethod.setRequestHeader("User-Agent", "Unmi Spot");

            httpClient.executeMethod(getMethod);

            // 打印出返回数据，检验一下是否成功
            String text = getMethod.getResponseBodyAsString();
            System.out.println(text);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpcookies;
    }

    public static class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }

    public static void DownloadBinnary(String realUrl, String destFilePath, boolean overwrite) {
        if (StringUtility.isNullOrEmpty(realUrl)) {
            return;
        }
        File destFile = new File(destFilePath);
        if (!destFile.exists()) {
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
        } else {
            if (overwrite) {
                destFile.delete();
            } else {
                return;
            }
        }
        URLConnection conn = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(realUrl);
            conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            InputStream inputStream = conn.getInputStream(); // 通过输入流获得图片数据
            byte[] getData = readInputStream(inputStream); // 获得图片的二进制数据
            fos = new FileOutputStream(destFilePath);
            fos.write(getData);
        } catch (Exception ex) {
            logger.error(String.format("real Url:%s, dest File Path:%s", realUrl, destFilePath), ex);
            System.out.println(realUrl);
            ex.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error(String.format("real Url:%s, dest File Path:%s", realUrl, destFilePath), e);
                    e.printStackTrace();
                }
            }
        }
    }

    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static void main(String[] args) {
        String str = ".";
        System.out.println("修改前：" + str);
        str = str.replace(str.substring(0, str.lastIndexOf(".") + 1), "");
        System.out.println("修改后：" + str);
    }

    private static class UTF8PostMethod extends PostMethod {
        public UTF8PostMethod(String url) {
            super(url);
        }

        @Override
        public String getRequestCharSet() {
            return "UTF-8";
        }
    }
}
