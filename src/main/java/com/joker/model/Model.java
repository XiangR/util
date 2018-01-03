package com.joker.model;

import com.alibaba.fastjson.JSONObject;

public class Model {
	private Integer id;
	private Integer module_id;
	private String menuName;
	private String modelName;
	private Byte status;
	private Integer idx;
	private String module_id_name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getModule_id() {
		return module_id;
	}

	public void setModule_id(Integer module_id) {
		this.module_id = module_id;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
	}

	public Integer getIdx() {
		return idx;
	}

	public void setIdx(Integer idx) {
		this.idx = idx;
	}

	public String getModule_id_name() {
		return module_id_name;
	}

	public void setModule_id_name(String module_id_name) {
		this.module_id_name = module_id_name;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

	/*** 自定义开始 ***/
	/*** 自定义结束 ***/
}
