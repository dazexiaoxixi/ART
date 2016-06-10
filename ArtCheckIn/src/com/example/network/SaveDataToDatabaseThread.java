package com.example.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.example.artcheckin.FrameFormat;
import com.example.database.DbManager;
import com.example.database.ExamnieeInfo;
import com.example.imagehandle_new.ImageLoader;

public class SaveDataToDatabaseThread extends Thread{
	private DbManager mDbManager = null;
	private List<ExamnieeInfo> list = null;
	public volatile boolean mSaveThreadStop = false;
	private ExamnieeInfo recvExamniee = null;
	Iterator<ExamnieeInfo> it = null;
	private final Object mutex = new Object();
	private Handler mHandler = null;
	private ImageLoader imageLoader;
	
	public SaveDataToDatabaseThread(Context context,Handler _mHandler){
		System.out.println("----lt-----cotext="+context);
		mDbManager = new DbManager(context);
		imageLoader=new ImageLoader(context);
		mHandler = _mHandler; 
		recvExamniee = new ExamnieeInfo();
		//list = new ArrayList<ExamnieeInfo>();
		list = Collections.synchronizedList(new LinkedList<ExamnieeInfo>());

	}
	public void run(){

		while(!mSaveThreadStop){

			if(list.size()>0){
				synchronized (mutex) {	
			
					recvExamniee = list.remove(0);
					
					System.out.println("----->>SaveDataToDatabaseThread list remove element："+recvExamniee.getGroupID()+"组"+recvExamniee.getExamnieeID()+"号");
				}
				
				if(mDbManager.insertExamnieeInfo(recvExamniee.getGroupID(), recvExamniee.getExamnieeID(),
						recvExamniee.getStartTime(),recvExamniee.getEndTime(),recvExamniee.getExaminationID(),recvExamniee.getExamnieeName(),
						recvExamniee.getExamnieeIdentity(),recvExamniee.getExamnieeSex(),recvExamniee.getExamnieePicURL())){
					System.out.println("----->>SaveDataToDatabaseThread数据库中成功插入一条数据："+recvExamniee.getGroupID()+"组"+recvExamniee.getExamnieeID()+"号");
				//	imageLoader.SaveImage(recvExamniee.getExamnieePicURL()); 
					Message msg = new Message();
				//	msg.obj = recvExamniee;
					msg.what = FrameFormat.HANDLE_UPDATEINFO;
					mHandler.sendMessage(msg);
				}else{
					//-----lt-----database 操作更改,加入下面一句，若数据库中已存在该数据，则更新开考时间和结束时间-----
					mDbManager.updateExamTime(recvExamniee.getGroupID(), recvExamniee.getExamnieeID(), recvExamniee.getStartTime(), recvExamniee.getEndTime());
					System.out.println("----->>SaveDataToDatabaseThread数据库中已存在该数据"+recvExamniee.getGroupID()+"组"+recvExamniee.getExamnieeID()+"号");
				}
			}
		}

	}
	public void StopThread(){
		mSaveThreadStop = true;
	}
	
	public void changeHandler(Handler _mHandler){
		mHandler = _mHandler;
	}
	/**
	 * 供Recorder放入待处理的数据
	 * 
	 * @param data
	 * @param size
	 */
	public void putData(ExamnieeInfo ei) {

		synchronized (mutex) {
			System.out.println("----->>SaveDataToDatabaseThread list add element："+ei.getGroupID()+"组"+ei.getExamnieeID()+"号");
			list.add(ei);
		}
	}
	
}
