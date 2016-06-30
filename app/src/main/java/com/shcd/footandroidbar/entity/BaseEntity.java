package com.shcd.footandroidbar.entity;

import java.io.Serializable;

import com.shcd.footandroidbar.util.Util;

public class BaseEntity implements Serializable {

	/***/
	private static final long serialVersionUID = -1718989921953047193L;

	protected long id;

	/**这个日期是create的时候日期， 不可以更改的日期*/
	protected String inputDt;

	/**这个日期是create的时候日期， 不可以更改的日期*/
	protected String inputTm;

	protected String lstUptDt;

	protected String lstUptTm;

	protected String lstUptBy;

	public Long getId() {
		return Util.maskLong(id);
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLstUptDt() {
		return Util.maskString(lstUptDt);
	}

	public void setLstUptDt(String lstUptDt) {
		this.lstUptDt = lstUptDt;
	}

	public String getLstUptTm() {
		return Util.maskString(lstUptTm);
	}

	public void setLstUptTm(String lstUptTm) {
		this.lstUptTm = lstUptTm;
	}

	public String getLstUptBy() {
		return Util.maskString(lstUptBy);
	}

	public void setLstUptBy(String lstUptBy) {
		this.lstUptBy = lstUptBy;
	}

	public String getInputDt() {
		return Util.maskString(inputDt);
	}

	public void setInputDt(String inputDt) {
		this.inputDt = inputDt;
	}

	public String getInputTm() {
		return Util.maskString(inputTm);
	}

	public void setInputTm(String inputTm) {
		this.inputTm = inputTm;
	}

}
