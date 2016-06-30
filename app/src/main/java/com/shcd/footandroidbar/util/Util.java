package com.shcd.footandroidbar.util;

import java.math.BigDecimal;

import com.cahd.util.DES3;

public class Util {
	public static Boolean maskBoolean(Boolean fromData){
		if (fromData==null) {
			return Const.DEFAULT_BOOLEAN_VALUE;
		}else{
			return fromData;
		}
	}

	/**
	 * 格式话String值，如果参数为 null 返回 ""
	 * */
	public static String maskString(String fromData) {
		if (fromData == null) {
			return Const.DEFAULT_STRING_VALUE;
		} else {
			return fromData;
		}
	}

	 /**
	  * 格式话BigDecimal值，如果参数为 null 返回  0.00
	  * */
	public static BigDecimal maskBigDecimal(BigDecimal fromData){
		if(fromData==null){
			return Const.DEFAULT_BIGDECIMAL_VALUE;
		}else{
			return fromData.setScale(Const.CALC_DECIMAL, BigDecimal.ROUND_HALF_UP);
		}
	}
	
	public static Long maskLong(Long fromData) {
		if (fromData == null) {
			return new Long(0);
		} else {
			return fromData;
		}
	}

	public static Integer maskInteger(Integer fromData) {
		if (fromData == null) {
			return Const.DEFAULT_INTGER_VALUE;
		} else {
			return fromData;
		}
	}

	/**
	 * 解密函数
	 * 
	 * @param text
	 * @return
	 */
	public static String decrypt(String text) {
		if (text == null) {
			text = "";
		}
		if (text.equalsIgnoreCase("")) {
			return "";
		}
		try {

			DES3 des = new DES3();
			byte[] stringToByte = des.stringToByte(text);
			byte[] decryptorByte = des.createDecryptor(stringToByte);
			return new String(decryptorByte);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
}
