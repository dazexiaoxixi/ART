package com.example.artadmission;

public final class define_var {
	public static int maxStudentPerGroup = 10;   //每组最多联系人数
	public static int tickN = 0;
//	public static int[] needScore = new int[maxStudentPerGroup];
//	public static boolean[] absentStudent = new boolean[maxStudentPerGroup];
	public static float[] score = new float[maxStudentPerGroup];
	public static int checked[] = new int[define_var.maxStudentPerGroup];	
	public static String subject="",room="",group="",userNameStr="",examTypeStr="";
	public static String std;
	/***测试的IP/端口号****/
	public static String RHOST = null;
	public static String TPORT = null;
	/***状态**/
	public static int loginLossProcess = 0;	//登录失败了
	public static int loginSuccessProcess = 1;	//登录成功了
	public static int agreeProcess = 2;			//许可了
	public static int groupStartProcess = 3;	//本组开始考试了
	public static int studentStartProcess = 4;	//某考生开始考试了
	public static int checkStopProcess = 5;		//检录结束了
	public static int uploadScoreProcess = 6;	//分数提交成功了
	public static int groupStopProcess = 7;		//本组考试结束了
	/****handle*****/
	public static int conOKHandle = 3;		//连接成功
	public static int conNOHandle = 4;		//连接失败
	public static int logOKHandle = 5;		//登录成功
	public static int logNOHandle = 6;		//登录失败
	public static int newtHandle = 7;		//推送
	public static int loseHandle = 8;		//缺考
	public static int uploadScoreHandle = 9;		//收到分数
	public static int uploadScoreHandle_NO = 10;		//收到分数,失败
	public static int infoHandle = 11;		//已考和待考人数	
	
	/***指令*****/
	public static String connect = "CON";			//连接
	public static String connect_ok = "CON_OK|";		//连接成功
	public static String connect_no = "CON_NO|";		//连接失败
	public static String log = "LOG";			//登录
	public static String log_ok = "LOG_OK|";		//登录成功
	public static String log_no = "LOG_NO|";		//登录失败
	public static String newt = "NEWT|";			//推送给我的消息
	public static String newt_ack = "NEWT_OK";	//推送的回复
	public static String lose = "LOSE|";			//缺考
	public static String lose_ack = "LOSE_OK";	//缺考的回复
	public static String uploadScore = "SCORE";			//上传分数
	public static String uploadScore_ack = "SCORE_OK|";	//上传分数的回复
	public static String uploadScore_NO = "SCORE_NO|";	//上传分数的回复，失败
	public static String info = "INFO|";			//广播消息，例如已考和待考人数
	public static String info_ack = "INFO_OK";	//已考和待考人数的回复
	
	public static int newt_endHandle = 11;		//测试用的-----已考和待考人数	
	public static String newt_end = "NEWT_END|";			//推送给我的消息
	public static String newt_end_ack = "NEWT_END_OK";	//推送的回复
}
