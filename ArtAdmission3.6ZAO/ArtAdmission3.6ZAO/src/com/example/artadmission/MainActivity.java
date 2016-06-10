package com.example.artadmission;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.example.artadmission.DB.DBHelper;
import com.example.artadmission.DB.IPDB;
import com.example.artadmission.DB.InitUserNameDB;
import com.example.artadmission.UI.CustomProgressDialog;
import com.example.artadmission.UI.IPEditText;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	//socketͨ��
	public static final String TAG = "MainActivity";  
	private Handler mHandler = null;  //���������̷߳��͹���������
	private SocketSingle mConnectThread =null;  
	//��¼ҳ��ؼ�
	private EditText mEditUserName;			//�û���
	private EditText mEditPassword;			//����
	private Button mBtnLogin;				//��¼��ť
	private TextView noNetText;
	private Button setIPButton;
	private Button deleteButton;
	//	UploadCount_Timer upload_timer;
	private CustomProgressDialog progressDialog;
	//��ӭ��������
	private RelativeLayout welcomeLayout;  
	private TextView titleText;
	private Animation toLargeAnimation;
	private TranslateAnimation toUpAnimation;
	private AlphaAnimation toAppearAnimation;
	private AlphaAnimation toDisappearAnimation;
	private FrameLayout loginFrameLayout;
	private LinearLayout loginLinearLayout;
	private AnimationSet animationSet;
	private Dialog dialog;
	private AlertDialog myDialog;
	TextView textView2;//����ҳ�浹��ʱ��ʾ
	//������������
	private int DATA_CHECK_CODE = 0;
	private TextToSpeech readerTTS;
	private List<EngineInfo> engineList = null;
	String defaultEngineName = null;
	private boolean haveIflyPackage = false;
	private boolean selectIflyPackage = false;
	private boolean notDefaultPackage = false;
	//����
	private String userNameStr;						//�û�������
	private String password;
	private String userNumStr;						//�������
	private String teacherNameStr;					//��������
	private String subjectStr;						//������Ŀ
	private String roomNumStr;						//������Ŀ�Ϳ�����
	private String roomPlaceStr;					//�����ص�
	private String examTypeStr;						//���Խ׶Σ����ԡ����ԡ�����
	private String sendBuffer;						//���ͻ�����
	private Count_Timer timer;
	//	private close_Timer close_timer;//����ʱ
	private Toast toast;
	private boolean connectOK = false;
	private String receive = null;//�����ҳ���Ƿ�ӵ������͵���Ϣ
	private String infoStr =null;
	//	private boolean logNO = false;
	private boolean reject = false;
	private int count = 0;//���ڼ��㷢�����ݵĴ������������10�λ�û�յ��ظ����ͶϿ�
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //�����ޱ�����ʾ  
		setContentView(R.layout.activity_login);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //���������Ĭ�ϲ�����
		initWidges();		//�󶨿ؼ�
		bindListener();		//�ؼ��󶨼�����
		Intent intent =this.getIntent();	//Mark��������ֵ
		String error = intent.getStringExtra("error"); 
		//�����mark��ת�ص�¼ҳ�棬�Ͳ����Ŷ���
		if(error!=null)
		{
			if(error!=null)
			{
//				showToast(error);
				welcomeLayout.setVisibility(View.GONE);
				loginLinearLayout.setVisibility(View.VISIBLE);
				noNetText.setText(error);
			}
		}
		else
		{
			//����������ʼ����֤
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent, DATA_CHECK_CODE);
			//��ӭ����---���ֶ���
			titleText = (TextView)findViewById(R.id.titleText);
			animationSet = new AnimationSet(true);
			toLargeAnimation = AnimationUtils.loadAnimation(this, R.anim.to_large);
			toUpAnimation = new TranslateAnimation(0, 0, 0,-100);
			animationSet.setDuration(2000);
			animationSet.addAnimation(toUpAnimation);
			animationSet.addAnimation(toLargeAnimation);
			//��ӭ����---��������
			loginFrameLayout = (FrameLayout)findViewById(R.id.loginFrameLayout);
			loginLinearLayout = (LinearLayout)findViewById(R.id.loginLinearLayout);		
			welcomeLayout = (RelativeLayout)findViewById(R.id.welcomeLayout);
			//�������ȵĶ�����ʵ�ֽ�����ʾ����0.3��1.0(ȫ��)
			toAppearAnimation = new AlphaAnimation(0.3f, 1.0f);
			toAppearAnimation.setDuration(4000);         //���ý���ʱ��
			welcomeLayout.startAnimation(toAppearAnimation);          //��ʼһ������
			//�������ȵĶ�����ʵ�ֽ�����ʾ����1.0��0.0(ȫ��)
			toDisappearAnimation = new AlphaAnimation(1.0f, 0.0f);
			toDisappearAnimation.setDuration(2000);         //���ý���ʱ��
			//���ö�����������������������ʱ�������µ�Activity
			toAppearAnimation.setAnimationListener(new welcomeAnimationListener() );
			toDisappearAnimation.setAnimationListener(new welcomeAnimationListener() );		
		}
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if(msg.what == 1)//�������Ͽ�����
				{
					if(!reject)
					{
						Log.d("MainActivity", "---��������ʧ��");
						noNetText.setText("����������ʧ�ܣ����Ժ��¼");
					}
					stopProgressDialog();
					if(timer!=null)
					{timer.cancel();count = 0;}
					connectOK = false;
					if(mConnectThread!=null){
						mConnectThread.stopThread();
					}
					reject = false;		
				}
				else if(msg.what == 2)//�������ӳɹ�
				{
					Log.d("MainActivity", "---�������ӳɹ�");
					noNetText.setText("");

					sendBuffer = define_var.connect+"|"+getCPUSerial();
					sendDataNeedACK(sendBuffer);
				}
				else if(msg.what == define_var.conOKHandle)
				{
					Log.d("MainActivity", "---���������ӳɹ�");
					showToast("���������ӳɹ�");
					noNetText.setText("");
					if(timer!=null)
					{timer.cancel();count = 0;}
					sendBuffer = define_var.log+"|"+userNameStr+"|"+encryption(password)+"|1";
					sendDataNeedACK(sendBuffer);
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					connectOK = true;
					//					String b =a.substring(i+1);
					//b�Ƿ�����ʱ�䣬���ڻ�û����
				}
				else if(msg.what == define_var.conNOHandle)
				{
					System.out.println("----����ʧ��");
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					if(b!=null)
						noNetText.setText("�������ܾ�����"+b);
					showToast("�������ܾ�����");
					stopProgressDialog();
					if(timer!=null)
					{timer.cancel();count = 0;}
					if(mConnectThread!=null){
						mConnectThread.stopThread();
						//						logNO = false;
						reject = true;
					}
					//					if(upload_timer!=null)
					//						upload_timer.cancel();
				}
				else if(msg.what == define_var.logOKHandle)//��¼�ɹ�|������Ŀ|�������|��ί���|�û���ע��|�����ص�|���Խ׶�
				{
					if(timer!=null)
					{timer.cancel();count = 0;}
					int i=0;
					noNetText.setText("");
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b = a.substring(i+1);
					String c[] = b.split("\\|");
					if(c.length==6)
					{	
						connectOK = true;
						subjectStr = c[0];
						roomNumStr = c[1];
						userNumStr = c[2];
						teacherNameStr = c[3];
						roomPlaceStr = c[4];
						examTypeStr = c[5];
						define_var.examTypeStr = examTypeStr;
						define_var.userNameStr = userNameStr;
						if(myDialog!=null) 
						{myDialog.dismiss();myDialog=null;}
						//						if(upload_timer!=null)
						//							upload_timer.cancel();
						stopProgressDialog();
						permissionPage();
					}
					else//����ʧ�ܣ��ٷ�һ��
					{
						if(userNameStr!=null&&password!=null)
						{
							sendBuffer = define_var.log+"|"+userNameStr+"|"+encryption(password)+"|1";
							sendDataNeedACK(sendBuffer);
						}
					}    
				}
				else if(msg.what == define_var.logNOHandle)
				{
					if(timer!=null)
					{timer.cancel();count = 0;}
					int i=0;
					String a = (String) msg.obj;
					while(a.charAt(i)!='|') i++;
					String b =a.substring(i+1);
					if(b!=null)
						noNetText.setText("��¼ʧ��"+b);
					if(myDialog!=null) 
					{myDialog.dismiss();myDialog=null;}
					stopProgressDialog();
					reject = true;
					//					if(upload_timer!=null)
					//						upload_timer.cancel();
					if(mConnectThread!=null)
						mConnectThread.stopThread();
				}
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
						receive = "1";
						subjectReceive = c[0];
						roomNumReceive = c[1];
						groupNumReceive = c[2];
						stdNumReceive = c[3];
						startTimeReceive = c[4];
						endTimeReceive = c[5];
						define_var.subject = subjectReceive;
						define_var.room = roomNumReceive;
						define_var.group = groupNumReceive;
						if(subjectReceive!=null&&subjectReceive.equals(""))
						{
							String height,standHeight,armLength,legLength;
							height = c[6];
							standHeight = c[7];
							armLength = c[8];
							legLength = c[9];
							/***��ʾ��һ������,�Ȳ��룬�ٸ�����ʾ****/
							newtQueryDB(stdNumReceive,startTimeReceive,endTimeReceive,height,standHeight,armLength,legLength);
						}
						else if(subjectReceive!=null&&!subjectReceive.equals("Dance"))
						{
							/***��ʾ��һ������****/
							newtQueryDB(stdNumReceive,startTimeReceive,endTimeReceive,null,null,null,null);
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
						receive = "1";
						subjectReceive = c[0];
						roomNumReceive = c[1];
						groupNumReceive = c[2];
						stdNumReceive = c[3];
						loseQueryDB(subjectReceive, roomNumReceive, groupNumReceive, stdNumReceive);
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
						//						receive = "1";
						infoStr = b;
					}
					sendData(define_var.info_ack);
				}
			}
		};   
		if(timer!=null)
		{timer.cancel();count = 0;}
