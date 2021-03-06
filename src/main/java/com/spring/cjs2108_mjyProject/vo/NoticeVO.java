package com.spring.cjs2108_mjyProject.vo;

import lombok.Data;

@Data
public class NoticeVO {
	private int idx;
	private String nickName;
	private String title;
	private String content;
	private String wDate;
	private int readNum;
	private String mid;
	
	private int diffTime; //시간계산을 위해 저장한 변수(sql에서 시간단위로 계산해서 넘어온값을 저장)
	private String oriContent;// 원본 content의 내용을 저장시켜두기위한 필드
}
