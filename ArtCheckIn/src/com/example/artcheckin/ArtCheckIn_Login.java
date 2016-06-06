package com.example.artcheckin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
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

import com.example.database.DbManager;
import com.example.database.DefineVar;
import com.example.database.SocketConfigInfo;
import com.example.network.SocketConnectThread;

public class ArtCheckIn_Login extends Activity {
	//	private Button BtnLogin;
	//��¼ҳ��ؼ�
	private EditText mEditUserName;			//�û���
	private EditText mEditPassword;			//����
	private Button mBtnLogin;				//��¼��ť
	private Button mBtnConfig;              //����IP�Ͷ˿ڵİ�ť
	//private Button mBtnClearDb;              //����IP�Ͷ˿ڵİ�ť
	private TextView mWarningText; 
	private DbManager mSocketDbManager=null;
	private DbManager mMainDbManager=null;
	//����
	private byte[] sendBuffer;
	private String userNameStr;						//�û�������
	private String password ;
	private byte[] userNameBuffer;
	private String subjectNumStr;					//������Ŀ//
	private String roomNumStr;							//������Ŀ�Ϳ�����
	private String teacherNumInRoomStr;
	private String examStyleStr;
	private String userRemarkNameStr;
	private String examAddress;
	private String sendStr;
	private Count_Timer timer = new Count_Timer(20000, 1000);
	private Handler mHandler = null;  //���������̷߳��͹���������
	private SocketConnectThread mConnectThread =null; 
	//�������˳�����
	private static Boolean isQuit = false;
	Timer timer1 = new Timer();

	private int process = 0;
	private SocketConfigInfo socketConfigInfo = new SocketConfigInfo();
	//������������
	private int DATA_CHECK_CODE = 0;
	private TextToSpeech readerTTS;
	private List<EngineInfo> engineList = null;
	String defaultEngineName = null;
	private boolean haveIflyPackage = false;
	private boolean selectIflyPackage = false;
	private boolean notDefaultPackage = false;

	private OutputStream outStream = null;
	private PrintWriter out = null;

	//��ӭ��������
	private RelativeLayout welcomeLayout;  
	private TextView titleText;
	private Animation toLargeAnimation;
	private Animation toSmallAnimation;
	private TranslateAnimation toUpAnimation;
	private Animation toDownAnimation;
	private AlphaAnimation toAppearAnimation;
	private AlphaAnimation toDisappearAnimation;
	private FrameLayout loginFrameLayout;
	private LinearLayout loginLinearLayout;
	private AnimationSet animationSet;

	private CustomProgressDialog progressDialog = null;
	private int repeatTimes = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //���������Ĭ�ϲ�����		
		Intent intent =this.getIntent();	//Main���洫������ֵ
		String failReason=intent.getStringExtra("failReason");
		initWidges();		//�󶨿ؼ�
		initDataBase();
		clearDatabase();
		if(failReason==null){
			//����������ʼ����֤
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent, DATA_CHECK_CODE);

