package com.example.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.example.artcheckin.FrameFormat;
import com.example.database.DefineVar;
import com.example.database.ExamnieeInfo;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;

public class SocketConnectThread extends Thread{
	//private static final String RHOST="202.114.36.118";   //����ʦ������IP
	//private static final String RHOST="122.204.148.165";   //ʵ���ҷ�����
	//private static final String RHOST="122.204.148.149";   //ʵ���ҷ�����
	//private static final String RHOST="192.168.1.106";   //�ҷ�����
	//	private static final int TPORT=3206;            //����ʦ�������˿�
	public volatile boolean mConnectStop = false;
	//private static final int TPORT=11000;     //������
	private int timeout = 5000;
	private static Handler mHandler = null;  
	private static  Context context = null;
	public static Socket clientSocket = null;
	private PrintWriter out = null;
	private String str = null;
	private Message msg ;
	private String subject; // ���Կ�Ŀ
	private String roomNum; // ������
	private String groupNum; // ���
	private String studentNum; // �������
	// private int process; //����
	private String examnieeID; // ����
	private String studentName; // ��������

	private String imageURL; // ����ͼƬURL
	private int repeatTimes = 0;
	//	public OutputStream outStream = null;
	//	private SocketSingle mConnectThread =null;  
	//private static SocketReceiveThread mReceiveThread =null; 
	private BufferedReader in;
	//����ģʽ
	private static SocketConnectThread instance = null;
	private static SaveDataToDatabaseThread saveThread = null;
	public static synchronized SocketConnectThread getSocketSingleInstance(Handler Handler,Context _context)
	{
		context = _context;
		mHandler = Handler;
		if(saveThread != null){
			saveThread.changeHandler(Handler);
		}
		if(instance == null)
		{
			System.out.println("------instance=null:");
			instance = new SocketConnectThread();
			System.out.println("------instance new:"+instance.getId());
		}else{
			System.out.println("------instance!=null:"+instance.getId());
		}
		return instance; 
	}
	public void run()
	{
		System.out.println("------SocketConnectThread Thread id="+this.getId());
		while(!mConnectStop){
			if(clientSocket== null){
				SocketAddress socketaddress = new InetSocketAddress(DefineVar.ServerIP,Integer.valueOf(DefineVar.ServerPort));
				try{
					//System.out.println("----------new Socket()����");
					clientSocket = new Socket();
					clientSocket.connect(socketaddress,timeout);
					ConnectSuccessFunc();

					try {

						while(in!=null&&(str = in.readLine())!=null){
							System.out.println("--->>�յ�����"+str);
							DefineVar.tickN = 0;
							receive_msg(str);
						}
						if(in!=null&&in.readLine()==null){
							clientSocket = null;
							System.out.println("----�������Ͽ�����clientSocket="+clientSocket);
							repeatTimes = 0;
							msg = new Message();
							msg.what = FrameFormat.HANDLE_SERVERDISCONNECT;
							if(mHandler!=null){
								mHandler.sendMessage(msg);
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						clientSocket = null;
						System.out.println("----�������Ͽ�����readLine IOException");
						repeatTimes = 0;
						msg = new Message();
						msg.what = FrameFormat.HANDLE_SERVERDISCONNECT;
						if(mHandler!=null){
							mHandler.sendMessage(msg);
						}
						e.printStackTrace();
					}catch(Exception e){
						System.out.println("----��������������11clientSocket="+clientSocket);
						clientSocket = null;
						repeatTimes = 0;
						msg = new Message();
						msg.what = FrameFormat.HANDLE_SERVERDISCONNECT;
						if(mHandler!=null){
							mHandler.sendMessage(msg);
						}
					}

				}catch(IOException e){
					System.out.println("----���������ӳ�ʱclientSocket="+clientSocket);
				//	repeatTimes++;
				//	if(repeatTimes==FrameFormat.MAXRECONNTIMES){
						repeatTimes = 0;
						msg = new Message();
						msg.what = FrameFormat.HANDLE_SERVERSOCKETCONTIMEOUT;
						if(mHandler!=null){
							mHandler.sendMessage(msg);
						}
				//	}
					clientSocket = null;
				}catch(Exception e){
					System.out.println("----��������������clientSocket="+clientSocket);
					clientSocket = null;
					repeatTimes = 0;
					msg = new Message();
					msg.what = FrameFormat.HANDLE_SERVERDISCONNECT;
					if(mHandler!=null){
						mHandler.sendMessage(msg);
					}
				}
				try{
					Thread.sleep(200);
				}catch(Exception e){
				}
			}

		}
		System.out.println("----connectThread run stop");
	}
	public void ConnectSuccessFunc(){
		if(clientSocket!=null){
			try {
				in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream(),"GBK"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			repeatTimes = 0;
			msg = new Message();
			msg.what = FrameFormat.HANDLE_SERVERSOCKETCONSUCCESS;
			if(mHandler!=null){
				mHandler.sendMessage(msg);
			}
		}

		//		System.out.println("----send command to server");
		//������Ϣ�߳̿���	
		//		System.out.println("--->>�_������receive");
		//		mReceiveThread = new  SocketReceiveThread(mHandler,clientSocket);	
		//		mReceiveThread.mRecvStop = false;
		//		mReceiveThread.start(); 
	}

	private void receive_msg(String msgStr) {
		// TODO Auto-generated method stub
		String strCommand ="";// �õ�����
		String strContent =""; // �õ�����
		String []msgString = msgStr.split("\\|",2);

		if(msgString.length==1){
			strCommand = msgStr.split("\\|",2)[0];// �õ�����

		}else if(msgString.length==2){
			strCommand = msgStr.split("\\|",2)[0];// �õ�����
			strContent =  msgStr.split("\\|",2)[1]; // �õ�����
		}
		if(strCommand!=null&&strContent!=null){
			String strParam[] =strContent.split("\\|");

			System.out.println("--->>�յ���strCommand:"+strCommand+"  ��strContent:"+strContent);
			if (strCommand.equals(FrameFormat.COMMAND_CONNECTOK)) {
				if(strParam.length==FrameFormat.COMMANDLEN_CONNECTOK){
					System.out.println("--->>receiveThread�߳��յ�����������ظ�:����");
					DefineVar.process = FrameFormat.PROCESS_CONNECTSUCCESS;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_CONNECTSUCCESS;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_CONNECTNO)) {
				if(strParam.length==FrameFormat.COMMANDLEN_CONNECTNO){
					System.out.println("--->>receiveThread�߳��յ�����������ظ�:�ܾ�");
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_CONNECTFAIL;
					mHandler.sendMessage(msg);
				}
			}
			else if (strCommand.equals(FrameFormat.COMMAND_LOGINOK)) {
				if(strParam.length==FrameFormat.COMMANDLEN_LOGINOK){
					System.out.println("--->>receiveThread�߳��յ��˵�½����ظ�:����");
					DefineVar.process = FrameFormat.PROCESS_LOGINSUCCESS;
					String str[] = strContent.split("\\|");
					String subjectNumStr = str[0];
					String roomNumStr = str[1];
					String examStyleStr = str[5];
					DefineVar.databaseName =getSubjectEnglish(subjectNumStr)+ roomNumStr+"-"+getExamStyleEnglish(examStyleStr)+".db";
					System.out.println("--->>receiveThread�߳����ݿ�����"+DefineVar.databaseName);
					saveThread = new SaveDataToDatabaseThread(context,mHandler);
					saveThread.start();
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_LOGINSUCCESS;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_LOGINNO)) {
				if(strParam.length==FrameFormat.COMMANDLEN_LOGINNO){
					System.out.println("--->>receiveThread�߳��յ��˵�½����ظ�:�ܾ�");
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_LOGINFAIL;
					mHandler.sendMessage(msg); 
				}
			}
			else if (strCommand.equals(FrameFormat.COMMAND_BEGINEXAMOK)) {
				if(strParam.length==FrameFormat.COMMANDLEN_BEGINEXAMOK){
					System.out.println("--->>receiveThread�߳��յ��˿�ʼ���Իظ�:����");
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_STRATEXAMOK;
					mHandler.sendMessage(msg);
				}
			} else if (strCommand.equals(FrameFormat.COMMAND_BEGINEXAMNO)) {
				if(strParam.length==FrameFormat.COMMANDLEN_BEGINEXAMNO){
					System.out.println("--->>receiveThread�߳��յ��˿�ʼ���Իظ�:�ܾ�");
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_STRATEXAMNO;
					mHandler.sendMessage(msg);
				}
			}
			else if (strCommand.equals(FrameFormat.COMMAND_STOPEXAMOK)) {
				if(strParam.length==FrameFormat.COMMANDLEN_STOPEXAMOK){
					System.out.println("--->>receiveThread�߳��յ��˽������Իظ�");
					//DefineVar.process = FrameFormat.PROCESS_ONESTOPEXAMACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_STOPEXAMOK;
					mHandler.sendMessage(msg);
				}
			}
			else if (strCommand.equals(FrameFormat.COMMAND_STOPEXAMNO)) {
				if(strParam.length==FrameFormat.COMMANDLEN_STOPEXAMNO){
					System.out.println("--->>receiveThread�߳��յ��˽������Իظ�");
					//DefineVar.process = FrameFormat.PROCESS_ONESTOPEXAMACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_STOPEXAMNO;
					mHandler.sendMessage(msg);
				}
			}
			else if (strCommand.equals(FrameFormat.COMMAND_ABSENTOK)) {
				if(strParam.length==FrameFormat.COMMANDLEN_ABSENTOK){
					System.out.println("--->>receiveThread�߳��յ���ȱ���ظ�OK");
					//DefineVar.process = FrameFormat.PROCESS_ABSENTINFOACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_ABSENTOK;
					mHandler.sendMessage(msg);
				}
			}
			else if (strCommand.equals(FrameFormat.COMMAND_ABSENTNO)) {
				if(strParam.length==FrameFormat.COMMANDLEN_ABSENTNO){
					System.out.println("--->>receiveThread�߳��յ���ȱ���ظ�NO");
					//DefineVar.process = FrameFormat.PROCESS_ABSENTINFOACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_ABSENTNO;
					mHandler.sendMessage(msg);
				}
			}
			else if (strCommand.equals(FrameFormat.COMMAND_UPDATEINFO)) {
				//ֻ�е�PRocessΪ��½�ɹ�״̬���ſ����յ���Щ������Ϣ
				if(strParam.length==FrameFormat.COMMANDLEN_UPDATEINFO&&
						(DefineVar.process==FrameFormat.PROCESS_LOGINSUCCESS||DefineVar.process==FrameFormat.PROCESS_CONNECTSUCCESS)){
					subject = strParam[0];
					roomNum = strParam[1];
					groupNum = strParam[2];
					studentNum = strParam[3];
					String startExamTime =strParam[4];
					String stopExamTime =strParam[5];
					examnieeID = strParam[6];
					studentName = strParam[7];
					String studentIdentity=strParam[8];
					String sex =strParam[9];
					imageURL = strParam[10];

					System.out.println("--->>receiveThread�߳��յ����´�������Ϣ��" + "   subject:" + subject
							+ " roomNum:" + roomNum+ " groupNum:" + groupNum+ " studentNum:" + studentNum+ " startExamTime:" + startExamTime
							+ " stopExamTime:" + stopExamTime+ " examnieeID:" + examnieeID+ " studentName:"+ studentName+ " studentIdentity:"+ studentIdentity+ " sex:" + sex+ " imageURL:" + imageURL);
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
					// ͬʱhandle��MainActivity
					//					msg = new Message();
					//					msg.obj = strContent;
					//					msg.what = FrameFormat.HANDLE_UPDATEINFO;
					//					mHandler.sendMessage(msg);
					// �յ���ظ�������ACK
					String str = FrameFormat.COMMAND_UPDATEINFOOK + "|" 
							+ subject + "|" + roomNum + "|" + groupNum + "|"
							+ studentNum;
					SendInfoToServer(str);
				}
			}
			else if (strCommand.equals(FrameFormat.COMMAND_INFO)) {
				if(strParam.length==FrameFormat.COMMANDLEN_INFO){
					System.out.println("--->>receiveThread�߳��յ���ϵͳ��Ϣ");
					//DefineVar.process = FrameFormat.PROCESS_ABSENTINFOACK;
					msg = new Message();
					msg.obj = strContent;
					msg.what = FrameFormat.HANDLE_INFO;
					mHandler.sendMessage(msg);
					// �յ���ظ�������ACK
					String str = FrameFormat.COMMAND_INFOOK;
					SendInfoToServer(str);
				}
			}
		}

	}

	private void SendInfoToServer(String msgStr) {
		if (clientSocket != null) {
			try {
				out = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(
								clientSocket.getOutputStream(),"GBK")),
								true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(out!=null){
				out.println(msgStr);
				out.flush();
			}
		}
	}
	public void StopThread(){
		this.interrupt();
		Thread.interrupted();
		this.mConnectStop = true;
		instance = null;
		//mReceiveThread.stopThread();
		if(clientSocket!=null){
			try {
				clientSocket.shutdownInput();
				if(clientSocket!=null){
					clientSocket.shutdownOutput();
				}
				//	clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clientSocket = null;
		}
	}
	/****�õ���ĿӢ��****/
	public String getSubjectEnglish(String subject)
	{
		if(subject.equals("����"))
			return FrameFormat.pianoStr;
		if(subject.equals("�赸"))
			return FrameFormat.danceStr;
		if(subject.equals("����"))
			return FrameFormat.vocalStr;
		else 
			return FrameFormat.othersStr;
	}/****�õ����Խ׶�Ӣ��****/
	public String getExamStyleEnglish(String examStyle)
	{
		if(examStyle.equals("����"))
			return FrameFormat.firstStr;
		if(examStyle.equals("����"))
			return FrameFormat.twiceStr;
		if(examStyle.equals("����"))
			return FrameFormat.threeStr;
		else 
			return FrameFormat.firstStr;
	}
}
