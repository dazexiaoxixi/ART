package com.example.database;

import android.content.Context;
import android.graphics.Bitmap;

public class ExamnieeInfo {
	/*"groupID varchar(12)," +
			"examnieeID varchar(12),"+
			"examnieeName varchar(30)"+
			"examnieeStatus varchar(1)"+
			"examnieePic BLOB"+")";*/
	private String groupID;
	private String examnieeID;
	private String startTime;
	private String endTime;
	private String examnieeStatus;
	private String examinationID;
	private String examnieeName;
	private String examnieeIdentity;
	private String examnieeSex;
	private String examnieePicURL;
//	private String isFirstStart;
	//private Bitmap  examnieePicBitmap;
	public ExamnieeInfo(String groupID,String examnieeID,String startTime,String endTime,String examnieeStatus,String examinationID,
			String examnieeName,String examnieeIdentity,String examnieeSex, String examnieePicURL) {
		this.groupID = groupID;
		this.examnieeID = examnieeID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.examnieeStatus = examnieeStatus;
		this.examinationID = examinationID;
		this.examnieeName = examnieeName;
		this.examnieeIdentity = examnieeIdentity;
		this.examnieeSex = examnieeSex;
		this.examnieePicURL = examnieePicURL;
		//this.isFirstStart = isFirstStart;
	}
	public ExamnieeInfo(){
		
	}
	public String getGroupID(){
		return groupID;
	}
	public String getExamnieeID(){
		return examnieeID;
	}
	public String getStartTime(){
		return startTime;
	}
	public String getEndTime(){
		return endTime;
	}
	public String getExamnieeStatus(){
		return examnieeStatus;
	}
	public String getExaminationID(){
		return examinationID;
	}
	public String getExamnieeName(){
		return examnieeName;
	}
	public String getExamnieeIdentity(){
		return examnieeIdentity;
	}
	public String getExamnieeSex(){
		return examnieeSex;
	}
	public String getExamnieePicURL(){
		return examnieePicURL;
	}
//	public String getIsFirstStart(){
//		return isFirstStart;
//	}
//	public Bitmap getExamnieePicBitmape(){
//		return examnieePicBitmap;
//	}
	public void setGroupID(String groupID){
		this.groupID = groupID;
	}
	public void setExamnieeID(String examnieeID){
		this.examnieeID = examnieeID;
	}
	public void setStartTime(String startTime){
		this.startTime = startTime;
	}
	
	public void setEndTime(String endTime){
		this.endTime = endTime;
	}
	public void setExamnieeStatus(String examnieeStatus){
		this.examnieeStatus = examnieeStatus;
	}
	public void setExaminationID(String examinationID){
		this.examinationID = examinationID;
	}
	public void setExamnieeName(String examnieeName){
		this.examnieeName = examnieeName;
	}
	public void setExamnieeIdentity(String examnieeIdentity){
		this.examnieeIdentity = examnieeIdentity;
	}
	public void setExamnieeSex(String examnieeSex){
		this.examnieeSex = examnieeSex;
	}
	public void setExamnieePicURL(String examnieePicURL){
		this.examnieePicURL = examnieePicURL;
	}
//	public void setIsFirstStart(String isFirstStart){
//		this.isFirstStart = isFirstStart;
//	}
}