//								Intent intent2 = new Intent();
//								intent2.setClass(MainActivity.this, Mark.class);
//								intent2.putExtra("userNameStr", "123");
//								intent2.putExtra("subjectStr","�赸");
//								intent2.putExtra("roomNumStr", "1");
//								intent2.putExtra("receive", "1");
//								intent2.putExtra("teacherNameStr", "��٩");
//								intent2.putExtra("infoStr","aaaaaaaaa");
//								intent2.putExtra("userNumStr", "1");
//								intent2.putExtra("roomPlaceStr", "1");
//								intent2.putExtra("examTypeStr", "����");
//								startActivity(intent2);
//								finish();
	}

	private void newtQueryDB(String stdNum,String startTime,String endTime,
			String height,String standHeight,String armLength,String legLength)
	{
		DBHelper helpter = new DBHelper(MainActivity.this);
		ContentValues values = new ContentValues(); 
		SQLiteDatabase db = helpter.getWritableDatabase();
		Cursor cursor = helpter.query(define_var.subject,define_var.room, define_var.group, stdNum);//�鿴������������������ݿ���治����
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
	//�յ�ȱ��ʱ��ѯ���ݿ�
	private void loseQueryDB(String subject,String roomNum,String groupNum,String stdNum)
	{
		DBHelper helpter = new DBHelper(MainActivity.this);
		ContentValues values = new ContentValues(); 
		SQLiteDatabase db= helpter.getWritableDatabase();
		Cursor c = helpter.query(subject, roomNum, groupNum, stdNum); 
		if(c.moveToFirst()==false)	//������͹����Ŀ������ݲ����ڣ�����
		{
			values = new ContentValues();
			values.put("subject", subject);     
			values.put("roomNum",roomNum);
			values.put("groupNum", groupNum);		
			values.put("stdNum", stdNum);     
			values.put("score","-1");
			values.put("submitOK","NO");
			values.put("examType", define_var.examTypeStr);
			helpter.insert(values); 
		}
		if(db!=null)	db.close();
	}
	/*****��ʼ���ؼ�******/
	private void initWidges() {
		// TODO Auto-generated method stub
		/*��½ҳ��ؼ�*/
		welcomeLayout = (RelativeLayout)findViewById(R.id.welcomeLayout);
		loginLinearLayout = (LinearLayout)findViewById(R.id.loginLinearLayout);	
		mEditUserName = (EditText)this.findViewById(R.id.username_edit);
		mEditPassword = (EditText)this.findViewById(R.id.password_edit);
		mBtnLogin = (Button)this.findViewById(R.id.signin_button);
		noNetText = (TextView)this.findViewById(R.id.noNetText);
		setIPButton = (Button)this.findViewById(R.id.setIPButton);
		deleteButton = (Button)this.findViewById(R.id.deleteButton);
		timer = new Count_Timer(10000, 1000);
		initipDB();
	}

	/***�󶨼�����****/
	private void bindListener() {
		// TODO Auto-generated method stub
		mBtnLogin.setOnClickListener(new BtnLoginClickListener());	//��¼
		//�޸�ip��ַ��ť
		setIPButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LayoutInflater inflater = LayoutInflater.from(MainActivity.this);  
				final View textEntryView = inflater.inflate(R.layout.iptest, null);  
				Button cancelBtn = (Button)textEntryView.findViewById(R.id.cancelBtn);
				Button sureBtn = (Button)textEntryView.findViewById(R.id.sureBtn);
				final IPEditText mEditText=(IPEditText) textEntryView.findViewById(R.id.mycustom);
				final TextView tipsText = (TextView)textEntryView.findViewById(R.id.tipsText);
				final EditText portText = (EditText)textEntryView.findViewById(R.id.portText);
				portText.setText(define_var.TPORT);
				dialog = new Dialog(MainActivity.this, R.style.MyDialog);
				//��������ContentView
				dialog.setContentView(textEntryView);
				dialog.show();
				cancelBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});
				sureBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String ip = mEditText.getText(MainActivity.this);
						//�ȱ��ؼ���Ƿ�Ϸ����Ϸ��ʹ浽���ݿ⣬��������
						boolean detectOK = false;
						System.out.println("----ip="+ip);
						String ipStr[] = ip.split("\\.");
						if(ipStr.length == 4)
						{
							if((ipStr[0]!=null&&!ipStr[0].equals("")&&Integer.valueOf(ipStr[0])>0&&Integer.valueOf(ipStr[0])<255)&&(ipStr[1]!=null&&!ipStr[1].equals("")&&Integer.valueOf(ipStr[1])>0&&Integer.valueOf(ipStr[1])<255)
									&&(ipStr[2]!=null&&!ipStr[2].equals("")&&Integer.valueOf(ipStr[2])>0&&Integer.valueOf(ipStr[2])<255)&&(ipStr[3]!=null&&!ipStr[3].equals("")&&Integer.valueOf(ipStr[3])>0&&Integer.valueOf(ipStr[3])<255))
								detectOK = true;
						}
						if(detectOK)
						{
							mEditText.getText(MainActivity.this);
							define_var.RHOST = ip;
							define_var.TPORT = portText.getText().toString();
							IPDB helpter1 = new IPDB(MainActivity.this);
							ContentValues values1 = new ContentValues(); 
							SQLiteDatabase db1 = helpter1.getWritableDatabase();	
							Cursor c1 = helpter1.query();
							values1.put("ip", define_var.RHOST);
							values1.put("port", define_var.TPORT);
							if(c1.moveToFirst()!=false)
								db1.delete("socket", null, null);
							db1.insert("socket", null, values1);
							if(mConnectThread!=null)
								mConnectThread.stopThread();
							dialog.dismiss();
							//							logNO = false;
						}
						else tipsText.setText("IP��ַ���Ϸ�");
					}
				});
			}
		});
		//���Լӵ�ɾ�����ݿ�
		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				define_var.userNameStr = mEditUserName.getText().toString();
				if(define_var.userNameStr!=null)
				{
					final File file = MainActivity.this.getDatabasePath(define_var.userNameStr+".db");
					final File file2 = MainActivity.this.getDatabasePath(define_var.userNameStr+"Init.db");
					boolean deleteOK = false;
					if(file.exists())
					{
						if(file.delete()){
							deleteOK = true;
						}
						else deleteOK = false;
					}
					if(file2.exists())
					{
						if(file2.delete()){
							deleteOK = true;
						}
						else deleteOK = false;
					}
					if(deleteOK)
						showToast("ɾ���ɹ�");
				}
			}});
	}
	private void initipDB()
	{
		/***��һ��������û���****/
		InitUserNameDB helpter2 = new InitUserNameDB(MainActivity.this);
		Cursor c2 = helpter2.query(); 
		if(c2.moveToFirst()!=false)
		{
			/**�ȵõ���һ��������û���***/
			mEditUserName.setText(c2.getString(c2.getColumnIndex("userNameTable.userName")));
			//			mEditPassword.setText(c2.getString(c2.getColumnIndex("userNameTable.userName")));
			mEditUserName.setSelection(mEditUserName.getText().toString().length());
		}
		/***IP���ݿ�****/
		IPDB helpter1 = new IPDB(MainActivity.this);
		Cursor c1 = helpter1.query(); 
		if(c1.moveToFirst()!=false)
		{
			/**�ȵõ���һ���˳�ʱ�Ŀ������***/
			define_var.RHOST = c1.getString(c1.getColumnIndex("socket.ip"));
			define_var.TPORT = c1.getString(c1.getColumnIndex("socket.port"));
		}
		else
		{
//			showToast("���޸�IP�Ͷ˿ں�");
			//			define_var.RHOST = "192.168.1.100";
			define_var.RHOST = "10.128.22.210";
			define_var.TPORT = "3206";
		}	
	}
	/****��¼��ť****/
	class BtnLoginClickListener implements OnClickListener{
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//���EditTex������
			userNameStr = mEditUserName.getText().toString();
			password = mEditPassword.getText().toString();
			mEditUserName.setBackgroundResource(R.drawable.edit_text_shape);
			mEditPassword.setBackgroundResource(R.drawable.edit_text_shape);
			/***���ؼ���û����������Ƿ�Ϸ�,���Ϸ�***/
			if(userNameStr.length()==0)
			{
				speakWords("���������û���");
				noNetText.setText("���������û���");
				mEditUserName.setBackgroundResource(R.drawable.edit_text_shape_error);
			}
			else if(userNameStr.length()<2)	//���Ϸ�			
			{
				noNetText.setText("�û������Ȳ���ȷ");
				mEditUserName.setBackgroundResource(R.drawable.edit_text_shape_error);
			}
			else if(password.length()==0)
			{
				speakWords("������������");
				noNetText.setText("������������");
				if(mEditUserName.isFocused())//�л�����
				{
					mEditUserName.clearFocus();
					mEditPassword.requestFocus();
				}
				mEditPassword.setBackgroundResource(R.drawable.edit_text_shape_error);
			}
			/***���ؼ��Ϸ������͵�¼����***/
			else	
			{
				/********����Ϊ���͵�¼��Ϣ����*******/
				startProgressDialog();
				noNetText.setText("");
				//				if(!logNO)
				//				{
				mConnectThread = SocketSingle.getSocketSingleInstance(mHandler);
				mConnectThread.start();
				//				}
				//				else
				//				{
				//					logNO = true;
				//					sendBuffer = define_var.log+"|"+userNameStr+"|"+encryption(password)+"|1";
				//					sendDataNeedACK(sendBuffer);
				//				}
				//				upload_timer.start();
				InitUserNameDB helpter1 = new InitUserNameDB(MainActivity.this);
				ContentValues values1 = new ContentValues(); 
				SQLiteDatabase db1 = helpter1.getWritableDatabase();	
				Cursor c1 = helpter1.query();
				values1.put("userName", userNameStr);
				if(c1.moveToFirst()!=false)
					db1.delete("userNameTable", null, null);
				db1.insert("userNameTable", null, values1);
			};
		}}
	/******�������ҳ��*******/
	public void permissionPage()
	{
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);  
		final View textEntryView = inflater.inflate(R.layout.xuzhi, null);  
		TextView roomText = (TextView)textEntryView.findViewById(R.id.roomText); 
		TextView placeText = (TextView)textEntryView.findViewById(R.id.placeText); 
		Button agreeBtn = (Button)textEntryView.findViewById(R.id.agreeBtn); 
		Button rejectBtn = (Button)textEntryView.findViewById(R.id.rejectBtn); 
		roomText.setText(subjectStr+roomNumStr+"�ſ���");
		placeText.setText(roomPlaceStr);
		if(myDialog!=null)
			myDialog.dismiss();
		myDialog = new AlertDialog.Builder(MainActivity.this).create();  
		myDialog.show();  
		myDialog.getWindow().setContentView(textEntryView);  
		agreeBtn.setOnClickListener(new View.OnClickListener() {  
			@Override  
			public void onClick(View v) { 
				////////����
				if(connectOK)
				{
					myDialog.dismiss();  
					if(timer!=null)
					{timer.cancel();count = 0;}
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, Mark.class);
					intent.putExtra("userNameStr", userNameStr);
					intent.putExtra("subjectStr",subjectStr);
					intent.putExtra("roomNumStr", roomNumStr);
					intent.putExtra("receive", receive);
					intent.putExtra("teacherNameStr", teacherNameStr);
					intent.putExtra("infoStr",infoStr);
					intent.putExtra("userNumStr", userNumStr);
					intent.putExtra("roomPlaceStr", roomPlaceStr);
					intent.putExtra("examTypeStr", examTypeStr);
					intent.putExtra("passwordStr", encryption(password));
					startActivity(intent);
					finish();
				}
				else
				{
					if(myDialog!=null)
						myDialog.dismiss();
					noConnect();
				}
			}  
		});  
		rejectBtn.setOnClickListener(new View.OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				myDialog.dismiss();  
				if(timer!=null)
				{timer.cancel();count = 0;}
				stopProgressDialog();
				if(mConnectThread!=null)
					mConnectThread.stopThread();
				reject = true;
				//				logNO = false;
			}  
		});
		myDialog.setCanceledOnTouchOutside(false);//���ҳ��������ط�dialog������ʧ
		myDialog.setCancelable(false);
	}
	void sendDataNeedACK(String str)
	{
		timer.start();
		sendData(str);
	}
	/********�������ݺ���*********/
	void sendData(String str)
	{
		if(mConnectThread!=null)
		{
			if(mConnectThread.clientSocket!=null)
			{
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
							mConnectThread.clientSocket.getOutputStream(),"GBK")));
				} catch (Exception e) {  
					e.printStackTrace();  
				} 
				if(str!=null)
				{
					pw.println(str);  
					System.out.println("--------���͵���Ϣstr=="+str);
					pw.flush();
				}	  
			}
		}
	}
	private void noConnect()
	{
		if(timer!=null)
			timer.cancel();
		stopProgressDialog();
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);  
		final View textEntryView = inflater.inflate(R.layout.tips, null);  
		TextView tips = (TextView)textEntryView.findViewById(R.id.textView);
		Button button = (Button)textEntryView.findViewById(R.id.button);
		textView2 = (TextView)textEntryView.findViewById(R.id.textView2);
		tips.setText("���������ӳ�ʱ�������ԡ�");
		myDialog = new AlertDialog.Builder(MainActivity.this).create();  
		myDialog.show();  
		myDialog.getWindow().setContentView(textEntryView); 
		button.setOnClickListener(new View.OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				myDialog.dismiss();  
			}  
		});   
	}
	//MD5����
	public String encryption(String plainText) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			re_md5 = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
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
				if(readerTTS != null){
					readerTTS.stop();
					readerTTS.shutdown();
				}
				finish();
				super.onDestroy();
				if(mConnectThread!=null)
					mConnectThread.stopThread();
				System.exit(0);
			}
		}
		return false;
	}
	//��ʾtoast
	private void showToast(String str)
	{
		toast = Toast.makeText(getApplicationContext(),str, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	//��ӭ����������
	class welcomeAnimationListener implements AnimationListener{

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			if (animation.hashCode() == toAppearAnimation.hashCode()){//�𽥳���
				loginLinearLayout.setVisibility(View.VISIBLE);
				welcomeLayout.startAnimation(toDisappearAnimation);   
				titleText.startAnimation(animationSet);
			}else if (animation.hashCode() == toDisappearAnimation.hashCode()){//����ʧ
				loginFrameLayout.bringChildToFront(loginLinearLayout);			
				//				initWidges();		//�󶨿ؼ�
				//				bindListener();		//�ؼ��󶨼�����
				speakWords("��ӭ����������������ϵͳ");
				speakWords("���������û���������");
			}			
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
		}

	}

	//��������init������
	class readerOnInitListener implements OnInitListener{

		@Override
		public void onInit(int initStatus) {
			// TODO Auto-generated method stub
			if (initStatus == TextToSpeech.SUCCESS) {
				if(readerTTS.isLanguageAvailable(Locale.CHINA)==TextToSpeech.LANG_AVAILABLE){
					readerTTS.setLanguage(Locale.CHINA);
				}					
			}
			else if (initStatus == TextToSpeech.ERROR) {
				Toast.makeText(getApplicationContext(), "��Ǹ����������ʧ�ܣ�", Toast.LENGTH_LONG).show();
			}
		}

	}

	//����TTS������֤�ɹ�
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		System.out.println("----����˳�����---onActivityResult----");
		int setEngineResult;
		if (requestCode == DATA_CHECK_CODE) { 
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) { 
				readerTTS = new TextToSpeech(this, new readerOnInitListener()); 
				defaultEngineName = readerTTS.getDefaultEngine();	
				if (queryIFlyAudioPackage()) {//�����Ѷ��������
					haveIflyPackage = true;
					System.out.println("----��ǰ��������----haveIflyPackage----"+haveIflyPackage);
					if (!defaultEngineName.equals("com.iflytek.speechcloud")){
						notDefaultPackage = true;
						System.out.println("----��ǰ��������----notDefaultPackage----"+notDefaultPackage);
						setEngineResult = readerTTS.setEngineByPackageName("com.iflytek.speechcloud");
						if (setEngineResult == 0){ //������������ɹ�
							selectIflyPackage = true;
							System.out.println("----��ǰ��������----selectIflyPackage----"+selectIflyPackage);
						}						
					}
					else{
						selectIflyPackage = true;
						System.out.println("----��ǰ��������----selectIflyPackage----"+selectIflyPackage);
					}
				} else {
					readerTTS.setEngineByPackageName(defaultEngineName);
				}
			} 
			else { 
				Intent installTTSIntent = new Intent(); 
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA); 
				startActivity(installTTSIntent);
			} 
		} 
	}

	//��������
	private void speakWords(String speech) {
		System.out.println("---��ǰ��������---speakWords---");
		if (haveIflyPackage){
			if (selectIflyPackage){
				readerTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
			}else{
				noNetText.setText("���顰Ѷ������+������Ϊϵͳ��ѡ��������");
			}
			if (notDefaultPackage){
				noNetText.setText("���顰Ѷ������+������Ϊϵͳ��ѡ��������");
			}
		}else {
			noNetText.setText("���豸��֧���������������������Ȱ�װ��Ѷ������+��Ӧ�����");
		}
	}

	//��ѯϵͳ�����Ƿ��Ѱ�װѶ��������
	private boolean queryIFlyAudioPackage() {
		engineList = readerTTS.getEngines();
		if (engineList.size() > 0){//�������������������ѯ�Ƿ���Ѷ��������
			for (EngineInfo engine: engineList){//����engineList
				System.out.println("----����Ѷ�ɰ�����---"+engine.name);
				if (engine.name.equals("com.iflytek.speechcloud")){
					return true;
				}
			}
		}else {//������û���κ����������򷵻�false
			return false;
		}
		return false;
	}
	private void startProgressDialog(){
		if (progressDialog == null){
			progressDialog = CustomProgressDialog.createDialog(this);
			progressDialog.setMessage("���ڵ�¼��...");
		}
		progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
			@SuppressLint("NewApi")
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
				if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
				{
					if(timer!=null)
					{timer.cancel();count = 0;}
					stopProgressDialog();
					if(mConnectThread!=null){
						mConnectThread.stopThread();
						reject = true;
						//						logNO = false;
					}
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
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	//	class close_Timer extends CountDownTimer   
	//	{
	//		public close_Timer(long millisInFuture, long countDownInterval) {
	//			super(millisInFuture, countDownInterval);
	//			// TODO Auto-generated constructor stub
	//		}
	//		@Override
	//		public void onFinish() 
	//		{ //��ʱ����,ʱ�䵽���ص���ҳ��
	//			// TODO Auto-generated method stub
	//			if(textView2!=null)
	//				textView2.setText("0s���Զ��ر�");
	//			myDialog.dismiss();
	//			if(close_timer!=null)	close_timer.cancel();
	//		}
	//		@Override
	//		public void onTick(long millisUntilFinished) 
	//		{   
	//			// TODO Auto-generated method stub
	//			if(textView2!=null)
	//				textView2.setText("("+millisUntilFinished/1000+"s���Զ��ر�)");
	//		}
	//	}
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
	//	class UploadCount_Timer extends CountDownTimer   
	//	{
	//		public UploadCount_Timer(long millisInFuture, long countDownInterval) {
	//			super(millisInFuture, countDownInterval);
	//			// TODO Auto-generated constructor stub
	//		}
	//		@Override
	//		public void onFinish() 
	//		{ //��ʱ����,ʱ�䵽���ص���ҳ��
	//			// TODO Auto-generated method stub
	//			stopProgressDialog();
	//			//			noConnect();
	//			if(timer!=null)
	//			{timer.cancel();count = 0;}
	//			if(mConnectThread!=null)
	//				mConnectThread.stopThread();
	//			//			if(upload_timer!=null)
	//			//				upload_timer.cancel();
	//		}
	//		@Override
	//		public void onTick(long millisUntilFinished) 
	//		{   
	//			// TODO Auto-generated method stub
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
		{ //��ʱ����,ʱ�䵽���ٷ�һ������
			// TODO Auto-generated method stub
			//			count++;
			//			if(count == 10)
			//			{
			//				stopProgressDialog();
			//				if(timer!=null)
			//				{timer.cancel();count = 0;}
			//				if(mConnectThread!=null)
			//					mConnectThread.stopThread();
			//				
			//			}
			if(!connectOK)
			{
				if(sendBuffer!=null)
				{sendDataNeedACK(sendBuffer);}
			}
			else
			{
				LayoutInflater inflater = LayoutInflater.from(MainActivity.this);  
				final View textEntryView = inflater.inflate(R.layout.tips, null);  
				TextView tips = (TextView)textEntryView.findViewById(R.id.textView);
				Button button = (Button)textEntryView.findViewById(R.id.button);
				textView2 = (TextView)textEntryView.findViewById(R.id.textView2);
				tips.setText("����������Ӧ�����Ժ�����");
				stopProgressDialog();
				if(myDialog!=null)
					myDialog.dismiss();
				myDialog = new AlertDialog.Builder(MainActivity.this).create();  
				myDialog.show();  
				myDialog.getWindow().setContentView(textEntryView); 
				button.setOnClickListener(new View.OnClickListener() {  
					@Override  
					public void onClick(View v) {  
						myDialog.dismiss();  
					}  
				});   
				if(mConnectThread!=null)
					mConnectThread.stopThread();
			}
		}
		@Override
		public void onTick(long millisUntilFinished) 
		{   
			// TODO Auto-generated method stub
		}
	}
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
////		System.out.println("-----mark destroy");
////		if(mConnectThread!=null)
////			mConnectThread.stopThread();
////		System.exit(0);
//	}
}
