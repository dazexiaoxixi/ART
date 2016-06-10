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
	//�ؼ���������
	private TextView mExamRoom;
	//	private TextView mHavaExamedStudentText;
	private TextView mToExamStudentText;
	private TextView mcurExamStudentText;
	private TextView mExamStyle;
	private TextView mExamAddress;
	private TextView mStartExamTimeText;
	private TextView mCheckInWorker;    //��¼Ա
	private Button BtnStopExam;
	private ListView examGroupList;
	private GridView examineeGrid;
	private View lastClickView = null;  //��һ��ѡ�е���
	private View lastExamedStd = null;  //��һ������Ŀ���
	private CustomProgressDialog progressDialog = null;
	private AutoScrollTextView noticeTextView;
	private CustomDialog dialogInfo = null;

	//������Ϣ����
	private String userNameStr;
	private String userPswStr;
	private String subjectNumStr;       //��Ŀ
	private String roomNumStr;		//������
	private String examAddress;
	private String examStyle;
	private String userRemarkNameStr;
	
	private boolean isWarnDialogShowing = false;
	//	private boolean isConfirmDialogShowing = false;
	//Handler����
	private Handler mHandler = null;  //���������̷߳��͹���������
	private Handler mDialogHandler = null; 
	//���ݿ����
	private DbManager mDbManager=null;
	private DbManager mSocketDbManager = null;
	//ͼƬ���ر���
	private ImageLoader imageLoader;
	//���Ʊ���
	private String choosedExamnieeIDStr ;   //��ǰѡ�еĿ�����
	private int curExamingStudentID =0; // ��ǰ���ڿ��ԵĿ�����
	private int choosedGroupID = 0;   //��ǰѡ�е��� 
	private int curExamingGroupID = 0;  //��ǰ���ڿ�����
	private int absentNumInGroup = 0;  //ȱ��ѧ����
	private int Examed[] = new int[FrameFormat.maxStudentPerGroup];
	ArrayList<String> groupNumSet = new ArrayList<String>();   //�洢���յ������
	//LinkedHashSet<String> groupNumSet = new LinkedHashSet<String>();   //�洢���յ������
	private int AbsentIndex = 0;
	private boolean isCall = false;
	//�������
	private SocketConnectThread mConnectThread =null;  
	private PrintWriter out = null;
	private String sendStr;
	private String sendPara ;   //���͵Ĳ���
	private Count_Timer timer = null;
	
	private Timer tickTimer;
	private TimerTask task_tickTimer;