			//��ӭ����---���ֶ���			
			animationSet = new AnimationSet(true);
			toLargeAnimation = AnimationUtils.loadAnimation(this, R.anim.to_large);
			toUpAnimation = new TranslateAnimation(0, 0, 0,-100);
			animationSet.setDuration(2000);
			animationSet.addAnimation(toUpAnimation);
			animationSet.addAnimation(toLargeAnimation);
			//��ӭ����---��������
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
		}else{			
			loginFrameLayout.bringChildToFront(loginLinearLayout);
			loginLinearLayout.setVisibility(View.VISIBLE);
			welcomeLayout.setVisibility(View.INVISIBLE);
			Toast.makeText(getApplicationContext(), failReason, 4000).show();
		}
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				//��ʾ���յ�������
				if(msg.what == FrameFormat.HANDLE_SERVERSOCKETCONSUCCESS)
				{
					//Toast.makeText(getApplicationContext(), "TCP���ӳɹ�", 2000).show();
					//sendStr = FrameFormat.COMMAND_CONNECT+"|"+getLocalMacAddress();
					sendStr = FrameFormat.COMMAND_CONNECT+"|"+getCPUSerial();
					SendInfoToServer(sendStr);
					timer.start();
				}
				else if(msg.what == FrameFormat.HANDLE_SERVERSOCKETCONTIMEOUT)
				{
					stopProgressDialog();
					timer.cancel();
					if(mConnectThread!=null){
						mConnectThread.StopThread();
					}
					if(mWarningText!=null){
						//mWarningText.setText("���������ӳ�ʱ");
						mWarningText.setText("����������ʧ�ܣ����Ժ��¼");
					}
				}
				else if(msg.what == FrameFormat.HANDLE_SERVERDISCONNECT)
				{
					System.out.println("---login �������Ͽ�");
					stopProgressDialog();
					if(timer!=null){
						timer.cancel();
					}
					if(mConnectThread!=null){
						mConnectThread.StopThread();
					}
					if(process!=FrameFormat.PROCESS_CONNECTFAIL&&process!=FrameFormat.PROCESS_LOGINFAIL&&
							process!=FrameFormat.PROCESS_DIALOGBACKDOWN){

						if(mWarningText!=null){
							//mWarningText.setText("���������ӶϿ�");
							mWarningText.setText("����������ʧ�ܣ����Ժ��¼");
						}
					}
				}
				//����ΪSOCKET���յ��ķ��������͵���������
				else if(msg.what == FrameFormat.HANDLE_CONNECTSUCCESS)
				{
					//Toast.makeText(getApplicationContext(), "TCP���ӳɹ�", 2000).show();

					//					if(timer!=null){
					//						timer.cancel();
					//						}
					sendStr = FrameFormat.COMMAND_LOGIN+"|"+userNameStr+"|"+encryption(password)+"|"+"0";
					//sendStr = FrameFormat.COMMAND_LOGIN+"|"+userNameStr+"|"+password+"|"+"0";
					SendInfoToServer(sendStr);
					timer.start();
				}
				else if(msg.what == FrameFormat.HANDLE_CONNECTFAIL)
				{
					System.out.println("---login �������ܾ�");
					process = FrameFormat.PROCESS_CONNECTFAIL;
					stopProgressDialog();
					//Toast.makeText(getApplicationContext(), failReason, 2000).show();
					timer.cancel();
					if(mConnectThread!=null){
						mConnectThread.StopThread();
					}
					String failReason = (String)msg.obj;
					if(mWarningText!=null){
						mWarningText.setText("�������󱻾ܾ�,"+failReason);
					}
				}
				else if(msg.what == FrameFormat.HANDLE_LOGINSUCCESS)	//��¼�ɹ�
				{
					//msg.obj = subject + "," + roomNum + "," + userRemarkName + ","
					//		+ examPlace + "," + examStage;
					stopProgressDialog();
					Log.d("MainActivity", "---��¼�ɹ����յ������ݣ�"+(String)(msg.obj));
					timer.cancel();
					String str[] = ((String)(msg.obj)).split("\\|");
					subjectNumStr = str[0];
					roomNumStr = str[1];
					examAddress = str[4];
					userRemarkNameStr = str[3];
					examStyleStr = str[5];
					DefineVar.databaseName =getSubjectEnglish(subjectNumStr)+ roomNumStr+"-"+getExamStyleEnglish(examStyleStr)+".db";
					//DefineVar.databaseName =subjectNumStr+ roomNumStr+"-"+examStyleStr+".db";
					mMainDbManager=new DbManager(getBaseContext());
					Log.d("MainActivity", "---��¼�ɹ����յ������ݣ�"+subjectNumStr+" "+roomNumStr+" "+str[2]
							+" "+userRemarkNameStr+" "+examAddress+" "+examStyleStr);
					if(str[2].equals(FrameFormat.LOGINWORKERSTYLE_CHECKIN)){
						Toast.makeText(getApplicationContext(), "��½�ɹ�", 2000).show();
						Intent intent = new Intent();
						intent.setClass(ArtCheckIn_Login.this, ArtCheckIn_Main.class);
						intent.putExtra("userNameStr", userNameStr);
						intent.putExtra("userPswStr", password);
						intent.putExtra("subjectNumStr", subjectNumStr);
						intent.putExtra("roomNumStr", roomNumStr);
						intent.putExtra("userRemarkNameStr", userRemarkNameStr);
						intent.putExtra("examAddress", examAddress);
						intent.putExtra("examStyleStr", examStyleStr);
						startActivity(intent);
						finish();
					}else{
						Log.d("Login", "---�ظ�����str[2]="+str[2]);
					}
				}
				else if(msg.what == FrameFormat.HANDLE_LOGINFAIL)
				{
					process=FrameFormat.PROCESS_LOGINFAIL;
					stopProgressDialog();
					timer.cancel();
					if(mConnectThread!=null){
						mConnectThread.StopThread();
					}
					String failReason = (String)msg.obj;
					if(mWarningText!=null){
						mWarningText.setText("��¼���ɹ�,"+failReason);
					}
					mEditPassword.setText("");
				}
				//				else if (msg.what == FrameFormat.HANDLE_UPDATEINFO) {
				//					String str[] = ((String) (msg.obj)).split("\\|");
				//					String recSubjectNumStr = str[0];   //Ϊ����֤�ã���recSubjectNumStr���LoginActivity��������subjectNumStr�Ա�
				//					String recRoomNumStr = str[1];		//Ϊ����֤�ã���recRoomNumStr���LoginActivity��������roomNumStr�Ա� 
				//					String RecGroupNumStr = str[2];
				//					String RecStudentNumStr = str[3];
				//					String RecStartExamTime =str[4];
				//					String RecStopExamTime =str[5];
				//					String RecExamnieeID = str[6];
				//					String RecStudentName = str[7];
				//					String RecStudentIdentity = str[8];
				//					String RecSex =str[9];
				//					String RecImageURL=str[10];
				//					System.out.println("--->>LoginActivity�յ��յ����´�������Ϣ��������:"
				//							+ recSubjectNumStr + recRoomNumStr + "����"+RecStudentNumStr+"��");
				//					//���յ������ݲ������ݿ��У�������б�
				//					if(!mMainDbManager.queryOneExamnieeInfoIsExist(RecGroupNumStr,RecStudentNumStr)){
				//						System.out.println("--->>LoginActivity���ݿ��в����ڸ�����,�������ݿ���");
				////						public boolean insertExamnieeInfo(String groupID,String examnieeID,String startTime,String endTime,String examinationID,
				////								String examnieeName,String examnieeIdentity,String examnieeSex,String examnieePicURL) {
				//						mMainDbManager.insertExamnieeInfo(RecGroupNumStr, RecStudentNumStr,
				//								RecStartExamTime,RecStopExamTime,RecExamnieeID,RecStudentName,RecStudentIdentity,RecSex,RecImageURL);
				//					}else{
				//						System.out.println("--->>LoginActivity���ݿ����Ѵ��ڸ�����");
				//					}
				//				}
			}
		};

		//	mConnectThread = SocketConnectThread.getSocketSingleInstance(mHandler);
	}
	private void initWidges() {
		// TODO Auto-generated method stub
		/*��½ҳ��ؼ�*/
		mEditUserName = (EditText)this.findViewById(R.id.username_edit);
		mEditPassword = (EditText)this.findViewById(R.id.password_edit);
		mBtnLogin = (Button)this.findViewById(R.id.signin_button);
		//mBtnClearDb = (Button)this.findViewById(R.id.clearDb_button);
		//		IPText = (EditText)this.findViewById(R.id.IPText);
		//		PortText = (EditText)this.findViewById(R.id.portText);
		mBtnConfig = (Button)this.findViewById(R.id.configButton);
		mWarningText = (TextView)this.findViewById(R.id.warningText);

		//�����ؼ�
		titleText = (TextView)findViewById(R.id.titleText);
		loginFrameLayout = (FrameLayout)findViewById(R.id.loginFrameLayout);
		loginLinearLayout = (LinearLayout)findViewById(R.id.loginLinearLayout);		
		welcomeLayout = (RelativeLayout)findViewById(R.id.welcomeLayout);

		OnClickListener LoginClickListener = new LoginClickListener();
		mBtnLogin.setOnClickListener(LoginClickListener);
		OnClickListener ConfigClickListener = new ConfigClickListener();
		mBtnConfig.setOnClickListener(ConfigClickListener);
		//	OnClickListener ClearDbClickListener = new ClearDbClickListener();
		//	mBtnClearDb.setOnClickListener(ClearDbClickListener);
	}
	private void initDataBase(){
		mSocketDbManager=new DbManager(getBaseContext(),"Socket");
		if(!mSocketDbManager.queryIsExistDataInSocketTable()){
			mSocketDbManager.insertSocketConfi(DefineVar.DEFAULT_IP, DefineVar.DEFAULT_PORT);
		}
		socketConfigInfo = mSocketDbManager.querySocketConfigInfoOfLastOne();
		if(socketConfigInfo!=null){
			DefineVar.ServerIP = socketConfigInfo.getIpStr();
			DefineVar.ServerPort = socketConfigInfo.getPortStr();
		}
		String userNameTemp = mSocketDbManager.queryUserNameInfoOfLastOne();
		if(userNameTemp!=null){
			mEditUserName.setText(userNameTemp);
			mEditUserName.setSelection(userNameTemp.length());
		}
	}
	class LoginClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//if(DefineVar.process==FrameFormat.PROCESS_CONNECTSUCCESS){
			//hyz0114
			//mBtnLogin.setText("��¼��...");
			//loginProgress = ProgressDialog.show(ArtCheckIn_Login.this,"���ڵ�½","��ȴ�",true,false);
			//���EditTex������
			userNameStr = mEditUserName.getText().toString();
			password = mEditPassword.getText().toString();
			/***���ؼ���û����������Ƿ�Ϸ�,���Ϸ�***/
			//if(userNameStr.length()<2||userNameStr.length()>8)	//���Ϸ�		
			if(userNameStr.length()<2)	//���Ϸ�		
			{
				speakWords("��������û��������벻�Ϸ�");
				Toast.makeText(getApplicationContext(), "�û��������벻�Ϸ�", 2000).show();
				mBtnLogin.setText("��   ¼");
			}else if(password.equals("")){
				speakWords("������������");
				Toast.makeText(getApplicationContext(), "���벻��Ϊ��", 2000).show();
				mBtnLogin.setText("��   ¼");
			}
			else	//���ؼ��Ϸ������͵�¼����
			{
				mSocketDbManager.insertUserName(userNameStr);
				//mConnectThread.startConnectSocket();
				if(mConnectThread!=null){
					mConnectThread.StopThread();
				}
				mConnectThread = SocketConnectThread.getSocketSingleInstance(mHandler,getBaseContext());
				if(mConnectThread!=null){
					mConnectThread.start(); 
				}
				if(mWarningText!=null){
					mWarningText.setText("");
				}
				startProgressDialog("���ڵ�½������"," IP:"+DefineVar.ServerIP,"�˿�:"+DefineVar.ServerPort);
			}
			//			}else{
			//				Toast.makeText(getApplicationContext(), "TCP���������⣬��ȴ�", 2000).show();
			//			}
		}

	}
	class ConfigClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			LayoutInflater inflater = LayoutInflater.from(ArtCheckIn_Login.this);  
			final View textEntryView = inflater.inflate(R.layout.config_dialog, null);  
			Button cancelBtn = (Button)textEntryView.findViewById(R.id.cancelBtn);
			Button sureBtn = (Button)textEntryView.findViewById(R.id.sureBtn);
			final IPEditText IPText = (IPEditText)textEntryView.findViewById(R.id.IPText);
			//			final EditText IPText = (EditText)textEntryView.findViewById(R.id.IPText);
			final EditText PortText = (EditText)textEntryView.findViewById(R.id.portText);
			IPText.setText(DefineVar.ServerIP);
			PortText.setText(DefineVar.ServerPort);
			final Dialog dialog = new Dialog(ArtCheckIn_Login.this, R.style.MyDialogNew);
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
					if(IPText.getText().toString()!=null&&PortText.getText().toString()!=null)
					{
						DefineVar.ServerIP = IPText.getText().toString();
						DefineVar.ServerPort = PortText.getText().toString();
						if(!DefineVar.ServerIP.equals("")&&!DefineVar.ServerPort.equals("")){
							mSocketDbManager.insertSocketConfi(DefineVar.ServerIP, DefineVar.ServerPort);
						}
						else{
							Toast.makeText(getApplicationContext(), "��Ǹ��IP�Ͷ˿ںŲ���Ϊ�գ�", Toast.LENGTH_LONG).show();
						}
						dialog.dismiss();
					}
				}
			});

		}

	}
	//	class ClearDbClickListener implements OnClickListener{
	//
	//		@Override
	//		public void onClick(View arg0) {
	//			// TODO Auto-generated method stub
	////			String tempName = null;
	////			LinkedHashSet<String> databaseNameSet = new LinkedHashSet<String>();
	////			databaseNameSet = mSocketDbManager.queryExistingDatabase();
	////			if(databaseNameSet.size()>0){
	////				Iterator<String> iterator = databaseNameSet.iterator();
	////				while(iterator.hasNext()){
	////					tempName = iterator.next();
	////					mSocketDbManager.deleteDatabase(getBaseContext(), tempName);
	////				}
	////			}
	//		}
	//	}
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
			//check for successful instantiation
			System.out.println("----����˳�����---readerOnInitListener----");
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
					System.out.println("----��ǰ��������----��Ѷ��������----");
					if (!defaultEngineName.equals("com.iflytek.speechcloud")){
						System.out.println("----��ǰ��������----Ѷ������������Ĭ�ϵ�----"+defaultEngineName);
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
			System.out.println("---��ǰ��������---speakWords---"+haveIflyPackage);
			if (selectIflyPackage){
				System.out.println("---��ǰ��������---speakWords---"+selectIflyPackage);
				readerTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
			}else{
				mWarningText.setText("���顰Ѷ������+������Ϊϵͳ��ѡ��������");
			}
			if (notDefaultPackage){
				mWarningText.setText("���顰Ѷ������+������Ϊϵͳ��ѡ��������");
				//Toast.makeText(getApplicationContext(), "�뽫��Ѷ������+������Ϊϵͳ��ѡ��������", 2000).show();
				System.out.println("---��ǰ��������---speakWords---"+notDefaultPackage);
			}

		}else {
			mWarningText.setText("���豸��֧���������������������Ȱ�װ��Ѷ������+��Ӧ�����");
			//Toast.makeText(getApplicationContext(), "���豸��֧���������������������Ȱ�װ��Ѷ������+��Ӧ�����", 2000).show();
			System.out.println("----���鰲װѶ������");
		}
	}
	//��ѯϵͳ�����Ƿ��Ѱ�װѶ��������
	private boolean queryIFlyAudioPackage() {
		engineList = readerTTS.getEngines();
		if (engineList.size() > 0){//�������������������ѯ�Ƿ���Ѷ��������
			for (EngineInfo engine: engineList){//����engineList
				System.out.println("----����Ѷ�ɰ�---");
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

	private void SendInfoToServer(String msgStr ){
		if(mConnectThread!=null){
			if(mConnectThread.clientSocket!=null){
				try {
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
							mConnectThread.clientSocket.getOutputStream(),"GBK")), true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(out!=null){
					out.println(msgStr);
					out.flush();
				}
			}
		}
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
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
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
				if(mConnectThread!=null){
					mConnectThread.StopThread();
				}
				finish();
				System.exit(0);
			}
		}
		return false;
	}
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
			//repeatTimes++;
			SendInfoToServer(sendStr);
			timer.start();
			//			if(repeatTimes==FrameFormat.MAXRECONNTIMES){
			//				//����LoginAcitivity��LOGIN�����5�Σ���û�л�Ӧ,����ʾ��¼��ʱ�����Ի�����������ݣ�ÿ5������һ��
			//				//Toast.makeText(getApplicationContext(), "��½��ʱ", 2000).show();
			//				//������������
			//				repeatTimes = 0;
			//			}

		}
		@Override
		public void onTick(long millisUntilFinished) 
		{   
			// TODO Auto-generated method stub

		}
	}
	public String getLocalMacAddress() {
		String macSerial = null;
		String str = "";
		try {
			Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);


			for (; null != str;) {
				str = input.readLine();
				if (str != null) {
					macSerial = str.trim();// ȥ�ո�
					break;
				}
			}
		} catch (IOException ex) {
			// ����Ĭ��ֵ
			ex.printStackTrace();
		}
		return macSerial;
	}
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
	private void clearDatabase(){
		String tempName = null;
		LinkedHashSet<String> databaseNameSet = new LinkedHashSet<String>();
		databaseNameSet = mSocketDbManager.queryExistingDatabase();
		if(databaseNameSet.size()>0){
			Iterator<String> iterator = databaseNameSet.iterator();
			while(iterator.hasNext()){
				tempName = iterator.next();
				mSocketDbManager.deleteDatabase(getBaseContext(), tempName);
			}
		}
	}
	private void startProgressDialog(String message,String ipStr,String portStr){
		if (progressDialog == null){
			progressDialog = CustomProgressDialog.createDialog(this);
			progressDialog.setMessage(message,ipStr,portStr);
		}
		if(progressDialog != null){
			progressDialog.setOnKeyListener(keylistener);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}
	}

	private void stopProgressDialog(){
		if (progressDialog != null){
			System.out.println("----login progressDialog.dismiss");
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	OnKeyListener keylistener = new DialogInterface.OnKeyListener(){
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
			{
				System.out.println("----login dialog back down");
				process = FrameFormat.PROCESS_DIALOGBACKDOWN;
				timer.cancel();
				stopProgressDialog();
				if(mConnectThread!=null){
					mConnectThread.StopThread();
				}
				return true;
			}
			else
			{
				return false;
			}
		}
	} ;
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(readerTTS != null){
			readerTTS.stop();
			readerTTS.shutdown();
		}
	}

}
