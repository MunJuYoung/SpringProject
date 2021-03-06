package com.spring.cjs2108_mjyProject.vo;

import lombok.Data;

@Data
public class CartListVO {
	private int idx;
	private String cartDate;
	private String mid;
	private int productIdx;
	private String productName;
	private int mainPrice;
	private String thumbImg;
	private String optionName;
	private String optionPrice;
	private String optionNum;
	private int totalPrice;
}
