package com.example.artadmission;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artadmission.DB.DBHelper;
import com.example.artadmission.DB.InitDB;
import com.example.artadmission.UI.AutoScrollTextView;
import com.example.artadmission.UI.CustomProgressDialog;
import com.example.artadmission.UI.StateAdapter;

public class Mark extends Activity{
	//通信
	private SocketSingle mConnectThread =null;  
	private Handler mHandler;
	private boolean connectOK = true;
	private boolean reject = false;
	private boolean first = true;
	//控件
	private GridView mStudentGridView;
	private Button mBtnUpload;
	private Button lookButton;
	private TextView mGroupNumberText;
	private TextView examTypeText;
	private TextView mStudentNumberText;
	private TextView curExamStd;
	private TextView mTechearText;
	private TextView classroomNum;
	private TextView startTimeText;	//本组进场时间
	private StateAdapter mStateAdapter = new StateAdapter(this);
	private AutoScrollTextView noticeTextView;
	private AlertDialog myDialog;
	TextView textView2;//警告页面倒计时显示
	private CustomProgressDialog progressDialog;
	//变量
	private String userNameStr;
	private String userNumStr;		//考官序号
	private String sendBuffer;		//发送缓存
	private String scoreStr[] = new String[define_var.maxStudentPerGroup];
	private String sendScoreOkNum;
	private Count_Timer timer;//倒计时
	private close_Timer closeTimer;//关闭恭喜打分成功的倒计时
	private DBHelper helpter;
	private SQLiteDatabase db;
	private ContentValues values;
	private Cursor cursor;
	private String firstTime ;
	String infoStr = "";
	String receiveIntent = null;
	private boolean canEdit = true;//分数是否可以修改
	//	private int count = 0;//用于计算发送数据的次数，如果到了10次还没收到回复，就断开
	private String password;
	private boolean sendSore = false;//标记是不是在发送分数，如果分数发了十次还没成功就发下一个人的
	private String uploadFail = "";
	//闪烁
	private Timer timer_twinkle;
	private TimerTask task_twinkle;
	private Timer tickTimer;
	private TimerTask task_tickTimer;
	private TickAnswer_Timer tickAnswertimer;
	private int clo=0;
	private boolean isCall = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //设置无标题显示  
		setContentView(R.layout.main);
		initWidges();		//绑定控件
		bindListener();		//控件绑定监听器
		initData();
		//消息处理
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if(msg.what == 1)//服务器断开连接
				{
					Log.d("Mark", "---网络连接失败");
					if(connectOK && !reject)
					{
						noticeTextView.setText("服务器连接失败，请稍侯");
						noticeTextView.init(getWindowManager());
					}
					stopProgressDialog();
					if(timer!=null)
					{timer.cancel();}		
					connectOK = false;
					reject = false;
				}
				else if(msg.what == 2)//网络连接成功
				{
					Log.d("Mark", "---网络连接成功");
					noticeTextView.init(getWindowManager());
					sendSore = false;
					sendBuffer = define_var.connect+"|"+getCPUSerial();
					sendData(sendBuffer);
					sendBuffer = define_var.log+"|"+userNameStr+"|"+password+"|1";
					sendData(sendBuffer);
					//					timer.start();
				}
				else if(msg.what == define_var.conOKHandle)
				{
					Log.d("MainActivity", "---服务器连接成功");
					if(infoStr!=null&&!infoStr.equals(""))
						noticeTextView.setText(infoStr);
					else noticeTextView.setText("艺术考试评分系统");
					noticeTextView.init(getWindowManager());
					//					sendBuffer = define_var.log+"|"+userNameStr+"|"+password+"|1";
					//					sendData(sendBuffer);
					connectOK = true;
					//timer.start();
					if(tickAnswertimer!=null){
						tickAnswertimer.cancel();
						isCall = false;
					}
				}
				else if(msg.what == define_var.conNOHandle)
				{
					System.out.println("----连接失败");
					String a = (String) msg.obj;
					int i=0;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					stopProgressDialog();
					noticeTextView.setText("服务器拒绝连接，请重新登录");
					noticeTextView.init(getWindowManager());
					reject = true;
					//登录失败，跳转回去
					if(mConnectThread!=null)
						mConnectThread.stopThread();
					Intent intent = new Intent();
					intent.setClass(Mark.this, MainActivity.class);
					intent.putExtra("error", b);
					startActivity(intent);
					finish();
				}
				else if(msg.what == define_var.logOKHandle)//登录成功|考场科目|考场序号|评委序号|用户备注名|考场地点|考试阶段
				{
					//					if(timer!=null)
					//					{timer.cancel();}
					//					int i=0;
					//					String a = (String) msg.obj;
					//					while(a.charAt(i)!='|') i++;
					//					String b = a.substring(i+1);
					//					String c[] = b.split("\\|");
					//									if(c.length==6)
					//									{	
					if(infoStr!=null&&!infoStr.equals(""))
						noticeTextView.setText(infoStr);
					else noticeTextView.setText("艺术考试评分系统");
					noticeTextView.init(getWindowManager());
					//					sendBuffer = define_var.log+"|"+userNameStr+"|"+password+"|1";
					//					sendData(sendBuffer);
					connectOK = true;
					//timer.start();
					if(tickAnswertimer!=null){
						tickAnswertimer.cancel();
						isCall = false;
					}
					//									}
					//					else//接收失败，再发一次
					//					{
					//						if(userNameStr!=null&&password!=null)
					//						{
					//							sendBuffer = define_var.log+"|"+userNameStr+"|"+password+"|1";
					//							sendData(sendBuffer);
					//							timer.start();
					//						}
					//					}    
				}
				//				else if(msg.what == define_var.logNOHandle)
				//				{
				//					if(timer!=null)
				//					{timer.cancel();}
				//					int i=0;
				//					String a = (String) msg.obj;
				//					while(a.charAt(i)!='|') i++;
				//					String b =a.substring(i+1);
				//					if(myDialog!=null) 
				//					{myDialog.dismiss();myDialog=null;}
				//					stopProgressDialog();
				//					noticeTextView.setText("登录失败"+b+",请重新登录");
				//					noticeTextView.init(getWindowManager());
				//					reject = true;
				//					//登录失败，跳转回去
				//					if(mConnectThread!=null)
				//						mConnectThread.stopThread();
				//					Intent intent = new Intent();
				//					intent.setClass(Mark.this, MainActivity.class);
				//					intent.putExtra("error", b);
				//					startActivity(intent);
				//					finish();
				//				}
				else if(msg.what == define_var.newtHandle)//收到推送消息
				{
					Log.d("Mark", "---收到推送的消息"+(String) msg.obj);
					/**数据解析**/
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					String c[] = b.split("\\|");
					String subjectReceive,roomNumReceive,groupNumReceive,stdNumReceive,startTimeReceive,endTimeReceive;
					sendData(define_var.newt_ack+"|"+b);
					if(c.length==10)
					{
						if(timer_twinkle!=null)
						{
							timer_twinkle.cancel();
							curExamStd.setText("当前考生:");
							handler.postDelayed(runnable, 500);// 打开定时器，执行操作
						}
						curExamStd.setText("当前考生:");
						mStudentNumberText.setText("");
						mBtnUpload.setText("提交本组成绩");
						mBtnUpload.setClickable(false);
						mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
						canEdit = true;
//						if(timer!=null)
//						{timer.cancel();}
						subjectReceive = c[0];
						roomNumReceive = c[1];
						groupNumReceive = c[2];
						stdNumReceive = c[3];
						startTimeReceive = c[4];
						endTimeReceive = c[5];
						define_var.subject = subjectReceive;
						define_var.room = roomNumReceive;
						define_var.group = groupNumReceive;
						if(subjectReceive!=null&&(subjectReceive.equals("Dance")||subjectReceive.equals("舞蹈")))
						{
							String height,standHeight,armLength,legLength;
							height = c[6];
							standHeight = c[7];
							armLength = c[8];
							legLength = c[9];
							/***显示这一组的情况,先插入，再更新显示****/
							newtQueryDB(stdNumReceive,startTimeReceive,endTimeReceive,height,standHeight,armLength,legLength);
							queryAndShow(1);
						}
						else if(subjectReceive!=null&&!(subjectReceive.equals("Dance")||subjectReceive.equals("舞蹈")))
						{
							/***显示这一组的情况****/
							newtQueryDB(stdNumReceive,startTimeReceive,endTimeReceive,null,null,null,null);
							queryAndShow(1);
						}			
					}
				}
				else if(msg.what == define_var.loseHandle)//收到缺考消息
				{
					Log.d("Mark", "---收到缺考的消息"+(String) msg.obj);
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					String c[] = b.split("\\|");
					String subjectReceive;
					String roomNumReceive;
					String groupNumReceive;
					String stdNumReceive;
					sendData(define_var.lose_ack+"|"+b);
					if(c.length==4)
					{
						subjectReceive = c[0];
						roomNumReceive = c[1];
						groupNumReceive = c[2];
						stdNumReceive = c[3];
						loseQueryDB(subjectReceive, roomNumReceive, groupNumReceive, stdNumReceive);
					}
				}
				else if(msg.what == define_var.uploadScoreHandle)//收到分数的回复
				{
					Log.d("Mark", "---收到分数"+(String) msg.obj);
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					if(sendBuffer!=null&&sendBuffer.equals(b)) 
					{
						if(timer!=null )
						{timer.cancel();}
						db= helpter.getWritableDatabase();
						values = new ContentValues(); 
						values.put("submitOK", "OK");
						try {	
							db.update("record", values,"subject=? and roomNum=? and groupNum=? and stdNum=?",
									new String[]{define_var.subject, define_var.room, define_var.group, sendScoreOkNum});					
						} catch (Exception e) {
							e.printStackTrace();  
						}
						/***如果发送成功，标记到数据库**/
						if(cursor!=null&&!cursor.isLast())
						{
							cursor.moveToNext();
							getDataAndSendScore();
						}
						else//查询是否还有没打分的，如果有就提醒，如果没有就恭喜成绩提交成功
						{
							stopProgressDialog();
							if(myDialog!=null) myDialog.dismiss();
							if(db!=null)	db.close();
							if(uploadFail!=null&&uploadFail.equals(""))
							{
								mBtnUpload.setText("成绩提交成功");
								mBtnUpload.setClickable(false);
								mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
								canEdit = false;
								popTips("恭喜您，本组成绩提交成功。");
								if(timer_twinkle!=null)
								{
									timer_twinkle.cancel();
								}
								closeTimer.start();
								curExamStd.setText(define_var.group+"组打分成功");
								handler.postDelayed(runnable, 500);// 打开定时器，执行操作
							}
							else if(uploadFail!=null&&!uploadFail.equals(""))
							{
								popTips(uploadFail);
							}
							if(timer!=null )
							{timer.cancel();}
							uploadFail = "";
						}
					}
					else if(sendBuffer!=null&&!sendBuffer.equals(b))	//不相同，重发
					{
						if(timer!=null )
						{timer.cancel();}
						sendScore();
					}
				}
				else if(msg.what == define_var.uploadScoreHandle_NO)//收到分数的回复,拒绝分数
				{
					Log.d("Mark", "---收到分数"+(String) msg.obj);
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					if(timer!=null )
					{timer.cancel();}
					if(cursor!=null&&!cursor.isLast())
					{
						uploadFail += cursor.getString(cursor.getColumnIndex("stdNum"))+"号考生分数提交失败，"+b+"\n";
						cursor.moveToNext();
						getDataAndSendScore();
					}
					else if(cursor!=null&&cursor.isLast())
					{
						uploadFail += cursor.getString(cursor.getColumnIndex("stdNum"))+"号考生分数提交失败，"+b+"\n";
						stopProgressDialog();
						if(myDialog!=null) myDialog.dismiss();
						if(db!=null)	db.close();
						popTips(uploadFail);
						if(timer!=null )
						{timer.cancel();}
						uploadFail = "";
					}
				}
				else if(msg.what == define_var.infoHandle)//广播消息~ 
				{
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					if(b!=null)
					{
						infoStr = b;
						noticeTextView.setText(infoStr);
						noticeTextView.init(getWindowManager());
					}
					sendData(define_var.info_ack);
				}
			}
		};
		mConnectThread = SocketSingle.getSocketSingleInstance(mHandler);
	}
	private void initDB()
	{
		/***把正在考的人存到数据库****/
		InitDB helpter1 = new InitDB(Mark.this);
		Cursor c1 = helpter1.query(); 
		if(c1.moveToFirst()!=false)
		{
			/**先得到上一次退出时的考试情况***/
			define_var.subject = c1.getString(c1.getColumnIndex("lastStatus.subject"));
			define_var.room = c1.getString(c1.getColumnIndex("lastStatus.roomNum"));
			define_var.group = c1.getString(c1.getColumnIndex("lastStatus.groupNum"));
			define_var.std = c1.getString(c1.getColumnIndex("lastStatus.stdNum"));
			mStudentNumberText.setText(define_var.group+"组"+define_var.std+"号");
			/***显示上一次退出时的页面*****/
			queryAndShow(0);
			queryAndShow(1);
		}
	}
	//数据库中若还有未发送的数据，则重新发送
	//i=1-->此时有人在考试，显示考试这一组的情况
	private void queryAndShow(int i) {
		// TODO Auto-generated method stub
		if(i==0)
		{
			helpter = new DBHelper(Mark.this);
			Cursor cursor1 = helpter.query(); 
			db = helpter.getReadableDatabase();
			//如果有=no的，而且不存在=0的就要提示有未提交的分数。如果没有=no的就要显示成绩提交成功
			cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","examType","submitOK","startTime"}, "subject=? and roomNum=? and groupNum=? and submitOK=? and examType=?"
					, new String[]{define_var.subject,define_var.room,define_var.group,"NO",define_var.examTypeStr}, null, null, null); 
			if(cursor1.moveToFirst() && !cursor.moveToFirst())//如果数据库不为空 且 不存在没有打分的
			{
				System.out.println("----没有要提交的分数");
				mBtnUpload.setText("成绩提交成功");
				mBtnUpload.setClickable(false);
				mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
				canEdit = false;
				if(myDialog!=null)
					myDialog.dismiss();
			}
			else
			{
				cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","examType","submitOK","startTime"}, "subject=? and roomNum=? and groupNum=? and examType=? and endTime=?"
						, new String[]{define_var.subject,define_var.room,define_var.group,define_var.examTypeStr,"0"}, null, null, null); 
				cursor1 = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","examType","submitOK","startTime"}, "subject=? and roomNum=? and groupNum=? and examType=? and submitOK=?"
						, new String[]{define_var.subject,define_var.room,define_var.group,define_var.examTypeStr,"OK"}, null, null, null); 
				//				if(receiveIntent == null && !cursor.moveToFirst())//如果没有收到推送
				//				{
				//					popTips("上次退出时还有分数未提交，请提交本组成绩。");
				////					spark();
				//				}
				if(cursor1.moveToFirst())//如果存在本组考生提交了部分成绩的，这组的分数就不能修改了
					canEdit = false;
			}
		}
		if(i==1)
		{
			/***显示这一组其他人***/
			helpter = new DBHelper(Mark.this);
			db = helpter.getReadableDatabase();
			cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","startTime","endTime","score","examType"}, "subject=? and roomNum=? and groupNum=? and examType=?"
					, new String[]{define_var.subject,define_var.room,define_var.group,define_var.examTypeStr}, null, null, null); 		
			/***数据清空**/
			define_var.score = new float[define_var.maxStudentPerGroup];
			for(int j=0;j<define_var.maxStudentPerGroup;j++)
			{
				define_var.score[j] = -2;
			}
			define_var.checked  = new int[define_var.maxStudentPerGroup];
			define_var.std = "0";
			firstTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
			{
				fun(cursor);
			}
			if(receiveIntent!=null && cursor.moveToFirst()!=false)
			{
				mBtnUpload.setText("提交本组成绩");
				mBtnUpload.setClickable(false);
				mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
			}
			int j=0;
			String a = (String) firstTime;
			while(a.charAt(j)!=' ') j++;
			String b =a.substring(j+1,a.length()-3);//显示时间，不显示秒数
			startTimeText.setText(b);

			cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","startTime","endTime","score","examType"}, "subject=? and roomNum=? and groupNum=? and examType=? and startTime=?"
					, new String[]{define_var.subject,define_var.room,define_var.group,define_var.examTypeStr,"0"}, null, null, null); 	
			if(define_var.std!=null&&define_var.std .equals("0")&&!cursor.moveToFirst())
			{
				if(first)
				{
					System.out.println("------first");
					if(mBtnUpload.getText().toString()!=null&&!mBtnUpload.getText().toString().equals("成绩提交成功"))
					{
						curExamStd.setText(define_var.group+"组已考完\n请提交分数");
						for(int m=0;m<10;m++)
						{
							if(define_var.checked[m]!=1)
								define_var.checked[m] = 2;
						}
						mStudentNumberText.setText("");
						mBtnUpload.setClickable(true);
						mBtnUpload.setBackgroundResource(R.drawable.button_shape);
						spark();
					}
					else
					{
						for(int m=0;m<10;m++)
						{
							if(define_var.checked[m]!=1)
								define_var.checked[m] = 2;
						}
						curExamStd.setText(define_var.group+"组打分成功");
						mStudentNumberText.setText("");
						mBtnUpload.setClickable(false);
						mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
					}
					first = false;
				}
				else
				{
					//					System.out.println("----再进入");
					curExamStd.setText(define_var.group+"组已考完\n请提交分数");
					for(int m=0;m<10;m++)
					{
						if(define_var.checked[m]!=1)
							define_var.checked[m] = 2;
					}
					mStudentNumberText.setText("");
					mBtnUpload.setClickable(true);
					mBtnUpload.setBackgroundResource(R.drawable.button_shape);
					spark();
				}
			}
		}
		((BaseAdapter)mStudentGridView.getAdapter()).notifyDataSetChanged();
	}
	private void fun(Cursor cursor)
	{
		String stdNum,startTime,endTime,score = null;
		stdNum = cursor.getString(cursor.getColumnIndex("stdNum"));
		startTime = cursor.getString(cursor.getColumnIndex("startTime"));
		endTime = cursor.getString(cursor.getColumnIndex("endTime"));
		score = cursor.getString(cursor.getColumnIndex("score"));
		//first在start后,得到最小的时间,得到第一个正在考试的人
		if(startTime != null&&compare_date(firstTime, startTime)==1)
		{
			firstTime = startTime;
		}
		//已考
		if(startTime!=null&&endTime!=null&&!startTime.equals("0") && !endTime.equals("0"))
		{
			define_var.checked[Integer.valueOf(stdNum)-1] = 1;
			if(score!=null)
				define_var.score[Integer.valueOf(stdNum)-1] = Float.valueOf(score);
		}
		//正在考
		else if(startTime!=null&&endTime!=null&&!startTime.equals("0") && endTime.equals("0"))
		{
			mStudentNumberText.setText(define_var.group+"组"+stdNum+"号");
			define_var.std = stdNum;
			define_var.checked[Integer.valueOf(stdNum)-1] = 1;
			if(score!=null)
				define_var.score[Integer.valueOf(stdNum)-1] = Float.valueOf(score);
			//			/***把正在考的人存到数据库****/
			InitDB helpter1 = new InitDB(Mark.this);
			ContentValues values1 = new ContentValues(); 
			SQLiteDatabase db1 = helpter1.getWritableDatabase();	
			Cursor c1 = helpter1.query();
			values1.put("subject", define_var.subject);
			values1.put("roomNum", define_var.room);
			values1.put("groupNum", define_var.group);
			values1.put("stdNum", define_var.std);
			values1.put("examType", define_var.examTypeStr);
			if(c1.moveToFirst()!=false)
				db1.delete("lastStatus", null, null);
			db1.insert("lastStatus", null, values1);
		}
		//		//待考
		//		else if(startTime!=null&&endTime!=null&&startTime.equals("0") && endTime.equals("0"))
		//		{
		//			if(score!=null&&score.equals("-1"))//缺考,可能刚开始收到startTime endTime为0，后面又收到缺考的信息
		//			{
		//				define_var.checked[Integer.valueOf(stdNum)-1] = 2;
		//			}
		//			else define_var.checked[Integer.valueOf(stdNum)-1] = 3;//还没考
		//		}
		//		//缺考
		//		else if(startTime==null||endTime==null)
		//		{
		//			if(score!=null&&score.equals("-1"))
		//			{a
		//				define_var.checked[Integer.valueOf(stdNum)-1] = 2;
		//			}
		//		}
	}

	//收到推送时查询数据库
	private void newtQueryDB(String stdNum,String startTime,String endTime,
			String height,String standHeight,String armLength,String legLength)
	{
		helpter = new DBHelper(Mark.this);
		values = new ContentValues(); 
		db = helpter.getWritableDatabase();
		cursor = helpter.query(define_var.subject,define_var.room, define_var.group, stdNum);//查看这个考生的数据在数据库里存不存在
		//如果不存在，插入
		if(cursor.moveToFirst()==false)	
		{
			values = new ContentValues();		
			values.put("subject", define_var.subject);     
			values.put("roomNum",define_var.room);
			values.put("groupNum", define_var.group);	
			values.put("stdNum", stdNum);     
			values.put("startTime",startTime);
			values.put("endTime", endTime);	
			values.put("height", height);     
			values.put("standHeight",standHeight);
			values.put("armLength", armLength);	
			values.put("legLength", legLength);	
			values.put("submitOK", "NO");
			values.put("examType", define_var.examTypeStr);
			helpter.insert(values); 
		}
		//如果存在，更新开考时间和结束时间
		else 	
		{
			helpter.update(startTime, endTime, stdNum);
		}		
		if(db!=null) db.close();
	}
	//收到推送时查询数据库----------测试用的
	private void newtQueryDBTest(String stdNum,String startTime,String endTime,
			String height,String standHeight,String armLength,String legLength)
	{
		helpter = new DBHelper(Mark.this);
		values = new ContentValues(); 
		db = helpter.getWritableDatabase();
		cursor = helpter.query(define_var.subject,define_var.room, define_var.group, stdNum);//查看这个考生的数据在数据库里存不存在
		//如果不存在，插入
		if(cursor.moveToFirst()==false)	
		{
			values = new ContentValues();		
			values.put("subject", define_var.subject);     
			values.put("roomNum",define_var.room);
			values.put("groupNum", define_var.group);	
			values.put("stdNum", stdNum);     
			values.put("startTime",startTime);
			values.put("endTime", endTime);	
			values.put("height", height);     
			values.put("standHeight",standHeight);
			values.put("armLength", armLength);	
			values.put("legLength", legLength);	
			values.put("submitOK", "NO");
			values.put("examType", define_var.examTypeStr);
			helpter.insert(values); 
		}
		//如果存在，更新开考时间和结束时间
		else 	
		{
			DBHelper helpter = new DBHelper(Mark.this);
			ContentValues values = new ContentValues();
			SQLiteDatabase db = helpter.getWritableDatabase(); 
			values.put("endTime", endTime);
			try {			
				db.update("record", values,"subject=? and roomNum=? and groupNum=? and stdNum=? and examType=?"
						, new String[]{define_var.subject,define_var.room,define_var.group,stdNum,define_var.examTypeStr}); 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		if(db!=null) db.close();
	}

	//收到缺考时查询数据库
	private void loseQueryDB(String subject,String roomNum,String groupNum,String stdNum)
	{
		helpter = new DBHelper(Mark.this);
		values = new ContentValues(); 
		db= helpter.getWritableDatabase();
		Cursor c = helpter.query(subject, roomNum, groupNum, stdNum); 
		if(c.moveToFirst()==false)	//如果推送过来的考生数据不存在，插入
		{
			values = new ContentValues();		
			values.put("subject", subject);    
			values.put("roomNum", roomNum);
			values.put("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
			values.put("groupNum", groupNum);		
			values.put("stdNum", stdNum);     
			values.put("score","-1");
			values.put("submitOK","NO");
			values.put("examType", define_var.examTypeStr);
			helpter.insert(values); 
		}
		//查看是否是正在考试的小组的缺考，若是-->显示
		if(subject!=null&&roomNum!=null&&groupNum!=null&&subject.equals(define_var.subject)&&roomNum.equals(define_var.room)&&groupNum.equals(define_var.group))
		{
			define_var.checked[Integer.valueOf(stdNum)-1] = 2;
		}
		if(db!=null)	db.close();
		((BaseAdapter)mStudentGridView.getAdapter()).notifyDataSetChanged();
	}

	/****上传分数按钮监听器，先本地检测打分是否合法，如果合法，弹窗是否确认提交。不合法--给出提示****/
	class BtnUploadClickListener implements OnClickListener{
		@SuppressLint("SimpleDateFormat")
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(myDialog!=null)
				myDialog.dismiss();
			/******弹出是否确认提交页面*******/
			if(connectOK)
			{
				boolean detectionOK = true;		//检测分数是否合法
				String tips = "";
				//				if(define_var.std!=null && !define_var.std.equals("0"))
				//				{
				//					tips+=(define_var.std+"号考生还未考完，请等待\n");
				//					detectionOK = false;
				//				}
				for(int i=0;i<define_var.maxStudentPerGroup;i++)	//本地检测是否全部打分
				{
					View v =mStudentGridView.getChildAt(i);
					RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.relaGrid);
					TextView scoreText = (TextView)rl.findViewById(R.id.scoreText);
					scoreStr[i] = scoreText.getText().toString();
					if(define_var.checked[i]==1&&scoreStr[i]!=null&&(scoreStr[i].equals("请打分")))
					{
						tips+=(i+1+"号考生未打分\n");
						detectionOK = false;
					}
					else if(define_var.checked[i]==3)
					{
						tips+=(i+1+"号考生还未考试\n");
						detectionOK = false;
					}
				}
				/****先弹出是否确认提交，如果确认先发送第一个考生的分数****/
				if(detectionOK)
				{
					//					canEdit = false;
					helpter = new DBHelper(Mark.this);
					db = helpter.getWritableDatabase();
					if(define_var.group!=null)
					{
						cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","score","submitOK","examType"}, "subject=? and roomNum=? and groupNum=? and submitOK=? and examType=?"
								, new String[]{define_var.subject, define_var.room, define_var.group,"NO",define_var.examTypeStr}, null, null, null); 
						if(cursor.moveToFirst()!=false)
						{
							LayoutInflater inflater = LayoutInflater.from(Mark.this);  
							final View textEntryView = inflater.inflate(R.layout.upload, null);  
							Button agreeBtn = (Button)textEntryView.findViewById(R.id.agreeBtn);
							Button rejectBtn = (Button)textEntryView.findViewById(R.id.rejectBtn);
							myDialog = new AlertDialog.Builder(Mark.this).create();  
							myDialog.show();  
							myDialog.getWindow().setContentView(textEntryView);  
							agreeBtn.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									if(connectOK)
									{
										//										mBtnUpload.setClickable(false);	//不能重复提交本组成绩
										//										mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
										canEdit = false;//确认提交了就不能再修改
										startProgressDialog();
										getDataAndSendScore();
										myDialog.dismiss();
									}
									else
									{
										sendSore = false;
										noConnect();
									}
								}
							});
							rejectBtn.setOnClickListener(new OnClickListener() {	
								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									myDialog.dismiss();
								}
							});
						}
						else
						{
							popTips("当前没有需要提交的分数。");
						}
					}
					else
					{
						popTips("当前没有需要提交的分数。");
					}
				}
				else
				{
					popTips(tips);
				}
			}
			else
			{
				sendSore = false;
				noConnect();
			}
		}
	}

	//查看历史记录按钮，可以查看本考官在某一时间段内打的分数
	class BtnLookHistoryClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Toast.makeText(getBaseContext(), "查看历史记录", Toast.LENGTH_SHORT).show();
			showHistory(Mark.this);
		}
	}
	@SuppressLint("SimpleDateFormat")
	private void showHistory(Context context)
	{
		LayoutInflater inflater = LayoutInflater.from(this);  
		final View messageView = inflater.inflate(R.layout.show_message, null); 
		ListView messageList = (ListView)messageView.findViewById(R.id.messageList);
		Button button = (Button)messageView.findViewById(R.id.button);
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String,String>>();  
		HashMap<String, String> map1 = new HashMap<String,String>();     

		map1.put("id" ,  "序号" );  
		map1.put("studentNumText" ,  "考生序号" );  
		map1.put("scoreText" ,  "分数" );  
		map1.put("timeText" ,  "开考时间" );  
		mylist.add(map1); 

		DBHelper helpter = new DBHelper(Mark.this);
		SQLiteDatabase db = helpter.getWritableDatabase();  
		Cursor c = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","examType","submitOK","startTime","score"}, "submitOK=?"
				, new String[]{"OK"}, null, null, null); 
		//		Cursor c = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","examType","submitOK","startTime","score"}, null
		//				,null, null, null, null); 
		int id = 0;
		String studentNumText,scoreText,timeText;
		String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		String StdTime = new SimpleDateFormat("yyyy-MM-dd ").format(new Date(System.currentTimeMillis()))+"13:00:00";
		String start = null,end = null;
		int i,j;
		if(compare_date(nowTime, StdTime)==-1)	//当前时间在13点之前
		{
			start = new SimpleDateFormat("yyyy-MM-dd ").format(new Date(System.currentTimeMillis()))+"07:00:00";
			end = new SimpleDateFormat("yyyy-MM-dd ").format(new Date(System.currentTimeMillis()))+"13:00:00";
		}
		else if(compare_date(nowTime, StdTime)==1)//当前时间在13点之后
		{
			start = new SimpleDateFormat("yyyy-MM-dd ").format(new Date(System.currentTimeMillis()))+"13:00:00";
			end = new SimpleDateFormat("yyyy-MM-dd ").format(new Date(System.currentTimeMillis()))+"22:00:00";
		}
		while(c.moveToNext())
		{
			timeText = c.getString(c.getColumnIndex("startTime"));
			scoreText = c.getString(c.getColumnIndex("score"));
			i = compare_date(timeText, start);
			j = compare_date(timeText, end);
			studentNumText = c.getString(c.getColumnIndex("groupNum"))+"组"+c.getString(c.getColumnIndex("stdNum"))+"号";
			if(i==1&&j==-1)
			{
				if(scoreText!=null&&scoreText.equals("-1"))
				{
					map1 = new HashMap<String,String>();
					map1.put("id" ,  (++id)+"" );  
					map1.put("studentNumText" ,  studentNumText ); 
					map1.put("scoreText" ,  "缺考" ); 
					map1.put("timeText" ,  "" ); 
					mylist.add(map1);
				}	
				else 
				{
					int m=0;
					String a = (String) timeText;
					while(a.charAt(m)!=' ') m++;
					String b = a.substring(m+1,a.length()-3);

					map1 = new HashMap<String,String>();
					map1.put("id" ,  (++id)+"" );  
					map1.put("studentNumText" ,  studentNumText );  
					map1.put("scoreText" , scoreText ); 
					map1.put("timeText" ,  b ); 
					mylist.add(map1);
				}
			}
		}
		c = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","examType","submitOK","startTime","score"}, "submitOK=?"
				, new String[]{"NO"}, null, null, null); 
		if(c.moveToFirst())
		{
			map1 = new HashMap<String,String>();
			map1.put("id" ,  "" );  
			map1.put("studentNumText" ,  "" );  
			map1.put("scoreText" ,  "" ); 
			map1.put("timeText" ,  "" ); 
			mylist.add(map1);

			map1 = new HashMap<String,String>();
			map1.put("id" ,  "" );  
			map1.put("studentNumText" ,  "以下分数未提交" );  
			map1.put("scoreText" ,  "" ); 
			map1.put("timeText" ,  "" ); 
			mylist.add(map1);
			for(c.moveToFirst();!c.isAfterLast();c.moveToNext())
			{
				timeText = c.getString(c.getColumnIndex("startTime"));
				scoreText = c.getString(c.getColumnIndex("score"));
				i = compare_date(timeText, start);
				j = compare_date(timeText, end);
				studentNumText = c.getString(c.getColumnIndex("groupNum"))+"组"+c.getString(c.getColumnIndex("stdNum"))+"号";
				if(i==1&&j==-1)
				{
					if(scoreText!=null&&scoreText.equals("-1"))
					{
						map1 = new HashMap<String,String>();
						map1.put("id" ,  (++id)+"" );  
						map1.put("studentNumText" ,  studentNumText ); 
						map1.put("scoreText" ,  "缺考" ); 
						map1.put("timeText" ,  "" ); 
						mylist.add(map1);
					}	
					else 
					{
						int m=0;
						String a = (String) timeText;
						while(a.charAt(m)!=' ') m++;
						String b =a.substring(m+1,a.length()-3);

						map1 = new HashMap<String,String>();
						map1.put("id" ,  (++id)+"" );  
						map1.put("studentNumText" ,  studentNumText );  
						map1.put("scoreText" ,  scoreText ); 
						map1.put("timeText" ,  b ); 
						mylist.add(map1);
					}
				}
			}
		}

		//创建SimpleAdapter适配器将数据绑定到item显示控件上  
		SimpleAdapter adapter = new SimpleAdapter(this, mylist, R.layout.history_list_item,   
				new String[]{"id", "studentNumText", "scoreText","timeText"}, new int[]{R.id.idText, R.id.studentNumText, R.id.scoreText,R.id.timeText});
		//实现列表的显示  
		messageList.setAdapter(adapter);  

		myDialog = new AlertDialog.Builder(Mark.this).create();  
		myDialog.show();  
		myDialog.getWindow().setContentView(messageView);  
		myDialog.getWindow()  
		.findViewById(R.id.button)  
		.setOnClickListener(new View.OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				myDialog.dismiss();  
			}  
		});  
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				myDialog.dismiss();
			}
		});
	}
	//发送分数
	private void getDataAndSendScore()
	{
		String subject,roomNum,groupNum,stdNum,score;
		cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","score","submitOK","examType"}, "subject=? and roomNum=? and groupNum=? and submitOK=? and examType=?"
				, new String[]{define_var.subject, define_var.room, define_var.group,"NO",define_var.examTypeStr}, null, null, null); 
		if(cursor!=null&&cursor.moveToFirst()!=false)
		{
			System.out.println("----111subject=");
			subject = cursor.getString(cursor.getColumnIndex("subject"));
			System.out.println("----subject="+subject);
			roomNum = cursor.getString(cursor.getColumnIndex("roomNum"));
			System.out.println("----subject="+subject+"roomNum= "+roomNum);
			groupNum = cursor.getString(cursor.getColumnIndex("groupNum"));
			System.out.println("----subject="+subject+"roomNum= "+roomNum+"groupNum="+groupNum);
			stdNum= cursor.getString(cursor.getColumnIndex("stdNum"));
			System.out.println("----subject="+subject+"roomNum= "+roomNum+"groupNum="+groupNum+"stdNum="+stdNum);
			score = cursor.getString(cursor.getColumnIndex("score"));
			System.out.println("----subject="+subject+"roomNum= "+roomNum+"groupNum="+groupNum+"stdNum="+stdNum+"score="+score);
			sendScoreOkNum = stdNum;
			if(define_var.subject == null)
				define_var.subject = subject;
			if(define_var.room == null)
				define_var.room = roomNum;
			if(define_var.group == null)
				define_var.group = groupNum;
			sendBuffer = subject+"|"+roomNum+"|"+userNumStr+"|"+groupNum+"|"+stdNum+"|"+score;
			sendScore();
		}
	}
	//发送考生的分数
	private void sendScore() 
	{
		if(connectOK)
		{
			System.out.println("-----connect0k1");
			sendData(define_var.uploadScore+"|"+sendBuffer);
			System.out.println("-----connect0k2");
			timer.start();
			System.out.println("-----connect0k3");
			sendSore = true;
			System.out.println("-----connect0k4");
		}
		else
		{
			System.out.println("-----connect0k5");
			sendSore = false;
			System.out.println("-----connect0k6");
			noConnect();
			if(myDialog!=null)
				myDialog.dismiss();
			System.out.println("-----connect0k7");
		}
	} 
	/********发送数据函数*********/
	void sendData(String str)
	{
		if(mConnectThread!=null&&mConnectThread.clientSocket!=null)
		{
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
						mConnectThread.clientSocket.getOutputStream(),"GBK")));
			} catch (Exception e) {  
				e.printStackTrace();  
			} 
			if(pw!=null)
			{
				pw.println(str);  
				System.out.println("--------发送的消息str=="+str);
				pw.flush();
			}	  
		}
	}
	//初始化考场信息和考官信息
	private void initData() {
		// TODO Auto-generated method stub
		Intent intent =this.getIntent();	//主页面传过来的值
		userNameStr = intent.getStringExtra("userNameStr"); 
		define_var.subject = intent.getStringExtra("subjectStr");
		String roomPlaceStr = intent.getStringExtra("roomPlaceStr");
		define_var.examTypeStr = intent.getStringExtra("examTypeStr");
		userNumStr = intent.getStringExtra("userNumStr");
		define_var.room = intent.getStringExtra("roomNumStr");
		mGroupNumberText.setText(define_var.subject+define_var.room+"考场");
		examTypeText.setText(define_var.examTypeStr);
		mTechearText.setText( intent.getStringExtra("teacherNameStr"));
		classroomNum.setText(roomPlaceStr);
		define_var.room = intent.getStringExtra("roomNumStr");
		define_var.userNameStr = userNameStr;
		receiveIntent = intent.getStringExtra("receive");
		password = intent.getStringExtra("passwordStr");
		if( receiveIntent==null)//没接收到推送
		{
			initDB();
		}
		else
		{
			queryAndShow(1);
			queryAndShow(0);
		}	
		if(intent.getStringExtra("infoStr")!=null)
		{
			noticeTextView.setText(intent.getStringExtra("infoStr"));
			noticeTextView.init(getWindowManager());
		}
	}
	//比较时间
	@SuppressLint("SimpleDateFormat")
	private int compare_date(String DATE1, String DATE2) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date dt1 = df.parse(DATE1);
			java.util.Date dt2 = df.parse(DATE2);
			if (dt1.getTime() >= dt2.getTime()) {
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				return -1;
			} 
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}
	private void bindListener() {
		// TODO Auto-generated method stub
		mStudentGridView.setAdapter(mStateAdapter);
		mStudentGridView.setOnItemClickListener(new mGridViewOnItemClickListener());
		mBtnUpload.setOnClickListener(new BtnUploadClickListener());		//上传分数
		lookButton.setOnClickListener(new BtnLookHistoryClickListener());	//查看历史记录
		mBtnUpload.setClickable(false);
	}

	//ITEM点击,直接点击item也可以打分
	class mGridViewOnItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView parent, View v, int position, long arg3) {
			// TODO Auto-generated method stub
			if(canEdit)
			{
				String a = null;
				if(define_var.checked[position]==1)		//打分页面
				{
					if(Math.abs(define_var.score[position]+2)<1e-6)
						a= "请打分";			//打的分数
					else
					{
						if((define_var.score[position]*10)%10!=0)	//为了打分的时候，98.0显示为98写的
							a = define_var.score[position]+"";
						else a = ((int)(define_var.score[position])+"");
					}	
				}			
				if(a!=null)
				{
					if(define_var.subject!=null && (define_var.subject.equals("Dance")||define_var.subject.equals("舞蹈")))
						mStateAdapter.showShape(position,a,mStudentGridView);
					else
						mStateAdapter.showScoring(position,a,mStudentGridView);
					((BaseAdapter)mStudentGridView.getAdapter()).notifyDataSetChanged();
				}
				((BaseAdapter)mStudentGridView.getAdapter()).notifyDataSetChanged();
			}
			else
				popTips("本组分数已部分或全部提交，无法修改。");
		}
	}
	private void initWidges() {
		// TODO Auto-generated method stub
		mStudentGridView = (GridView)findViewById(R.id.examineeList);
		mBtnUpload = (Button)findViewById(R.id.Btn_StopExam);
		mGroupNumberText = (TextView)findViewById(R.id.testRoomNum);
		mStudentNumberText =  (TextView)findViewById(R.id.curExamStudent);
		curExamStd =  (TextView)findViewById(R.id.curExamStd);
		mTechearText = (TextView)findViewById(R.id.clerkNum);
		classroomNum = (TextView)findViewById(R.id.classroomNum);
		startTimeText = (TextView)findViewById(R.id.startTime);
		examTypeText  = (TextView)findViewById(R.id.testType);
		lookButton = (Button)findViewById(R.id.lookButton);
		noticeTextView = (AutoScrollTextView)this.findViewById(R.id.TextViewNotice);
		noticeTextView.init(getWindowManager());
		noticeTextView.startScroll();
		timer = new Count_Timer(15000, 1000);
		closeTimer = new close_Timer(5000, 1000);
		tickAnswertimer = new TickAnswer_Timer(15000, 1000);
		tickTimer = new Timer();  
		task_tickTimer = new TimerTask() {  
			public void run() {  
				runOnUiThread(new Runnable() {  
					public void run() {  
						define_var.tickN++;
						System.out.println("-----tickTimer;define_var.tickN="+define_var.tickN);
						if(define_var.tickN==50)
						{
							define_var.tickN = 0;
							sendBuffer = define_var.connect+"|"+getCPUSerial();
							sendData(sendBuffer);
							sendBuffer = define_var.log+"|"+userNameStr+"|"+password+"|1";
							sendData(sendBuffer);
							tickAnswertimer.start();
							//							if(mConnectThread!=null)
							//								mConnectThread.stopThread();
							//							mConnectThread = SocketSingle.getSocketSingleInstance(mHandler);
							//							mConnectThread.start();
						}
					}  
				});  
			}  
		};  
		tickTimer.schedule(task_tickTimer, 1000, 1000);  


		mBtnUpload.setClickable(false);
	}
	/******弹出提示页面*******/
	private void popTips(String str)
	{
		LayoutInflater inflater = LayoutInflater.from(Mark.this);  
		final View textEntryView = inflater.inflate(R.layout.tips, null);  
		TextView tips = (TextView)textEntryView.findViewById(R.id.textView);
		textView2 = (TextView)textEntryView.findViewById(R.id.textView2);
		tips.setText(str);
		if(myDialog!=null)
			myDialog.dismiss();
		myDialog = new AlertDialog.Builder(Mark.this).create();  
		myDialog.show();  
		myDialog.getWindow().setContentView(textEntryView);  
		myDialog.getWindow()  
		.findViewById(R.id.button)  
		.setOnClickListener(new View.OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				myDialog.dismiss();  
				if(closeTimer!=null)//关闭倒计时
					closeTimer.cancel();
			}  
		});  
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	//按两次退出程序
	private static Boolean isQuit = false;
	Timer timer1 = new Timer();
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isQuit == false) {
				isQuit = true;
				Toast.makeText(getBaseContext(), "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
				TimerTask task = null;
				task = new TimerTask() {
					@Override
					public void run() {
						isQuit = false;
					}
				};
				timer1.schedule(task, 2000);
			} else {
				finish();
				super.onDestroy();
				mConnectThread.stopThread();
				System.exit(0);
			}
		}
		return false;
	}
	private void noConnect()
	{
		//		System.out.println("----nonoct");
		//		if(timer!=null)
		//		{timer.cancel();count = 0;}
		stopProgressDialog();
		popTips("服务器未连接成功，请稍候。");
	}
	private void startProgressDialog(){
		if (progressDialog == null){
			progressDialog = CustomProgressDialog.createDialog(this);
			progressDialog.setMessage("提交本组成绩中...");
		}
		progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
				{
					return true;
				}
				else
				{
					return false;
				}
			}});
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	private void stopProgressDialog(){
		if (progressDialog != null){
			popTips("分数提交超时，请重新提交");
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	class close_Timer extends CountDownTimer   
	{
		public close_Timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onFinish() 
		{ //计时结束,时间到，回到主页面
			// TODO Auto-generated method stub
			if(textView2!=null)
				textView2.setText("(0s后将自动关闭)");
			myDialog.dismiss();
		}
		@Override
		public void onTick(long millisUntilFinished) 
		{   
			// TODO Auto-generated method stub
			if(textView2!=null)
				textView2.setText("("+millisUntilFinished/1000+"s后将自动关闭)");
		}
	}
	//	private String getMac() {
	//		String macSerial = null;
	//		String str = "";
	//		try {
	//			Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
	//			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
	//			LineNumberReader input = new LineNumberReader(ir);
	//			for (; null != str;) {
	//				str = input.readLine();
	//				if (str != null) {
	//					macSerial = str.trim();// 去空格
	//					break;
	//				}
	//			}
	//		} catch (IOException ex) {
	//			// 赋予默认值
	//			ex.printStackTrace();
	//		}
	//		return macSerial;
	//	}
	public void spark() {   
		handler.removeCallbacks(runnable);
		timer_twinkle = new Timer();  
		task_twinkle = new TimerTask() {  
			public void run() {  
				runOnUiThread(new Runnable() {  
					public void run() {  
						if (clo == 0) {  
							clo = 1;  
							curExamStd.setTextColor(Color.YELLOW);  
						} else {  
							if (clo == 1) {  

								clo = 2;  
								curExamStd.setTextColor(Color.RED);  
							} else{  

								clo = 0;  
								curExamStd.setTextColor(Color.BLUE);  
							} 
						}  
					}  
				});  
			}  
		};  
		timer_twinkle.schedule(task_twinkle, 500, 500);  
	}  
	//	class Tick_Timer extends Timer   
	//	{
	//		public Tick_Timer( long countDownInterval) {
	//			super(countDownInterval);
	//			// TODO Auto-generated constructor stub
	//		}
	//		@Override
	//		public void onFinish() 
	//		{ //计时结束,时间到，如果没收到服务器的回应，就断开重连
	//			// TODO Auto-generated method stub
	//			if(!connectOK&&define_var.tickN)//没收到回应
	//			{
	//				if(mConnectThread!=null)
	//					mConnectThread.stopThread();
	//				mConnectThread = SocketSingle.getSocketSingleInstance(mHandler);
	//				mConnectThread.start();
	//			}
	//			else
	//			{
	//				tickTimer.cancel();
	//			}
	//		}
	//		@Override
	//		public void onTick(long arg0) {
	//			// TODO Auto-generated method stub
	//			define_var.tickN++;
	//		}
	//	}

	class Count_Timer extends CountDownTimer   
	{
		public Count_Timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onFinish() 
		{ //计时结束,时间到，回到主页面
			// TODO Auto-generated method stub
			stopProgressDialog();
//						if(connectOK)
//						{
//			//				sendData(sendBuffer);
//			//				timer.start();
//							
//						}
//						else
//						{
//			//				popTips("服务器无响应，请稍后重试");
//							stopProgressDialog();
//						}
		}
		@Override
		public void onTick(long millisUntilFinished) 
		{   
			// TODO Auto-generated method stub
		}
	}
	class TickAnswer_Timer extends CountDownTimer   
	{
		public TickAnswer_Timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onFinish() 
		{ //计时结束,时间到，回到主页面
			// TODO Auto-generated method stub
			define_var.tickN = 0;
			if(!isCall)
			{
				if(mConnectThread!=null)
					mConnectThread.stopThread();
				mConnectThread = SocketSingle.getSocketSingleInstance(mHandler);
				mConnectThread.start();
				isCall = true;
			}
		}
		@Override
		public void onTick(long millisUntilFinished) 
		{   
			// TODO Auto-generated method stub
		}
	}
	final Handler handler = new Handler();
	Runnable runnable = new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 在此处添加执行的代码
			curExamStd.setTextColor(Color.BLACK);  
			handler.postDelayed(this, 150);// 150是延时时长
		} 
	}; 
	//得到序列号
	public static String getCPUSerial() {
		String str = "", strCPU = "", cpuAddress = "0000000000000000";
		try {
			//读取CPU信息
			Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			//查找CPU序列号
			for (int i = 1; i < 100; i++) {
				str = input.readLine();
				if (str != null) {
					//查找到序列号所在行
					if (str.indexOf("Serial") > -1) {
						//提取序列号
						strCPU = str.substring(str.indexOf(":") + 1,
								str.length());
						//去空格
						cpuAddress = strCPU.trim();
						break;
					}
				}else{
					//文件结尾
					break;
				}
			}
		} catch (IOException ex) {
			//赋予默认值
			ex.printStackTrace();
		}
		return cpuAddress;
	}
	//	@Override
	//	protected void onDestroy()
	//	{
	//		super.onDestroy();
	//		System.out.println("-----mark destroy");
	//		if(mConnectThread!=null)
	//			mConnectThread.stopThread();
	////		System.exit(0);
	//	}
}
