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
	//登录页面控件
	private EditText mEditUserName;			//用户名
	private EditText mEditPassword;			//密码
	private Button mBtnLogin;				//登录按钮
	private Button mBtnConfig;              //配置IP和端口的按钮
	//private Button mBtnClearDb;              //配置IP和端口的按钮
	private TextView mWarningText; 
	private DbManager mSocketDbManager=null;
	private DbManager mMainDbManager=null;
	//变量
	private byte[] sendBuffer;
	private String userNameStr;						//用户名变量
	private String password ;
	private byte[] userNameBuffer;
	private String subjectNumStr;					//考场科目//
	private String roomNumStr;							//考场科目和考场号
	private String teacherNumInRoomStr;
	private String examStyleStr;
	private String userRemarkNameStr;
	private String examAddress;
	private String sendStr;
	private Count_Timer timer = new Count_Timer(20000, 1000);
	private Handler mHandler = null;  //接收其他线程发送过来的数据
	private SocketConnectThread mConnectThread =null; 
	//按两次退出程序
	private static Boolean isQuit = false;
	Timer timer1 = new Timer();

	private int process = 0;
	private SocketConfigInfo socketConfigInfo = new SocketConfigInfo();
	//语音播报变量
	private int DATA_CHECK_CODE = 0;
	private TextToSpeech readerTTS;
	private List<EngineInfo> engineList = null;
	String defaultEngineName = null;
	private boolean haveIflyPackage = false;
	private boolean selectIflyPackage = false;
	private boolean notDefaultPackage = false;

	private OutputStream outStream = null;
	private PrintWriter out = null;

	//欢迎动画变量
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
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //设置软键盘默认不弹出		
		Intent intent =this.getIntent();	//Main界面传过来的值
		String failReason=intent.getStringExtra("failReason");
		initWidges();		//绑定控件
		initDataBase();
		clearDatabase();
		if(failReason==null){
			//语音播报初始化验证
			Intent checkTTSIntent = new Intent();
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkTTSIntent, DATA_CHECK_CODE);

			//欢迎动画---文字动画			
			animationSet = new AnimationSet(true);
			toLargeAnimation = AnimationUtils.loadAnimation(this, R.anim.to_large);
			toUpAnimation = new TranslateAnimation(0, 0, 0,-100);
			animationSet.setDuration(2000);
			animationSet.addAnimation(toUpAnimation);
			animationSet.addAnimation(toLargeAnimation);
			//欢迎动画---背景动画
			//设置亮度的动画，实现渐变显示，从0.3到1.0(全亮)
			toAppearAnimation = new AlphaAnimation(0.3f, 1.0f);
			toAppearAnimation.setDuration(4000);         //设置渐变时间
			welcomeLayout.startAnimation(toAppearAnimation);          //开始一个动画
			//设置亮度的动画，实现渐变显示，从1.0到0.0(全亮)
			toDisappearAnimation = new AlphaAnimation(1.0f, 0.0f);
			toDisappearAnimation.setDuration(2000);         //设置渐变时间
			//设置动画监听器，当动画结束的时候，启动新的Activity
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
				//显示接收到的内容
				if(msg.what == FrameFormat.HANDLE_SERVERSOCKETCONSUCCESS)
				{
					//Toast.makeText(getApplicationContext(), "TCP连接成功", 2000).show();
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
						//mWarningText.setText("服务器连接超时");
						mWarningText.setText("服务器连接失败，请稍后登录");
					}
				}
				else if(msg.what == FrameFormat.HANDLE_SERVERDISCONNECT)
				{
					System.out.println("---login 服务器断开");
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
							//mWarningText.setText("服务器连接断开");
							mWarningText.setText("服务器连接失败，请稍后登录");
						}
					}
				}
				//以下为SOCKET接收到的服务器发送的网络命令
				else if(msg.what == FrameFormat.HANDLE_CONNECTSUCCESS)
				{
					//Toast.makeText(getApplicationContext(), "TCP连接成功", 2000).show();

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
					System.out.println("---login 服务器拒绝");
					process = FrameFormat.PROCESS_CONNECTFAIL;
					stopProgressDialog();
					//Toast.makeText(getApplicationContext(), failReason, 2000).show();
					timer.cancel();
					if(mConnectThread!=null){
						mConnectThread.StopThread();
					}
					String failReason = (String)msg.obj;
					if(mWarningText!=null){
						mWarningText.setText("连接请求被拒绝,"+failReason);
					}
				}
				else if(msg.what == FrameFormat.HANDLE_LOGINSUCCESS)	//登录成功
				{
					//msg.obj = subject + "," + roomNum + "," + userRemarkName + ","
					//		+ examPlace + "," + examStage;
					stopProgressDialog();
					Log.d("MainActivity", "---登录成功，收到的数据："+(String)(msg.obj));
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
					Log.d("MainActivity", "---登录成功，收到的数据："+subjectNumStr+" "+roomNumStr+" "+str[2]
							+" "+userRemarkNameStr+" "+examAddress+" "+examStyleStr);
					if(str[2].equals(FrameFormat.LOGINWORKERSTYLE_CHECKIN)){
						Toast.makeText(getApplicationContext(), "登陆成功", 2000).show();
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
						Log.d("Login", "---回复有误str[2]="+str[2]);
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
						mWarningText.setText("登录不成功,"+failReason);
					}
					mEditPassword.setText("");
				}
				//				else if (msg.what == FrameFormat.HANDLE_UPDATEINFO) {
				//					String str[] = ((String) (msg.obj)).split("\\|");
				//					String recSubjectNumStr = str[0];   //为了验证用，此recSubjectNumStr与从LoginActivity传过来的subjectNumStr对比
				//					String recRoomNumStr = str[1];		//为了验证用，此recRoomNumStr与从LoginActivity传过来的roomNumStr对比 
				//					String RecGroupNumStr = str[2];
				//					String RecStudentNumStr = str[3];
				//					String RecStartExamTime =str[4];
				//					String RecStopExamTime =str[5];
				//					String RecExamnieeID = str[6];
				//					String RecStudentName = str[7];
				//					String RecStudentIdentity = str[8];
				//					String RecSex =str[9];
				//					String RecImageURL=str[10];
				//					System.out.println("--->>LoginActivity收到收到更新待考组信息，考场号:"
				//							+ recSubjectNumStr + recRoomNumStr + "考场"+RecStudentNumStr+"号");
				//					//若收到的数据不在数据库中，则更新列表
				//					if(!mMainDbManager.queryOneExamnieeInfoIsExist(RecGroupNumStr,RecStudentNumStr)){
				//						System.out.println("--->>LoginActivity数据库中不存在该数据,插入数据库中");
				////						public boolean insertExamnieeInfo(String groupID,String examnieeID,String startTime,String endTime,String examinationID,
				////								String examnieeName,String examnieeIdentity,String examnieeSex,String examnieePicURL) {
				//						mMainDbManager.insertExamnieeInfo(RecGroupNumStr, RecStudentNumStr,
				//								RecStartExamTime,RecStopExamTime,RecExamnieeID,RecStudentName,RecStudentIdentity,RecSex,RecImageURL);
				//					}else{
				//						System.out.println("--->>LoginActivity数据库中已存在该数据");
				//					}
				//				}
			}
		};

		//	mConnectThread = SocketConnectThread.getSocketSingleInstance(mHandler);
	}
	private void initWidges() {
		// TODO Auto-generated method stub
		/*登陆页面控件*/
		mEditUserName = (EditText)this.findViewById(R.id.username_edit);
		mEditPassword = (EditText)this.findViewById(R.id.password_edit);
		mBtnLogin = (Button)this.findViewById(R.id.signin_button);
		//mBtnClearDb = (Button)this.findViewById(R.id.clearDb_button);
		//		IPText = (EditText)this.findViewById(R.id.IPText);
		//		PortText = (EditText)this.findViewById(R.id.portText);
		mBtnConfig = (Button)this.findViewById(R.id.configButton);
		mWarningText = (TextView)this.findViewById(R.id.warningText);

		//动画控件
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
			//mBtnLogin.setText("登录中...");
			//loginProgress = ProgressDialog.show(ArtCheckIn_Login.this,"正在登陆","请等待",true,false);
			//获得EditTex的内容
			userNameStr = mEditUserName.getText().toString();
			password = mEditPassword.getText().toString();
			/***本地检测用户名和密码是否合法,不合法***/
			//if(userNameStr.length()<2||userNameStr.length()>8)	//不合法		
			if(userNameStr.length()<2)	//不合法		
			{
				speakWords("您输入的用户名或密码不合法");
				Toast.makeText(getApplicationContext(), "用户名或密码不合法", 2000).show();
				mBtnLogin.setText("登   录");
			}else if(password.equals("")){
				speakWords("请您输入密码");
				Toast.makeText(getApplicationContext(), "密码不能为空", 2000).show();
				mBtnLogin.setText("登   录");
			}
			else	//本地检测合法，发送登录请求
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
				startProgressDialog("正在登陆···"," IP:"+DefineVar.ServerIP,"端口:"+DefineVar.ServerPort);
			}
			//			}else{
			//				Toast.makeText(getApplicationContext(), "TCP连接有问题，请等待", 2000).show();
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
			//设置它的ContentView
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
							Toast.makeText(getApplicationContext(), "抱歉，IP和端口号不能为空！", Toast.LENGTH_LONG).show();
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
	//欢迎动画监听器
	class welcomeAnimationListener implements AnimationListener{

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			if (animation.hashCode() == toAppearAnimation.hashCode()){//逐渐呈现
				loginLinearLayout.setVisibility(View.VISIBLE);
				welcomeLayout.startAnimation(toDisappearAnimation);   
				titleText.startAnimation(animationSet);
			}else if (animation.hashCode() == toDisappearAnimation.hashCode()){//逐渐消失
				loginFrameLayout.bringChildToFront(loginLinearLayout);
				speakWords("欢迎进入艺术考试评分系统");
				speakWords("请您输入用户名和密码");
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

	//语音播报init监听器
	class readerOnInitListener implements OnInitListener{

		@Override
		public void onInit(int initStatus) {
			// TODO Auto-generated method stub
			//check for successful instantiation
			System.out.println("----调用顺序测试---readerOnInitListener----");
			if (initStatus == TextToSpeech.SUCCESS) {
				if(readerTTS.isLanguageAvailable(Locale.CHINA)==TextToSpeech.LANG_AVAILABLE){
					readerTTS.setLanguage(Locale.CHINA);
				}					
			}
			else if (initStatus == TextToSpeech.ERROR) {
				Toast.makeText(getApplicationContext(), "抱歉，语音播报失败！", Toast.LENGTH_LONG).show();
			}
		}

	}

	//语音TTS数据验证成功
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		System.out.println("----调用顺序测试---onActivityResult----");
		int setEngineResult;
		if (requestCode == DATA_CHECK_CODE) { 
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) { 
				readerTTS = new TextToSpeech(this, new readerOnInitListener()); 
				defaultEngineName = readerTTS.getDefaultEngine();	
				if (queryIFlyAudioPackage()) {//如果有讯飞语音包
					haveIflyPackage = true;
					System.out.println("----当前语音引擎----haveIflyPackage----"+haveIflyPackage);
					System.out.println("----当前语音引擎----有讯飞语音包----");
					if (!defaultEngineName.equals("com.iflytek.speechcloud")){
						System.out.println("----当前语音引擎----讯飞语音包不是默认的----"+defaultEngineName);
						notDefaultPackage = true;
						System.out.println("----当前语音引擎----notDefaultPackage----"+notDefaultPackage);
						setEngineResult = readerTTS.setEngineByPackageName("com.iflytek.speechcloud");
						if (setEngineResult == 0){ //设置语音引擎成功
							selectIflyPackage = true;
							System.out.println("----当前语音引擎----selectIflyPackage----"+selectIflyPackage);
						}						
					}
					else{
						selectIflyPackage = true;
						System.out.println("----当前语音引擎----selectIflyPackage----"+selectIflyPackage);
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

	//播报语音
	private void speakWords(String speech) {
		System.out.println("---当前语音引擎---speakWords---");
		if (haveIflyPackage){
			System.out.println("---当前语音引擎---speakWords---"+haveIflyPackage);
			if (selectIflyPackage){
				System.out.println("---当前语音引擎---speakWords---"+selectIflyPackage);
				readerTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
			}else{
				mWarningText.setText("建议“讯飞语音+”设置为系统首选语音引擎");
			}
			if (notDefaultPackage){
				mWarningText.setText("建议“讯飞语音+”设置为系统首选语音引擎");
				//Toast.makeText(getApplicationContext(), "请将“讯飞语音+”设置为系统首选语音引擎", 2000).show();
				System.out.println("---当前语音引擎---speakWords---"+notDefaultPackage);
			}

		}else {
			mWarningText.setText("该设备不支持中文语音播报，建议先安装“讯飞语音+”应用软件");
			//Toast.makeText(getApplicationContext(), "该设备不支持中文语音播报，建议先安装“讯飞语音+”应用软件", 2000).show();
			System.out.println("----建议安装讯飞语音");
		}
	}
	//查询系统本地是否已安装讯飞语音包
	private boolean queryIFlyAudioPackage() {
		engineList = readerTTS.getEngines();
		if (engineList.size() > 0){//若本地有语音包，则查询是否有讯飞语音包
			for (EngineInfo engine: engineList){//遍历engineList
				System.out.println("----遍历讯飞包---");
				System.out.println("----遍历讯飞包名字---"+engine.name);
				if (engine.name.equals("com.iflytek.speechcloud")){
					return true;
				}
			}
		}else {//若本地没有任何语音包，则返回false
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
	//MD5加密
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
		{ //计时结束,时间到，回到主页面
			// TODO Auto-generated method stub
			//repeatTimes++;
			SendInfoToServer(sendStr);
			timer.start();
			//			if(repeatTimes==FrameFormat.MAXRECONNTIMES){
			//				//告诉LoginAcitivity，LOGIN命令发了5次，仍没有回应,则提示登录超时，但仍会继续发送数据，每5次提醒一次
			//				//Toast.makeText(getApplicationContext(), "登陆超时", 2000).show();
			//				//将进度条消除
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
					macSerial = str.trim();// 去空格
					break;
				}
			}
		} catch (IOException ex) {
			// 赋予默认值
			ex.printStackTrace();
		}
		return macSerial;
	}
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

	/****得到科目英文****/
	public String getSubjectEnglish(String subject)
	{
		if(subject.equals("钢琴"))
			return FrameFormat.pianoStr;
		if(subject.equals("舞蹈"))
			return FrameFormat.danceStr;
		if(subject.equals("声乐"))
			return FrameFormat.vocalStr;
		else 
			return FrameFormat.othersStr;
	}/****得到考试阶段英文****/
	public String getExamStyleEnglish(String examStyle)
	{
		if(examStyle.equals("初试"))
			return FrameFormat.firstStr;
		if(examStyle.equals("复试"))
			return FrameFormat.twiceStr;
		if(examStyle.equals("三试"))
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
