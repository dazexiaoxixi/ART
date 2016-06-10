package com.example.artcheckin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.imagehandle.GridViewAdapter;
import com.example.imagehandle_new.ImageLoader;
import com.example.database.DbManager;
import com.example.database.DefineVar;
import com.example.database.ExamnieeInfo;
import com.example.network.SocketConnectThread;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ArtCheckIn_Main extends Activity {
	private static final String LOG_TAG = "ArtCheckIn_Main";
	//控件变量定义
	private TextView mExamRoom;
	//	private TextView mHavaExamedStudentText;
	private TextView mToExamStudentText;
	private TextView mcurExamStudentText;
	private TextView mExamStyle;
	private TextView mExamAddress;
	private TextView mStartExamTimeText;
	private TextView mCheckInWorker;    //检录员
	private Button BtnStopExam;
	private ListView examGroupList;
	private GridView examineeGrid;
	private View lastClickView = null;  //上一个选中的组
	private View lastExamedStd = null;  //上一个考完的考生
	private CustomProgressDialog progressDialog = null;
	private AutoScrollTextView noticeTextView;
	private CustomDialog dialogInfo = null;

	//考试信息变量
	private String userNameStr;
	private String userPswStr;
	private String subjectNumStr;       //科目
	private String roomNumStr;		//考场号
	private String examAddress;
	private String examStyle;
	private String userRemarkNameStr;
	
	private boolean isWarnDialogShowing = false;
	//	private boolean isConfirmDialogShowing = false;
	//Handler变量
	private Handler mHandler = null;  //接收其他线程发送过来的数据
	private Handler mDialogHandler = null; 
	//数据库变量
	private DbManager mDbManager=null;
	private DbManager mSocketDbManager = null;
	//图片加载变量
	private ImageLoader imageLoader;
	//控制变量
	private String choosedExamnieeIDStr ;   //当前选中的考生号
	private int curExamingStudentID =0; // 当前正在考试的考生号
	private int choosedGroupID = 0;   //当前选中的组 
	private int curExamingGroupID = 0;  //当前正在考试组
	private int absentNumInGroup = 0;  //缺考学生数
	private int Examed[] = new int[FrameFormat.maxStudentPerGroup];
	ArrayList<String> groupNumSet = new ArrayList<String>();   //存储已收到的组号
	//LinkedHashSet<String> groupNumSet = new LinkedHashSet<String>();   //存储已收到的组号
	private int AbsentIndex = 0;
	private boolean isCall = false;
	//网络变量
	private SocketConnectThread mConnectThread =null;  
	private PrintWriter out = null;
	private String sendStr;
	private String sendPara ;   //发送的参数
	private Count_Timer timer = null;
	
	private Timer tickTimer;
	private TimerTask task_tickTimer;
//	private TickAnswer_Timer tickAnswertimer;
	private int repeatTimes = 0;
	private boolean isWaitingStartACK = false;
	private boolean isWaitingStopACK = false;
	//private boolean isWaitingLoseACK = false;
	private boolean isWaitingACK = false;  //控制gridview和listview是否响应点击的标志变量
	private boolean stillWaitStopACK = false;
	private int waitingACKPosition = 0;//记录当前正在等在ACK的考生位置
	//Adapter定义
	private ArtListAdapter<String> ExamGroupAdapter;
	private GridViewAdapter gridViewAdapter ;
	private List<Integer> absentStudentIDList =new ArrayList<Integer>();
	//	private boolean flag_lastStudentExam = false;
	//按两次退出程序
	private static Boolean isQuit = false;
	Timer timer1 = new Timer();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //设置无标题显示  
		setContentView(R.layout.activity_main);
		initWidges();
		
		//ListView初始化
		initExamGroupListView();
		//GridView初始化
		initExamnieeGridView();
		imageLoader=new ImageLoader(getBaseContext());
		mSocketDbManager=new DbManager(getBaseContext(),"Socket");
		getIntentData();
		initExamedByte();//放到getIntentData()后面，因为getIntentData()中对Examed[]进行了操作

		//启动公告滚动条
		noticeTextView = (AutoScrollTextView)findViewById(R.id.TextViewNotice);
		noticeTextView.init(getWindowManager());
		noticeTextView.startScroll();


		//消息处理
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				System.out.println("--->>MainActivity进入handleMessage函数");
				// 显示接收到的内容
				if (msg.what == FrameFormat.HANDLE_UPDATEINFO) {
					System.out.println("--->>MainActivity收到handler HANDLE_UPDATEINFO数据");
					//更新已考人数和待考人数
					int tempHavaExamedNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
					int tempToExamNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
					//mHavaExamedStudentText.setText(tempHavaExamedNum+"");
					mToExamStudentText.setText(tempToExamNum+"");
					//更新listview
					LinkedHashSet<String> groupSet =  mDbManager.queryGroupSetAccordingTime(FrameFormat.STATUS_NOTEXAMEDOREXAMING);
					String tempGroupID = null;
					groupNumSet.clear();
					ExamGroupAdapter.clear();
					Iterator<String> it = groupSet.iterator();
					while(it.hasNext()) {
						tempGroupID = it.next();
						System.out.println("-----"+tempGroupID);
						groupNumSet.add(tempGroupID);
						ExamGroupAdapter.add(tempGroupID); // 每次都插入第一行
					}
					ExamnieeInfo examnieeInfo = mDbManager.queryStudentInfoAccordingTime(FrameFormat.STATUS_EXAMING);
					if(curExamingGroupID ==0){
						//当前没有正在考试的组
						if(examnieeInfo.getExamnieeID()!=null){
							//查询数据库后，有正在考试的组
							curExamingGroupID = Integer.valueOf(examnieeInfo.getGroupID());
							curExamingStudentID = Integer.valueOf(examnieeInfo.getExamnieeID());
							mcurExamStudentText.setText(curExamingGroupID+"组"+curExamingStudentID+"号");
							int choosedItemPosition = ExamGroupAdapter.getPosition(curExamingGroupID+"");
							examGroupList.setSelection(choosedItemPosition);
							ExamGroupAdapter.setSelectItem(choosedItemPosition); 
							ExamGroupAdapter.notifyDataSetInvalidated(); 
							choosedGroupID = curExamingGroupID;
							String mStartTime = mDbManager.queryStartTimeOfFirstStartNew(curExamingGroupID+"");
							if(mStartTime!=null){
								String mStartTimeTemp[] = mStartTime.split(" ");
								if(mStartTimeTemp.length>1){
									String mStartTimeTemp2[] = mStartTimeTemp[1].split(":");
									if(mStartTimeTemp2.length>2){
										mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
									}else{
										mStartExamTimeText.setText("00:00");
									}
								}
								else {
									String mStartTimeTemp2[] = mStartTime.split(":");
									if(mStartTimeTemp2.length>2){
										mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
									}else{
										mStartExamTimeText.setText("00:00");
									}
								}
							}
							//然后更新gridView
							updateGroupInfoInGridViewFunc(choosedGroupID);
						}else{
							//查询数据库后，确实没有正在考试的组
							if(choosedGroupID==0){
								//更新选中状态
								if (ExamGroupAdapter.getCount() > 0){
									examGroupList.setSelection(0);
									ExamGroupAdapter.setSelectItem(0);
									ExamGroupAdapter.notifyDataSetInvalidated();
									choosedGroupID = Integer.valueOf(ExamGroupAdapter.getItem(0));
									//然后更新gridView
									updateGroupInfoInGridViewFunc(choosedGroupID);
								}

							}else{
								updateGroupInfoInGridViewFunc(choosedGroupID);
							}
						}
					}else{
						//当前有正在考试的组
						int choosedItemPosition = ExamGroupAdapter.getPosition(curExamingGroupID+"");
						examGroupList.setSelection(choosedItemPosition);
						ExamGroupAdapter.setSelectItem(choosedItemPosition); 
						ExamGroupAdapter.notifyDataSetInvalidated(); 
						choosedGroupID = curExamingGroupID;
						//更新当前考生
						//	ExamnieeInfo examnieeInfo = mDbManager.queryStudentInfoAccordingTime(FrameFormat.STATUS_EXAMING);
						if(examnieeInfo.getExamnieeID()!=null){
							curExamingStudentID = Integer.valueOf(examnieeInfo.getExamnieeID());
							mcurExamStudentText.setText(curExamingGroupID+"组"+curExamingStudentID+"号");
						}
						//更新开考时间
						String mStartTime = mDbManager.queryStartTimeOfFirstStartNew(curExamingGroupID+"");
						if(mStartTime!=null){
							String mStartTimeTemp[] = mStartTime.split(" ");
							if(mStartTimeTemp.length>1){
								String mStartTimeTemp2[] = mStartTimeTemp[1].split(":");
								if(mStartTimeTemp2.length>2){
									mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
								}else{
									mStartExamTimeText.setText("00:00");
								}
							}
							else {
								String mStartTimeTemp2[] = mStartTime.split(":");
								if(mStartTimeTemp2.length>2){
									mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
								}else{
									mStartExamTimeText.setText("00:00");
								}
							}
						}
						//然后更新gridView
						updateGroupInfoInGridViewFunc(choosedGroupID);
					}

					
				}
				//当界面处于MainAcitivity时，若断开连接，又重连成功，此时的handle发给了MainActivity，而不会发给LoginActivity，因此，在
				//这里也要处理此handle消息
				else if(msg.what == FrameFormat.HANDLE_SERVERSOCKETCONTIMEOUT||msg.what == FrameFormat.HANDLE_SERVERDISCONNECT)
				{
					startProgressDialog("正在连接网络···");
				}
				else if(msg.what == FrameFormat.HANDLE_SERVERSOCKETCONSUCCESS)
				{
					//stopProgressDialog();
					//sendStr = FrameFormat.COMMAND_CONNECT+"|"+getLocalMacAddress();
					sendStr = FrameFormat.COMMAND_CONNECT+"|"+getCPUSerial();
					SendInfoToServer(sendStr);
//					sendStr = FrameFormat.COMMAND_LOGIN+"|"+userNameStr+"|"+encryption(userPswStr)+"|"+"0";
//					SendInfoToServer(sendStr);
					//timer.start();
				}
				else if(msg.what == FrameFormat.HANDLE_CONNECTSUCCESS)
				{
//					tickAnswertimer.cancel();
					stopProgressDialog();
					isCall = false;//清除线程已重启标志位
					sendStr = FrameFormat.COMMAND_LOGIN+"|"+userNameStr+"|"+encryption(userPswStr)+"|"+"0";
					SendInfoToServer(sendStr);
//					timer.start();
				}
				else if(msg.what == FrameFormat.HANDLE_LOGINFAIL)
				{
					System.out.println("---login 服务器拒绝");
					stopProgressDialog();
					//Toast.makeText(getApplicationContext(), failReason, 2000).show();
					if(timer!=null){
						timer.cancel();
					}
					if(mConnectThread!=null){
						mConnectThread.StopThread();
					}
					String failReason = (String)msg.obj;
					//跳转到登陆界面
					Intent intent = new Intent();
					intent.setClass(ArtCheckIn_Main.this, ArtCheckIn_Login.class);
					intent.putExtra("failReason", "服务器拒绝此设备登陆");
					startActivity(intent);
					finish();
				}
				else if(msg.what == FrameFormat.HANDLE_LOGINSUCCESS)	//登录成功
				{
					//msg.obj = subject + "," + roomNum + "," + userRemarkName + ","
					//		+ examPlace + "," + examStage;
//					tickAnswertimer.cancel();
					stopProgressDialog();
					isCall = false;//清除线程已重启标志位

				}
				else if(msg.what == FrameFormat.HANDLE_STRATEXAMOK){
					String recStr = (String)(msg.obj);

					System.out.println("--->>>>>HANDLE_STRATEXAMOK收到recStr="+recStr);
					System.out.println("--->>>>>HANDLE_STRATEXAMOK发送的sendPara="+sendPara);
					if(recStr.equals(sendPara)){
						System.out.println("--->>MainActivity收到同意考生开始考试");
						sendPara = "";
						timer.cancel();
						isWaitingStartACK = false;
						isWaitingACK = false; 
						//						repeatTimes = 0;
						//成功开始开始后才关闭dialog
						Message dialogMsg = mDialogHandler.obtainMessage();
						dialogMsg.what = FrameFormat.STARTOK;
						dialogMsg.sendToTarget();
						String str[] = recStr.split("\\|");
						//	subjectNumStr = str[0];
						//	roomNumStr = str[1];			
						String groupIDStr = str[2];
						String mStudentNumStr = str[3];
						int mGroupID= Integer.valueOf(groupIDStr);
						int mStudentNum = Integer.valueOf(mStudentNumStr);
						Toast.makeText(getApplicationContext(),
								"某一考生开始考试:" + mStudentNum, 2000).show();
						Examed[mStudentNum-1] = FrameFormat.STATUS_EXAMING;
						mcurExamStudentText.setText(groupIDStr + "组"
								+ mStudentNumStr + "号");
						//更新当前正在考试的考试的考试状态。
						//mDbManager.updateExamStatus(groupIDStr, mStudentNumStr, FrameFormat.STATUS_EXAMING+"");
						mDbManager.updateStartExamTime(groupIDStr, mStudentNumStr,getCurrentTime());
						int  havaExamedStudentNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
						int toBeExamedStudentNum= mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
						//mHavaExamedStudentText.setText(havaExamedStudentNum+"");
						mToExamStudentText.setText(toBeExamedStudentNum
								+ "");
						Log.d(LOG_TAG, "!!!!!!!!!!!已考人数加1，已考人数="+havaExamedStudentNum+" 待考人数="+toBeExamedStudentNum);

						//若开始考试的本考生所在的组不等于当前考试组，则表明该考生为新的一组的第一个考生，上一组结束了
						//否则，表示本组的上一个考生考试结束了，该考生考试考试
						if(0==curExamingGroupID){
							//	numberInGroup = mDbManager.queryExamnieeNumInGroup(groupIDStr);
							//updateExamnieeStatusFunc(mGroupID);
							//更新开考时间
							String mStartTime = getCurrentTime();
							if(mStartTime!=null){
								String mStartTimeTemp[] = mStartTime.split(" ");
								if(mStartTimeTemp.length>1){
									String mStartTimeTemp2[] = mStartTimeTemp[1].split(":");
									if(mStartTimeTemp2.length>2){
										mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
									}else{
										mStartExamTimeText.setText("00:00");
									}
								}
								else {
									String mStartTimeTemp2[] = mStartTime.split(":");
									if(mStartTimeTemp2.length>2){
										mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
									}else{
										mStartExamTimeText.setText("00:00");
									}
								}
							}
							////	mDbManager.updateIsFirstStart(groupIDStr, mStudentNumStr, FrameFormat.FLAG_ISFIRSTSTART);
						}
						curExamingStudentID = mStudentNum;
						curExamingGroupID = mGroupID;
					}else{
						Log.d(LOG_TAG, "--->>>>>不是所发送的同意考生开始考试对应的回复");
					}
				}
				else if(msg.what == FrameFormat.HANDLE_STRATEXAMNO){
					System.out.println("--->>MainActivity收到拒绝考生开始考试");
					if(timer!=null){
						timer.cancel();
					}
					isWaitingStartACK = false;
					isWaitingACK = false; 
					//					repeatTimes = 0;
					String failReason = (String)msg.obj;
					//Toast.makeText(getApplicationContext(), failReason, 2000).show();
					//					showWarningDialog(failReason);
					//服务器决绝考生开考后关闭dialog，弹出拒绝原因
					Message dialogMsg = mDialogHandler.obtainMessage();
					dialogMsg.what = FrameFormat.STARTNO;
					dialogMsg.obj = failReason;
					dialogMsg.sendToTarget();
				}
				else if(msg.what == FrameFormat.HANDLE_STOPEXAMOK){
					String recStr = (String)(msg.obj);
					System.out.println("--->>>>>HANDLE_STOPEXAMACK收到recStr="+recStr);
					System.out.println("--->>>>>HANDLE_STOPEXAMACK发送的sendPara="+sendPara);
					if(recStr.equals(sendPara)){
						System.out.println("--->>MainActivity收到某一考生结束考试回复");
						sendPara = "";
						if(timer!=null){
							timer.cancel();
						}
						isWaitingStopACK = false;
						stillWaitStopACK = false;
						//						repeatTimes = 0;

						String str[] = recStr.split("\\|");
						//int  StopExamStudentId = Integer.valueOf(str[3]);
						String StopExamStudentGroupId = str[2];
						String StopExamStudentId = str[3];
						
						mDbManager.updateEndExamTime(StopExamStudentGroupId, StopExamStudentId,getCurrentTime());
						System.out.println("--->>MainActivity收到最后一个考生结束考试回复");
						
							System.out.println("--->>缺考人数=0,收到END_ACK后就执行结束本组考试的函数");
							OneGroupStopExamConfirmedFunc(Integer.valueOf(StopExamStudentGroupId));
					}else{
						Log.d(LOG_TAG, "--->>>>>不是所发送的结束考试对应的回复");
					}
				}
				else if (msg.what == FrameFormat.HANDLE_STOPEXAMNO){
					System.out.println("--->>MainActivity收到结束考试回复:");
					if(timer!=null){
						timer.cancel();
					}
					isWaitingStopACK = false;
					stillWaitStopACK = false;
					//					repeatTimes = 0;
					String failReason = (String)msg.obj;
					showWarningDialog(failReason);
				}
				
				else if(msg.what == FrameFormat.HANDLE_ABSENTOK){
					String recStr = (String)(msg.obj);
					System.out.println("--->>>>>HANDLE_ABSENTACK收到recStr="+recStr);
					System.out.println("--->>>>>HANDLE_ABSENTACK发送的sendPara="+sendPara);
					if(recStr.equals(sendPara)){
						System.out.println("--->>MainActivity收到缺考信息回复");
						if(timer!=null){
							timer.cancel();
						}
						String str[] = recStr.split("\\|");
						String AbsentStudentGroupId = str[2];
						String AbsentStudentId = str[3];
						if(AbsentIndex <absentNumInGroup){
							uploadAbsentInfoFunc(absentStudentIDList.get(AbsentIndex));
							AbsentIndex ++;
						}else{
							//结束本组考试的操作
							OneGroupStopExamConfirmedFunc(curExamingGroupID);
						}
					}else{
						Log.d(LOG_TAG, "--->>>>>不是所发送的缺考信息对应的回复");
					}
				}
				else if (msg.what == FrameFormat.HANDLE_ABSENTNO){
					System.out.println("--->>MainActivity收到缺考信息回复:");
					timer.cancel();
					//isWaitingLoseACK = false;
					//					repeatTimes = 0;
					String failReason = (String)msg.obj;
					showWarningDialog(failReason);
				}
				else if (msg.what == FrameFormat.HANDLE_INFO){
					String recStr = (String)msg.obj;
					System.out.println("--->>MainActivity收到系统消息:"+recStr);
					noticeTextView.setText(recStr);
					noticeTextView.init(getWindowManager());
				}
			}
		};
		mConnectThread = SocketConnectThread.getSocketSingleInstance(mHandler,getBaseContext()); 
		Message msg = new Message();
		msg.what = FrameFormat.HANDLE_UPDATEINFO;
		mHandler.sendMessage(msg);

	}
	private void initWidges() {
		mExamRoom = (TextView)findViewById(R.id.testRoomNum);
		//		mHavaExamedStudentText=(TextView)findViewById(R.id.havaExamedStudent);
		mToExamStudentText=(TextView)findViewById(R.id.toExamStudent);
		mExamAddress = (TextView)findViewById(R.id.classroomNum);
		mCheckInWorker=(TextView)findViewById(R.id.clerkNum);
		mExamStyle = (TextView)findViewById(R.id.testType);
		mcurExamStudentText = (TextView)findViewById(R.id.curExamStudent);
		mStartExamTimeText =(TextView)findViewById(R.id.startTime);
		examGroupList = (ListView)findViewById(R.id.examGroupList);
		examineeGrid = (GridView) findViewById(R.id.examineeList);  
		BtnStopExam  = (Button)findViewById(R.id.Btn_StopExam);
		OnItemClickListener itemClickListener = new ItemClickListener();
		examGroupList.setOnItemClickListener(itemClickListener);
		OnClickListener mStopExamClickListener = new StopExamClickListener();
		BtnStopExam.setOnClickListener(mStopExamClickListener);
		OnItemClickListener mGridViewOnItemClickListener = new GridViewOnItemClickListener();
		examineeGrid.setOnItemClickListener(mGridViewOnItemClickListener);
		timer = new Count_Timer(20000,1000);//20s
//		tickAnswertimer = new TickAnswer_Timer(15000, 1000);
//		tickTimer = new Timer();  
//		task_tickTimer = new TimerTask() {  
//			public void run() {  
//				runOnUiThread(new Runnable() {  
//					public void run() {  
//						DefineVar.tickN++;
//						System.out.println("-----tickTimer;define_var.tickN="+DefineVar.tickN);
//						if(DefineVar.tickN==50)
//						{
//							DefineVar.tickN = 0;
//							sendStr = FrameFormat.COMMAND_CONNECT+"|"+getCPUSerial();
//							SendInfoToServer(sendStr);
//							sendStr = FrameFormat.COMMAND_LOGIN+"|"+userNameStr+"|"+encryption(userPswStr)+"|"+"0";
//							SendInfoToServer(sendStr);
//							tickAnswertimer.start();
//						}
//					}  
//				});  
//			}  
//		};  
//		tickTimer.schedule(task_tickTimer, 1000, 1000); 
	}
	public DbManager getmDbManager() {
		return mDbManager;
	}
	class ItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			if(stillWaitStopACK){
				showWarningDialog("已申请结束本组考试，不能进行其他操作");
			}else if(isWaitingACK){
				showWarningDialog("本组有考生正在等待考试认证，请在本组考试结束后查看其它待考组信息，谢谢！");
			}else{
				final TextView testView = (TextView) arg1
						.findViewById(R.id.choosedGroupId);
				choosedGroupID = Integer.valueOf(testView.getText().toString());
				//有组正在考试，且用户欲选非当前考试组时，弹窗提示**组正在考试
				if(curExamingGroupID != 0){
					if(choosedGroupID != curExamingGroupID){
						//弹窗提示**组正在考试
						choosedGroupID = curExamingGroupID;  //恢复choosedGroupID的值
						showWarningDialog("第"+curExamingGroupID+"组正在考试，请在本组考试结束后查看其它待考组信息，谢谢！");
					}else{
						//选择的仍为当前考试组，则不做任何处理
					}
				}else{  //curExamingGroupID==0，表示当前没有正在考试组，用户可以任意查看待考组
					//显示选中效果
					ExamGroupAdapter.setSelectItem(position);  
					ExamGroupAdapter.notifyDataSetInvalidated();  
					lastClickView = arg1;
					System.out.println("-----hyz-----选中listview的item-----"+ExamGroupAdapter.getItem(position));
					//	          adapter.notifyDataSetChanged(); 
					updateGroupInfoInGridViewFunc(choosedGroupID);
				}
			}			
		}
	}
	class StopExamClickListener implements OnClickListener{
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			//BtnStopExam.setEnabled(false);
			if(!isWaitingStartACK&&!isWaitingStopACK&&!isWaitingACK){
				OneGroupStopExamFunc(curExamingGroupID,curExamingStudentID);
			}else if(isWaitingStopACK==true){
				showWarningDialog("正在结束本组考试，请稍候");
			}
			else {
				showWarningDialog("对不起，第"+(waitingACKPosition+1)+"号考生未认证完，请稍候");
			}
		}
	}
	class GridViewOnItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			View cur_view = examineeGrid.getChildAt(position);
			if (cur_view != null) {

				float gridViewAlpha = cur_view.getAlpha();
				System.out.println("--->>点击gridview的第"+position+"项的图片透明度"+gridViewAlpha);
				if (gridViewAlpha == 1.0f){
					if(!isWaitingStartACK&&!isWaitingStopACK&&!stillWaitStopACK&&!isWaitingACK){
						//					if(!isWaitingStopACK&&!isWaitingLoseACK){
						ConfirmExamnieeInfoFunc(position);
					}else if(isWaitingACK && (position == waitingACKPosition)){
						//正在等在ACK的考生可以再次打开他对应的确认考生信息窗口
						ConfirmExamnieeInfoFunc(position);												
					}else if(isWaitingStopACK==true){
						showWarningDialog("对不起，正在结束本组考试，不能进行此操作");
					}else if(stillWaitStopACK){
						showWarningDialog("已申请结束本组考试，不能进行其他操作");
					}else{
						showWarningDialog("对不起，第"+(waitingACKPosition+1)+"号考生未认证完，请稍候");
					}
				}
			}

		}

	}
	//初始化考场信息和考官信息
	private void getIntentData() {
		System.out.println("------getIntentData");
		Intent intent =this.getIntent();	//登陆界面传过来的值
		userNameStr=intent.getStringExtra("userNameStr");
		userPswStr=intent.getStringExtra("userPswStr");
		subjectNumStr = intent.getStringExtra("subjectNumStr");
		roomNumStr = intent.getStringExtra("roomNumStr");
		examAddress= intent.getStringExtra("examAddress");
		examStyle = intent.getStringExtra("examStyleStr");
		userRemarkNameStr = intent.getStringExtra("userRemarkNameStr");
		//数据库名字为dance1-first.db
		System.out.println("------获得数据");
		DefineVar.databaseName =getSubjectEnglish(subjectNumStr)+ roomNumStr+"-"+getExamStyleEnglish(examStyle)+".db";
		//DefineVar.databaseName =subjectNumStr+ roomNumStr+"-"+examStyle+".db";
		mDbManager=new DbManager(this);

		//自己给自己handler一次，避免saveDataToDatabaseThread在跳转到mainActivity之前就已经handle完了和至少handle一次消息

		System.out.println("--!!!MainActivity插入的数据库名字:"+DefineVar.databaseName);
		mSocketDbManager.insertDatabaseName(DefineVar.databaseName);
		//mExamRoom.setText(getSubjectChinese(subjectNumStr)+roomNumStr+"考场");
		mExamRoom.setText(subjectNumStr+roomNumStr+"考场");
		mExamAddress.setText(examAddress);
		mCheckInWorker.setText(userRemarkNameStr);
		//		mSubmitRadioText.setText(submitedTeacherNum+"/"+teacherNumInRoom);
		//mExamStyle.setText(getExamStyleChinese(examStyle));
		mExamStyle.setText(examStyle);
		gridViewAdapter.notifyDataSetChanged();

		int tempHavaExamedNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
		int tempToExamNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
		//mHavaExamedStudentText.setText(tempHavaExamedNum+"");
		mToExamStudentText.setText(tempToExamNum+"");
	}
	private void initMainActivity(){
		//若数据库中未考人数大于0，或者有正在考试的人，则需要恢复
		if(mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED)>0
				||mDbManager.queryNumAccordingTime(FrameFormat.STATUS_EXAMING)>0){
			//数据库中有数据，需要恢复
			recovFromDatabase();
		}
		else{
			System.out.println("---%%%数据库中没有待考数据，不需要恢复");
		}
	}
	private void recovFromDatabase(){
		Log.i(LOG_TAG, "---%%%数据库中有数据，进入recovFromDatabase，回复阶段。。。");
		LinkedHashSet<String> groupSet = new LinkedHashSet<String>();

		int totalStudentNum = mDbManager.queryExamnieeNumInTable();
		Log.d(LOG_TAG, "!!!!!!!!!!!总人数="+totalStudentNum);
		ExamnieeInfo examnieeInfo = mDbManager.queryStudentInfoAccordingTime(FrameFormat.STATUS_EXAMING);
		//通过查询数据库，查到正在考试的组,和考生号
		if(examnieeInfo!=null&&examnieeInfo.getGroupID()!=null)
		{
			//表示有正在考试的人
			Log.i(LOG_TAG, "---%%%数据库有正在考试的人");
			curExamingGroupID = Integer.valueOf(examnieeInfo.getGroupID());
			curExamingStudentID = Integer.valueOf(examnieeInfo.getExamnieeID());
			mcurExamStudentText.setText(curExamingGroupID+"组"+curExamingStudentID+"号");
			String mStartTime = mDbManager.queryStartTimeOfFirstStartNew(curExamingGroupID+"");
			if(mStartTime!=null){
				String mStartTimeTemp[] = mStartTime.split(" ");
				if(mStartTimeTemp.length>1){
					String mStartTimeTemp2[] = mStartTimeTemp[1].split(":");
					if(mStartTimeTemp2.length>2){
						mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
					}else{
						mStartExamTimeText.setText("00:00");
					}
				}
				else {
					String mStartTimeTemp2[] = mStartTime.split(":");
					if(mStartTimeTemp2.length>2){
						mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
					}else{
						mStartExamTimeText.setText("00:00");
					}
				}
			}
		}
		else{
			//若数据库没有正在考试的人,则查找看是否有正在考试的组
			//------lt------xiugai  2.6日晚上10点----start---
			//	ExamnieeInfo tempExamnieeInfo = mDbManager.queryExamnieeInfoOfFirstStart(FrameFormat.FLAG_ISFIRSTSTART);
			Log.i(LOG_TAG, "---%%%查询数据库有正在考试的组");
			String getTempGroupID= mDbManager.queryExamingGroupAccordingTime();
			if(getTempGroupID!=null){
				Log.i(LOG_TAG, "---%%%数据库有正在考试的组");
				curExamingGroupID = Integer.valueOf(getTempGroupID);
				mcurExamStudentText.setText(curExamingGroupID+"组"+"0号");
				String mStartTime = mDbManager.queryStartTimeOfFirstStartNew(curExamingGroupID+"");
				if(mStartTime!=null){
					String mStartTimeTemp[] = mStartTime.split(" ");
					if(mStartTimeTemp.length>1){
						String mStartTimeTemp2[] = mStartTimeTemp[1].split(":");
						if(mStartTimeTemp2.length>2){
							mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
						}else{
							mStartExamTimeText.setText("00:00");
						}
					}
					else {
						String mStartTimeTemp2[] = mStartTime.split(":");
						if(mStartTimeTemp2.length>2){
							mStartExamTimeText.setText(mStartTimeTemp2[0]+":"+mStartTimeTemp2[1]);
						}else{
							mStartExamTimeText.setText("00:00");
						}
					}
				}
			}
			//------lt------xiugai  2.6日晚上10点----end---
		}
		//listview的其他item都设置为不可按

		//将数据库中未考人的信息，显示在listview和gridview中
		groupSet = mDbManager.queryGroupSetAccordingTime(FrameFormat.STATUS_NOTEXAMEDOREXAMING);
		if(curExamingGroupID!=0){
			groupSet.add(curExamingGroupID+"");
		}
		Iterator<String> iterator = groupSet.iterator();
		//更新了listview
		recovUpdateListView(groupSet);
		Log.i(LOG_TAG, "--恢复数据库信息时，正在考试的组="+curExamingGroupID);
		if(curExamingGroupID!=0){
			//若有正在考试的组，则在gridview更新正在考试组的信息
			recovUdateGridView(curExamingGroupID);
		}else{
			//若没有正在考试的组，则获取gruopSet的第一个元素对应的组，更新此组的信息
			System.out.println("-----恢复数据库信息时，没有正在考试的组，获取的默认显示组=");
			recovUdateGridView(Integer.valueOf(iterator.next()));
		}
	}
	private void recovUpdateListView(LinkedHashSet<String> tempGroupSet){
		System.out.println("-----recovUpdateListView");
		String tempGroupID = null;
		//	groupNumArray.clear();
		Iterator<String> it = tempGroupSet.iterator();
		while(it.hasNext()) {
			tempGroupID = it.next();
			System.out.println("-----"+tempGroupID);
			ExamGroupAdapter.add(tempGroupID); // 每次都插入第一行
			groupNumSet.add(tempGroupID);
		}
		//恢复listview的选中状态
		if(curExamingGroupID!=0){
			int choosedItemPosition = ExamGroupAdapter.getPosition(curExamingGroupID+"");
			examGroupList.setSelection(choosedItemPosition);
			ExamGroupAdapter.setSelectItem(choosedItemPosition); 
			ExamGroupAdapter.notifyDataSetInvalidated(); 
			choosedGroupID = curExamingGroupID;
		}else{
			if (ExamGroupAdapter.getCount() > 0){
				examGroupList.setSelection(0);
				ExamGroupAdapter.setSelectItem(0);
				ExamGroupAdapter.notifyDataSetInvalidated(); 

				choosedGroupID = Integer.valueOf(ExamGroupAdapter.getItem(0));
			}
		} 
	}
	private void recovUdateGridView( final int tempGroupID){
		Log.i(LOG_TAG, "--恢复数据库信息，更新gridview的组="+tempGroupID);
		//final int examnieeNum = mDbManager.queryExamnieeNumInGroup(tempGroupID + "");
		//final int maxExamnieeID= mDbManager.queryMaxExamnieeIDInGroup(tempGroupID + "");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				for (int index = 1; index <= 10; index++) {
					Log.i(LOG_TAG, "-更新gridview的第几个item="+index);
					Log.i(LOG_TAG, "-更新的gridview="+examineeGrid);
					View cur_view = examineeGrid.getChildAt(index - 1);
					if (cur_view != null) {
						ImageView imageView = (ImageView) cur_view
								.findViewById(R.id.itemImage);
						TextView textView = (TextView) cur_view
								.findViewById(R.id.stdNum);
						Log.i(LOG_TAG, "-得到gridview的第几个item的view="+index);
						//	if (textView != null && imageView != null && textName != null) {   //需要的
						if (textView != null && imageView != null ) {
							//	if (index <= examnieeNum) {
							//	if (index <= maxExamnieeID) {
							Log.i(LOG_TAG, "----正在更新gridview的组号，序号:" + tempGroupID
									+ "examnieeID:" + index);
							ExamnieeInfo examnieeInfo = mDbManager.queryExamnieeInfoNew(tempGroupID
									+ "", index + "");
							if(examnieeInfo!=null && examnieeInfo.getExamnieeStatus()!=null){
								//此考生考试状态
								int mExamStatus = Integer.valueOf(examnieeInfo.getExamnieeStatus());
								//if (mName != null ) {
								textView.setText(String.valueOf(index));
								System.out.println("----该考生考试状态"+mExamStatus);
								//更新此考生的考试状态
								if(mExamStatus==FrameFormat.STATUS_ABSENT){
									System.out.println("----该考生缺考"+tempGroupID+"组"+index+"号");
									Examed[index-1] = FrameFormat.STATUS_ABSENT;
								}else if(mExamStatus==FrameFormat.STATUS_EXAMING){
									System.out.println("----该考生正在考试"+tempGroupID+"组"+index+"号");
									Examed[index-1] = FrameFormat.STATUS_EXAMING;
									cur_view.setAlpha(0.9f);
									//imageView.setAlpha(0.9f);
									cur_view.setBackgroundResource(R.drawable.current_examing_group_shape);
									lastExamedStd = cur_view;
								}else if(mExamStatus==FrameFormat.STATUS_HAVAEXAMED){
									System.out.println("----该考生已考试完"+tempGroupID+"组"+index+"号");
									Examed[index-1] = FrameFormat.STATUS_HAVAEXAMED;
									cur_view.setAlpha(0.5f);
									//imageView.setAlpha(0.5f);
								}else if(mExamStatus==FrameFormat.STATUS_NOTEXAMED){
									System.out.println("----该考生还未考试"+tempGroupID+"组"+index+"号");
									Examed[index-1] = FrameFormat.STATUS_NOTEXAMED;
								}

								Log.i(LOG_TAG, "-----<<<<<<<<“"+tempGroupID+"组"+index+"号:::"+"在恢复数据库信息时，recovUdateGridView函数中调用loadImage(PicURL)");
								//									loadImage(tempGroupID,examnieeInfo
								//											.getExamnieePicURL(),imageView);
								imageLoader.DisplayImage(examnieeInfo
										.getExamnieePicURL(), imageView); 
							}else{
								//若这一组序号比较小的学生信息还没收到

								Examed[index-1] = FrameFormat.STATUS_NOPERSON;
								imageView.setImageResource(R.drawable.default_lock);
								cur_view.setAlpha(1.0f);
								//imageView.setAlpha(1.0f);
								cur_view.setBackgroundResource(R.drawable.login_shape);
							}

							//							} else {
							//								//该位置没有考生
							//								imageLoader.DisplayImage(null, imageView); 
							//								Examed[index-1] = FrameFormat.STATUS_NOPERSON;
							//								textView.setText("");
							//							}
						}
					}else{
						Examed[index-1] = FrameFormat.STATUS_NOPERSON;
						Log.i(LOG_TAG, "---cur_view==null");
					}
				}
			}
		}, 1000);
	}
	private void initExamnieeGridView() {
		// TODO Auto-generated method stub 
		//		examineeGrid.setClickable(false);
		examineeGrid.setAdapter(gridViewAdapter=new GridViewAdapter(this,mHandler)); 
	}
	private void initExamGroupListView() {
		// TODO Auto-generated method stub
		ExamGroupAdapter = new ArtListAdapter<String>(this,R.layout.listview_item,R.id.choosedGroupId);
		examGroupList.setAdapter(ExamGroupAdapter);
		examGroupList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	/***初始化Examed***/
	private void initExamedByte() {
		// TODO Auto-generated method stub
		for(int i = 0;i <FrameFormat.maxStudentPerGroup;i++){
			Examed[i] = FrameFormat.STATUS_NOTEXAMED;
		}
	}
	//某一考生开始考试
	private void OneStudentStartExam(){
		if (ExamGroupAdapter.getCount() > 0){
			String tempGroupID =null;
			if(curExamingGroupID==0){
				if(choosedGroupID==0){
					//若没有选择组，那么点击的考生所在的组肯定是listview的第一个item对应的组号
					tempGroupID = ExamGroupAdapter.getItem(0)+"";
				}else{
					tempGroupID = choosedGroupID+"";
				}
			}
			else {
				tempGroupID= curExamingGroupID+"";
			}
			sendPara = subjectNumStr + "|"
					+ roomNumStr + "|" + tempGroupID + "|" + choosedExamnieeIDStr;
			sendStr = FrameFormat.COMMAND_BEGINEXAM + "|" + sendPara;
			SendInfoToServer(sendStr);
			timer.start();
			isWaitingStartACK = true;
		}
	}
	private void uploadAbsentInfoFunc(int absentStudentID){
		sendPara =subjectNumStr+"|"+roomNumStr+"|"+curExamingGroupID+"|"+absentStudentID;
		sendStr = FrameFormat.COMMAND_ABSENT+"|"+sendPara;
		SendInfoToServer(sendStr);
		timer.start();
	//	isWaitingLoseACK = true;
	}
	// 某一考生结束考试
	private void OneStudentStopExamFunc(int curStopExamGroupId,int curStopExamStudentId){
		Examed[Integer.valueOf(curStopExamStudentId)-1]=FrameFormat.STATUS_HAVAEXAMED;
		//	mDbManager.updateExamStatus(curStopExamGroupId, curStopExamStudentId, FrameFormat.STATUS_HAVAEXAMED+"");
		mDbManager.updateEndExamTime(curStopExamGroupId+"", curStopExamStudentId+"",getCurrentTime());
		int  havaExamedStudentNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
		int toBeExamedStudentNum= mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
		//mHavaExamedStudentText.setText(havaExamedStudentNum+"");
		mToExamStudentText.setText(toBeExamedStudentNum
				+ "");
		Log.d(LOG_TAG, "!!!!!!!!!!!已考人数加1，已考人数="+havaExamedStudentNum+" 待考人数="+toBeExamedStudentNum);

		OneStudentStartExam();
		//	}
	}
	//某组结束考试
	private void OneGroupStopExamFunc(final int curExamGroupId,final int curExamStudentId) {
//		Toast.makeText(getApplicationContext(),
//				curExamGroupId+"组结束考试" , 2000).show();		
		if(curExamingGroupID==0){
			showWarningDialog("对不起，当前没有正在进行考试的组");
		}else{			
			String message ="第"+curExamGroupId+"组以下考生缺考："+"\n"+"\n";
			int notExamedNum = 0;
			for(int i = 0;i <FrameFormat.maxStudentPerGroup;i++){
				System.out.println("-----examed="+i+":"+Examed[i]);
				if(Examed[i]==FrameFormat.STATUS_NOTEXAMED){
					ExamnieeInfo tempExamnieeInfo = mDbManager.queryExamnieeInfoNew(curExamGroupId+"", (i+1)+"");
					if(tempExamnieeInfo!=null){
						message = message + (i+1)+"号： "+ tempExamnieeInfo.getExamnieeName()+"\n";
					}
					notExamedNum ++;
				}
			}
			message = message+"\n"+"确认结束本组考试？";
			if(notExamedNum == 0){
				//若没有人缺考
				message = "确认结束本组考试？";
			}

		
			CustomDialog.Builder builder =
					new
					CustomDialog.Builder(ArtCheckIn_Main.this);
			builder.setTitle("确定结束考试？")
			.setIcon(R.drawable.tip)
			.setMessage(message)
			.setPositiveButton("确   定",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					//		flag_lastStudentExam = true;

					if(curExamingStudentID>0){
						//是为了解决问题：若某一组已经开始考试，2人已考完，然后点击第三人的通过，但是在没有收到ACK的时候就退出了程序，然后再进来，此时查到只有正在考试的组，
						//没有正在考试的人，此时若直接点击结束本组考试，则会发送END|钢琴|1|1|0(例子)，1组0号结束考试，此消息有误，不应该发送，所以加上了if条件。
						sendPara = subjectNumStr + "|"
								+ roomNumStr + "|" + curExamGroupId + "|" + curExamStudentId;
						sendStr = FrameFormat.COMMAND_STOPEXAM + "|" + sendPara;
						SendInfoToServer(sendStr);
						timer.start();
						isWaitingStopACK = true;
						stillWaitStopACK = true;
						//	OneStudentStopExamFunc(curExamingGroupID,curExamingStudentID);
					}else{
						if(absentStudentIDList.size()>0){
							System.out.println("--->>缺考人数大于0");
							uploadAbsentInfoFunc(absentStudentIDList.get(AbsentIndex));
							AbsentIndex ++;
						}else{
							System.out.println("--->>缺考人数=0,收到END_ACK后就执行结束本组考试的函数");
							OneGroupStopExamConfirmedFunc(Integer.valueOf(curExamingGroupID));
						}
					}
					//OneGroupStopExamConfirmedFunc(curExamGroupId);

					//					builder.dismissDialog();
					dialog.dismiss();//关闭对话框
				}
			});
			builder.setNegativeButton("返   回",  
					new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int whichButton) {  
					//					builder.dismissDialog();
					dialog.dismiss();
				}  
			}); 
			//			builder.create();
			//			builder.showDialog();
			CustomDialog dialog = builder.create();
			dialog.show();
		}


	}
	private void OneGroupStopExamConfirmedFunc(int curExamGroupId) {
		//最后一位考生结束考试
		Log.d(LOG_TAG, "!!!!!!!!!!!一组结束考试");
		//将结束的该组第一个开始考试的学生对于的isFirstStart设置为0；
		//mDbManager.updateFirstStartToInit(curExamingGroupID+"");

		updateDatabase(curExamGroupId);
		updateExamStatusText();
		updateListViewAfterOneGroupStopExam(curExamGroupId);
		deleteLocalPicAfterOneGroupStopExam();
		initDataAfterOneGroupStopExam();   //这一函数的执行不能在以上三个函数的前面，因为这一函数将值都初始化了
		updateGroupInfoInGridViewFunc(choosedGroupID);
	}
	private void updateDatabase(int _groupID){
		//一组结束考试，则删除该组的信息
		mDbManager.deleteHavaExamedExamnieeInfo(_groupID+"");
	}
	private void updateExamStatusText(){
		//mStartExamTimeText.setText("00:00:00");
		mStartExamTimeText.setText("00:00");
		mcurExamStudentText.setText("");
		//已考人数要加上缺考的
		int  havaExamedStudentNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
		int toBeExamedStudentNum= mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
		//	mHavaExamedStudentText.setText(havaExamedStudentNum+"");
		mToExamStudentText.setText(toBeExamedStudentNum
				+ "");
		Log.d(LOG_TAG, "!!!!!!!!!!!已考人数加缺考人数后，已考人数="+havaExamedStudentNum+" 待考人数="+toBeExamedStudentNum);
	}
	private void updateListViewAfterOneGroupStopExam(int deleteGroupID){

		int choosedItemPosition = ExamGroupAdapter.getPosition(deleteGroupID+"");
		if(choosedItemPosition>=0){
         //若listview中存在准备删除的组号deleteGroupID，则执行删除操作。

			View cur_ListItem = examGroupList.getChildAt(choosedItemPosition);
			if (cur_ListItem != null) {
				cur_ListItem.setBackgroundResource(R.drawable.list_item_selector);
			}

			// 结束考试，将listView选中组删除
			groupNumSet.remove(curExamingGroupID + "");
			ExamGroupAdapter.remove(curExamingGroupID + "");
			ExamGroupAdapter.notifyDataSetChanged();

			//若当前选中组被删除，则默认选中当前第一组 		 
			examGroupList.setSelection(0);
			ExamGroupAdapter.setSelectItem(0);   
			ExamGroupAdapter.notifyDataSetInvalidated();  


			if (ExamGroupAdapter.getCount() > 0){
				// 更新listview最上面item对应的组的gridview
				choosedGroupID = Integer.valueOf(ExamGroupAdapter.getItem(0));
				System.out.println("-----hyz-----选中listview的第一个-----"+choosedGroupID);
				//若被删除的那一组是原来listview的第一行对应的组，则
				//加入下面这一句if的原因是删除第一行之后，再次得到第一行的组号，总是被删除掉的那一行的组号，不知道为什么
				if(curExamingGroupID==choosedGroupID)
				{
					choosedGroupID = Integer.valueOf(ExamGroupAdapter.getItem(1));
					System.out.println("-----hyz-----选中listview的第一个-----"+choosedGroupID);
				}
			}else{
				choosedGroupID = 0;
			}
		}
	}
	private void deleteLocalPicAfterOneGroupStopExam(){
		Log.d(LOG_TAG, "----~~~~一组结束考试，删除文件夹   "+examGroupList.getCount());
		if(examGroupList.getCount()==0)
		{
			Log.d(LOG_TAG, "----~~~~一组结束考试，删除文件夹");
			String path = android.os.Environment.getExternalStorageDirectory()+File.separator+"ArtCheckIn";
			File fileDir = new File(path);   
			if(fileDir.exists()){
				if (fileDir.isDirectory())
				{
					// 声明目录下所有的文件 files[];
					File files[] = fileDir.listFiles();
					for (int i = 0; i < files.length; i++)
					{ // 遍历目录下所有的文件
						files[i].delete(); // 把每个文件 删除
					}
					//fileDir.delete();  //删除空文件夹
				}
				//fileDir.delete();
				Log.d(LOG_TAG, "----~~~~成功删除文件夹:"+path);
			}else{
				Log.d(LOG_TAG, "----~~~~文件夹不存在");
			}
		}
	}
	private void initDataAfterOneGroupStopExam(){
		curExamingGroupID =0;
		curExamingStudentID=0;
		//	haveExamedNumInGroup = 0;

		absentNumInGroup = 0;
		AbsentIndex = 0;
		absentStudentIDList.clear();
		//	flag_lastStudentExam = false;
		lastExamedStd = null;
		initExamedByte();

	}
	//点击图片，查看考生信息是否正确
	private void ConfirmExamnieeInfoFunc(final int position){
		final View cur_view = examineeGrid.getChildAt(position);
		ImageView imageView = null;
		TextView textView = null;
		//		TextView textName = null;
		if(cur_view!=null){
			imageView = (ImageView) cur_view.findViewById(R.id.itemImage);
			textView = (TextView) cur_view.findViewById(R.id.stdNum);
			//         textName = (TextView) cur_view.findViewById(R.id.stdName);
		}
		System.out.println("-----该item的考生号:"+textView.getText().toString());
		if(textView.getText().toString()!=null&&(!textView.getText().toString().equals(""))){  //若该item有考生信息

			choosedExamnieeIDStr= textView.getText().toString();

			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.student_information_dialog, 
					(ViewGroup)findViewById(R.id.stdInformationDialog));
			TextView examnieeID = (TextView)layout.findViewById(R.id.examnieeID);
			TextView examnieeName = (TextView)layout.findViewById(R.id.exmnieeName);
			TextView examnieeSex =  (TextView)layout.findViewById(R.id.exmnieeGender);
			TextView examnieeIdentity =  (TextView)layout.findViewById(R.id.exmnieeIdentity);
			ImageView examnieePic = (ImageView)layout.findViewById(R.id.examnieePic);
			//查询当前查看的学生的信息
			String tempGroupID ;
			if(curExamingGroupID==0){
				if(choosedGroupID==0){
					//若没有选择组，那么点击的考生所在的组肯定是listview的第一个item对应的组号
					tempGroupID = ExamGroupAdapter.getItem(0);
				}else{
					tempGroupID = choosedGroupID+"";
				}
			}
			else {
				tempGroupID= curExamingGroupID+"";
			}
			ExamnieeInfo tempExamnieeInfo = mDbManager.queryExamnieeInfoNew(tempGroupID+"", choosedExamnieeIDStr);

			if(tempExamnieeInfo!=null){
				if(tempExamnieeInfo.getExaminationID()!=null){
					//向dialog中的控件填充内容
					examnieeID.setText(tempExamnieeInfo.getExaminationID());
					examnieeName.setText(tempExamnieeInfo.getExamnieeName());
					examnieeSex.setText(tempExamnieeInfo.getExamnieeSex());
					examnieeIdentity.setText(tempExamnieeInfo.getExamnieeIdentity());
				}
			}
			//examieeIden
			examnieePic.setImageDrawable(imageView.getDrawable());
			//弹窗显示考生详细信息，并进行确认			
			CustomDialog.Builder  builderDialog= new CustomDialog.Builder(ArtCheckIn_Main.this);
			builderDialog.setIcon(R.drawable.std_icon)
			.setTitle((position+1)+"号考生信息")
			.setContentView(layout)
			.setPositiveButton("开   考",
					new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, int which) {
					//增加开考再次确认，以避免操作错误
					CustomDialog.Builder  confirmBuilder = new CustomDialog.Builder(ArtCheckIn_Main.this);
					confirmBuilder.setTitle("是否开考？")
					.setMessage((position+1)+"号考生开始考试？")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface confirmDialog, int which) {
							confirmDialog.dismiss();
							//设置申请考试标志位，成功收到任意类型ACK(即服务器响应)后清除标志位
							isWaitingACK = true; 
							waitingACKPosition = position;
							//成功开考后才关闭dialog
							mDialogHandler = new Handler(){
								@Override
								public void handleMessage(Message msg)
								{
									// 显示接收到的内容					
									if (msg.what == FrameFormat.STARTOK ) {
										//收到成功开考信息后关闭进度条和dialog
										stopProgressDialog();
										dialog.dismiss();
										dialogInfo = null;
										//										isConfirmDialogShowing = false;
										if (lastExamedStd != null) {
											lastExamedStd.setAlpha(0.5f);
											lastExamedStd.setBackgroundResource(R.drawable.login_shape);
										}
										//正在考试的考生griditem外框设置成绿色									
										cur_view.setAlpha(0.9f);
										cur_view.setBackgroundResource(R.drawable.current_examing_group_shape);
										lastExamedStd = cur_view;	
									}else if(msg.what == FrameFormat.STARTNO ){ 
										stopProgressDialog();
										//										builderDialog.dismissDialog();
										String failInfo = (String) msg.obj;
										showWarningDialog(failInfo);
										dialog.dismiss();
										dialogInfo = null;
										//										isConfirmDialogShowing = false;
										//										isDialogShowing = false;
									}else if(msg.what == FrameFormat.STARTNOACK ){
										stopProgressDialog();
										//										builderDialog.dismissDialog();
										showWarningDialog("考生开考认证超时，此考生未成功开始考试，请稍后重试");
									}
								}
							};
							if(curExamingStudentID==0){
								System.out.println("------当前没有人考试，一个人开始考试:"+curExamingGroupID+"组"+curExamingStudentID+"号");
								OneStudentStartExam();
							}
							else{
								System.out.println("------当前结束考试的人:"+curExamingGroupID+"组"+curExamingStudentID+"号");
								OneStudentStopExamFunc(curExamingGroupID,curExamingStudentID);
							}
							//点击“通过”后，就不能再点击“返回”
							//												builderDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
							startProgressDialog("等待服务器授权···");					
						}
					})
					.setNegativeButton("否", 
							new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface confirmDialog, int which) {
							confirmDialog.dismiss();
						}
					});
					CustomDialog confirmDialog = confirmBuilder.create();	
					confirmDialog.show();		
				}
			})
			.setNegativeButton("返   回", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					dialogInfo = null;
					//					isConfirmDialogShowing = false;
				}
			});		
			if(dialogInfo != null){
				dialogInfo.dismiss();
				dialogInfo = null;
			}
			dialogInfo = builderDialog.create();	
			dialogInfo.show();
			//			if (isConfirmDialogShowing == true){
			//				dialogInfo.dismiss();
			//				isConfirmDialogShowing = false;
			//			}

			//			isConfirmDialogShowing = true;

		}

	} 
	//从服务器处更新代考组信息，在列表中显示
	private void updateGroupInfoInListViewFunc(String _groupID,String _examnieeID){
		// 更新列表,若接收到的组号是新的，则加入listview显示
		boolean isExist = false;
		System.out.println("----listview中已有" + ExamGroupAdapter.getCount()
				+ "行");
		if (ExamGroupAdapter.getCount() > 0) {
			for (int index = 0; index < ExamGroupAdapter.getCount(); index++) {
				System.out.println("----listview中第:" + (index + 1) + "个元素:第"
						+ groupNumSet.get(index) + "组");
				if (_groupID.equals(groupNumSet.get(index))) {
					System.out
					.println("----listview中第:" + _groupID + "组已存在");
					isExist = true;
					break;
				} else {
					isExist = false;
					System.out
					.println("----listview中第:" + _groupID + "组不存在");
				}
			}
		}
		if (!isExist) {
			System.out.println("----listview加入一行:" + _groupID + "组");
			ExamGroupAdapter.add(_groupID); // 每次都插入第一行
			groupNumSet.add(_groupID);
		}

		//当用户没有选择待考组时，默认选定当前第一组
		if ((ExamGroupAdapter.getCount() > 0) && (lastClickView == null)){
			examGroupList.setSelection(0);
			ExamGroupAdapter.setSelectItem(0);  
			ExamGroupAdapter.notifyDataSetInvalidated();  
			//此处没有设置choosedGroupID，本来应该设置成选中的第一个item对应的组号的，
			//但是设置之后不能正确显示gridview中的考生信息(不显示任何信息)，
			//估计与saveExamnieeInfo()函数有关系
		}

		// 更新gridview??
		// 将待考组信息存入数据库中
		System.out.println("--->>MainActivity正要异步加载的图片对应的学生为:"
				+ _groupID+"组"+_examnieeID+"号");
		saveExamnieeInfo(_groupID);
	}
	//选中某一组，则在gridview更新该组的考生信息

	private void updateGroupInfoInGridViewFunc(int groupID){
		//int examnieeNum = mDbManager.queryExamnieeNumInGroup(groupID + "");
		initExamedByte();
		//int maxExamnieeID= mDbManager.queryMaxExamnieeIDInGroup(groupID + "");
		ExamnieeInfo examnieeInfo =  new ExamnieeInfo();
		for (int index = 1; index <= 10; index++) {
			View cur_view = examineeGrid.getChildAt(index - 1);
			if (cur_view != null) {
				Log.i(LOG_TAG, "-----<<<<<<<<gridview的cur_view != null");
				ImageView imageView = (ImageView) cur_view
						.findViewById(R.id.itemImage);
				TextView textView = (TextView) cur_view
						.findViewById(R.id.stdNum);
				if (textView != null && imageView != null ) {
					Log.i(LOG_TAG, "-----<<<<<<<<gridview的textView != null，imageView != null");
					//	if (index <= maxExamnieeID) {
					examnieeInfo = mDbManager.queryExamnieeInfoNew(groupID
							+ "", index + "");
					if(examnieeInfo!=null){ 
						if(examnieeInfo.getExamnieeStatus()!=null)
						{
							int mExamStatus = Integer.valueOf(examnieeInfo.getExamnieeStatus());
							textView.setText(String.valueOf(index));

							//								loadImage(groupID,examnieeInfo
							//										.getExamnieePicURL(),imageView);
							//								if(examnieeInfo
							//										.getExamnieePicURL()!=null){
							Log.i(LOG_TAG, "-----<<<<<<<<在gridview中显示图片："+groupID+"组"+index+"号:::");
							imageLoader.DisplayImage(examnieeInfo
									.getExamnieePicURL(), imageView); 
							//}

							if(mExamStatus==FrameFormat.STATUS_HAVAEXAMED){
								System.out.println("-----<<<<<<<<“"+groupID+"组"+index+"号:::已经考试"
										+examnieeInfo.getExamnieeStatus().equals(FrameFormat.STATUS_HAVAEXAMED+""));
								Examed[index-1] = FrameFormat.STATUS_HAVAEXAMED;
								cur_view.setBackgroundResource(R.drawable.login_shape);
								cur_view.setAlpha(0.5f);
								//imageView.setAlpha(0.5f);
							}else if(mExamStatus==FrameFormat.STATUS_EXAMING){
								System.out.println("-----<<<<<<<<“"+groupID+"组"+index+"号:::正在考试"
										+examnieeInfo.getExamnieeStatus().equals(FrameFormat.STATUS_EXAMING+""));
								Examed[index-1] = FrameFormat.STATUS_EXAMING;
								cur_view.setBackgroundResource(R.drawable.current_examing_group_shape);
								cur_view.setAlpha(0.9f);
								//imageView.setAlpha(0.9f);
								lastExamedStd = cur_view;
							}
							else if(mExamStatus==FrameFormat.STATUS_NOTEXAMED){
								Examed[index-1] = FrameFormat.STATUS_NOTEXAMED;
								cur_view.setAlpha(1.0f);
								//imageView.setAlpha(1.0f);
								cur_view.setBackgroundResource(R.drawable.login_shape);
							}
							System.out.println("------当前考试组="+curExamingGroupID+" 当前更新的组="+groupID);
						}else{
							//若小于最大考生号的位置没有学生
							Log.i(LOG_TAG, "-----<<<<<<<<小于最大考生号的位置没有学生，设置相应的显示");
							imageLoader.DisplayImage(null, imageView); 
							Examed[index-1] = FrameFormat.STATUS_NOPERSON;
							textView.setText("");
							cur_view.setBackgroundResource(R.drawable.login_shape);
							imageView.setImageResource(R.drawable.default_lock);
							cur_view.setAlpha(1.0f);
							//imageView.setAlpha(1.0f);
						}
					}
					//					} else {
					//						Log.i(LOG_TAG, "-----<<<<<<<<此位置无人，设置相应的显示");
					//						imageLoader.DisplayImage(null, imageView); 
					//						Examed[index-1] = FrameFormat.STATUS_NOPERSON;
					//						textView.setText("");
					//						//textName.setText("");
					//						cur_view.setBackgroundResource(R.drawable.login_shape);
					//						imageView.setImageResource(R.drawable.default_lock);
					//						cur_view.setAlpha(1.0f);
					//						//imageView.setAlpha(1.0f);
					//						// cur_view.setClickable(false);
					//						// System.out.println("------不可点击"+index+" gridview item");
					//						//isAbsentBox.setEnabled(false);
					//						//	isAbsentBox.setChecked(false);
					//					}
				}
			}
		}
	}
	public void saveExamnieeInfo(final String _groupID){
		// Bitmap bmp =GetNetBitmap(imageURL);
		//	if (bmp != null) {
		System.out.println("---->获取图片bitmap成功");
		//如果插入成功，则totalStudentNum++;
		//		insertExamnieeInfo(String groupID,String examnieeID,String startTime,String endTime,String examinationID,
		//				String examnieeName,String examnieeSex,String examnieePicURL)
		//		if (mDbManager.insertExamnieeInfo(RecGroupNumStr, RecStudentNumStr,
		//				RecStartExamTime,RecStopExamTime,RecExamnieeID,RecStudentName,RecStudentIdentity,RecSex,RecImageURL)) {
		int  havaExamedStudentNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
		int toBeExamedStudentNum= mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
		//	mHavaExamedStudentText.setText(havaExamedStudentNum+"");
		mToExamStudentText.setText(toBeExamedStudentNum+ "");
		Log.d(LOG_TAG, "!!!!!!!!!!!总人数加1，已考人数="+havaExamedStudentNum+" 待考人数="+toBeExamedStudentNum);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				// 若没有选组，则将listview第一个item对应的组的信息在gridview中实时显示
				// 若选中的组等于收到的待考组的组号，则实时显示
				if (choosedGroupID == 0) {
					System.out.println("---->还没有选择组号，则listview中的第一组显示在gridview中:"+examGroupList.getFirstVisiblePosition());
					View cur_view = examGroupList.getChildAt(0);
					TextView textView = null;
					int firstGroup = 0;
					if (cur_view != null) {
						textView = (TextView) cur_view
								.findViewById(R.id.choosedGroupId);
						firstGroup = Integer.valueOf(textView.getText().toString());
						System.out.println("---->listview中的第一项对应组号为："+firstGroup);
						updateGroupInfoInGridViewFunc(firstGroup);
					}else{
						System.out.println("---->cur_view==null");
					}
				} else if (choosedGroupID == Integer.valueOf(_groupID)) {
					System.out.println("---->listview中的选中item对应组号等于收到的更新组");
					updateGroupInfoInGridViewFunc(choosedGroupID);
				}
			}
		}, 1000);

		//		}
		//		else {
		//			System.out.println("---->数据插入失败，数据已存在？");
		//		}
		//			} else {
		//				System.out.println("---->获取图片bitmap失败");
		//			}
	}
	private void SendInfoToServer(String msgStr) {
		if (mConnectThread.clientSocket != null) {
			System.out.println("-----MainActivity SendInfoToServer="+msgStr);
			try {
				out = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(
								mConnectThread.clientSocket.getOutputStream(),"GBK")),
								true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(out!=null){
				out.println(msgStr);
				System.out.println("---->msgStr---"+msgStr);
				out.flush();
			}
		}
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
	}
	/****得到考试阶段英文****/
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
	/****得到科目英文****/
	public String getExamStyleStr(int examStyle)
	{
		switch(examStyle){
		case 1:return "初试";
		case 2:return "复试";
		case 3:return "三试";
		default:return "初试";
		}
	}
	/****得到考试阶段英文****/
	public String getSubjectChinese(String subject)
	{
		if(subject.equals("Dance"))
			return "舞蹈";
		if(subject.equals("Vocal"))
			return "声乐";
		if(subject.equals("Piano"))
			return "钢琴";
		else 
			return "其他";
	}
	/****得到科目英文****/
	public String getExamStyleChinese(String examStyle)
	{
		if(examStyle.equals("First"))
			return "初试";
		if(examStyle.equals("Second"))
			return "复试";
		if(examStyle.equals("Third"))
			return "三试";
		else 
			return "初试";
	}
	//警告对话框
	public void showWarningDialog(String message){
		CustomDialog dialog;
		final CustomDialog.Builder customBuilder = new
				CustomDialog.Builder(ArtCheckIn_Main.this);
		customBuilder.setTitle("提示消息")
		.setIcon(R.drawable.tip)
		.setMessage(message)
		.setNeutralButton("确   定",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//				customBuilder.dismissDialog();
				dialog.dismiss();
				isWarnDialogShowing = false;
			}
		});
		//		customBuilder.create();
		//		customBuilder.showDialog();
		if (isWarnDialogShowing == false){
			dialog = customBuilder.create();
			dialog.show();
			isWarnDialogShowing = true;
		}		
	}
	
	public String getCurrentTime() {
		String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		return nowTime;
	}

	private void startProgressDialog(String message){
		if (progressDialog == null){
			progressDialog = CustomProgressDialog.createDialog(this);
			progressDialog.setMessage(message,"","");
		}
		progressDialog.setOnKeyListener(keylistener);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
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
			if (keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==event.ACTION_UP
					&&event.getRepeatCount()==0){
				System.out.println("----login progressDialog back down");
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
					return true;
				} else {
					if(mConnectThread!=null){
						mConnectThread.StopThread();   //关闭线程
					}
					finish();
					System.exit(0);
					return false;
				}
			}
			else
			{
				return false;
			}
		}
	} ;
	class Count_Timer extends CountDownTimer   
	{
		public Count_Timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onFinish() 
		{ //计时结束,时间到，回到主页面
			//			repeatTimes++;
			//if(repeatTimes<FrameFormat.MAXRETRANSTIMES)
//			String str[] = sendStr.split("\\|");
//			System.out.println("----hyz---sendStr"+ str[0]);
//			if (str[0] == FrameFormat.COMMAND_CONNECT){
//				SendInfoToServer(sendStr);
//				timer.start();
//			}
			if(isWaitingStartACK){
				//只有等上一个考生收到ACK后，才可以点击下一个考生开始考试，否则，过5次提醒一次，但仍发送BEG命令，直到收到ACK为止
				isWaitingStartACK = false;
				Message dialogMsg = mDialogHandler.obtainMessage();
				dialogMsg.what = FrameFormat.STARTNOACK;
				dialogMsg.sendToTarget();
				//				Toast.makeText(getApplicationContext(), "考生开始考试认证超时，此考生未成功开始考试", 2000).show();
			}
			else if(isWaitingStopACK){
				isWaitingStopACK = false;
				showWarningDialog("结束考试失败，请稍后重试");
			}
		}

		@Override
		public void onTick(long millisUntilFinished) 
		{   
			// TODO Auto-generated method stub

		}
	}
	
