package com.example.artcheckin;

public class FrameFormat {
	public static int maxStudentPerGroup = 10;   //每组最多联系人数
	public static byte frameHead = 0x18;		 //帧头		
	public static byte frameEnd = 0x0A;			 //帧尾
	public static int usernameLength = 6;		 //用户名长度
	public static int passwordLength = 6;		 //密码长度
	public static byte[] userName= new byte[usernameLength];//用户名
	public static int IO_BUFFER_SIZE = 2*1024;
	
	public static String LOGINWORKERSTYLE_CHECKIN = "0";  //登录者的类型
	public static int MAXRECONNTIMES = 5;  //最大重连次数
	public static int MAXRETRANSTIMES = 5;  //最大重发次数
	public static int STATUS_NOTEXAMED = 0;	//还没考试
	public static int STATUS_EXAMING = 1;	//正在考试
	public static int STATUS_HAVAEXAMED = 2;	//已经考试
	public static int STATUS_ABSENT = 3;	//缺考
	public static int STATUS_NOPERSON = 4;  //无人
	public static int STATUS_NOTEXAMEDOREXAMING = 5;  //无人
//	public static String FLAG_ISFIRSTSTART = "1";   //若为本组第一个开考的，则标记为1
//	public static String FLAG_ISNOTFIRSTSTART = "0";   //若为本组第一个开考的，则标记为1
	
	public static String FLAG_ISWAITINGBEGACK = "2";
	public static String FLAG_ISWAITINGENDACK = "3";
	public static String FLAG_ISWAITINGLOSEACK = "4";
	/***状态**/
	public static int PROCESS_DIALOGBACKDOWN = 8;	//processdialog的返回键按下了
	public static int PROCESS_CONNECTSUCCESS = 1;	//登录失败了
	public static int PROCESS_LOGINSUCCESS = 2;	//登录失败了
	public static int PROCESS_SERVERSOCKETCONSUCCESS = 3;	//登录失败了
	public static int PROCESS_SERVERSOCKETCONTIMEOUT = 4;	//登录失败了
	public static int PROCESS_HANDLE_SERVERDISCONNECT = 5;	//登录失败了
	public static int PROCESS_CONNECTFAIL = 6;	//登录失败了
	public static int PROCESS_LOGINFAIL = 7;	//登录失败了
	
	public static int STARTOK = 1;
	public static int STARTNO = 2;
	public static int STARTNOACK = 3;
	/***
	 * 上行，检录器--->服务器
	 *1	    1	    1	    30	     2	    1
	 *帧头 	帧类型	帧长  	数据域	校验位	帧尾
	 ***/
	public static int totalSendLength = 36;		//上行帧的总长度
	//上行命令
	public static String COMMAND_CONNECT = "CON";			//连接帧	
	public static String COMMAND_LOGIN = "LOG";			//登录帧
	public static String COMMAND_BEGINEXAM ="BEG";				//开始检录帧
	public static String COMMAND_STOPEXAM = "END";			//考生检录帧
	public static String COMMAND_ABSENT = "LOSE";	    //结束检录
	//下行命令
	public static String COMMAND_UPDATEINFO = "NEWW";
	public static String COMMAND_UPDATEINFOOK = "NEWW_OK";
//	public static String COMMAND_UPDATEINFOACK = "NEWW_ACK";
	public static int COMMANDLEN_UPDATEINFO =11 ;	    //COMMAND_UPDATEINFO长度
	
