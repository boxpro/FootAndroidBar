package com.shcd.footandroidbar.entity;

import com.shcd.footandroidbar.util.Util;

public class CompanyInfo extends BaseEntity{

	/***/
	private static final long serialVersionUID = 4606004926439583448L;
	
	/**公司编号*/
	private String code;
	
	/**公司全称*/
	private String fullName;
	
	/**公司简称**/
	private String shortName;

	/**
	 *是否启动蓝牙功能
	 */
	private boolean isBluetooth;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public boolean isBluetooth() {
		return isBluetooth;
	}

	public void setBluetooth(boolean bluetooth) {
		isBluetooth = bluetooth;
	}

	/**公司编号*/
	public String getCode() {
		return Util.maskString(code);
	}

	/**公司编号*/
	public void setCode(String code) {
		this.code = code;
	}

	/**公司全称*/
	public String getFullName() {
		return Util.maskString(fullName);
	}

	/**公司全称*/
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**公司简称**/
	public String getShortName() {
		return Util.maskString(shortName);
	}

	/**公司简称**/
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
}
