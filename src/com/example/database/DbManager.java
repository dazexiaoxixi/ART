package com.example.database;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import com.example.artcheckin.FrameFormat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DbManager {
	private static final String LOG_TAG = "DbManager";
	private static final String MAINTABLE_NAME = "artCheckInTable";// 表示数据库的名称
	private static final String SOCKETTABLE_NAME = "socketConfigTable";// 表示数据库的名称
	private static final String DATABASETABLE_NAME = "databaseConfigTable";// 表示表的名称
	private static final String USERTABLE_NAME = "userInfoTable";// 表示表的名称
	private DbOpenHelper helper=null;
	private SocketDbOpenHelper socketHelper=null;
	private ExamnieeInfo examnieeInfo = null;
	private SocketConfigInfo socketConfigInfo = null;
	public DbManager(Context context) {		
		helper=new DbOpenHelper(context);		
		System.out.println("---初始化DbManager");
	}
	public DbManager(Context context,String flag) {			
		socketHelper = new SocketDbOpenHelper(context);
		System.out.println("--!!!初始化DbManager");
	}
	/**
	 * 每个考生的信息是唯一的，groupID，examnieeID决定
	 * 先查询没有记录才插入记录
	 * @param 
	 * "groupID varchar(12)," +
				"examnieeID varchar(12),"+
				"examnieeName varchar(30)"+
				"examnieeStatus varchar(1)"+
				"examnieePic BLOB"+
	 * @return
	 */
	public boolean insertExamnieeInfo(String groupID,String examnieeID,String startTime,String endTime,String examinationID,
			String examnieeName,String examnieeIdentity,String examnieeSex,String examnieePicURL) {
		boolean flag = false;
		SQLiteDatabase database = null;
		Log.i(LOG_TAG, "---插入数据库考生信息。。。"+groupID+"组  "+examnieeID+"号");
		try {			
			Log.i(LOG_TAG, "---获取数据库，存储数据。。。");
			database = helper.getWritableDatabase();								
			ContentValues values = new ContentValues(); 
			values.put("groupID",groupID);
			values.put("examnieeID", examnieeID); 
			values.put("startTime", startTime); 
			values.put("endTime", endTime); 
			values.put("examinationID", examinationID); 
			values.put("examnieeName", examnieeName); 
			values.put("examnieeIdentity", examnieeIdentity); 
			values.put("examnieeSex", examnieeSex); 
			values.put("examnieePicURL",examnieePicURL);
			if (database.insertOrThrow(MAINTABLE_NAME, null, values)!=-1) {
				flag = true;
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---数据在数据库中已存在。。。");
			e.printStackTrace();			
		}finally{
			if(database!=null){
				database.close();
			}
		}
		return flag;
	}
	//查询数据库中是否有记录
	public boolean queryIsExistDataInTable() {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		boolean isExist = false;
		try {			
			database = helper.getWritableDatabase();
			cursor=database.query(MAINTABLE_NAME, null,null,null, null, null, null);
			if(cursor.moveToFirst()==false){
				isExist=false;
			}
			else isExist= true;
		} catch (Exception e) {
			Log.e(LOG_TAG, "---查询有误。");
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return isExist;
	}
	//查询，查询条件：groupID,examnieeID  查询项：examnieeName,examnieeStatus,examnieePicPath
//	public ExamnieeInfo queryExamnieeInfo(String _groupID,String _examnieeID) {
//		Cursor cursor=null;
//		SQLiteDatabase database = null;
//		try {		
//			examnieeInfo = new ExamnieeInfo();
//			database = helper.getWritableDatabase();
//			cursor=database.query(MAINTABLE_NAME, 
//					new String[]{"examnieeName,examnieeStatus,examnieePicURL,examinationID,examnieeSex"}, 
//					"groupID=? and examnieeID=?",new String[]{_groupID,_examnieeID}, null, null, null);
//			while(cursor.moveToNext()){
//				examnieeInfo.setExamnieeName(cursor.getString(cursor.getColumnIndex("examnieeName")));	
//				examnieeInfo.setExamnieeStatus(cursor.getString(cursor.getColumnIndex("examnieeStatus")));
//				examnieeInfo.setExamnieePicURL(cursor.getString(cursor.getColumnIndex("examnieePicURL")));
//				examnieeInfo.setExaminationID(cursor.getString(cursor.getColumnIndex("examinationID")));	
//				examnieeInfo.setExamnieeSex(cursor.getString(cursor.getColumnIndex("examnieeSex")));
//			}
//			Log.i(LOG_TAG, "-----取到"+"第"+_groupID+"组 第"+_examnieeID+"号"+"学生信息：姓名 "
//					+examnieeInfo.getExamnieeName() +" "+examnieeInfo.getExamnieeStatus()+" URL:"+examnieeInfo.getExamnieePicURL()
//					+"考号:"+examnieeInfo.getExaminationID()+"性别:"+examnieeInfo.getExamnieeSex());						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---queryExamnieeInfo未查询到"+"第"+_groupID+"组 第"+_examnieeID+"号"+"学生信息");
//			examnieeInfo=null;
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return examnieeInfo;
//	}	
	
	public ExamnieeInfo queryExamnieeInfoNew(String _groupID,String _examnieeID) {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		String startTimeGet = null;
		String endTimeGet = null;
		try {		
			examnieeInfo = new ExamnieeInfo();
			database = helper.getWritableDatabase();
			cursor=database.query(MAINTABLE_NAME, 
					new String[]{"examnieeName,examnieeIdentity,startTime,endTime,examnieePicURL,examinationID,examnieeSex"}, 
					"groupID=? and examnieeID=?",new String[]{_groupID,_examnieeID}, null, null, null);
			while(cursor.moveToNext()){
				examnieeInfo.setExamnieeName(cursor.getString(cursor.getColumnIndex("examnieeName")));	
				startTimeGet = cursor.getString(cursor.getColumnIndex("startTime"));
				endTimeGet = cursor.getString(cursor.getColumnIndex("endTime"));
				if(startTimeGet.equals("0")&&endTimeGet.equals("0")){
					examnieeInfo.setExamnieeStatus(FrameFormat.STATUS_NOTEXAMED+"");
				}
				else if(startTimeGet!=null&&!startTimeGet.equals("0")&&endTimeGet.equals("0")){
					examnieeInfo.setExamnieeStatus(FrameFormat.STATUS_EXAMING+"");
				}
				else if(startTimeGet!=null&&!startTimeGet.equals("0")&&endTimeGet!=null&&!endTimeGet.equals("0")){
					examnieeInfo.setExamnieeStatus(FrameFormat.STATUS_HAVAEXAMED+"");
				}
				//examnieeInfo.setExamnieeStatus(cursor.getString(cursor.getColumnIndex("examnieeStatus")));
				examnieeInfo.setExamnieeIdentity(cursor.getString(cursor.getColumnIndex("examnieeIdentity")));
				examnieeInfo.setExamnieePicURL(cursor.getString(cursor.getColumnIndex("examnieePicURL")));
				examnieeInfo.setExaminationID(cursor.getString(cursor.getColumnIndex("examinationID")));	
				examnieeInfo.setExamnieeSex(cursor.getString(cursor.getColumnIndex("examnieeSex")));
			}
			Log.i(LOG_TAG, "-----取到"+"第"+_groupID+"组 第"+_examnieeID+"号"+"学生信息：姓名 "
					+examnieeInfo.getExamnieeName() +" "+examnieeInfo.getExamnieeStatus()+" URL:"+examnieeInfo.getExamnieePicURL()
					+"考号:"+examnieeInfo.getExaminationID()+"性别:"+examnieeInfo.getExamnieeSex());						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---queryExamnieeInfo未查询到"+"第"+_groupID+"组 第"+_examnieeID+"号"+"学生信息");
			examnieeInfo=null;
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return examnieeInfo;
	}	
	////查询，查询条件：groupID,examnieeID  
	public boolean queryOneExamnieeInfoIsExist(String _groupID,String _examnieeID) {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		boolean isExist = false;
		try {			
			database = helper.getWritableDatabase();
			cursor=database.query(MAINTABLE_NAME, null, 
					"groupID=? and examnieeID=?",new String[]{_groupID,_examnieeID}, null, null, null);
			if(cursor.moveToFirst()==false){
				isExist=false;
			}
			else isExist= true;
		} catch (Exception e) {
			Log.e(LOG_TAG, "---查询有误。");
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return isExist;
	}	
	//查询总人数
	public int queryExamnieeNumInTable() {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		int Number = 0;
		try {			
			database = helper.getReadableDatabase();
			cursor=database.query(MAINTABLE_NAME,null,null,null, null, null, null);
			if(cursor!=null){
				Number=cursor.getCount();
			}
			//			while(cursor.moveToNext()){
			//				Number ++;
			//			}
			Log.i(LOG_TAG, "!!!!!!!!!!!本数据库共有"+Number+"个学生");						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---未查询到学生信息。。。");
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return Number;
	}	
	//查询，查询条件：groupID  查询groupID中该组的人数
	public int queryExamnieeNumInGroup(String _groupID) {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		int Number = 0;
		try {			
			database = helper.getReadableDatabase();
			cursor=database.query(MAINTABLE_NAME,null,"groupID=?",new String[]{_groupID}, null, null, null);
			while(cursor.moveToNext()){
				Number ++;
			}
			Log.i(LOG_TAG, "-----该组共有"+Number+"个学生");						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---未查询到学生信息。。。");
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return Number;
	}	
	//查询，查询条件：groupID  查询groupID中该组学生的最大序号，在更新gridview时使用
//	public int queryMaxExamnieeIDInGroup(String _groupID) {
//		Cursor cursor=null;
//		SQLiteDatabase database = null;
//		int maxID = 0;
//		int getExamnieeID = 0;
//		try {			
//			database = helper.getReadableDatabase();
//			cursor=database.query(MAINTABLE_NAME,new String[]{"examnieeID"},"groupID=?",new String[]{_groupID}, null, null, null);
//			while(cursor.moveToNext()){
//				getExamnieeID = Integer.valueOf(cursor.getString(cursor.getColumnIndex("examnieeID")));
//				if(getExamnieeID>maxID){
//					maxID = getExamnieeID;
//				}
//			}
//			Log.i(LOG_TAG, "-----该组最大的学生序号为："+maxID);						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---未查询到学生信息。。。");
//			e.printStackTrace();			
//		}finally{
//			if(cursor!=null){
//				cursor.close();
//			}
//			
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return maxID;
//	}	
	//查询，查询条件：examnieeStatus  查询组号集合Set
	//private LinkedHashSet<String> groupSet = new LinkedHashSet<String>();
//	public LinkedHashSet<String> queryGroupSetAccordingStatus(String _examnieeStatus) {
//		Cursor cursor=null;
//		SQLiteDatabase database = null;
//		LinkedHashSet<String> groupSet = new LinkedHashSet<String>();
//		try {			
//			database = helper.getReadableDatabase();
//			cursor=database.query(MAINTABLE_NAME,new String[]{"groupID"},"examnieeStatus=?",new String[]{_examnieeStatus}, null, null, null);
//			while(cursor.moveToNext()){
//				groupSet.add(cursor.getString(cursor.getColumnIndex("groupID")));
//			}
//			Log.i(LOG_TAG, "-----");						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---未查询到学生信息。。。");
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return groupSet;
//	}
	public LinkedHashSet<String> queryGroupSetAccordingTime(int _examnieeStatus) {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		LinkedHashSet<String> groupSet = new LinkedHashSet<String>();
		try {			
			database = helper.getReadableDatabase();
			if(_examnieeStatus==FrameFormat.STATUS_NOTEXAMED){
				cursor=database.query(MAINTABLE_NAME,new String[]{"groupID"},"startTime=? and endTime=?",new String[]{"0","0"}, null, null, null);
			}else if(_examnieeStatus==FrameFormat.STATUS_EXAMING){
				cursor=database.query(MAINTABLE_NAME,new String[]{"groupID"},"startTime<>? and endTime=?",new String[]{"0","0"}, null, null, null);
			}else if(_examnieeStatus==FrameFormat.STATUS_HAVAEXAMED){
				cursor=database.query(MAINTABLE_NAME,new String[]{"groupID"},"startTime<>? and endTime<>?",new String[]{"0","0"}, null, null, null);
			}else if(_examnieeStatus==FrameFormat.STATUS_NOTEXAMEDOREXAMING){
				cursor=database.query(MAINTABLE_NAME,new String[]{"groupID"},"groupID<>? and endTime=?",new String[]{"0","0"}, null, null, null);
			}
			
			while(cursor.moveToNext()){
				groupSet.add(cursor.getString(cursor.getColumnIndex("groupID")));
			}
			Log.i(LOG_TAG, "-----");						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---未查询到学生信息。。。");
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return groupSet;
	}
//	//查询，查询条件：examnieeStatus  查询已考/缺考/未考/人数
//	public int queryNumAccordingStatus(String _examnieeStatus) {
//		Cursor cursor=null;
//		SQLiteDatabase database = null;
//		int Number = 0;
//		try {			
//			database = helper.getReadableDatabase();
//			cursor=database.query(MAINTABLE_NAME,null,"examnieeStatus=?",new String[]{_examnieeStatus}, null, null, null);
//			while(cursor.moveToNext()){
//				Number ++;
//			}
//			Log.i(LOG_TAG, "-----");						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---未查询到学生信息。。。");
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return Number;
//	}	
	//查询，查询条件：examnieeStatus  查询已考/缺考/未考/人数
		public int queryNumAccordingTime(int _examnieeStatus) {
			Cursor cursor=null;
			SQLiteDatabase database = null;
			int Number = 0;
			try {			
				database = helper.getReadableDatabase();
				if(_examnieeStatus==FrameFormat.STATUS_NOTEXAMED){
					cursor=database.query(MAINTABLE_NAME,null,"startTime=? and endTime=?",new String[]{"0","0"}, null, null, null);
				}else if(_examnieeStatus==FrameFormat.STATUS_EXAMING){
					cursor=database.query(MAINTABLE_NAME,null,"startTime<>? and endTime=?",new String[]{"0","0"}, null, null, null);
				}else if(_examnieeStatus==FrameFormat.STATUS_HAVAEXAMED){
					cursor=database.query(MAINTABLE_NAME,null,"startTime<>? and endTime<>?",new String[]{"0","0"}, null, null, null);
				}
				while(cursor.moveToNext()){
					Number ++;
				}
				Log.i(LOG_TAG, "-----");						
			} catch (Exception e) {
				Log.e(LOG_TAG, "---未查询到学生信息。。。");
				e.printStackTrace();			
			}finally{
				if(cursor!=null){
					cursor.close();
				}
				if(database!=null){
					database.close();				
				}
			}
			return Number;
		}	
//	//查询，查询条件：groupID  查询该组中第一个开考学生的开考时间
//	public String queryStartTimeOfFirstStart(String _groupID,String _isFirstStart) {
//		Log.i(LOG_TAG, "---queryStartTimeOfFirstStart。。。"+_groupID+"组  ");
//		Cursor cursor=null;
//		SQLiteDatabase database = null;
//		String mStartTime = null;
//		try {			
//			database = helper.getReadableDatabase();
//			cursor=database.query(MAINTABLE_NAME, 
//					new String[]{"examnieeName,startTime"}, 
//					"groupID=? and isFirstStart=?",new String[]{_groupID,_isFirstStart}, null, null, null);
//			Log.i(LOG_TAG, "-----cursor="+cursor);
//			while(cursor.moveToNext()){
//				mStartTime = cursor.getString(cursor.getColumnIndex("startTime"));
//			}
//
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---未查询到学生开考时间。。。");
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return mStartTime;
//	}	
	
	//查询，查询条件：groupID  查询该组中第一个开考学生的开考时间
	public String queryStartTimeOfFirstStartNew(String _groupID) {
		Log.i(LOG_TAG, "---queryStartTimeOfFirstStart。。。"+_groupID+"组  ");
		Cursor cursor=null;
		SQLiteDatabase database = null;
		String mStartTime = null;
		String mMinStartTime = null;
		try {			
			database = helper.getReadableDatabase();
			cursor=database.query(MAINTABLE_NAME, 
					new String[]{"startTime"}, 
					"groupID=? and startTime<>?",new String[]{_groupID,"0"}, null, null, null);
			Log.i(LOG_TAG, "-----cursor="+cursor);
			if(cursor.moveToNext()){
				mMinStartTime = cursor.getString(cursor.getColumnIndex("startTime"));
			}
			while(cursor.moveToNext()){
				mStartTime = cursor.getString(cursor.getColumnIndex("startTime"));
				//if(mStartTime<mMinStartTime){
				if(compare_date(mStartTime,mMinStartTime)==-1){
					mMinStartTime = mStartTime;
				}
			}

		} catch (Exception e) {
			Log.e(LOG_TAG, "---未查询到学生开考时间。。。");
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return mMinStartTime;
	}	
//	//查询，查询条件：groupID  查询该组中第一个开考学生的开考时间
//	public ExamnieeInfo queryExamnieeInfoOfFirstStart(String _isFirstStart) {
//		Cursor cursor=null;
//		SQLiteDatabase database = null;
//		try {			
//			examnieeInfo = new ExamnieeInfo();
//			database = helper.getReadableDatabase();
//			cursor=database.query(MAINTABLE_NAME, 
//					new String[]{"groupID,examnieeID"}, 
//					"isFirstStart=?",new String[]{_isFirstStart}, null, null, null);
//			while(cursor.moveToNext()){
//				examnieeInfo.setGroupID(cursor.getString(cursor.getColumnIndex("groupID")));	
//				examnieeInfo.setExamnieeID(cursor.getString(cursor.getColumnIndex("examnieeID")));	
//			}
//
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---未查询到学生开考时间。。。");
//			examnieeInfo = null;
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return examnieeInfo;
//	}	
	//查询，查询第一个开考学生的信息组号、序号，先不写，只有在查询是否有正在考试的组时用了。功能同上一个queryExamnieeInfoOfFirstStart函数是一样的，
	//只是不用isFirstStart这一项了
		public String queryExamingGroupAccordingTime() {
			Cursor cursor=null;
			SQLiteDatabase database = null;
			String groupId = null;
			try {			
				//examnieeInfo = new ExamnieeInfo();
				Log.e(LOG_TAG, "--------查询正在考试的组。。。");
				database = helper.getReadableDatabase();
				cursor=database.query(MAINTABLE_NAME, 
						new String[]{"groupID,examnieeID"}, 
						"startTime<>? and endTime<>?",new String[]{"0","0"}, null, null, null);
				while(cursor.moveToNext()){
					groupId = cursor.getString(cursor.getColumnIndex("groupID"));
//					examnieeInfo.setGroupID(cursor.getString(cursor.getColumnIndex("groupID")));	
//					examnieeInfo.setExamnieeID(cursor.getString(cursor.getColumnIndex("examnieeID")));	
				}
				Log.e(LOG_TAG, "--------查询到正在考试的组。。。"+groupId);
			} catch (Exception e) {
				Log.e(LOG_TAG, "--------未查询到正在考试的组。。。");
				e.printStackTrace();			
			}finally{
				if(cursor!=null){
					cursor.close();
				}
				if(database!=null){
					database.close();				
				}
			}
			return groupId;
		}	
//	//查询，查询条件：考试状态  查询项：groupID,examnieeID
//	public ExamnieeInfo queryStudentInfoAccordingStatus(String _examnieeStatus) {
//		Cursor cursor=null;
//		SQLiteDatabase database = null;
//		try {			
//			examnieeInfo = new ExamnieeInfo();
//			database = helper.getWritableDatabase();
//			cursor=database.query(MAINTABLE_NAME, 
//					new String[]{"groupID,examnieeID"}, 
//					"examnieeStatus=?",new String[]{_examnieeStatus}, null, null, null);
//			while(cursor.moveToNext()){
//				examnieeInfo.setGroupID(cursor.getString(cursor.getColumnIndex("groupID")));	
//				examnieeInfo.setExamnieeID(cursor.getString(cursor.getColumnIndex("examnieeID")));
//			}
//			Log.i(LOG_TAG, "-----取到当前正在考试:"+"第"+examnieeInfo.getGroupID()+"组 第"+examnieeInfo.getExamnieeID()+"号");						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---未查询到配置信息。。。");
//			examnieeInfo = null;
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return examnieeInfo;
//	}	
	public ExamnieeInfo queryStudentInfoAccordingTime(int _examnieeStatus) {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		try {			
			examnieeInfo = new ExamnieeInfo();
			database = helper.getWritableDatabase();
			if(_examnieeStatus==FrameFormat.STATUS_NOTEXAMED){
				cursor=database.query(MAINTABLE_NAME,new String[]{"groupID,examnieeID"},"startTime=? and endTime=?",new String[]{"0","0"}, null, null, null);
			}else if(_examnieeStatus==FrameFormat.STATUS_EXAMING){
				cursor=database.query(MAINTABLE_NAME,new String[]{"groupID,examnieeID"},"startTime<>? and endTime=?",new String[]{"0","0"}, null, null, null);
			}else if(_examnieeStatus==FrameFormat.STATUS_HAVAEXAMED){
				cursor=database.query(MAINTABLE_NAME,new String[]{"groupID,examnieeID"},"startTime<>? and endTime<>?",new String[]{"0","0"}, null, null, null);
			}
			while(cursor.moveToNext()){
				examnieeInfo.setGroupID(cursor.getString(cursor.getColumnIndex("groupID")));	
				examnieeInfo.setExamnieeID(cursor.getString(cursor.getColumnIndex("examnieeID")));
			}
			Log.i(LOG_TAG, "-----取到当前正在考试:"+"第"+examnieeInfo.getGroupID()+"组 第"+examnieeInfo.getExamnieeID()+"号");						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---未查询到配置信息。。。");
			examnieeInfo = null;
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return examnieeInfo;
	}	
//	//查询，查询条件：groupID,examnieeID  查询考生考试状态
//	public String queryExamStatus(String _groupID,String _examnieeID) {
//		Cursor cursor=null;
//		SQLiteDatabase database = null;
//		String examnieeStatus = null;
//		try {			
//			database = helper.getReadableDatabase();
//			cursor=database.query(MAINTABLE_NAME,new String[]{"examnieeStatus"},"groupID=?,examnieeID=?",new String[]{_groupID,_examnieeID}, null, null, null);
//			while(cursor.moveToNext()){
//				examnieeStatus = cursor.getString(cursor.getColumnIndex("examnieeStatus"));
//			}
//			Log.i(LOG_TAG, "-----");						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---未查询到学生信息。。。");
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return examnieeStatus;
//	}	
	
	//更新考生考试状态：0：未考；1：正在考试；2：考完；3：缺考
//	public boolean updateExamStatus(String _groupID,String _examnieeID,String examnieeStatus) {//先删除，再插入
//		Log.i(LOG_TAG, "---更新缺考状态。。。"+_groupID+"组  "+_examnieeID+"号");
//		boolean flag = false;
//		SQLiteDatabase database = null;
//		database = helper.getWritableDatabase();
//		try {			
//			ContentValues values = new ContentValues(); 
//			values.put("examnieeStatus", examnieeStatus);
//			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
//				flag = true;
//				Log.i(LOG_TAG, "---成功更新。。。");
//			}						
//		} catch (Exception e) {
//			Log.i(LOG_TAG, "---插入更新。。。");
//			e.printStackTrace();
//			flag = false;
//		}finally{
//			if(database!=null){
//				database.close();
//				Log.i(LOG_TAG, "关闭dStatus。。。");
//			}
//		}
//		return flag;
//	}
	//更新考生是否为所在组第一个开考，若是，则存入1
//	public boolean updateIsFirstStart(String _groupID,String _examnieeID,String isFirstStart) {
//		Log.i(LOG_TAG, "---updateIsFirstStart。。。"+_groupID+"组  "+_examnieeID+"号");
//		boolean flag = false;
//		SQLiteDatabase database = null;
//		database = helper.getWritableDatabase();
//		try {			
//			ContentValues values = new ContentValues(); 
//			values.put("isFirstStart", isFirstStart);
//			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
//				flag = true;
//				Log.i(LOG_TAG, "---成功更新。。。");
//			}						
//		} catch (Exception e) {
//			Log.i(LOG_TAG, "---插入更新。。。");
//			e.printStackTrace();
//			flag = false;
//		}finally{
//			if(database!=null){
//				database.close();
//				Log.i(LOG_TAG, "关闭dStatus。。。");
//			}
//		}
//		return flag;
//	}
	//将第一个开始考试的考生设置的flag isFirstStart设置为初始值0
//	public boolean updateFirstStartToInit(String _groupID) {
//		boolean flag = false;
//		SQLiteDatabase database = null;
//		database = helper.getWritableDatabase();
//		try {			
//			ContentValues values = new ContentValues(); 
//			values.put("isFirstStart", FrameFormat.FLAG_ISNOTFIRSTSTART);
//			if (database.update(MAINTABLE_NAME, values, "groupID=? and isFirstStart=?", new String[]{_groupID,FrameFormat.FLAG_ISFIRSTSTART})!=-1) {
//				flag = true;
//				Log.i(LOG_TAG, "---成功将第一个开始考试的考生设置的flag isFirstStart设置为初始值0。。。");
//			}						
//		} catch (Exception e) {
//			Log.i(LOG_TAG, "---不成功设置isFirstStart。。。");
//			e.printStackTrace();
//			flag = false;
//		}finally{
//			if(database!=null){
//				database.close();
//				Log.i(LOG_TAG, "关闭dStatus。。。");
//			}
//		}
//		return flag;
//	}
	//更新考生开始和结束考试时间
	public boolean updateExamTime(String _groupID,String _examnieeID,String mStartTime,String mEndTime) {//先删除，再插入
		Log.i(LOG_TAG, "---updateEndExamTime。。。"+_groupID+"组  "+_examnieeID+"号");
		boolean flag = false;
		SQLiteDatabase database = null;
		database = helper.getWritableDatabase();
		try {			
			ContentValues values = new ContentValues(); 
			values.put("startTime", mStartTime);
			values.put("endTime", mEndTime);
			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
				flag = true;
				Log.i(LOG_TAG, "---成功更新。。。");
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---插入更新。。。");
			e.printStackTrace();
			flag = false;
		}finally{
			if(database!=null){
				database.close();
				Log.i(LOG_TAG, "关闭dStatus。。。");
			}
		}
		return flag;
	}
	//更新考生开考时间
	public boolean updateStartExamTime(String _groupID,String _examnieeID,String mStartTime) {//先删除，再插入
		Log.i(LOG_TAG, "---updateStartExamTime。。。"+_groupID+"组  "+_examnieeID+"号"+" 开始时间:"+mStartTime);
		boolean flag = false;
		SQLiteDatabase database = null;
		database = helper.getWritableDatabase();
		try {			
			ContentValues values = new ContentValues(); 
			values.put("startTime", mStartTime);
			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
				flag = true;
				Log.i(LOG_TAG, "---成功更新开考时间。。。");
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---插入更新。。。");
			e.printStackTrace();
			flag = false;
		}finally{
			if(database!=null){
				database.close();
				Log.i(LOG_TAG, "关闭dStatus。。。");
			}
		}
		return flag;
	}
	//更新考生结束考试时间
	public boolean updateEndExamTime(String _groupID,String _examnieeID,String mEndTime) {//先删除，再插入
		Log.i(LOG_TAG, "---updateEndExamTime。。。"+_groupID+"组  "+_examnieeID+"号"+" 结束时间:"+mEndTime);
		boolean flag = false;
		SQLiteDatabase database = null;
		database = helper.getWritableDatabase();
		try {			
			ContentValues values = new ContentValues(); 
			values.put("endTime", mEndTime);
			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
				flag = true;
				Log.i(LOG_TAG, "---成功更新。。。");
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---插入更新。。。");
			e.printStackTrace();
			flag = false;
		}finally{
			if(database!=null){
				database.close();
				Log.i(LOG_TAG, "关闭dStatus。。。");
			}
		}
		return flag;
	}

//删除已经考试了的组的考生信息
	public boolean deleteHavaExamedExamnieeInfo(String _groupID) {//先删除，再插入
		Log.i(LOG_TAG, "---deleteHavaExamedExamnieeInfo。。。"+_groupID+"组  ");
		boolean flag = false;
		SQLiteDatabase database = null;
		database = helper.getWritableDatabase();
		try {			
			if (database.delete(MAINTABLE_NAME, "groupID=?", new String[]{_groupID})!=-1) {
				flag = true;
				Log.i(LOG_TAG, "---删除已考组。。。"+_groupID+"组  成功");
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---删除已考组。。。。"+_groupID+"组  失败");
			e.printStackTrace();
		}finally{
			if(database!=null){
				database.close();
				Log.i(LOG_TAG, "关闭dStatus。。。");
			}
		}
		return flag;
	}
	//SOCKETTABLE_NAME 的操作函数
	//插入一条记录
	public boolean insertSocketConfi(String ip,String port) {
		boolean flag = false;
		SQLiteDatabase database = null;
		Log.i(LOG_TAG, "---插入数据库socket配置信息。。。"+ip+"网址  "+port+"号");
		try {			
			Log.i(LOG_TAG, "---获取socket数据库，存储数据。。。");
			database = socketHelper.getWritableDatabase();	
			ContentValues values = new ContentValues(); 
			Cursor cursor = database.query(SOCKETTABLE_NAME,new String[]{"Ip","Port"},null,null, null, null, null);
			values.put("Ip",ip);
			values.put("Port", port); 
			if(cursor.moveToFirst()!=false){
				if(database.delete(SOCKETTABLE_NAME, null, null)==-1){
					return false;
				}
			}
			if (database.insertOrThrow(SOCKETTABLE_NAME, null, values)!=-1) {
				flag = true;
			}		
			Log.i(LOG_TAG, "---成功插入数据库socket配置信息。。。"+ip+"网址  "+port+"号");
		} catch (Exception e) {
			Log.i(LOG_TAG, "---socket数据在数据库中已存在。。。");
			e.printStackTrace();			
		}finally{
			if(database!=null){
				database.close();
			}
		}
		return flag;
	}
	//查询数据库中是否有记录
	public boolean queryIsExistDataInSocketTable() {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		boolean isExist = false;
		try {			
			database = socketHelper.getWritableDatabase();
			cursor=database.query(SOCKETTABLE_NAME, null,null,null, null, null, null);
			if(cursor.moveToFirst()==false){
				isExist=false;
			}
			else isExist= true;
		} catch (Exception e) {
			Log.e(LOG_TAG, "---查询有误。");
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return isExist;
	}
	//查询最后1条记录
	public SocketConfigInfo querySocketConfigInfoOfLastOne() {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		try {			
			socketConfigInfo = new SocketConfigInfo();
			database = socketHelper.getReadableDatabase();
			cursor=database.query(SOCKETTABLE_NAME, 
					new String[]{"Ip,Port"}, 
					null,null, null, null, null);
			while(cursor.moveToNext()){
				if(cursor.isLast()){
					socketConfigInfo.setIpStr(cursor.getString(cursor.getColumnIndex("Ip")));	
					socketConfigInfo.setPortStr(cursor.getString(cursor.getColumnIndex("Port")));	
				}
			}

		} catch (Exception e) {
			Log.e(LOG_TAG, "--!!!未查询到最后一条socket信息。。。");
			socketConfigInfo = null;
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return socketConfigInfo;
	}	
	//UserTable_Name的操作函数
		//插入一条记录
		public boolean insertUserName(String userName) {
			boolean flag = false;
			SQLiteDatabase database = null;
			Log.i(LOG_TAG, "---插入用户名信息。。。");
			try {			
				Log.i(LOG_TAG, "---获取socket数据库，存储数据。。。");
				database = socketHelper.getWritableDatabase();	
				ContentValues values = new ContentValues(); 
				Cursor cursor = database.query(USERTABLE_NAME,new String[]{"userName"},null,null, null, null, null);
				values.put("userName",userName);
				if(cursor.moveToFirst()!=false){
					if(database.delete(USERTABLE_NAME, null, null)==-1){
						return false;
					}
				}
				if (database.insertOrThrow(USERTABLE_NAME, null, values)!=-1) {
					flag = true;
				}		
//				Log.i(LOG_TAG, "---成功插入数据库socket配置信息。。。"+ip+"网址  "+port+"号");
			} catch (Exception e) {
				Log.i(LOG_TAG, "---userName数据在数据库中已存在。。。");
				e.printStackTrace();			
			}finally{
				if(database!=null){
					database.close();
				}
			}
			return flag;
		}
		//查询数据库中是否有记录
		public boolean queryIsExistDataInUserNameTable() {
			Cursor cursor=null;
			SQLiteDatabase database = null;
			boolean isExist = false;
			try {			
				database = socketHelper.getWritableDatabase();
				cursor=database.query(USERTABLE_NAME, null,null,null, null, null, null);
				if(cursor.moveToFirst()==false){
					isExist=false;
				}
				else isExist= true;
			} catch (Exception e) {
				Log.e(LOG_TAG, "---查询有误。");
				e.printStackTrace();			
			}finally{
				if(cursor!=null){
					cursor.close();
				}
				if(database!=null){
					database.close();				
				}
			}
			return isExist;
		}
		//查询最后1条记录
		public String queryUserNameInfoOfLastOne() {
			Cursor cursor=null;
			SQLiteDatabase database = null;
			String name = null;
			try {			
				socketConfigInfo = new SocketConfigInfo();
				database = socketHelper.getReadableDatabase();
				cursor=database.query(USERTABLE_NAME, 
						new String[]{"userName"}, 
						null,null, null, null, null);
				while(cursor.moveToNext()){
					if(cursor.isLast()){
						name = cursor.getString(cursor.getColumnIndex("userName"));
//						socketConfigInfo.setIpStr(cursor.getString(cursor.getColumnIndex("Ip")));	
//						socketConfigInfo.setPortStr(cursor.getString(cursor.getColumnIndex("Port")));	
					}
				}

			} catch (Exception e) {
				Log.e(LOG_TAG, "--!!!未查询到最后一条user信息。。。");
				e.printStackTrace();			
			}finally{
				if(cursor!=null){
					cursor.close();
				}
				if(database!=null){
					database.close();				
				}
			}
			return name;
		}	
	//DatabaseTABLE_NAME的操作函数
	public boolean insertDatabaseName(String name) {
		boolean flag = false;
		SQLiteDatabase database = null;
		Log.i(LOG_TAG, "--!!!插入数据库名字。。。"+name);
		try {		
			Log.i(LOG_TAG, "--!!!socketHelper = "+socketHelper);
			database = socketHelper.getWritableDatabase();	
			
			ContentValues values = new ContentValues(); 
			values.put("databaseName",name);
			if (database.insertOrThrow(DATABASETABLE_NAME, null, values)!=-1) {
				flag = true;
				Log.i(LOG_TAG, "--!!!插入数据库名字成功。。。"+name);
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "--!!!数据库名字在数据库中已存在。。。");
			e.printStackTrace();			
		}finally{
			if(database!=null){
				database.close();
			}
		}
		return flag;
	}
	//查询数据库名字
	public LinkedHashSet<String> queryExistingDatabase() {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		LinkedHashSet<String> databaseNameSet = new LinkedHashSet<String>();
		Log.i(LOG_TAG, "--!!!查询数据库名字。。。");
		try {			
			database = socketHelper.getReadableDatabase();
			cursor=database.query(DATABASETABLE_NAME,new String[]{"databaseName"},null,null, null, null, null);
			while(cursor.moveToNext()){
				Log.i(LOG_TAG, "--!!!查询到的数据库名字:"+cursor.getString(cursor.getColumnIndex("databaseName")));
				databaseNameSet.add(cursor.getString(cursor.getColumnIndex("databaseName")));
			}
									
		} catch (Exception e) {
			Log.e(LOG_TAG, "--!!!未查询到的数据库名字。。。");
			e.printStackTrace();			
		}finally{
			if(cursor!=null){
				cursor.close();
			}
			if(database!=null){
				database.close();				
			}
		}
		return databaseNameSet;
	}	
	//删除某个数据库
		public boolean deleteDatabase(Context context,String name) {
			boolean flag = false;
			SQLiteDatabase database = null;
			final File file = context.getDatabasePath(name);
			Log.e(LOG_TAG, "--!!!删除数据库:"+name);
			if(file.exists()){
				if(file.delete()){
	            	Log.e(LOG_TAG, "--!!!删除数据库成功:"+name);
	            	flag = true;
	            }
				try {			
	    			database = socketHelper.getWritableDatabase();								 
	    			if (database.delete(DATABASETABLE_NAME, "databaseName=?",
	    					new String[]{name})!=-1) {
	    				flag = true;
	    				Log.e(LOG_TAG, "--!!!删除databaseName记录成功:"+name);
	    			}						
	    		} catch (Exception e) {
	    			flag = false;
	    			e.printStackTrace();			
	    		}finally{
	    			if(database!=null){
	    				database.close();
	    			}
	    		}
			}
            return flag;
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

}
