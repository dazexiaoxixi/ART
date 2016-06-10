package com.example.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.artcheckin.FrameFormat;
import com.example.artcheckin.MyUtils;
import com.example.database.DefineVar;
import com.example.database.ExamnieeInfo;

public class SocketConnectThread extends Thread {
	// private static final String RHOST="202.114.36.118"; //余老师服务器IP
	// private static final String RHOST="122.204.148.165"; //实验室服务器
	// private static final String RHOST="122.204.148.149"; //实验室服务器
	// private static final String RHOST="192.168.1.106"; //家服务器
	// private static final int TPORT=3206; //余老师服务器端口
	public volatile boolean mConnectStop = false;
	// private static final int TPORT=11000; //测试用
	private int timeout = 5000;
	private static Handler mHandler = null;
	private static Context context = null;
	public static Socket clientSocket = null;
	private PrintWriter out = null;
	private String str = null;
	private Message msg;
	private String subject; // 考试科目
	private String roomNum; // 考场号
	private String groupNum; // 组号
	private String studentNum; // 考生序号
	// private int process; //步骤
	private String examnieeID; // 考号
	private String studentName; // 考生名字

	private String imageURL; // 考生图片URL
	private int repeatTimes = 0;
	// public OutputStream outStream = null;
	// private SocketSingle mConnectThread =null;
	// private static SocketReceiveThread mReceiveThread =null;
	private BufferedReader in;
	// 单例模式
	private static SocketConnectThread instance = null;
	private static SaveDataToDatabaseThread saveThread = null;

	public static synchronized SocketConnectThread getSocketSingleInstance(
			Handler Handler, Context _context) {
		context = _context;
		mHandler = Handler;
		if (saveThread != null) {
			saveThread.changeHandler(Handler);
		}
		if (instance == null) {
			System.out.println("------instance=null:");
			instance = new SocketConnectThread();
			System.out.println("------instance new:" + instance.getId());
		} else {
			System.out.println("------instance!=null:" + instance.getId());
		}
		return instance;
	}

	@Override
	public void run() 
	{
	    int count=0;	
	    final String HEART_STRING=FrameFormat.COMMAND_CONNECT+"|1";
		SocketAddress socketaddress = new InetSocketAddress(
				DefineVar.ServerIP,
				Integer.valueOf(DefineVar.ServerPort));
		while(!mConnectStop)
		{
//			System.out.println("开启循环……-------");
			try{
				clientSocket = new Socket();
				clientSocket.connect(socketaddress, timeout);
				System.out.println("------------>线程连接上了------>");
				in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream(), "GBK"));
				sendConnMsg();
				while (clientSocket.isConnected()&& !clientSocket.isClosed())
				{
//					Log.e("socket","connect success");
					if(((str = in.readLine()) == null)&&(clientSocket.isConnected())&& !clientSocket.isClosed()) 
					{
						throw new Exception("服务器异常关闭");
					}		
//					Log.e("socket","haha");
					receive_msg(str);		
				}
				System.out.println("end");
			}
			catch(Exception e)
			{

				sendDisconnMsg();
				closeSocket();
				System.out.println("出现异常情况  服务器或网络出现故障 无法连接");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		System.out.println("------------>线程结束");
	}
	
