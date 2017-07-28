package com.joker.enumcommon;

public enum PhotoSize {
	M((byte) 1), XL((byte) 3), FULL((byte) 4), ORIGINAL((byte) 5);

	private Byte value;

	public Byte getValue() {
		return value;
	}

	public void setValue(Byte value) {
		this.value = value;
	}

	private PhotoSize(Byte value) {
		this.value = value;
	}

	public String getPath() {
		return this.toString().toLowerCase();
	}

}
