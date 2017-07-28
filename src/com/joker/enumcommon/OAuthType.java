package com.joker.enumcommon;

public enum OAuthType {
	UWOJIALOCAL((byte) 0), QQ((byte) 1), SINA((byte) 2), WEIXIN((byte) 3), QQAPP((byte) 4), WEIXINAPP((byte) 5), WEIXINWEB((byte) 6), ;

	private byte value;

	private OAuthType(byte value) {
		this.value = value;
	}

	public byte getValue() {
		return value;
	}

	public static OAuthType parse(byte b) {
		if (b == QQ.getValue()) {
			return QQ;
		} else if (b == SINA.getValue()) {
			return SINA;
		} else if (b == WEIXIN.getValue()) {
			return WEIXIN;
		} else if (b == QQAPP.getValue()) {
			return QQAPP;
		} else if (b == WEIXINAPP.getValue()) {
			return WEIXINAPP;
		} else if (b == WEIXINWEB.getValue()) {
			return WEIXINWEB;
		}
		return OAuthType.UWOJIALOCAL;
	}
}