	private void sendDisconnMsg() {
		// TODO Auto-generated method stub
		msg = new Message();
		msg.what = FrameFormat.HANDLE_SERVERDISCONNECT;
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		}
	}

	private void sendConnMsg() {
		// TODO Auto-generated method stub
		msg = new Message();
		msg.what = FrameFormat.HANDLE_SERVERSOCKETCONSUCCESS;
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		}
	}


	private void receive_msg(String msgStr) throws Exception{
		// TODO Auto-generated method stub
		String strCommand = "";// 得到命令
		String strContent = ""; // 得到参数
		String[] msgString = msgStr.split("\\|", 2);

		if (msgString.length == 1) {
			strCommand = msgStr.split("\\|", 2)[0];// 得到命令

		} else if (msgString.length == 2) {
			strCommand = msgStr.split("\\|", 2)[0];// 得到命令
			strContent = msgStr.split("\\|", 2)[1]; // 得到参数
		}
		if (strCommand != null && strContent != null) {
			String strParam[] = strContent.split("\\|");

			System.out.println("--->>收到了strCommand:" + strCommand
					+ "  和strContent:" + strContent);
			if (strCommand.equals(FrameFormat.COMMAND_CONNECTOK)) {
				if (strParam.length == FrameFormat.COMMANDLEN_CONNECTOK) {
					System.out.println("--->>receiveThread线程收到了连接请求回复:接受");
					DefineVar.process = FrameFormat.PROCESS_CONNECTSUCCESS;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_CONNECTSUCCESS;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_CONNECTNO)) {
				if (strParam.length == FrameFormat.COMMANDLEN_CONNECTNO) {
					System.out.println("--->>receiveThread线程收到了连接请求回复:拒绝");
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_CONNECTFAIL;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_LOGINOK)) {
				if (strParam.length == FrameFormat.COMMANDLEN_LOGINOK) {
					System.out.println("--->>receiveThread线程收到了登陆请求回复:接受");
					DefineVar.process = FrameFormat.PROCESS_LOGINSUCCESS;
					String str[] = strContent.split("\\|");
					String subjectNumStr = str[0];
					String roomNumStr = str[1];
					String examStyleStr = str[5];
					DefineVar.databaseName = getSubjectEnglish(subjectNumStr)
							+ roomNumStr + "-"
							+ getExamStyleEnglish(examStyleStr) + ".db";
					System.out.println("--->>receiveThread线程数据库名字"
							+ DefineVar.databaseName);
					saveThread = new SaveDataToDatabaseThread(context, mHandler);
					saveThread.start();
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_LOGINSUCCESS;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_LOGINNO)) {
				if (strParam.length == FrameFormat.COMMANDLEN_LOGINNO) {
					System.out.println("--->>receiveThread线程收到了登陆请求回复:拒绝");
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_LOGINFAIL;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_BEGINEXAMOK)) {
				if (strParam.length == FrameFormat.COMMANDLEN_BEGINEXAMOK) {
					System.out.println("--->>receiveThread线程收到了开始考试回复:接受");
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_STRATEXAMOK;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_BEGINEXAMNO)) {
				if (strParam.length == FrameFormat.COMMANDLEN_BEGINEXAMNO) {
					System.out.println("--->>receiveThread线程收到了开始考试回复:拒绝");
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_STRATEXAMNO;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_STOPEXAMOK)) {
				if (strParam.length == FrameFormat.COMMANDLEN_STOPEXAMOK) {
					System.out.println("--->>receiveThread线程收到了结束考试回复");
					// DefineVar.process = FrameFormat.PROCESS_ONESTOPEXAMACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_STOPEXAMOK;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_STOPEXAMNO)) {
				if (strParam.length == FrameFormat.COMMANDLEN_STOPEXAMNO) {
					System.out.println("--->>receiveThread线程收到了结束考试回复");
					// DefineVar.process = FrameFormat.PROCESS_ONESTOPEXAMACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_STOPEXAMNO;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_ABSENTOK)) {
				if (strParam.length == FrameFormat.COMMANDLEN_ABSENTOK) {
					System.out.println("--->>receiveThread线程收到了缺考回复OK");
					// DefineVar.process = FrameFormat.PROCESS_ABSENTINFOACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_ABSENTOK;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_ABSENTNO)) {
				if (strParam.length == FrameFormat.COMMANDLEN_ABSENTNO) {
					System.out.println("--->>receiveThread线程收到了缺考回复NO");
					// DefineVar.process = FrameFormat.PROCESS_ABSENTINFOACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_ABSENTNO;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_UPDATEINFO)) {
				// 只有当PRocess为登陆成功状态，才可以收到这些更新消息
				if (strParam.length == FrameFormat.COMMANDLEN_UPDATEINFO
						&& (DefineVar.process == FrameFormat.PROCESS_LOGINSUCCESS || DefineVar.process == FrameFormat.PROCESS_CONNECTSUCCESS)) {
					subject = strParam[0];
					roomNum = strParam[1];
					groupNum = strParam[2];
					studentNum = strParam[3];
					String startExamTime = strParam[4];
					String stopExamTime = strParam[5];
					examnieeID = strParam[6];
					studentName = strParam[7];
					String studentIdentity = strParam[8];
					String sex = strParam[9];
					imageURL = strParam[10];

					System.out.println("--->>receiveThread线程收到更新待考组信息："
							+ "   subject:" + subject + " roomNum:" + roomNum
							+ " groupNum:" + groupNum + " studentNum:"
							+ studentNum + " startExamTime:" + startExamTime
							+ " stopExamTime:" + stopExamTime + " examnieeID:"
							+ examnieeID + " studentName:" + studentName
							+ " studentIdentity:" + studentIdentity + " sex:"
							+ sex + " imageURL:" + imageURL);
					ExamnieeInfo tempEI = new ExamnieeInfo();
					tempEI.setGroupID(groupNum);
					tempEI.setExamnieeID(studentNum);
					tempEI.setStartTime(startExamTime);
					tempEI.setEndTime(stopExamTime);
					tempEI.setExaminationID(examnieeID);
					tempEI.setExamnieeName(studentName);
					tempEI.setExamnieeIdentity(studentIdentity);
					tempEI.setExamnieeSex(sex);
					tempEI.setExamnieePicURL(imageURL);
					saveThread.putData(tempEI);
					// 同时handle给MainActivity
					// msg = new Message();
					// msg.obj = strContent;
					// msg.what = FrameFormat.HANDLE_UPDATEINFO;
					// mHandler.sendMessage(msg);
					// 收到则回复服务器ACK
//					String str = FrameFormat.COMMAND_UPDATEINFOOK + "|"
//							+ subject + "|" + roomNum + "|" + groupNum + "|"
//							+ studentNum;
					String str=FrameFormat.COMMAND_UPDATEINFOOK+"|"+strContent;
					SendInfoToServer(str);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_INFO)) {
				if (strParam.length == FrameFormat.COMMANDLEN_INFO) {
					System.out.println("--->>receiveThread线程收到了系统消息");
					// DefineVar.process = FrameFormat.PROCESS_ABSENTINFOACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_INFO;
					mHandler.sendMessage(msg);
					// 收到则回复服务器ACK
					String str = FrameFormat.COMMAND_INFOOK;
					SendInfoToServer(str);
				}
			}
		}

	}

	private void SendInfoToServer(String msgStr) throws Exception {
		out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(clientSocket.getOutputStream(),
						"GBK")), true);

		out.println(msgStr);
		out.flush();
	}

	public void StopThread() {
		this.mConnectStop = true;
		System.out.println("stopThread");
		closeSocket();
		instance = null;

	}

	// 关闭socket
	private void closeSocket() {
		if (clientSocket != null) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clientSocket = null;
			Log.e("socekt","socket被关闭了");
		}

		if (in != null) {
			MyUtils.close(in);
			in = null;
		}
		if(out!=null){
			MyUtils.close(out);
			out=null;
		}
	}

	/**** 得到科目英文 ****/
	public String getSubjectEnglish(String subject) {
		if (subject.equals("钢琴"))
			return FrameFormat.pianoStr;
		if (subject.equals("舞蹈"))
			return FrameFormat.danceStr;
		if (subject.equals("声乐"))
			return FrameFormat.vocalStr;
		else
			return FrameFormat.othersStr;
	}

	/**** 得到考试阶段英文 ****/
	public String getExamStyleEnglish(String examStyle) {
		if (examStyle.equals("初试"))
			return FrameFormat.firstStr;
		if (examStyle.equals("复试"))
			return FrameFormat.twiceStr;
		if (examStyle.equals("三试"))
			return FrameFormat.threeStr;
		else
			return FrameFormat.firstStr;
	}
}
