package com.joker.enumcommon;

import java.util.LinkedHashMap;
import java.util.Map;

public enum PhotoType {
	UNKNOWN((byte) 0), TEMP((byte) 1), PARENT((byte) 2), EXCEL((byte) 3);

	private Byte value;

	public Byte getValue() {
		return value;
	}

	public void setValue(Byte value) {
		this.value = value;
	}

	private PhotoType(Byte value) {
		this.value = value;
	}

	static LinkedHashMap<Byte, String> values = null;

	public static Map<Byte, String> getValues() {
		if (values == null) {
			init();
		}
		return values;
	}

	synchronized private static void init() {
		if (values == null) {
			values = new LinkedHashMap<Byte, String>();
			values.put(PARENT.getValue(), "photos/");
			values.put(TEMP.getValue(), "photos/temp");
			values.put(UNKNOWN.getValue(), "photos/unknown");
			values.put(EXCEL.getValue(), "photos/excel");
		}
	}

	public static String getPath(PhotoType photoType) {
		Byte value = photoType.getValue();
		return getPath(value);
	}

	public static String getPath(Byte value) {
		Map<Byte, String> map = getValues();
		if (map.containsKey(value)) {
			return map.get(value);
		}
		return null;
	}

}
