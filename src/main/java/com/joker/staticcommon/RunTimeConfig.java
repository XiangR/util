package com.joker.staticcommon;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.joker.fileupload.ConfigParser;

/**
 * 
 * @author xiangR
 * @date 2017年7月28日上午9:47:36
 *
 */
public class RunTimeConfig {

	static String appLocation = null;
	static Logger logger = LogManager.getLogger(RunTimeConfig.class.getName());

	static void initAppLocation() {
		String path = ConfigParser.getCommonProperty("localPath");
		if (StringUtility.isNullOrEmpty(path) || !path.startsWith("/")) {
			try {
				path = URLDecoder.decode(RunTimeConfig.class.getProtectionDomain().getCodeSource().getLocation().toString(), "UTF-8");
				path = path.substring(0, path.indexOf("/webapps/") + "/webapps".length());
				if (path.startsWith("file:/")) {
					path = path.substring("file:/".length());
				}
			} catch (UnsupportedEncodingException e) {
				path = ConfigParser.getCommonProperty("localPath");
				e.printStackTrace();
			}
		}
		appLocation = path;
	}

	public static String getRealPath(String relatePath) {
		if (appLocation == null) {
			initAppLocation();
		}
		if (!StringUtility.isNullOrEmpty(relatePath)) {
			relatePath = relatePath.replace('\\', '/');
			if (relatePath.length() > 0 && relatePath.charAt(0) == '/') {
				relatePath = relatePath.substring(1);
			}
			return appLocation + '/' + relatePath;
		}
		return appLocation;
	}
}
