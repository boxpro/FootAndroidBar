package com.shcd.footandroidbar.entity;

import com.shcd.footandroidbar.util.Util;

public class PadTeaRoomMessageInfo extends BaseEntity {

	/**
	 * pad，message，此消息会在每天晚上自动清空的，为了加快读写速度 茶吧消息
	 */
	private static final long serialVersionUID = 7201006868339190898L;

	private Long companyID;
	
	/**消息内容*/
	private String messageContent;

	private Boolean readFlag;
	
	/** 房间编号 */
	private String roomCode;

	/** 产品数量 */
	private String productNum;

	public void setCompanyID(Long companyID) {
		this.companyID = companyID;
	}

	public Long getCompanyID() {
		return Util.maskLong(companyID);
	}

	/**消息内容*/
	public String getMessageContent() {
		return Util.maskString(messageContent);
	}

	/**消息内容*/
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	/**消息是否被确认过 false未读 true已读*/
	public Boolean getReadFlag() {
		return Util.maskBoolean(readFlag);
	}

	/**消息是否被确认过 false未读 true已读*/
	public void setReadFlag(Boolean readFlag) {
		this.readFlag = readFlag;
	}

	public String getRoomCode() {
		return Util.maskString(roomCode);
	}

	public void setRoomCode(String roomCode) {
		this.roomCode = roomCode;
	}

	/** 产品数量 */
	public String getProductNum() {
		return Util.maskString(productNum);
	}

	/** 产品数量 */
	public void setProductNum(String productNum) {
		this.productNum = productNum;
	}

}