	public static String COMMAND_INFO = "INFO";	    //系统推送消息
	public static String COMMAND_INFOOK = "INFO_OK";	    //系统推送消息
	public static int COMMANDLEN_INFO =1 ;	    //COMMAND_UPDATEINFO长度
	//参数类型，接受，拒绝，回复
	public static String COMMAND_CONNECTOK = "CON_OK";			//连接帧	
	public static String COMMAND_CONNECTNO = "CON_NO";			//连接帧	
	public static String COMMAND_LOGINOK = "LOG_OK";			//登录帧
	public static String COMMAND_LOGINNO = "LOG_NO";			//登录帧
	public static String COMMAND_BEGINEXAMOK ="BEG_OK";				//开始检录帧
	public static String COMMAND_BEGINEXAMNO ="BEG_NO";				//开始检录帧
	public static String COMMAND_ABSENTOK = "LOSE_OK";	    //结束检录
	public static String COMMAND_ABSENTNO = "LOSE_NO";	    //结束检录
	public static String COMMAND_STOPEXAMOK = "END_OK";			//考生检录帧
	public static String COMMAND_STOPEXAMNO = "END_NO";			//考生检录帧
//	public static String COMMAND_ABSENTACK = "LOSE_ACK";	    //结束检录
	
	
	//除命令外，命令后所带参数的个数
	public static int COMMANDLEN_CONNECTOK = 1;			//连接帧	
	public static int COMMANDLEN_CONNECTNO = 1;			//连接帧	
	public static int COMMANDLEN_LOGINOK = 6;			//登录帧
	public static int COMMANDLEN_LOGINNO = 1;			//登录帧
	public static int COMMANDLEN_BEGINEXAMOK =4;				//开始检录帧
	public static int COMMANDLEN_BEGINEXAMNO =1;				//开始检录帧
	public static int COMMANDLEN_STOPEXAMOK = 4;			//考生检录帧
	public static int COMMANDLEN_STOPEXAMNO = 1;			//考生检录帧
	public static int COMMANDLEN_ABSENTOK = 4;	    //结束检录
	public static int COMMANDLEN_ABSENTNO = 1;	    //结束检录
	
	
	//根文件夹名字
	public static String ROOTFILENAME = "ArtCheckIn";
	public static String pianoStr = "piano";				//钢琴
	public static String danceStr = "dance";				//舞蹈
	public static String vocalStr = "vocal";				//声乐
	public static String othersStr = "others";				//其他
	//考试阶段
	public static String firstStr = "First";				//初试
	public static String twiceStr = "Second";				//复试
	public static String threeStr = "Third";				//三试
	/****handle*****/
	//connectThread-->LoginActivity
	public static int HANDLE_SERVERSOCKETCONTIMEOUT = 2;   //CON5次仍没有收到服务器回应
	public static int HANDLE_SERVERDISCONNECT = 16;   //CON5次仍没有收到服务器回应
	public static int HANDLE_STARTEXAMACKTIMEOUT = 17;   //CON5次仍没有收到服务器回应
	public static int HANDLE_STOPEXAMACKTIMEOUT = 18;   //CON5次仍没有收到服务器回应
	public static int HANDLE_ABSENTTIMEOUT = 19;   //CON5次仍没有收到服务器回应
	public static int HANDLE_SERVERSOCKETCONSUCCESS = 20;   //socket链接成功
	//receiveThread-->LoginActivity
	public static int HANDLE_CONNECTSUCCESS = 3;		//登录成功
	public static int HANDLE_CONNECTFAIL = 4;			//登录失败
	public static int HANDLE_LOGINSUCCESS = 5;		//登录成功
	public static int HANDLE_LOGINFAIL = 6;			//登录失败
	//receiveThread-->MainActivity
	public static int HANDLE_STRATEXAMOK = 7;			//登录失败
	public static int HANDLE_STRATEXAMNO = 8;			//登录失败
	public static int HANDLE_STOPEXAMOK = 13;			//登录失败
	public static int HANDLE_STOPEXAMNO = 14;			//登录失败
	public static int HANDLE_ABSENTOK = 10;			//更新待考组信息
	public static int HANDLE_ABSENTNO = 9;			//更新待考组信息
	public static int HANDLE_UPDATEINFO = 11;			//更新待考组信息
	public static int HANDLE_INFO = 12;			//更新待考组信息
}
