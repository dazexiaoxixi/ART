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
	//ͨ��
	private SocketSingle mConnectThread =null;  
	private Handler mHandler;
	private boolean connectOK = true;
	private boolean reject = false;
	private boolean first = true;
	//�ؼ�
	private GridView mStudentGridView;
	private Button mBtnUpload;
	private Button lookButton;
	private TextView mGroupNumberText;
	private TextView examTypeText;
	private TextView mStudentNumberText;
	private TextView curExamStd;
	private TextView mTechearText;
	private TextView classroomNum;
	private TextView startTimeText;	//�������ʱ��
	private StateAdapter mStateAdapter = new StateAdapter(this);
	private AutoScrollTextView noticeTextView;
	private AlertDialog myDialog;
	TextView textView2;//����ҳ�浹��ʱ��ʾ
	private CustomProgressDialog progressDialog;
	//����
	private String userNameStr;
	private String userNumStr;		//�������
	private String sendBuffer;		//���ͻ���
	private String scoreStr[] = new String[define_var.maxStudentPerGroup];
	private String sendScoreOkNum;
	private Count_Timer timer;//����ʱ
	private close_Timer closeTimer;//�رչ�ϲ��ֳɹ��ĵ���ʱ
	private DBHelper helpter;
	private SQLiteDatabase db;
	private ContentValues values;
	private Cursor cursor;
	private String firstTime ;
	String infoStr = "";
	String receiveIntent = null;
	private boolean canEdit = true;//�����Ƿ�����޸�
	//	private int count = 0;//���ڼ��㷢�����ݵĴ������������10�λ�û�յ��ظ����ͶϿ�
	private String password;
	private boolean sendSore = false;//����ǲ����ڷ��ͷ����������������ʮ�λ�û�ɹ��ͷ���һ���˵�
	private String uploadFail = "";
	//��˸
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //�����ޱ�����ʾ  
		setContentView(R.layout.main);
		initWidges();		//�󶨿ؼ�
		bindListener();		//�ؼ��󶨼�����
		initData();
		//��Ϣ����
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if(msg.what == 1)//�������Ͽ�����
				{
					Log.d("Mark", "---��������ʧ��");
					if(connectOK && !reject)
					{
						noticeTextView.setText("����������ʧ�ܣ����Ժ�");
						noticeTextView.init(getWindowManager());
					}
					stopProgressDialog();
					if(timer!=null)
					{timer.cancel();}		
					connectOK = false;
					reject = false;
				}
				else if(msg.what == 2)//�������ӳɹ�
				{
					Log.d("Mark", "---�������ӳɹ�");
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
					Log.d("MainActivity", "---���������ӳɹ�");
					if(infoStr!=null&&!infoStr.equals(""))
						noticeTextView.setText(infoStr);
					else noticeTextView.setText("������������ϵͳ");
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
					System.out.println("----����ʧ��");
					String a = (String) msg.obj;
					int i=0;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					stopProgressDialog();
					noticeTextView.setText("�������ܾ����ӣ������µ�¼");
					noticeTextView.init(getWindowManager());
					reject = true;
					//��¼ʧ�ܣ���ת��ȥ
					if(mConnectThread!=null)
						mConnectThread.stopThread();
					Intent intent = new Intent();
					intent.setClass(Mark.this, MainActivity.class);
					intent.putExtra("error", b);
					startActivity(intent);
					finish();
				}
				else if(msg.what == define_var.logOKHandle)//��¼�ɹ�|������Ŀ|�������|��ί���|�û���ע��|�����ص�|���Խ׶�
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
					else noticeTextView.setText("������������ϵͳ");
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
					//					else//����ʧ�ܣ��ٷ�һ��
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
				//					noticeTextView.setText("��¼ʧ��"+b+",�����µ�¼");
				//					noticeTextView.init(getWindowManager());
				//					reject = true;
				//					//��¼ʧ�ܣ���ת��ȥ
				//					if(mConnectThread!=null)
				//						mConnectThread.stopThread();
				//					Intent intent = new Intent();
				//					intent.setClass(Mark.this, MainActivity.class);
				//					intent.putExtra("error", b);
				//					startActivity(intent);
				//					finish();
				//				}
				else if(msg.what == define_var.newtHandle)//�յ�������Ϣ
				{
					Log.d("Mark", "---�յ����͵���Ϣ"+(String) msg.obj);
					/**���ݽ���**/
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
							curExamStd.setText("��ǰ����:");
							handler.postDelayed(runnable, 500);// �򿪶�ʱ����ִ�в���
						}
						curExamStd.setText("��ǰ����:");
						mStudentNumberText.setText("");
						mBtnUpload.setText("�ύ����ɼ�");
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
						if(subjectReceive!=null&&(subjectReceive.equals("Dance")||subjectReceive.equals("�赸")))
						{
							String height,standHeight,armLength,legLength;
							height = c[6];
							standHeight = c[7];
							armLength = c[8];
							legLength = c[9];
							/***��ʾ��һ������,�Ȳ��룬�ٸ�����ʾ****/
							newtQueryDB(stdNumReceive,startTimeReceive,endTimeReceive,height,standHeight,armLength,legLength);
							queryAndShow(1);
						}
						else if(subjectReceive!=null&&!(subjectReceive.equals("Dance")||subjectReceive.equals("�赸")))
						{
							/***��ʾ��һ������****/
							newtQueryDB(stdNumReceive,startTimeReceive,endTimeReceive,null,null,null,null);
							queryAndShow(1);
						}			
					}
				}
				else if(msg.what == define_var.loseHandle)//�յ�ȱ����Ϣ
				{
					Log.d("Mark", "---�յ�ȱ������Ϣ"+(String) msg.obj);
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
				else if(msg.what == define_var.uploadScoreHandle)//�յ������Ļظ�
				{
					Log.d("Mark", "---�յ�����"+(String) msg.obj);
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
						/***������ͳɹ�����ǵ����ݿ�**/
						if(cursor!=null&&!cursor.isLast())
						{
							cursor.moveToNext();
							getDataAndSendScore();
						}
						else//��ѯ�Ƿ���û��ֵģ�����о����ѣ����û�о͹�ϲ�ɼ��ύ�ɹ�
						{
							stopProgressDialog();
							if(myDialog!=null) myDialog.dismiss();
							if(db!=null)	db.close();
							if(uploadFail!=null&&uploadFail.equals(""))
							{
								mBtnUpload.setText("�ɼ��ύ�ɹ�");
								mBtnUpload.setClickable(false);
								mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
								canEdit = false;
								popTips("��ϲ��������ɼ��ύ�ɹ���");
								if(timer_twinkle!=null)
								{
									timer_twinkle.cancel();
								}
								closeTimer.start();
								curExamStd.setText(define_var.group+"���ֳɹ�");
								handler.postDelayed(runnable, 500);// �򿪶�ʱ����ִ�в���
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
					else if(sendBuffer!=null&&!sendBuffer.equals(b))	//����ͬ���ط�
					{
						if(timer!=null )
						{timer.cancel();}
						sendScore();
					}
				}
				else if(msg.what == define_var.uploadScoreHandle_NO)//�յ������Ļظ�,�ܾ�����
				{
					Log.d("Mark", "---�յ�����"+(String) msg.obj);
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					if(timer!=null )
					{timer.cancel();}
					if(cursor!=null&&!cursor.isLast())
					{
						uploadFail += cursor.getString(cursor.getColumnIndex("stdNum"))+"�ſ��������ύʧ�ܣ�"+b+"\n";
						cursor.moveToNext();
						getDataAndSendScore();
					}
					else if(cursor!=null&&cursor.isLast())
					{
						uploadFail += cursor.getString(cursor.getColumnIndex("stdNum"))+"�ſ��������ύʧ�ܣ�"+b+"\n";
						stopProgressDialog();
						if(myDialog!=null) myDialog.dismiss();
						if(db!=null)	db.close();
						popTips(uploadFail);
						if(timer!=null )
						{timer.cancel();}
						uploadFail = "";
					}
				}
				else if(msg.what == define_var.infoHandle)//�㲥��Ϣ~ 
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
		/***�����ڿ����˴浽���ݿ�****/
		InitDB helpter1 = new InitDB(Mark.this);
		Cursor c1 = helpter1.query(); 
		if(c1.moveToFirst()!=false)
		{
			/**�ȵõ���һ���˳�ʱ�Ŀ������***/
			define_var.subject = c1.getString(c1.getColumnIndex("lastStatus.subject"));
			define_var.room = c1.getString(c1.getColumnIndex("lastStatus.roomNum"));
			define_var.group = c1.getString(c1.getColumnIndex("lastStatus.groupNum"));
			define_var.std = c1.getString(c1.getColumnIndex("lastStatus.stdNum"));
			mStudentNumberText.setText(define_var.group+"��"+define_var.std+"��");
			/***��ʾ��һ���˳�ʱ��ҳ��*****/
			queryAndShow(0);
			queryAndShow(1);
		}
	}
	//���ݿ���������δ���͵����ݣ������·���
	//i=1-->��ʱ�����ڿ��ԣ���ʾ������һ������
	private void queryAndShow(int i) {
		// TODO Auto-generated method stub
		if(i==0)
		{
			helpter = new DBHelper(Mark.this);
			Cursor cursor1 = helpter.query(); 
			db = helpter.getReadableDatabase();
			//�����=no�ģ����Ҳ�����=0�ľ�Ҫ��ʾ��δ�ύ�ķ��������û��=no�ľ�Ҫ��ʾ�ɼ��ύ�ɹ�
			cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","examType","submitOK","startTime"}, "subject=? and roomNum=? and groupNum=? and submitOK=? and examType=?"
					, new String[]{define_var.subject,define_var.room,define_var.group,"NO",define_var.examTypeStr}, null, null, null); 
			if(cursor1.moveToFirst() && !cursor.moveToFirst())//������ݿⲻΪ�� �� ������û�д�ֵ�
			{
				System.out.println("----û��Ҫ�ύ�ķ���");
				mBtnUpload.setText("�ɼ��ύ�ɹ�");
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
				//				if(receiveIntent == null && !cursor.moveToFirst())//���û���յ�����
				//				{
				//					popTips("�ϴ��˳�ʱ���з���δ�ύ�����ύ����ɼ���");
				////					spark();
				//				}
				if(cursor1.moveToFirst())//������ڱ��鿼���ύ�˲��ֳɼ��ģ�����ķ����Ͳ����޸���
					canEdit = false;
			}
		}
		if(i==1)
		{
			/***��ʾ��һ��������***/
			helpter = new DBHelper(Mark.this);
			db = helpter.getReadableDatabase();
			cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","startTime","endTime","score","examType"}, "subject=? and roomNum=? and groupNum=? and examType=?"
					, new String[]{define_var.subject,define_var.room,define_var.group,define_var.examTypeStr}, null, null, null); 		
			/***�������**/
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
				mBtnUpload.setText("�ύ����ɼ�");
				mBtnUpload.setClickable(false);
				mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
			}
			int j=0;
			String a = (String) firstTime;
			while(a.charAt(j)!=' ') j++;
			String b =a.substring(j+1,a.length()-3);//��ʾʱ�䣬����ʾ����
			startTimeText.setText(b);

			cursor = db.query("record", new String[]{"subject","roomNum","groupNum","stdNum","startTime","endTime","score","examType"}, "subject=? and roomNum=? and groupNum=? and examType=? and startTime=?"
					, new String[]{define_var.subject,define_var.room,define_var.group,define_var.examTypeStr,"0"}, null, null, null); 	
			if(define_var.std!=null&&define_var.std .equals("0")&&!cursor.moveToFirst())
			{
				if(first)
				{
					System.out.println("------first");
					if(mBtnUpload.getText().toString()!=null&&!mBtnUpload.getText().toString().equals("�ɼ��ύ�ɹ�"))
					{
						curExamStd.setText(define_var.group+"���ѿ���\n���ύ����");
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
						curExamStd.setText(define_var.group+"���ֳɹ�");
						mStudentNumberText.setText("");
						mBtnUpload.setClickable(false);
						mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
					}
					first = false;
				}
				else
				{
					//					System.out.println("----�ٽ���");
					curExamStd.setText(define_var.group+"���ѿ���\n���ύ����");
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
		//first��start��,�õ���С��ʱ��,�õ���һ�����ڿ��Ե���
		if(startTime != null&&compare_date(firstTime, startTime)==1)
		{
			firstTime = startTime;
		}
		//�ѿ�
		if(startTime!=null&&endTime!=null&&!startTime.equals("0") && !endTime.equals("0"))
		{
			define_var.checked[Integer.valueOf(stdNum)-1] = 1;
			if(score!=null)
				define_var.score[Integer.valueOf(stdNum)-1] = Float.valueOf(score);
		}
		//���ڿ�
		else if(startTime!=null&&endTime!=null&&!startTime.equals("0") && endTime.equals("0"))
		{
			mStudentNumberText.setText(define_var.group+"��"+stdNum+"��");
			define_var.std = stdNum;
			define_var.checked[Integer.valueOf(stdNum)-1] = 1;
			if(score!=null)
				define_var.score[Integer.valueOf(stdNum)-1] = Float.valueOf(score);
			//			/***�����ڿ����˴浽���ݿ�****/
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
		//		//����
		//		else if(startTime!=null&&endTime!=null&&startTime.equals("0") && endTime.equals("0"))
		//		{
		//			if(score!=null&&score.equals("-1"))//ȱ��,���ܸտ�ʼ�յ�startTime endTimeΪ0���������յ�ȱ������Ϣ
		//			{
		//				define_var.checked[Integer.valueOf(stdNum)-1] = 2;
		//			}
		//			else define_var.checked[Integer.valueOf(stdNum)-1] = 3;//��û��
		//		}
		//		//ȱ��
		//		else if(startTime==null||endTime==null)
		//		{
		//			if(score!=null&&score.equals("-1"))
		//			{a
		//				define_var.checked[Integer.valueOf(stdNum)-1] = 2;
		//			}
		//		}
	}

	//�յ�����ʱ��ѯ���ݿ�
	private void newtQueryDB(String stdNum,String startTime,String endTime,
			String height,String standHeight,String armLength,String legLength)
	{
		helpter = new DBHelper(Mark.this);
		values = new ContentValues(); 
		db = helpter.getWritableDatabase();
		cursor = helpter.query(define_var.subject,define_var.room, define_var.group, stdNum);//�鿴������������������ݿ���治����
		//��������ڣ�����
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
		//������ڣ����¿���ʱ��ͽ���ʱ��
		else 	
		{
			helpter.update(startTime, endTime, stdNum);
		}		
		if(db!=null) db.close();
	}
	//�յ�����ʱ��ѯ���ݿ�----------�����õ�
	private void newtQueryDBTest(String stdNum,String startTime,String endTime,
			String height,String standHeight,String armLength,String legLength)
	{
		helpter = new DBHelper(Mark.this);
		values = new ContentValues(); 
		db = helpter.getWritableDatabase();
		cursor = helpter.query(define_var.subject,define_var.room, define_var.group, stdNum);//�鿴������������������ݿ���治����
		//��������ڣ�����
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
		//������ڣ����¿���ʱ��ͽ���ʱ��
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

	//�յ�ȱ��ʱ��ѯ���ݿ�
	private void loseQueryDB(String subject,String roomNum,String groupNum,String stdNum)
	{
		helpter = new DBHelper(Mark.this);
		values = new ContentValues(); 
		db= helpter.getWritableDatabase();
		Cursor c = helpter.query(subject, roomNum, groupNum, stdNum); 
		if(c.moveToFirst()==false)	//������͹����Ŀ������ݲ����ڣ�����
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
		//�鿴�Ƿ������ڿ��Ե�С���ȱ��������-->��ʾ
		if(subject!=null&&roomNum!=null&&groupNum!=null&&subject.equals(define_var.subject)&&roomNum.equals(define_var.room)&&groupNum.equals(define_var.group))
		{
			define_var.checked[Integer.valueOf(stdNum)-1] = 2;
		}
		if(db!=null)	db.close();
		((BaseAdapter)mStudentGridView.getAdapter()).notifyDataSetChanged();
	}

	/****�ϴ�������ť���������ȱ��ؼ�����Ƿ�Ϸ�������Ϸ��������Ƿ�ȷ���ύ�����Ϸ�--������ʾ****/
	class BtnUploadClickListener implements OnClickListener{
		@SuppressLint("SimpleDateFormat")
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(myDialog!=null)
				myDialog.dismiss();
			/******�����Ƿ�ȷ���ύҳ��*******/
			if(connectOK)
			{
				boolean detectionOK = true;		//�������Ƿ�Ϸ�
				String tips = "";
				//				if(define_var.std!=null && !define_var.std.equals("0"))
				//				{
				//					tips+=(define_var.std+"�ſ�����δ���꣬��ȴ�\n");
				//					detectionOK = false;
				//				}
				for(int i=0;i<define_var.maxStudentPerGroup;i++)	//���ؼ���Ƿ�ȫ�����
				{
					View v =mStudentGridView.getChildAt(i);
					RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.relaGrid);
					TextView scoreText = (TextView)rl.findViewById(R.id.scoreText);
					scoreStr[i] = scoreText.getText().toString();
					if(define_var.checked[i]==1&&scoreStr[i]!=null&&(scoreStr[i].equals("����")))
					{
						tips+=(i+1+"�ſ���δ���\n");
						detectionOK = false;
					}
					else if(define_var.checked[i]==3)
					{
						tips+=(i+1+"�ſ�����δ����\n");
						detectionOK = false;
					}
				}
				/****�ȵ����Ƿ�ȷ���ύ�����ȷ���ȷ��͵�һ�������ķ���****/
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
										//										mBtnUpload.setClickable(false);	//�����ظ��ύ����ɼ�
										//										mBtnUpload.setBackgroundResource(R.drawable.button_unclick);
										canEdit = false;//ȷ���ύ�˾Ͳ������޸�
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
							popTips("��ǰû����Ҫ�ύ�ķ�����");
						}
					}
					else
					{
						popTips("��ǰû����Ҫ�ύ�ķ�����");
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

	//�鿴��ʷ��¼��ť�����Բ鿴��������ĳһʱ����ڴ�ķ���
	class BtnLookHistoryClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Toast.makeText(getBaseContext(), "�鿴��ʷ��¼", Toast.LENGTH_SHORT).show();
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

		map1.put("id" ,  "���" );  
		map1.put("studentNumText" ,  "�������" );  
		map1.put("scoreText" ,  "����" );  
		map1.put("timeText" ,  "����ʱ��" );  
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
		if(compare_date(nowTime, StdTime)==-1)	//��ǰʱ����13��֮ǰ
		{
			start = new SimpleDateFormat("yyyy-MM-dd ").format(new Date(System.currentTimeMillis()))+"07:00:00";
			end = new SimpleDateFormat("yyyy-MM-dd ").format(new Date(System.currentTimeMillis()))+"13:00:00";
		}
		else if(compare_date(nowTime, StdTime)==1)//��ǰʱ����13��֮��
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
			studentNumText = c.getString(c.getColumnIndex("groupNum"))+"��"+c.getString(c.getColumnIndex("stdNum"))+"��";
			if(i==1&&j==-1)
			{
				if(scoreText!=null&&scoreText.equals("-1"))
				{
					map1 = new HashMap<String,String>();
					map1.put("id" ,  (++id)+"" );  
					map1.put("studentNumText" ,  studentNumText ); 
					map1.put("scoreText" ,  "ȱ��" ); 
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
			map1.put("studentNumText" ,  "���·���δ�ύ" );  
			map1.put("scoreText" ,  "" ); 
			map1.put("timeText" ,  "" ); 
			mylist.add(map1);
			for(c.moveToFirst();!c.isAfterLast();c.moveToNext())
			{
				timeText = c.getString(c.getColumnIndex("startTime"));
				scoreText = c.getString(c.getColumnIndex("score"));
				i = compare_date(timeText, start);
				j = compare_date(timeText, end);
				studentNumText = c.getString(c.getColumnIndex("groupNum"))+"��"+c.getString(c.getColumnIndex("stdNum"))+"��";
				if(i==1&&j==-1)
				{
					if(scoreText!=null&&scoreText.equals("-1"))
					{
						map1 = new HashMap<String,String>();
						map1.put("id" ,  (++id)+"" );  
						map1.put("studentNumText" ,  studentNumText ); 
						map1.put("scoreText" ,  "ȱ��" ); 
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

		//����SimpleAdapter�����������ݰ󶨵�item��ʾ�ؼ���  
		SimpleAdapter adapter = new SimpleAdapter(this, mylist, R.layout.history_list_item,   
				new String[]{"id", "studentNumText", "scoreText","timeText"}, new int[]{R.id.idText, R.id.studentNumText, R.id.scoreText,R.id.timeText});
		//ʵ���б����ʾ  
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
	//���ͷ���
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
	//���Ϳ����ķ���
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
	/********�������ݺ���*********/
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
				System.out.println("--------���͵���Ϣstr=="+str);
				pw.flush();
			}	  
		}
	}
	//��ʼ��������Ϣ�Ϳ�����Ϣ
	private void initData() {
		// TODO Auto-generated method stub
		Intent intent =this.getIntent();	//��ҳ�洫������ֵ
		userNameStr = intent.getStringExtra("userNameStr"); 
		define_var.subject = intent.getStringExtra("subjectStr");
		String roomPlaceStr = intent.getStringExtra("roomPlaceStr");
		define_var.examTypeStr = intent.getStringExtra("examTypeStr");
		userNumStr = intent.getStringExtra("userNumStr");
		define_var.room = intent.getStringExtra("roomNumStr");
		mGroupNumberText.setText(define_var.subject+define_var.room+"����");
		examTypeText.setText(define_var.examTypeStr);
		mTechearText.setText( intent.getStringExtra("teacherNameStr"));
		classroomNum.setText(roomPlaceStr);
		define_var.room = intent.getStringExtra("roomNumStr");
		define_var.userNameStr = userNameStr;
		receiveIntent = intent.getStringExtra("receive");
		password = intent.getStringExtra("passwordStr");
		if( receiveIntent==null)//û���յ�����
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
	//�Ƚ�ʱ��
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
		mBtnUpload.setOnClickListener(new BtnUploadClickListener());		//�ϴ�����
		lookButton.setOnClickListener(new BtnLookHistoryClickListener());	//�鿴��ʷ��¼
		mBtnUpload.setClickable(false);
	}

	//ITEM���,ֱ�ӵ��itemҲ���Դ��
	class mGridViewOnItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView parent, View v, int position, long arg3) {
			// TODO Auto-generated method stub
			if(canEdit)
			{
				String a = null;
				if(define_var.checked[position]==1)		//���ҳ��
				{
					if(Math.abs(define_var.score[position]+2)<1e-6)
						a= "����";			//��ķ���
					else
					{
						if((define_var.score[position]*10)%10!=0)	//Ϊ�˴�ֵ�ʱ��98.0��ʾΪ98д��
							a = define_var.score[position]+"";
						else a = ((int)(define_var.score[position])+"");
					}	
				}			
				if(a!=null)
				{
					if(define_var.subject!=null && (define_var.subject.equals("Dance")||define_var.subject.equals("�赸")))
						mStateAdapter.showShape(position,a,mStudentGridView);
					else
						mStateAdapter.showScoring(position,a,mStudentGridView);
					((BaseAdapter)mStudentGridView.getAdapter()).notifyDataSetChanged();
				}
				((BaseAdapter)mStudentGridView.getAdapter()).notifyDataSetChanged();
			}
			else
				popTips("��������Ѳ��ֻ�ȫ���ύ���޷��޸ġ�");
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
	/******������ʾҳ��*******/
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
				if(closeTimer!=null)//�رյ���ʱ
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
	//�������˳�����
	private static Boolean isQuit = false;
	Timer timer1 = new Timer();
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isQuit == false) {
				isQuit = true;
				Toast.makeText(getBaseContext(), "�ٰ�һ�η��ؼ��˳�����", Toast.LENGTH_SHORT).show();
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
		popTips("������δ���ӳɹ������Ժ�");
	}
	private void startProgressDialog(){
		if (progressDialog == null){
			progressDialog = CustomProgressDialog.createDialog(this);
			progressDialog.setMessage("�ύ����ɼ���...");
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
			popTips("�����ύ��ʱ���������ύ");
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
		{ //��ʱ����,ʱ�䵽���ص���ҳ��
			// TODO Auto-generated method stub
			if(textView2!=null)
				textView2.setText("(0s���Զ��ر�)");
			myDialog.dismiss();
		}
		@Override
		public void onTick(long millisUntilFinished) 
		{   
			// TODO Auto-generated method stub
			if(textView2!=null)
				textView2.setText("("+millisUntilFinished/1000+"s���Զ��ر�)");
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
	//					macSerial = str.trim();// ȥ�ո�
	//					break;
	//				}
	//			}
	//		} catch (IOException ex) {
	//			// ����Ĭ��ֵ
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
	//		{ //��ʱ����,ʱ�䵽�����û�յ��������Ļ�Ӧ���ͶϿ�����
	//			// TODO Auto-generated method stub
	//			if(!connectOK&&define_var.tickN)//û�յ���Ӧ
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
		{ //��ʱ����,ʱ�䵽���ص���ҳ��
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
//			//				popTips("����������Ӧ�����Ժ�����");
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
		{ //��ʱ����,ʱ�䵽���ص���ҳ��
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
			// �ڴ˴����ִ�еĴ���
			curExamStd.setTextColor(Color.BLACK);  
			handler.postDelayed(this, 150);// 150����ʱʱ��
		} 
	}; 
	//�õ����к�
	public static String getCPUSerial() {
		String str = "", strCPU = "", cpuAddress = "0000000000000000";
		try {
			//��ȡCPU��Ϣ
			Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			//����CPU���к�
			for (int i = 1; i < 100; i++) {
				str = input.readLine();
				if (str != null) {
					//���ҵ����к�������
					if (str.indexOf("Serial") > -1) {
						//��ȡ���к�
						strCPU = str.substring(str.indexOf(":") + 1,
								str.length());
						//ȥ�ո�
						cpuAddress = strCPU.trim();
						break;
					}
				}else{
					//�ļ���β
					break;
				}
			}
		} catch (IOException ex) {
			//����Ĭ��ֵ
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