//	class TickAnswer_Timer extends CountDownTimer   
//	{
//		public TickAnswer_Timer(long millisInFuture, long countDownInterval) {
//			super(millisInFuture, countDownInterval);
//			// TODO Auto-generated constructor stub
//		}
//		@Override
//		public void onFinish() 
//		{ //计时结束,时间到，回到主页面
//			// TODO Auto-generated method stub
//			DefineVar.tickN = 0;
//			if (!isCall){
//				if(mConnectThread!=null){
//					mConnectThread.StopThread();
//					System.out.println("----mConnectThread.StopThread--执行");
//				}
//				startProgressDialog("正在连接网络···");
//				mConnectThread = SocketConnectThread.getSocketSingleInstance(mHandler,getBaseContext()); 
//				mConnectThread.start();
//				isCall = true;
//			}						
//		}
//		@Override
//		public void onTick(long millisUntilFinished) 
//		{   
//			// TODO Auto-generated method stub
//		}
//	}
	
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
		if (keyCode == KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0) {
			System.out.println("----key back down");
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
					mConnectThread.StopThread();   //关闭线程
				}
				finish();
				System.exit(0);
			}

		}
		return false;
	}

	public class ArtListAdapter<T> extends ArrayAdapter<T>{
		private LayoutInflater mInflater;   
		private int  selectItem=-1; 

		public ArtListAdapter(Context context, int resource,
				int textViewResourceId) {
			super(context, resource, textViewResourceId);
			// TODO Auto-generated constructor stub
			this.mInflater = LayoutInflater.from(context); 
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {   
			TextView groupIdText = null;
			if (convertView == null) {  
				convertView = mInflater.inflate(R.layout.listview_item, null);  
				groupIdText = (TextView) convertView.findViewById(R.id.choosedGroupId);  
				convertView.setTag(groupIdText);           
			} else {  
				groupIdText = (TextView) convertView.getTag();  
			}  

			groupIdText.setText(groupNumSet.get(position));

			if (position == selectItem) {  
				convertView.setBackgroundResource(R.drawable.list_item_selected); 
			}   
			else {  
				convertView.setBackgroundResource(R.drawable.list_item_selector);
			}     

			//convertView.getBackground().setAlpha(80);   

			return convertView;  
		}  

		public  void setSelectItem(int selectItem) { 
			this.selectItem = selectItem;  
		} 
	}  
}

