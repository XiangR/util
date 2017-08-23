package com.joker.payment;


public class RemoteApiException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RemoteApiException(Exception e) {
		super(e);
	}
}