//	private TickAnswer_Timer tickAnswertimer;
	private int repeatTimes = 0;
	private boolean isWaitingStartACK = false;
	private boolean isWaitingStopACK = false;
	//private boolean isWaitingLoseACK = false;
	private boolean isWaitingACK = false;  //����gridview��listview�Ƿ���Ӧ����ı�־����
	private boolean stillWaitStopACK = false;
	private int waitingACKPosition = 0;//��¼��ǰ���ڵ���ACK�Ŀ���λ��
	//Adapter����
	private ArtListAdapter<String> ExamGroupAdapter;
	private GridViewAdapter gridViewAdapter ;
	private List<Integer> absentStudentIDList =new ArrayList<Integer>();
	//	private boolean flag_lastStudentExam = false;
	//�������˳�����
	private static Boolean isQuit = false;
	Timer timer1 = new Timer();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  //�����ޱ�����ʾ  
		setContentView(R.layout.activity_main);
		initWidges();
		
		//ListView��ʼ��
		initExamGroupListView();
		//GridView��ʼ��
		initExamnieeGridView();
		imageLoader=new ImageLoader(getBaseContext());
		mSocketDbManager=new DbManager(getBaseContext(),"Socket");
		getIntentData();
		initExamedByte();//�ŵ�getIntentData()���棬��ΪgetIntentData()�ж�Examed[]�����˲���

		//�������������
		noticeTextView = (AutoScrollTextView)findViewById(R.id.TextViewNotice);
		noticeTextView.init(getWindowManager());
		noticeTextView.startScroll();


		//��Ϣ����
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				System.out.println("--->>MainActivity����handleMessage����");
				// ��ʾ���յ�������
				if (msg.what == FrameFormat.HANDLE_UPDATEINFO) {
					System.out.println("--->>MainActivity�յ�handler HANDLE_UPDATEINFO����");
					//�����ѿ������ʹ�������
					int tempHavaExamedNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
					int tempToExamNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
					//mHavaExamedStudentText.setText(tempHavaExamedNum+"");
					mToExamStudentText.setText(tempToExamNum+"");
					//����listview
					LinkedHashSet<String> groupSet =  mDbManager.queryGroupSetAccordingTime(FrameFormat.STATUS_NOTEXAMEDOREXAMING);
					String tempGroupID = null;
					groupNumSet.clear();
					ExamGroupAdapter.clear();
					Iterator<String> it = groupSet.iterator();
					while(it.hasNext()) {
						tempGroupID = it.next();
						System.out.println("-----"+tempGroupID);
						groupNumSet.add(tempGroupID);
						ExamGroupAdapter.add(tempGroupID); // ÿ�ζ������һ��
					}
					ExamnieeInfo examnieeInfo = mDbManager.queryStudentInfoAccordingTime(FrameFormat.STATUS_EXAMING);
					if(curExamingGroupID ==0){
						//��ǰû�����ڿ��Ե���
						if(examnieeInfo.getExamnieeID()!=null){
							//��ѯ���ݿ�������ڿ��Ե���
							curExamingGroupID = Integer.valueOf(examnieeInfo.getGroupID());
							curExamingStudentID = Integer.valueOf(examnieeInfo.getExamnieeID());
							mcurExamStudentText.setText(curExamingGroupID+"��"+curExamingStudentID+"��");
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
							//Ȼ�����gridView
							updateGroupInfoInGridViewFunc(choosedGroupID);
						}else{
							//��ѯ���ݿ��ȷʵû�����ڿ��Ե���
							if(choosedGroupID==0){
								//����ѡ��״̬
								if (ExamGroupAdapter.getCount() > 0){
									examGroupList.setSelection(0);
									ExamGroupAdapter.setSelectItem(0);
									ExamGroupAdapter.notifyDataSetInvalidated();
									choosedGroupID = Integer.valueOf(ExamGroupAdapter.getItem(0));
									//Ȼ�����gridView
									updateGroupInfoInGridViewFunc(choosedGroupID);
								}

							}else{
								updateGroupInfoInGridViewFunc(choosedGroupID);
							}
						}
					}else{
						//��ǰ�����ڿ��Ե���
						int choosedItemPosition = ExamGroupAdapter.getPosition(curExamingGroupID+"");
						examGroupList.setSelection(choosedItemPosition);
						ExamGroupAdapter.setSelectItem(choosedItemPosition); 
						ExamGroupAdapter.notifyDataSetInvalidated(); 
						choosedGroupID = curExamingGroupID;
						//���µ�ǰ����
						//	ExamnieeInfo examnieeInfo = mDbManager.queryStudentInfoAccordingTime(FrameFormat.STATUS_EXAMING);
						if(examnieeInfo.getExamnieeID()!=null){
							curExamingStudentID = Integer.valueOf(examnieeInfo.getExamnieeID());
							mcurExamStudentText.setText(curExamingGroupID+"��"+curExamingStudentID+"��");
						}
						//���¿���ʱ��
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
						//Ȼ�����gridView
						updateGroupInfoInGridViewFunc(choosedGroupID);
					}

					
				}
				//�����洦��MainAcitivityʱ�����Ͽ����ӣ��������ɹ�����ʱ��handle������MainActivity�������ᷢ��LoginActivity����ˣ���
				//����ҲҪ�����handle��Ϣ
				else if(msg.what == FrameFormat.HANDLE_SERVERSOCKETCONTIMEOUT||msg.what == FrameFormat.HANDLE_SERVERDISCONNECT)
				{
					startProgressDialog("�����������硤����");
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
					isCall = false;//����߳���������־λ
					sendStr = FrameFormat.COMMAND_LOGIN+"|"+userNameStr+"|"+encryption(userPswStr)+"|"+"0";
					SendInfoToServer(sendStr);
//					timer.start();
				}
				else if(msg.what == FrameFormat.HANDLE_LOGINFAIL)
				{
					System.out.println("---login �������ܾ�");
					stopProgressDialog();
					//Toast.makeText(getApplicationContext(), failReason, 2000).show();
					if(timer!=null){
						timer.cancel();
					}
					if(mConnectThread!=null){
						mConnectThread.StopThread();
					}
					String failReason = (String)msg.obj;
					//��ת����½����
					Intent intent = new Intent();
					intent.setClass(ArtCheckIn_Main.this, ArtCheckIn_Login.class);
					intent.putExtra("failReason", "�������ܾ����豸��½");
					startActivity(intent);
					finish();
				}
				else if(msg.what == FrameFormat.HANDLE_LOGINSUCCESS)	//��¼�ɹ�
				{
					//msg.obj = subject + "," + roomNum + "," + userRemarkName + ","
					//		+ examPlace + "," + examStage;
//					tickAnswertimer.cancel();
					stopProgressDialog();
					isCall = false;//����߳���������־λ

				}
				else if(msg.what == FrameFormat.HANDLE_STRATEXAMOK){
					String recStr = (String)(msg.obj);

					System.out.println("--->>>>>HANDLE_STRATEXAMOK�յ�recStr="+recStr);
					System.out.println("--->>>>>HANDLE_STRATEXAMOK���͵�sendPara="+sendPara);
					if(recStr.equals(sendPara)){
						System.out.println("--->>MainActivity�յ�ͬ�⿼����ʼ����");
						sendPara = "";
						timer.cancel();
						isWaitingStartACK = false;
						isWaitingACK = false; 
						//						repeatTimes = 0;
						//�ɹ���ʼ��ʼ��Źر�dialog
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
								"ĳһ������ʼ����:" + mStudentNum, 2000).show();
						Examed[mStudentNum-1] = FrameFormat.STATUS_EXAMING;
						mcurExamStudentText.setText(groupIDStr + "��"
								+ mStudentNumStr + "��");
						//���µ�ǰ���ڿ��ԵĿ��ԵĿ���״̬��
						//mDbManager.updateExamStatus(groupIDStr, mStudentNumStr, FrameFormat.STATUS_EXAMING+"");
						mDbManager.updateStartExamTime(groupIDStr, mStudentNumStr,getCurrentTime());
						int  havaExamedStudentNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
						int toBeExamedStudentNum= mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
						//mHavaExamedStudentText.setText(havaExamedStudentNum+"");
						mToExamStudentText.setText(toBeExamedStudentNum
								+ "");
						Log.d(LOG_TAG, "!!!!!!!!!!!�ѿ�������1���ѿ�����="+havaExamedStudentNum+" ��������="+toBeExamedStudentNum);

						//����ʼ���Եı��������ڵ��鲻���ڵ�ǰ�����飬������ÿ���Ϊ�µ�һ��ĵ�һ����������һ�������
						//���򣬱�ʾ�������һ���������Խ����ˣ��ÿ������Կ���
						if(0==curExamingGroupID){
							//	numberInGroup = mDbManager.queryExamnieeNumInGroup(groupIDStr);
							//updateExamnieeStatusFunc(mGroupID);
							//���¿���ʱ��
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
						Log.d(LOG_TAG, "--->>>>>���������͵�ͬ�⿼����ʼ���Զ�Ӧ�Ļظ�");
					}
				}
				else if(msg.what == FrameFormat.HANDLE_STRATEXAMNO){
					System.out.println("--->>MainActivity�յ��ܾ�������ʼ����");
					if(timer!=null){
						timer.cancel();
					}
					isWaitingStartACK = false;
					isWaitingACK = false; 
					//					repeatTimes = 0;
					String failReason = (String)msg.obj;
					//Toast.makeText(getApplicationContext(), failReason, 2000).show();
					//					showWarningDialog(failReason);
					//��������������������ر�dialog�������ܾ�ԭ��
					Message dialogMsg = mDialogHandler.obtainMessage();
					dialogMsg.what = FrameFormat.STARTNO;
					dialogMsg.obj = failReason;
					dialogMsg.sendToTarget();
				}
				else if(msg.what == FrameFormat.HANDLE_STOPEXAMOK){
					String recStr = (String)(msg.obj);
					System.out.println("--->>>>>HANDLE_STOPEXAMACK�յ�recStr="+recStr);
					System.out.println("--->>>>>HANDLE_STOPEXAMACK���͵�sendPara="+sendPara);
					if(recStr.equals(sendPara)){
						System.out.println("--->>MainActivity�յ�ĳһ�����������Իظ�");
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
						System.out.println("--->>MainActivity�յ����һ�������������Իظ�");
						
							System.out.println("--->>ȱ������=0,�յ�END_ACK���ִ�н������鿼�Եĺ���");
							OneGroupStopExamConfirmedFunc(Integer.valueOf(StopExamStudentGroupId));
					}else{
						Log.d(LOG_TAG, "--->>>>>���������͵Ľ������Զ�Ӧ�Ļظ�");
					}
				}
				else if (msg.what == FrameFormat.HANDLE_STOPEXAMNO){
					System.out.println("--->>MainActivity�յ��������Իظ�:");
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
					System.out.println("--->>>>>HANDLE_ABSENTACK�յ�recStr="+recStr);
					System.out.println("--->>>>>HANDLE_ABSENTACK���͵�sendPara="+sendPara);
					if(recStr.equals(sendPara)){
						System.out.println("--->>MainActivity�յ�ȱ����Ϣ�ظ�");
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
							//�������鿼�ԵĲ���
							OneGroupStopExamConfirmedFunc(curExamingGroupID);
						}
					}else{
						Log.d(LOG_TAG, "--->>>>>���������͵�ȱ����Ϣ��Ӧ�Ļظ�");
					}
				}
				else if (msg.what == FrameFormat.HANDLE_ABSENTNO){
					System.out.println("--->>MainActivity�յ�ȱ����Ϣ�ظ�:");
					timer.cancel();
					//isWaitingLoseACK = false;
					//					repeatTimes = 0;
					String failReason = (String)msg.obj;
					showWarningDialog(failReason);
				}
				else if (msg.what == FrameFormat.HANDLE_INFO){
					String recStr = (String)msg.obj;
					System.out.println("--->>MainActivity�յ�ϵͳ��Ϣ:"+recStr);
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
				showWarningDialog("������������鿼�ԣ����ܽ�����������");
			}else if(isWaitingACK){
				showWarningDialog("�����п������ڵȴ�������֤�����ڱ��鿼�Խ�����鿴������������Ϣ��лл��");
			}else{
				final TextView testView = (TextView) arg1
						.findViewById(R.id.choosedGroupId);
				choosedGroupID = Integer.valueOf(testView.getText().toString());
				//�������ڿ��ԣ����û���ѡ�ǵ�ǰ������ʱ��������ʾ**�����ڿ���
				if(curExamingGroupID != 0){
					if(choosedGroupID != curExamingGroupID){
						//������ʾ**�����ڿ���
						choosedGroupID = curExamingGroupID;  //�ָ�choosedGroupID��ֵ
						showWarningDialog("��"+curExamingGroupID+"�����ڿ��ԣ����ڱ��鿼�Խ�����鿴������������Ϣ��лл��");
					}else{
						//ѡ�����Ϊ��ǰ�����飬�����κδ���
					}
				}else{  //curExamingGroupID==0����ʾ��ǰû�����ڿ����飬�û���������鿴������
					//��ʾѡ��Ч��
					ExamGroupAdapter.setSelectItem(position);  
					ExamGroupAdapter.notifyDataSetInvalidated();  
					lastClickView = arg1;
					System.out.println("-----hyz-----ѡ��listview��item-----"+ExamGroupAdapter.getItem(position));
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
				showWarningDialog("���ڽ������鿼�ԣ����Ժ�");
			}
			else {
				showWarningDialog("�Բ��𣬵�"+(waitingACKPosition+1)+"�ſ���δ��֤�꣬���Ժ�");
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
				System.out.println("--->>���gridview�ĵ�"+position+"���ͼƬ͸����"+gridViewAlpha);
				if (gridViewAlpha == 1.0f){
					if(!isWaitingStartACK&&!isWaitingStopACK&&!stillWaitStopACK&&!isWaitingACK){
						//					if(!isWaitingStopACK&&!isWaitingLoseACK){
						ConfirmExamnieeInfoFunc(position);
					}else if(isWaitingACK && (position == waitingACKPosition)){
						//���ڵ���ACK�Ŀ��������ٴδ�����Ӧ��ȷ�Ͽ�����Ϣ����
						ConfirmExamnieeInfoFunc(position);												
					}else if(isWaitingStopACK==true){
						showWarningDialog("�Բ������ڽ������鿼�ԣ����ܽ��д˲���");
					}else if(stillWaitStopACK){
						showWarningDialog("������������鿼�ԣ����ܽ�����������");
					}else{
						showWarningDialog("�Բ��𣬵�"+(waitingACKPosition+1)+"�ſ���δ��֤�꣬���Ժ�");
					}
				}
			}

		}

	}
	//��ʼ��������Ϣ�Ϳ�����Ϣ
	private void getIntentData() {
		System.out.println("------getIntentData");
		Intent intent =this.getIntent();	//��½���洫������ֵ
		userNameStr=intent.getStringExtra("userNameStr");
		userPswStr=intent.getStringExtra("userPswStr");
		subjectNumStr = intent.getStringExtra("subjectNumStr");
		roomNumStr = intent.getStringExtra("roomNumStr");
		examAddress= intent.getStringExtra("examAddress");
		examStyle = intent.getStringExtra("examStyleStr");
		userRemarkNameStr = intent.getStringExtra("userRemarkNameStr");
		//���ݿ�����Ϊdance1-first.db
		System.out.println("------�������");
		DefineVar.databaseName =getSubjectEnglish(subjectNumStr)+ roomNumStr+"-"+getExamStyleEnglish(examStyle)+".db";
		//DefineVar.databaseName =subjectNumStr+ roomNumStr+"-"+examStyle+".db";
		mDbManager=new DbManager(this);

		//�Լ����Լ�handlerһ�Σ�����saveDataToDatabaseThread����ת��mainActivity֮ǰ���Ѿ�handle���˺�����handleһ����Ϣ

		System.out.println("--!!!MainActivity��������ݿ�����:"+DefineVar.databaseName);
		mSocketDbManager.insertDatabaseName(DefineVar.databaseName);
		//mExamRoom.setText(getSubjectChinese(subjectNumStr)+roomNumStr+"����");
		mExamRoom.setText(subjectNumStr+roomNumStr+"����");
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
		//�����ݿ���δ����������0�����������ڿ��Ե��ˣ�����Ҫ�ָ�
		if(mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED)>0
				||mDbManager.queryNumAccordingTime(FrameFormat.STATUS_EXAMING)>0){
			//���ݿ��������ݣ���Ҫ�ָ�
			recovFromDatabase();
		}
		else{
			System.out.println("---%%%���ݿ���û�д������ݣ�����Ҫ�ָ�");
		}
	}
	private void recovFromDatabase(){
		Log.i(LOG_TAG, "---%%%���ݿ��������ݣ�����recovFromDatabase���ظ��׶Ρ�����");
		LinkedHashSet<String> groupSet = new LinkedHashSet<String>();

		int totalStudentNum = mDbManager.queryExamnieeNumInTable();
		Log.d(LOG_TAG, "!!!!!!!!!!!������="+totalStudentNum);
		ExamnieeInfo examnieeInfo = mDbManager.queryStudentInfoAccordingTime(FrameFormat.STATUS_EXAMING);
		//ͨ����ѯ���ݿ⣬�鵽���ڿ��Ե���,�Ϳ�����
		if(examnieeInfo!=null&&examnieeInfo.getGroupID()!=null)
		{
			//��ʾ�����ڿ��Ե���
			Log.i(LOG_TAG, "---%%%���ݿ������ڿ��Ե���");
			curExamingGroupID = Integer.valueOf(examnieeInfo.getGroupID());
			curExamingStudentID = Integer.valueOf(examnieeInfo.getExamnieeID());
			mcurExamStudentText.setText(curExamingGroupID+"��"+curExamingStudentID+"��");
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
			//�����ݿ�û�����ڿ��Ե���,����ҿ��Ƿ������ڿ��Ե���
			//------lt------xiugai  2.6������10��----start---
			//	ExamnieeInfo tempExamnieeInfo = mDbManager.queryExamnieeInfoOfFirstStart(FrameFormat.FLAG_ISFIRSTSTART);
			Log.i(LOG_TAG, "---%%%��ѯ���ݿ������ڿ��Ե���");
			String getTempGroupID= mDbManager.queryExamingGroupAccordingTime();
			if(getTempGroupID!=null){
				Log.i(LOG_TAG, "---%%%���ݿ������ڿ��Ե���");
				curExamingGroupID = Integer.valueOf(getTempGroupID);
				mcurExamStudentText.setText(curExamingGroupID+"��"+"0��");
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
			//------lt------xiugai  2.6������10��----end---
		}
		//listview������item������Ϊ���ɰ�

		//�����ݿ���δ���˵���Ϣ����ʾ��listview��gridview��
		groupSet = mDbManager.queryGroupSetAccordingTime(FrameFormat.STATUS_NOTEXAMEDOREXAMING);
		if(curExamingGroupID!=0){
			groupSet.add(curExamingGroupID+"");
		}
		Iterator<String> iterator = groupSet.iterator();
		//������listview
		recovUpdateListView(groupSet);
		Log.i(LOG_TAG, "--�ָ����ݿ���Ϣʱ�����ڿ��Ե���="+curExamingGroupID);
		if(curExamingGroupID!=0){
			//�������ڿ��Ե��飬����gridview�������ڿ��������Ϣ
			recovUdateGridView(curExamingGroupID);
		}else{
			//��û�����ڿ��Ե��飬���ȡgruopSet�ĵ�һ��Ԫ�ض�Ӧ���飬���´������Ϣ
			System.out.println("-----�ָ����ݿ���Ϣʱ��û�����ڿ��Ե��飬��ȡ��Ĭ����ʾ��=");
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
			ExamGroupAdapter.add(tempGroupID); // ÿ�ζ������һ��
			groupNumSet.add(tempGroupID);
		}
		//�ָ�listview��ѡ��״̬
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
		Log.i(LOG_TAG, "--�ָ����ݿ���Ϣ������gridview����="+tempGroupID);
		//final int examnieeNum = mDbManager.queryExamnieeNumInGroup(tempGroupID + "");
		//final int maxExamnieeID= mDbManager.queryMaxExamnieeIDInGroup(tempGroupID + "");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				for (int index = 1; index <= 10; index++) {
					Log.i(LOG_TAG, "-����gridview�ĵڼ���item="+index);
					Log.i(LOG_TAG, "-���µ�gridview="+examineeGrid);
					View cur_view = examineeGrid.getChildAt(index - 1);
					if (cur_view != null) {
						ImageView imageView = (ImageView) cur_view
								.findViewById(R.id.itemImage);
						TextView textView = (TextView) cur_view
								.findViewById(R.id.stdNum);
						Log.i(LOG_TAG, "-�õ�gridview�ĵڼ���item��view="+index);
						//	if (textView != null && imageView != null && textName != null) {   //��Ҫ��
						if (textView != null && imageView != null ) {
							//	if (index <= examnieeNum) {
							//	if (index <= maxExamnieeID) {
							Log.i(LOG_TAG, "----���ڸ���gridview����ţ����:" + tempGroupID
									+ "examnieeID:" + index);
							ExamnieeInfo examnieeInfo = mDbManager.queryExamnieeInfoNew(tempGroupID
									+ "", index + "");
							if(examnieeInfo!=null && examnieeInfo.getExamnieeStatus()!=null){
								//�˿�������״̬
								int mExamStatus = Integer.valueOf(examnieeInfo.getExamnieeStatus());
								//if (mName != null ) {
								textView.setText(String.valueOf(index));
								System.out.println("----�ÿ�������״̬"+mExamStatus);
								//���´˿����Ŀ���״̬
								if(mExamStatus==FrameFormat.STATUS_ABSENT){
									System.out.println("----�ÿ���ȱ��"+tempGroupID+"��"+index+"��");
									Examed[index-1] = FrameFormat.STATUS_ABSENT;
								}else if(mExamStatus==FrameFormat.STATUS_EXAMING){
									System.out.println("----�ÿ������ڿ���"+tempGroupID+"��"+index+"��");
									Examed[index-1] = FrameFormat.STATUS_EXAMING;
									cur_view.setAlpha(0.9f);
									//imageView.setAlpha(0.9f);
									cur_view.setBackgroundResource(R.drawable.current_examing_group_shape);
									lastExamedStd = cur_view;
								}else if(mExamStatus==FrameFormat.STATUS_HAVAEXAMED){
									System.out.println("----�ÿ����ѿ�����"+tempGroupID+"��"+index+"��");
									Examed[index-1] = FrameFormat.STATUS_HAVAEXAMED;
									cur_view.setAlpha(0.5f);
									//imageView.setAlpha(0.5f);
								}else if(mExamStatus==FrameFormat.STATUS_NOTEXAMED){
									System.out.println("----�ÿ�����δ����"+tempGroupID+"��"+index+"��");
									Examed[index-1] = FrameFormat.STATUS_NOTEXAMED;
								}

								Log.i(LOG_TAG, "-----<<<<<<<<��"+tempGroupID+"��"+index+"��:::"+"�ڻָ����ݿ���Ϣʱ��recovUdateGridView�����е���loadImage(PicURL)");
								//									loadImage(tempGroupID,examnieeInfo
								//											.getExamnieePicURL(),imageView);
								imageLoader.DisplayImage(examnieeInfo
										.getExamnieePicURL(), imageView); 
							}else{
								//����һ����űȽ�С��ѧ����Ϣ��û�յ�

								Examed[index-1] = FrameFormat.STATUS_NOPERSON;
								imageView.setImageResource(R.drawable.default_lock);
								cur_view.setAlpha(1.0f);
								//imageView.setAlpha(1.0f);
								cur_view.setBackgroundResource(R.drawable.login_shape);
							}

							//							} else {
							//								//��λ��û�п���
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

	/***��ʼ��Examed***/
	private void initExamedByte() {
		// TODO Auto-generated method stub
		for(int i = 0;i <FrameFormat.maxStudentPerGroup;i++){
			Examed[i] = FrameFormat.STATUS_NOTEXAMED;
		}
	}
	//ĳһ������ʼ����
	private void OneStudentStartExam(){
		if (ExamGroupAdapter.getCount() > 0){
			String tempGroupID =null;
			if(curExamingGroupID==0){
				if(choosedGroupID==0){
					//��û��ѡ���飬��ô����Ŀ������ڵ���϶���listview�ĵ�һ��item��Ӧ�����
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
	// ĳһ������������
	private void OneStudentStopExamFunc(int curStopExamGroupId,int curStopExamStudentId){
		Examed[Integer.valueOf(curStopExamStudentId)-1]=FrameFormat.STATUS_HAVAEXAMED;
		//	mDbManager.updateExamStatus(curStopExamGroupId, curStopExamStudentId, FrameFormat.STATUS_HAVAEXAMED+"");
		mDbManager.updateEndExamTime(curStopExamGroupId+"", curStopExamStudentId+"",getCurrentTime());
		int  havaExamedStudentNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
		int toBeExamedStudentNum= mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
		//mHavaExamedStudentText.setText(havaExamedStudentNum+"");
		mToExamStudentText.setText(toBeExamedStudentNum
				+ "");
		Log.d(LOG_TAG, "!!!!!!!!!!!�ѿ�������1���ѿ�����="+havaExamedStudentNum+" ��������="+toBeExamedStudentNum);

		OneStudentStartExam();
		//	}
	}
	//ĳ���������
	private void OneGroupStopExamFunc(final int curExamGroupId,final int curExamStudentId) {
//		Toast.makeText(getApplicationContext(),
//				curExamGroupId+"���������" , 2000).show();		
		if(curExamingGroupID==0){
			showWarningDialog("�Բ��𣬵�ǰû�����ڽ��п��Ե���");
		}else{			
			String message ="��"+curExamGroupId+"�����¿���ȱ����"+"\n"+"\n";
			int notExamedNum = 0;
			for(int i = 0;i <FrameFormat.maxStudentPerGroup;i++){
				System.out.println("-----examed="+i+":"+Examed[i]);
				if(Examed[i]==FrameFormat.STATUS_NOTEXAMED){
					ExamnieeInfo tempExamnieeInfo = mDbManager.queryExamnieeInfoNew(curExamGroupId+"", (i+1)+"");
					if(tempExamnieeInfo!=null){
						message = message + (i+1)+"�ţ� "+ tempExamnieeInfo.getExamnieeName()+"\n";
					}
					notExamedNum ++;
				}
			}
			message = message+"\n"+"ȷ�Ͻ������鿼�ԣ�";
			if(notExamedNum == 0){
				//��û����ȱ��
				message = "ȷ�Ͻ������鿼�ԣ�";
			}

		
			CustomDialog.Builder builder =
					new
					CustomDialog.Builder(ArtCheckIn_Main.this);
			builder.setTitle("ȷ���������ԣ�")
			.setIcon(R.drawable.tip)
			.setMessage(message)
			.setPositiveButton("ȷ   ��",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					//		flag_lastStudentExam = true;

					if(curExamingStudentID>0){
						//��Ϊ�˽�����⣺��ĳһ���Ѿ���ʼ���ԣ�2���ѿ��꣬Ȼ���������˵�ͨ����������û���յ�ACK��ʱ����˳��˳���Ȼ���ٽ�������ʱ�鵽ֻ�����ڿ��Ե��飬
						//û�����ڿ��Ե��ˣ���ʱ��ֱ�ӵ���������鿼�ԣ���ᷢ��END|����|1|1|0(����)��1��0�Ž������ԣ�����Ϣ���󣬲�Ӧ�÷��ͣ����Լ�����if������
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
							System.out.println("--->>ȱ����������0");
							uploadAbsentInfoFunc(absentStudentIDList.get(AbsentIndex));
							AbsentIndex ++;
						}else{
							System.out.println("--->>ȱ������=0,�յ�END_ACK���ִ�н������鿼�Եĺ���");
							OneGroupStopExamConfirmedFunc(Integer.valueOf(curExamingGroupID));
						}
					}
					//OneGroupStopExamConfirmedFunc(curExamGroupId);

					//					builder.dismissDialog();
					dialog.dismiss();//�رնԻ���
				}
			});
			builder.setNegativeButton("��   ��",  
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
		//���һλ������������
		Log.d(LOG_TAG, "!!!!!!!!!!!һ���������");
		//�������ĸ����һ����ʼ���Ե�ѧ�����ڵ�isFirstStart����Ϊ0��
		//mDbManager.updateFirstStartToInit(curExamingGroupID+"");

		updateDatabase(curExamGroupId);
		updateExamStatusText();
		updateListViewAfterOneGroupStopExam(curExamGroupId);
		deleteLocalPicAfterOneGroupStopExam();
		initDataAfterOneGroupStopExam();   //��һ������ִ�в�������������������ǰ�棬��Ϊ��һ������ֵ����ʼ����
		updateGroupInfoInGridViewFunc(choosedGroupID);
	}
	private void updateDatabase(int _groupID){
		//һ��������ԣ���ɾ���������Ϣ
		mDbManager.deleteHavaExamedExamnieeInfo(_groupID+"");
	}
	private void updateExamStatusText(){
		//mStartExamTimeText.setText("00:00:00");
		mStartExamTimeText.setText("00:00");
		mcurExamStudentText.setText("");
		//�ѿ�����Ҫ����ȱ����
		int  havaExamedStudentNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
		int toBeExamedStudentNum= mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
		//	mHavaExamedStudentText.setText(havaExamedStudentNum+"");
		mToExamStudentText.setText(toBeExamedStudentNum
				+ "");
		Log.d(LOG_TAG, "!!!!!!!!!!!�ѿ�������ȱ���������ѿ�����="+havaExamedStudentNum+" ��������="+toBeExamedStudentNum);
	}
	private void updateListViewAfterOneGroupStopExam(int deleteGroupID){

		int choosedItemPosition = ExamGroupAdapter.getPosition(deleteGroupID+"");
		if(choosedItemPosition>=0){
         //��listview�д���׼��ɾ�������deleteGroupID����ִ��ɾ��������

			View cur_ListItem = examGroupList.getChildAt(choosedItemPosition);
			if (cur_ListItem != null) {
				cur_ListItem.setBackgroundResource(R.drawable.list_item_selector);
			}

			// �������ԣ���listViewѡ����ɾ��
			groupNumSet.remove(curExamingGroupID + "");
			ExamGroupAdapter.remove(curExamingGroupID + "");
			ExamGroupAdapter.notifyDataSetChanged();

			//����ǰѡ���鱻ɾ������Ĭ��ѡ�е�ǰ��һ�� 		 
			examGroupList.setSelection(0);
			ExamGroupAdapter.setSelectItem(0);   
			ExamGroupAdapter.notifyDataSetInvalidated();  


			if (ExamGroupAdapter.getCount() > 0){
				// ����listview������item��Ӧ�����gridview
				choosedGroupID = Integer.valueOf(ExamGroupAdapter.getItem(0));
				System.out.println("-----hyz-----ѡ��listview�ĵ�һ��-----"+choosedGroupID);
				//����ɾ������һ����ԭ��listview�ĵ�һ�ж�Ӧ���飬��
				//����������һ��if��ԭ����ɾ����һ��֮���ٴεõ���һ�е���ţ����Ǳ�ɾ��������һ�е���ţ���֪��Ϊʲô
				if(curExamingGroupID==choosedGroupID)
				{
					choosedGroupID = Integer.valueOf(ExamGroupAdapter.getItem(1));
					System.out.println("-----hyz-----ѡ��listview�ĵ�һ��-----"+choosedGroupID);
				}
			}else{
				choosedGroupID = 0;
			}
		}
	}
	private void deleteLocalPicAfterOneGroupStopExam(){
		Log.d(LOG_TAG, "----~~~~һ��������ԣ�ɾ���ļ���   "+examGroupList.getCount());
		if(examGroupList.getCount()==0)
		{
			Log.d(LOG_TAG, "----~~~~һ��������ԣ�ɾ���ļ���");
			String path = android.os.Environment.getExternalStorageDirectory()+File.separator+"ArtCheckIn";
			File fileDir = new File(path);   
			if(fileDir.exists()){
				if (fileDir.isDirectory())
				{
					// ����Ŀ¼�����е��ļ� files[];
					File files[] = fileDir.listFiles();
					for (int i = 0; i < files.length; i++)
					{ // ����Ŀ¼�����е��ļ�
						files[i].delete(); // ��ÿ���ļ� ɾ��
					}
					//fileDir.delete();  //ɾ�����ļ���
				}
				//fileDir.delete();
				Log.d(LOG_TAG, "----~~~~�ɹ�ɾ���ļ���:"+path);
			}else{
				Log.d(LOG_TAG, "----~~~~�ļ��в�����");
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
	//���ͼƬ���鿴������Ϣ�Ƿ���ȷ
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
		System.out.println("-----��item�Ŀ�����:"+textView.getText().toString());
		if(textView.getText().toString()!=null&&(!textView.getText().toString().equals(""))){  //����item�п�����Ϣ

			choosedExamnieeIDStr= textView.getText().toString();

			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.student_information_dialog, 
					(ViewGroup)findViewById(R.id.stdInformationDialog));
			TextView examnieeID = (TextView)layout.findViewById(R.id.examnieeID);
			TextView examnieeName = (TextView)layout.findViewById(R.id.exmnieeName);
			TextView examnieeSex =  (TextView)layout.findViewById(R.id.exmnieeGender);
			TextView examnieeIdentity =  (TextView)layout.findViewById(R.id.exmnieeIdentity);
			ImageView examnieePic = (ImageView)layout.findViewById(R.id.examnieePic);
			//��ѯ��ǰ�鿴��ѧ������Ϣ
			String tempGroupID ;
			if(curExamingGroupID==0){
				if(choosedGroupID==0){
					//��û��ѡ���飬��ô����Ŀ������ڵ���϶���listview�ĵ�һ��item��Ӧ�����
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
					//��dialog�еĿؼ��������
					examnieeID.setText(tempExamnieeInfo.getExaminationID());
					examnieeName.setText(tempExamnieeInfo.getExamnieeName());
					examnieeSex.setText(tempExamnieeInfo.getExamnieeSex());
					examnieeIdentity.setText(tempExamnieeInfo.getExamnieeIdentity());
				}
			}
			//examieeIden
			examnieePic.setImageDrawable(imageView.getDrawable());
			//������ʾ������ϸ��Ϣ��������ȷ��			
			CustomDialog.Builder  builderDialog= new CustomDialog.Builder(ArtCheckIn_Main.this);
			builderDialog.setIcon(R.drawable.std_icon)
			.setTitle((position+1)+"�ſ�����Ϣ")
			.setContentView(layout)
			.setPositiveButton("��   ��",
					new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, int which) {
					//���ӿ����ٴ�ȷ�ϣ��Ա����������
					CustomDialog.Builder  confirmBuilder = new CustomDialog.Builder(ArtCheckIn_Main.this);
					confirmBuilder.setTitle("�Ƿ񿪿���")
					.setMessage((position+1)+"�ſ�����ʼ���ԣ�")
					.setPositiveButton("��",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface confirmDialog, int which) {
							confirmDialog.dismiss();
							//�������뿼�Ա�־λ���ɹ��յ���������ACK(����������Ӧ)�������־λ
							isWaitingACK = true; 
							waitingACKPosition = position;
							//�ɹ�������Źر�dialog
							mDialogHandler = new Handler(){
								@Override
								public void handleMessage(Message msg)
								{
									// ��ʾ���յ�������					
									if (msg.what == FrameFormat.STARTOK ) {
										//�յ��ɹ�������Ϣ��رս�������dialog
										stopProgressDialog();
										dialog.dismiss();
										dialogInfo = null;
										//										isConfirmDialogShowing = false;
										if (lastExamedStd != null) {
											lastExamedStd.setAlpha(0.5f);
											lastExamedStd.setBackgroundResource(R.drawable.login_shape);
										}
										//���ڿ��ԵĿ���griditem������ó���ɫ									
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
										showWarningDialog("����������֤��ʱ���˿���δ�ɹ���ʼ���ԣ����Ժ�����");
									}
								}
							};
							if(curExamingStudentID==0){
								System.out.println("------��ǰû���˿��ԣ�һ���˿�ʼ����:"+curExamingGroupID+"��"+curExamingStudentID+"��");
								OneStudentStartExam();
							}
							else{
								System.out.println("------��ǰ�������Ե���:"+curExamingGroupID+"��"+curExamingStudentID+"��");
								OneStudentStopExamFunc(curExamingGroupID,curExamingStudentID);
							}
							//�����ͨ�����󣬾Ͳ����ٵ�������ء�
							//												builderDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
							startProgressDialog("�ȴ���������Ȩ������");					
						}
					})
					.setNegativeButton("��", 
							new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface confirmDialog, int which) {
							confirmDialog.dismiss();
						}
					});
					CustomDialog confirmDialog = confirmBuilder.create();	
					confirmDialog.show();		
				}
			})
			.setNegativeButton("��   ��", new DialogInterface.OnClickListener() {
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
	//�ӷ����������´�������Ϣ�����б�����ʾ
	private void updateGroupInfoInListViewFunc(String _groupID,String _examnieeID){
		// �����б�,�����յ���������µģ������listview��ʾ
		boolean isExist = false;
		System.out.println("----listview������" + ExamGroupAdapter.getCount()
				+ "��");
		if (ExamGroupAdapter.getCount() > 0) {
			for (int index = 0; index < ExamGroupAdapter.getCount(); index++) {
				System.out.println("----listview�е�:" + (index + 1) + "��Ԫ��:��"
						+ groupNumSet.get(index) + "��");
				if (_groupID.equals(groupNumSet.get(index))) {
					System.out
					.println("----listview�е�:" + _groupID + "���Ѵ���");
					isExist = true;
					break;
				} else {
					isExist = false;
					System.out
					.println("----listview�е�:" + _groupID + "�鲻����");
				}
			}
		}
		if (!isExist) {
			System.out.println("----listview����һ��:" + _groupID + "��");
			ExamGroupAdapter.add(_groupID); // ÿ�ζ������һ��
			groupNumSet.add(_groupID);
		}

		//���û�û��ѡ�������ʱ��Ĭ��ѡ����ǰ��һ��
		if ((ExamGroupAdapter.getCount() > 0) && (lastClickView == null)){
			examGroupList.setSelection(0);
			ExamGroupAdapter.setSelectItem(0);  
			ExamGroupAdapter.notifyDataSetInvalidated();  
			//�˴�û������choosedGroupID������Ӧ�����ó�ѡ�еĵ�һ��item��Ӧ����ŵģ�
			//��������֮������ȷ��ʾgridview�еĿ�����Ϣ(����ʾ�κ���Ϣ)��
			//������saveExamnieeInfo()�����й�ϵ
		}

		// ����gridview??
		// ����������Ϣ�������ݿ���
		System.out.println("--->>MainActivity��Ҫ�첽���ص�ͼƬ��Ӧ��ѧ��Ϊ:"
				+ _groupID+"��"+_examnieeID+"��");
		saveExamnieeInfo(_groupID);
	}
	//ѡ��ĳһ�飬����gridview���¸���Ŀ�����Ϣ

	private void updateGroupInfoInGridViewFunc(int groupID){
		//int examnieeNum = mDbManager.queryExamnieeNumInGroup(groupID + "");
		initExamedByte();
		//int maxExamnieeID= mDbManager.queryMaxExamnieeIDInGroup(groupID + "");
		ExamnieeInfo examnieeInfo =  new ExamnieeInfo();
		for (int index = 1; index <= 10; index++) {
			View cur_view = examineeGrid.getChildAt(index - 1);
			if (cur_view != null) {
				Log.i(LOG_TAG, "-----<<<<<<<<gridview��cur_view != null");
				ImageView imageView = (ImageView) cur_view
						.findViewById(R.id.itemImage);
				TextView textView = (TextView) cur_view
						.findViewById(R.id.stdNum);
				if (textView != null && imageView != null ) {
					Log.i(LOG_TAG, "-----<<<<<<<<gridview��textView != null��imageView != null");
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
							Log.i(LOG_TAG, "-----<<<<<<<<��gridview����ʾͼƬ��"+groupID+"��"+index+"��:::");
							imageLoader.DisplayImage(examnieeInfo
									.getExamnieePicURL(), imageView); 
							//}

							if(mExamStatus==FrameFormat.STATUS_HAVAEXAMED){
								System.out.println("-----<<<<<<<<��"+groupID+"��"+index+"��:::�Ѿ�����"
										+examnieeInfo.getExamnieeStatus().equals(FrameFormat.STATUS_HAVAEXAMED+""));
								Examed[index-1] = FrameFormat.STATUS_HAVAEXAMED;
								cur_view.setBackgroundResource(R.drawable.login_shape);
								cur_view.setAlpha(0.5f);
								//imageView.setAlpha(0.5f);
							}else if(mExamStatus==FrameFormat.STATUS_EXAMING){
								System.out.println("-----<<<<<<<<��"+groupID+"��"+index+"��:::���ڿ���"
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
							System.out.println("------��ǰ������="+curExamingGroupID+" ��ǰ���µ���="+groupID);
						}else{
							//��С��������ŵ�λ��û��ѧ��
							Log.i(LOG_TAG, "-----<<<<<<<<С��������ŵ�λ��û��ѧ����������Ӧ����ʾ");
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
					//						Log.i(LOG_TAG, "-----<<<<<<<<��λ�����ˣ�������Ӧ����ʾ");
					//						imageLoader.DisplayImage(null, imageView); 
					//						Examed[index-1] = FrameFormat.STATUS_NOPERSON;
					//						textView.setText("");
					//						//textName.setText("");
					//						cur_view.setBackgroundResource(R.drawable.login_shape);
					//						imageView.setImageResource(R.drawable.default_lock);
					//						cur_view.setAlpha(1.0f);
					//						//imageView.setAlpha(1.0f);
					//						// cur_view.setClickable(false);
					//						// System.out.println("------���ɵ��"+index+" gridview item");
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
		System.out.println("---->��ȡͼƬbitmap�ɹ�");
		//�������ɹ�����totalStudentNum++;
		//		insertExamnieeInfo(String groupID,String examnieeID,String startTime,String endTime,String examinationID,
		//				String examnieeName,String examnieeSex,String examnieePicURL)
		//		if (mDbManager.insertExamnieeInfo(RecGroupNumStr, RecStudentNumStr,
		//				RecStartExamTime,RecStopExamTime,RecExamnieeID,RecStudentName,RecStudentIdentity,RecSex,RecImageURL)) {
		int  havaExamedStudentNum = mDbManager.queryNumAccordingTime(FrameFormat.STATUS_HAVAEXAMED);
		int toBeExamedStudentNum= mDbManager.queryNumAccordingTime(FrameFormat.STATUS_NOTEXAMED);
		//	mHavaExamedStudentText.setText(havaExamedStudentNum+"");
		mToExamStudentText.setText(toBeExamedStudentNum+ "");
		Log.d(LOG_TAG, "!!!!!!!!!!!��������1���ѿ�����="+havaExamedStudentNum+" ��������="+toBeExamedStudentNum);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				// ��û��ѡ�飬��listview��һ��item��Ӧ�������Ϣ��gridview��ʵʱ��ʾ
				// ��ѡ�е�������յ��Ĵ��������ţ���ʵʱ��ʾ
				if (choosedGroupID == 0) {
					System.out.println("---->��û��ѡ����ţ���listview�еĵ�һ����ʾ��gridview��:"+examGroupList.getFirstVisiblePosition());
					View cur_view = examGroupList.getChildAt(0);
					TextView textView = null;
					int firstGroup = 0;
					if (cur_view != null) {
						textView = (TextView) cur_view
								.findViewById(R.id.choosedGroupId);
						firstGroup = Integer.valueOf(textView.getText().toString());
						System.out.println("---->listview�еĵ�һ���Ӧ���Ϊ��"+firstGroup);
						updateGroupInfoInGridViewFunc(firstGroup);
					}else{
						System.out.println("---->cur_view==null");
					}
				} else if (choosedGroupID == Integer.valueOf(_groupID)) {
					System.out.println("---->listview�е�ѡ��item��Ӧ��ŵ����յ��ĸ�����");
					updateGroupInfoInGridViewFunc(choosedGroupID);
				}
			}
		}, 1000);

		//		}
		//		else {
		//			System.out.println("---->���ݲ���ʧ�ܣ������Ѵ��ڣ�");
		//		}
		//			} else {
		//				System.out.println("---->��ȡͼƬbitmapʧ��");
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
	}
	/****�õ����Խ׶�Ӣ��****/
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
	/****�õ���ĿӢ��****/
	public String getExamStyleStr(int examStyle)
	{
		switch(examStyle){
		case 1:return "����";
		case 2:return "����";
		case 3:return "����";
		default:return "����";
		}
	}
	/****�õ����Խ׶�Ӣ��****/
	public String getSubjectChinese(String subject)
	{
		if(subject.equals("Dance"))
			return "�赸";
		if(subject.equals("Vocal"))
			return "����";
		if(subject.equals("Piano"))
			return "����";
		else 
			return "����";
	}
	/****�õ���ĿӢ��****/
	public String getExamStyleChinese(String examStyle)
	{
		if(examStyle.equals("First"))
			return "����";
		if(examStyle.equals("Second"))
			return "����";
		if(examStyle.equals("Third"))
			return "����";
		else 
			return "����";
	}
	//����Ի���
	public void showWarningDialog(String message){
		CustomDialog dialog;
		final CustomDialog.Builder customBuilder = new
				CustomDialog.Builder(ArtCheckIn_Main.this);
		customBuilder.setTitle("��ʾ��Ϣ")
		.setIcon(R.drawable.tip)
		.setMessage(message)
		.setNeutralButton("ȷ   ��",
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
					Toast.makeText(getBaseContext(), "�ٰ�һ�η��ؼ��˳�����", Toast.LENGTH_SHORT).show();
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
						mConnectThread.StopThread();   //�ر��߳�
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
		{ //��ʱ����,ʱ�䵽���ص���ҳ��
			//			repeatTimes++;
			//if(repeatTimes<FrameFormat.MAXRETRANSTIMES)
//			String str[] = sendStr.split("\\|");
//			System.out.println("----hyz---sendStr"+ str[0]);
//			if (str[0] == FrameFormat.COMMAND_CONNECT){
//				SendInfoToServer(sendStr);
//				timer.start();
//			}
			if(isWaitingStartACK){
				//ֻ�е���һ�������յ�ACK�󣬲ſ��Ե����һ��������ʼ���ԣ����򣬹�5������һ�Σ����Է���BEG���ֱ���յ�ACKΪֹ
				isWaitingStartACK = false;
				Message dialogMsg = mDialogHandler.obtainMessage();
				dialogMsg.what = FrameFormat.STARTNOACK;
				dialogMsg.sendToTarget();
				//				Toast.makeText(getApplicationContext(), "������ʼ������֤��ʱ���˿���δ�ɹ���ʼ����", 2000).show();
			}
			else if(isWaitingStopACK){
				isWaitingStopACK = false;
				showWarningDialog("��������ʧ�ܣ����Ժ�����");
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
//		{ //��ʱ����,ʱ�䵽���ص���ҳ��
//			// TODO Auto-generated method stub
//			DefineVar.tickN = 0;
//			if (!isCall){
//				if(mConnectThread!=null){
//					mConnectThread.StopThread();
//					System.out.println("----mConnectThread.StopThread--ִ��");
//				}
//				startProgressDialog("�����������硤����");
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
		if (keyCode == KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0) {
			System.out.println("----key back down");
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
					mConnectThread.StopThread();   //�ر��߳�
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

