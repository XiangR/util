package com.joker.staticcommon;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class RSASignUtil {

	public static void main(String[] args) {
		System.out.println(rsaSign("privateKey", "utf-8"));
	}

	public static String rsaSign(String privateKey, String charset) {
		try {
			PrivateKey priKey = getPrivateKeyFromPKCS8(AlipayConstants.SIGN_TYPE_RSA, new ByteArrayInputStream(privateKey.getBytes()));
			java.security.Signature signature = java.security.Signature.getInstance(AlipayConstants.SIGN_ALGORITHMS);
			signature.initSign(priKey);

			byte[] signed = signature.sign();
			return new String(Base64.encodeBase64(signed));
		} catch (InvalidKeySpecException ie) {
			System.out.println("RSA私钥格式不正确，请检查是否正确配置了PKCS8格式的私钥");
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * sha1WithRsa 加签
	 * 
	 * @param content
	 * @param privateKey
	 * @param charset
	 * @return
	 * @throws Exception
	 * @throws AlipayApiException
	 */
	public static String rsaSign(String content, String privateKey, String charset) throws Exception {
		try {
			PrivateKey priKey = getPrivateKeyFromPKCS8(AlipayConstants.SIGN_TYPE_RSA, new ByteArrayInputStream(privateKey.getBytes()));
			java.security.Signature signature = java.security.Signature.getInstance(AlipayConstants.SIGN_ALGORITHMS);
			signature.initSign(priKey);
			if (StringUtils.isEmpty(charset)) {
				signature.update(content.getBytes());
			} else {
				signature.update(content.getBytes(charset));
			}
			byte[] signed = signature.sign();
			return new String(Base64.encodeBase64(signed));
		} catch (InvalidKeySpecException ie) {
			throw new InvalidKeySpecException("RSA私钥格式不正确，请检查是否正确配置了PKCS8格式的私钥", ie);
		} catch (Exception e) {
			throw new Exception("RSAcontent = " + content + "; charset = " + charset, e);
		}
	}

	public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
		if (ins == null || StringUtils.isEmpty(algorithm)) {
			return null;
		}

		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

		byte[] encodedKey = StreamUtil.readText(ins).getBytes();

		encodedKey = Base64.decodeBase64(encodedKey);

		return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
	}
}
