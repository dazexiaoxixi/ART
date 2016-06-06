package com.example.artadmission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import android.os.Handler;
import android.os.Message;

class SocketSingle extends Thread{
	//socketͨ��
	private static  String RHOST = define_var.RHOST;
	private static  int TPORT = Integer.valueOf(define_var.TPORT);
	private static Handler mHandler = null;  
	public static Socket clientSocket = null;
	private BufferedReader in;
	private int repeatTimes=0;
	Message msg;
	//����
	public volatile boolean mConnectStop = false;
	private String str = null;
	private int timeout = 5000;
	//	private Count_Timer timer = new Count_Timer(3000,1000);
	//����ģʽ
	private static SocketSingle instance = null;
	public static synchronized SocketSingle getSocketSingleInstance(Handler Handler)
	{
		mHandler = Handler;
		if(instance == null)
		{
			instance = new SocketSingle();
		}
		return instance; 
	}
	@Override
	public void run()
	{
		System.out.println("------SocketConnectThread Thread id="+this.getId());
		while(!mConnectStop){
			if(clientSocket== null){
				 RHOST = define_var.RHOST;
				TPORT = Integer.valueOf(define_var.TPORT);
				SocketAddress socketaddress = new InetSocketAddress(RHOST,TPORT);
				try{
					clientSocket = new Socket();
					clientSocket.connect(socketaddress,timeout);
					ConnectSuccessFunc();
					try{
						while(in!=null&&(str = in.readLine())!=null){
							System.out.println("--->>�յ�����"+str);
							define_var.tickN = 0;
							receive_msg(str);
						}
						if(in!=null&&(in.readLine()==null)){
							clientSocket = null;
							System.out.println("----�������Ͽ�����clientSocket="+clientSocket);
							msg = new Message();
							msg.what = 1;
							mHandler.sendMessage(msg);
						}
					}catch(IOException e){
						clientSocket = null;
						System.out.println("----�������Ͽ�����clientSocket="+clientSocket);
						msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
					}catch(Exception e){
						clientSocket = null;
						System.out.println("----�������Ͽ�����clientSocket="+clientSocket);
						msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
				}catch(IOException e){
					System.out.println("----���������ӳ�ʱclientSocket="+clientSocket);
//					repeatTimes++;
//					if(repeatTimes==3){
					//	repeatTimes = 0;
						msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
					//}
					clientSocket = null;
				}
				catch(Exception e){
					clientSocket = null;
					System.out.println("----�������Ͽ�����clientSocket="+clientSocket);
					msg = new Message();
					msg.what = 1;
					mHandler.sendMessage(msg);
				}
				try{
					Thread.sleep(200);
				}catch(Exception e){
				}
			}
			
		}
		System.out.println("----connectThread run stop");
	}
	private void receive_msg(String receive)
	{
		if(receive.startsWith(define_var.connect_ok))		//���ӳɹ�
		{
			message(define_var.conOKHandle,receive);
		}
		else if(receive.startsWith(define_var.connect_no))	//����ʧ��
		{
			message(define_var.conNOHandle,receive);
		}
		else if(receive.startsWith(define_var.log_ok))		//��¼�ɹ� ������Ŀ|�������|��ί���|�û���ע��|�����ص�
		{
			message(define_var.logOKHandle,receive);
		}	
		else if(receive.startsWith(define_var.log_no))		//��¼ʧ��
		{
			message(define_var.logNOHandle,receive);
		}	
		else if(receive.startsWith(define_var.newt))		//����
		{
			message(define_var.newtHandle,receive);
		}	
		else if(receive.startsWith(define_var.lose))		//ȱ��
		{
			message(define_var.loseHandle,receive);
		}
		else if(receive.startsWith(define_var.uploadScore_ack))		//�ϴ������Ļظ�
		{
			message(define_var.uploadScoreHandle,receive);
		}
		else if(receive.startsWith(define_var.uploadScore_NO))		//�ϴ������Ļظ�
		{
			message(define_var.uploadScoreHandle_NO,receive);
		}
		else if(receive.startsWith(define_var.info))		//������������Ϣ
		{
			message(define_var.infoHandle,receive);
		}
		else if(receive.startsWith(define_var.newt_end))		//�����õģ�����ʱ��
		{
			message(define_var.newt_endHandle,receive);
		}
	}
	private void message(int i,String str)
	{
		Message msg = new Message();
		msg.what = i;
		msg.obj = str;
		mHandler.sendMessage(msg);
	}
	private void ConnectSuccessFunc()
	{
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
			Message msg = new Message();
			msg.what = 2;
			mHandler.sendMessage(msg);
			System.out.println("----send command to server");
		}
	}
	public void stopThread(){
		this.interrupt();
		Thread.interrupted();
		this.mConnectStop = true;
		if(clientSocket!=null){
			try {
				clientSocket.shutdownInput();
				if(clientSocket!=null){
					clientSocket.shutdownOutput();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clientSocket = null;
		}
		instance = null;
	}
}

