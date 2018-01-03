package com.joker.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.joker.model.SessionString;
import com.joker.staticcommon.RandomUtility;

@Controller
@RequestMapping("/volidationcode")
public class VolidationCodeController {

	Logger logger = LogManager.getLogger(this.getClass().getName());

	/**
	 * Ajex获取注册验证码
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "/register", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] getRegisterCode(HttpServletRequest request) throws IOException {
		RandomUtility random = RandomUtility.Instance();
		ByteArrayInputStream image = random.getImage();
		request.getSession().setAttribute(SessionString.RegisterVolidationCode, random.getString());
		return IOUtils.toByteArray(image);
	}

	/**
	 * Ajex检查注册验证码
	 */
	@RequestMapping("/register/check")
	@ResponseBody
	public Object checkRegisterCode(String fieldId, String fieldValue, HttpServletRequest request) {
		JSONArray json = new JSONArray();
		boolean flag = checkImgCode(fieldValue, request);
		json.add(fieldId);
		json.add(flag);
		return json;
	}

	public static boolean checkImgCode(String value, HttpServletRequest request) {
		System.out.println(request.getSession().getAttribute(SessionString.RegisterVolidationCode));
		boolean flag = value == null ? false : value.equalsIgnoreCase((String) request.getSession().getAttribute(SessionString.RegisterVolidationCode));
		return flag;
	}

}
