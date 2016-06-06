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
	private static final String MAINTABLE_NAME = "artCheckInTable";// ��ʾ���ݿ������
	private static final String SOCKETTABLE_NAME = "socketConfigTable";// ��ʾ���ݿ������
	private static final String DATABASETABLE_NAME = "databaseConfigTable";// ��ʾ�������
	private static final String USERTABLE_NAME = "userInfoTable";// ��ʾ�������
	private DbOpenHelper helper=null;
	private SocketDbOpenHelper socketHelper=null;
	private ExamnieeInfo examnieeInfo = null;
	private SocketConfigInfo socketConfigInfo = null;
	public DbManager(Context context) {		
		helper=new DbOpenHelper(context);		
		System.out.println("---��ʼ��DbManager");
	}
	public DbManager(Context context,String flag) {			
		socketHelper = new SocketDbOpenHelper(context);
		System.out.println("--!!!��ʼ��DbManager");
	}
	/**
	 * ÿ����������Ϣ��Ψһ�ģ�groupID��examnieeID����
	 * �Ȳ�ѯû�м�¼�Ų����¼
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
		Log.i(LOG_TAG, "---�������ݿ⿼����Ϣ������"+groupID+"��  "+examnieeID+"��");
		try {			
			Log.i(LOG_TAG, "---��ȡ���ݿ⣬�洢���ݡ�����");
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
			Log.i(LOG_TAG, "---���������ݿ����Ѵ��ڡ�����");
			e.printStackTrace();			
		}finally{
			if(database!=null){
				database.close();
			}
		}
		return flag;
	}
	//��ѯ���ݿ����Ƿ��м�¼
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
			Log.e(LOG_TAG, "---��ѯ����");
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
	//��ѯ����ѯ������groupID,examnieeID  ��ѯ�examnieeName,examnieeStatus,examnieePicPath
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
//			Log.i(LOG_TAG, "-----ȡ��"+"��"+_groupID+"�� ��"+_examnieeID+"��"+"ѧ����Ϣ������ "
//					+examnieeInfo.getExamnieeName() +" "+examnieeInfo.getExamnieeStatus()+" URL:"+examnieeInfo.getExamnieePicURL()
//					+"����:"+examnieeInfo.getExaminationID()+"�Ա�:"+examnieeInfo.getExamnieeSex());						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---queryExamnieeInfoδ��ѯ��"+"��"+_groupID+"�� ��"+_examnieeID+"��"+"ѧ����Ϣ");
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
			Log.i(LOG_TAG, "-----ȡ��"+"��"+_groupID+"�� ��"+_examnieeID+"��"+"ѧ����Ϣ������ "
					+examnieeInfo.getExamnieeName() +" "+examnieeInfo.getExamnieeStatus()+" URL:"+examnieeInfo.getExamnieePicURL()
					+"����:"+examnieeInfo.getExaminationID()+"�Ա�:"+examnieeInfo.getExamnieeSex());						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---queryExamnieeInfoδ��ѯ��"+"��"+_groupID+"�� ��"+_examnieeID+"��"+"ѧ����Ϣ");
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
	////��ѯ����ѯ������groupID,examnieeID  
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
			Log.e(LOG_TAG, "---��ѯ����");
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
	//��ѯ������
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
			Log.i(LOG_TAG, "!!!!!!!!!!!�����ݿ⹲��"+Number+"��ѧ��");						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---δ��ѯ��ѧ����Ϣ������");
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
	//��ѯ����ѯ������groupID  ��ѯgroupID�и��������
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
			Log.i(LOG_TAG, "-----���鹲��"+Number+"��ѧ��");						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---δ��ѯ��ѧ����Ϣ������");
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
	//��ѯ����ѯ������groupID  ��ѯgroupID�и���ѧ���������ţ��ڸ���gridviewʱʹ��
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
//			Log.i(LOG_TAG, "-----��������ѧ�����Ϊ��"+maxID);						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---δ��ѯ��ѧ����Ϣ������");
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
	//��ѯ����ѯ������examnieeStatus  ��ѯ��ż���Set
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
//			Log.e(LOG_TAG, "---δ��ѯ��ѧ����Ϣ������");
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
			Log.e(LOG_TAG, "---δ��ѯ��ѧ����Ϣ������");
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
//	//��ѯ����ѯ������examnieeStatus  ��ѯ�ѿ�/ȱ��/δ��/����
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
//			Log.e(LOG_TAG, "---δ��ѯ��ѧ����Ϣ������");
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return Number;
//	}	
	//��ѯ����ѯ������examnieeStatus  ��ѯ�ѿ�/ȱ��/δ��/����
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
				Log.e(LOG_TAG, "---δ��ѯ��ѧ����Ϣ������");
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
//	//��ѯ����ѯ������groupID  ��ѯ�����е�һ������ѧ���Ŀ���ʱ��
//	public String queryStartTimeOfFirstStart(String _groupID,String _isFirstStart) {
//		Log.i(LOG_TAG, "---queryStartTimeOfFirstStart������"+_groupID+"��  ");
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
//			Log.e(LOG_TAG, "---δ��ѯ��ѧ������ʱ�䡣����");
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return mStartTime;
//	}	
	
	//��ѯ����ѯ������groupID  ��ѯ�����е�һ������ѧ���Ŀ���ʱ��
	public String queryStartTimeOfFirstStartNew(String _groupID) {
		Log.i(LOG_TAG, "---queryStartTimeOfFirstStart������"+_groupID+"��  ");
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
			Log.e(LOG_TAG, "---δ��ѯ��ѧ������ʱ�䡣����");
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
//	//��ѯ����ѯ������groupID  ��ѯ�����е�һ������ѧ���Ŀ���ʱ��
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
//			Log.e(LOG_TAG, "---δ��ѯ��ѧ������ʱ�䡣����");
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
	//��ѯ����ѯ��һ������ѧ������Ϣ��š���ţ��Ȳ�д��ֻ���ڲ�ѯ�Ƿ������ڿ��Ե���ʱ���ˡ�����ͬ��һ��queryExamnieeInfoOfFirstStart������һ���ģ�
	//ֻ�ǲ���isFirstStart��һ����
		public String queryExamingGroupAccordingTime() {
			Cursor cursor=null;
			SQLiteDatabase database = null;
			String groupId = null;
			try {			
				//examnieeInfo = new ExamnieeInfo();
				Log.e(LOG_TAG, "--------��ѯ���ڿ��Ե��顣����");
				database = helper.getReadableDatabase();
				cursor=database.query(MAINTABLE_NAME, 
						new String[]{"groupID,examnieeID"}, 
						"startTime<>? and endTime<>?",new String[]{"0","0"}, null, null, null);
				while(cursor.moveToNext()){
					groupId = cursor.getString(cursor.getColumnIndex("groupID"));
//					examnieeInfo.setGroupID(cursor.getString(cursor.getColumnIndex("groupID")));	
//					examnieeInfo.setExamnieeID(cursor.getString(cursor.getColumnIndex("examnieeID")));	
				}
				Log.e(LOG_TAG, "--------��ѯ�����ڿ��Ե��顣����"+groupId);
			} catch (Exception e) {
				Log.e(LOG_TAG, "--------δ��ѯ�����ڿ��Ե��顣����");
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
//	//��ѯ����ѯ����������״̬  ��ѯ�groupID,examnieeID
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
//			Log.i(LOG_TAG, "-----ȡ����ǰ���ڿ���:"+"��"+examnieeInfo.getGroupID()+"�� ��"+examnieeInfo.getExamnieeID()+"��");						
//		} catch (Exception e) {
//			Log.e(LOG_TAG, "---δ��ѯ��������Ϣ������");
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
			Log.i(LOG_TAG, "-----ȡ����ǰ���ڿ���:"+"��"+examnieeInfo.getGroupID()+"�� ��"+examnieeInfo.getExamnieeID()+"��");						
		} catch (Exception e) {
			Log.e(LOG_TAG, "---δ��ѯ��������Ϣ������");
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
//	//��ѯ����ѯ������groupID,examnieeID  ��ѯ��������״̬
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
//			Log.e(LOG_TAG, "---δ��ѯ��ѧ����Ϣ������");
//			e.printStackTrace();			
//		}finally{
//			cursor.close();
//			if(database!=null){
//				database.close();				
//			}
//		}
//		return examnieeStatus;
//	}	
	
	//���¿�������״̬��0��δ����1�����ڿ��ԣ�2�����ꣻ3��ȱ��
//	public boolean updateExamStatus(String _groupID,String _examnieeID,String examnieeStatus) {//��ɾ�����ٲ���
//		Log.i(LOG_TAG, "---����ȱ��״̬������"+_groupID+"��  "+_examnieeID+"��");
//		boolean flag = false;
//		SQLiteDatabase database = null;
//		database = helper.getWritableDatabase();
//		try {			
//			ContentValues values = new ContentValues(); 
//			values.put("examnieeStatus", examnieeStatus);
//			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
//				flag = true;
//				Log.i(LOG_TAG, "---�ɹ����¡�����");
//			}						
//		} catch (Exception e) {
//			Log.i(LOG_TAG, "---������¡�����");
//			e.printStackTrace();
//			flag = false;
//		}finally{
//			if(database!=null){
//				database.close();
//				Log.i(LOG_TAG, "�ر�dStatus������");
//			}
//		}
//		return flag;
//	}
	//���¿����Ƿ�Ϊ�������һ�����������ǣ������1
//	public boolean updateIsFirstStart(String _groupID,String _examnieeID,String isFirstStart) {
//		Log.i(LOG_TAG, "---updateIsFirstStart������"+_groupID+"��  "+_examnieeID+"��");
//		boolean flag = false;
//		SQLiteDatabase database = null;
//		database = helper.getWritableDatabase();
//		try {			
//			ContentValues values = new ContentValues(); 
//			values.put("isFirstStart", isFirstStart);
//			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
//				flag = true;
//				Log.i(LOG_TAG, "---�ɹ����¡�����");
//			}						
//		} catch (Exception e) {
//			Log.i(LOG_TAG, "---������¡�����");
//			e.printStackTrace();
//			flag = false;
//		}finally{
//			if(database!=null){
//				database.close();
//				Log.i(LOG_TAG, "�ر�dStatus������");
//			}
//		}
//		return flag;
//	}
	//����һ����ʼ���ԵĿ������õ�flag isFirstStart����Ϊ��ʼֵ0
//	public boolean updateFirstStartToInit(String _groupID) {
//		boolean flag = false;
//		SQLiteDatabase database = null;
//		database = helper.getWritableDatabase();
//		try {			
//			ContentValues values = new ContentValues(); 
//			values.put("isFirstStart", FrameFormat.FLAG_ISNOTFIRSTSTART);
//			if (database.update(MAINTABLE_NAME, values, "groupID=? and isFirstStart=?", new String[]{_groupID,FrameFormat.FLAG_ISFIRSTSTART})!=-1) {
//				flag = true;
//				Log.i(LOG_TAG, "---�ɹ�����һ����ʼ���ԵĿ������õ�flag isFirstStart����Ϊ��ʼֵ0������");
//			}						
//		} catch (Exception e) {
//			Log.i(LOG_TAG, "---���ɹ�����isFirstStart������");
//			e.printStackTrace();
//			flag = false;
//		}finally{
//			if(database!=null){
//				database.close();
//				Log.i(LOG_TAG, "�ر�dStatus������");
//			}
//		}
//		return flag;
//	}
	//���¿�����ʼ�ͽ�������ʱ��
	public boolean updateExamTime(String _groupID,String _examnieeID,String mStartTime,String mEndTime) {//��ɾ�����ٲ���
		Log.i(LOG_TAG, "---updateEndExamTime������"+_groupID+"��  "+_examnieeID+"��");
		boolean flag = false;
		SQLiteDatabase database = null;
		database = helper.getWritableDatabase();
		try {			
			ContentValues values = new ContentValues(); 
			values.put("startTime", mStartTime);
			values.put("endTime", mEndTime);
			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
				flag = true;
				Log.i(LOG_TAG, "---�ɹ����¡�����");
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---������¡�����");
			e.printStackTrace();
			flag = false;
		}finally{
			if(database!=null){
				database.close();
				Log.i(LOG_TAG, "�ر�dStatus������");
			}
		}
		return flag;
	}
	//���¿�������ʱ��
	public boolean updateStartExamTime(String _groupID,String _examnieeID,String mStartTime) {//��ɾ�����ٲ���
		Log.i(LOG_TAG, "---updateStartExamTime������"+_groupID+"��  "+_examnieeID+"��"+" ��ʼʱ��:"+mStartTime);
		boolean flag = false;
		SQLiteDatabase database = null;
		database = helper.getWritableDatabase();
		try {			
			ContentValues values = new ContentValues(); 
			values.put("startTime", mStartTime);
			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
				flag = true;
				Log.i(LOG_TAG, "---�ɹ����¿���ʱ�䡣����");
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---������¡�����");
			e.printStackTrace();
			flag = false;
		}finally{
			if(database!=null){
				database.close();
				Log.i(LOG_TAG, "�ر�dStatus������");
			}
		}
		return flag;
	}
	//���¿�����������ʱ��
	public boolean updateEndExamTime(String _groupID,String _examnieeID,String mEndTime) {//��ɾ�����ٲ���
		Log.i(LOG_TAG, "---updateEndExamTime������"+_groupID+"��  "+_examnieeID+"��"+" ����ʱ��:"+mEndTime);
		boolean flag = false;
		SQLiteDatabase database = null;
		database = helper.getWritableDatabase();
		try {			
			ContentValues values = new ContentValues(); 
			values.put("endTime", mEndTime);
			if (database.update(MAINTABLE_NAME, values, "groupID=? and examnieeID=?", new String[]{_groupID,_examnieeID})!=-1) {
				flag = true;
				Log.i(LOG_TAG, "---�ɹ����¡�����");
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---������¡�����");
			e.printStackTrace();
			flag = false;
		}finally{
			if(database!=null){
				database.close();
				Log.i(LOG_TAG, "�ر�dStatus������");
			}
		}
		return flag;
	}

//ɾ���Ѿ������˵���Ŀ�����Ϣ
	public boolean deleteHavaExamedExamnieeInfo(String _groupID) {//��ɾ�����ٲ���
		Log.i(LOG_TAG, "---deleteHavaExamedExamnieeInfo������"+_groupID+"��  ");
		boolean flag = false;
		SQLiteDatabase database = null;
		database = helper.getWritableDatabase();
		try {			
			if (database.delete(MAINTABLE_NAME, "groupID=?", new String[]{_groupID})!=-1) {
				flag = true;
				Log.i(LOG_TAG, "---ɾ���ѿ��顣����"+_groupID+"��  �ɹ�");
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "---ɾ���ѿ��顣������"+_groupID+"��  ʧ��");
			e.printStackTrace();
		}finally{
			if(database!=null){
				database.close();
				Log.i(LOG_TAG, "�ر�dStatus������");
			}
		}
		return flag;
	}
	//SOCKETTABLE_NAME �Ĳ�������
	//����һ����¼
	public boolean insertSocketConfi(String ip,String port) {
		boolean flag = false;
		SQLiteDatabase database = null;
		Log.i(LOG_TAG, "---�������ݿ�socket������Ϣ������"+ip+"��ַ  "+port+"��");
		try {			
			Log.i(LOG_TAG, "---��ȡsocket���ݿ⣬�洢���ݡ�����");
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
			Log.i(LOG_TAG, "---�ɹ��������ݿ�socket������Ϣ������"+ip+"��ַ  "+port+"��");
		} catch (Exception e) {
			Log.i(LOG_TAG, "---socket���������ݿ����Ѵ��ڡ�����");
			e.printStackTrace();			
		}finally{
			if(database!=null){
				database.close();
			}
		}
		return flag;
	}
	//��ѯ���ݿ����Ƿ��м�¼
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
			Log.e(LOG_TAG, "---��ѯ����");
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
	//��ѯ���1����¼
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
			Log.e(LOG_TAG, "--!!!δ��ѯ�����һ��socket��Ϣ������");
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
	//UserTable_Name�Ĳ�������
		//����һ����¼
		public boolean insertUserName(String userName) {
			boolean flag = false;
			SQLiteDatabase database = null;
			Log.i(LOG_TAG, "---�����û�����Ϣ������");
			try {			
				Log.i(LOG_TAG, "---��ȡsocket���ݿ⣬�洢���ݡ�����");
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
//				Log.i(LOG_TAG, "---�ɹ��������ݿ�socket������Ϣ������"+ip+"��ַ  "+port+"��");
			} catch (Exception e) {
				Log.i(LOG_TAG, "---userName���������ݿ����Ѵ��ڡ�����");
				e.printStackTrace();			
			}finally{
				if(database!=null){
					database.close();
				}
			}
			return flag;
		}
		//��ѯ���ݿ����Ƿ��м�¼
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
				Log.e(LOG_TAG, "---��ѯ����");
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
		//��ѯ���1����¼
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
				Log.e(LOG_TAG, "--!!!δ��ѯ�����һ��user��Ϣ������");
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
	//DatabaseTABLE_NAME�Ĳ�������
	public boolean insertDatabaseName(String name) {
		boolean flag = false;
		SQLiteDatabase database = null;
		Log.i(LOG_TAG, "--!!!�������ݿ����֡�����"+name);
		try {		
			Log.i(LOG_TAG, "--!!!socketHelper = "+socketHelper);
			database = socketHelper.getWritableDatabase();	
			
			ContentValues values = new ContentValues(); 
			values.put("databaseName",name);
			if (database.insertOrThrow(DATABASETABLE_NAME, null, values)!=-1) {
				flag = true;
				Log.i(LOG_TAG, "--!!!�������ݿ����ֳɹ�������"+name);
			}						
		} catch (Exception e) {
			Log.i(LOG_TAG, "--!!!���ݿ����������ݿ����Ѵ��ڡ�����");
			e.printStackTrace();			
		}finally{
			if(database!=null){
				database.close();
			}
		}
		return flag;
	}
	//��ѯ���ݿ�����
	public LinkedHashSet<String> queryExistingDatabase() {
		Cursor cursor=null;
		SQLiteDatabase database = null;
		LinkedHashSet<String> databaseNameSet = new LinkedHashSet<String>();
		Log.i(LOG_TAG, "--!!!��ѯ���ݿ����֡�����");
		try {			
			database = socketHelper.getReadableDatabase();
			cursor=database.query(DATABASETABLE_NAME,new String[]{"databaseName"},null,null, null, null, null);
			while(cursor.moveToNext()){
				Log.i(LOG_TAG, "--!!!��ѯ�������ݿ�����:"+cursor.getString(cursor.getColumnIndex("databaseName")));
				databaseNameSet.add(cursor.getString(cursor.getColumnIndex("databaseName")));
			}
									
		} catch (Exception e) {
			Log.e(LOG_TAG, "--!!!δ��ѯ�������ݿ����֡�����");
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
	//ɾ��ĳ�����ݿ�
		public boolean deleteDatabase(Context context,String name) {
			boolean flag = false;
			SQLiteDatabase database = null;
			final File file = context.getDatabasePath(name);
			Log.e(LOG_TAG, "--!!!ɾ�����ݿ�:"+name);
			if(file.exists()){
				if(file.delete()){
	            	Log.e(LOG_TAG, "--!!!ɾ�����ݿ�ɹ�:"+name);
	            	flag = true;
	            }
				try {			
	    			database = socketHelper.getWritableDatabase();								 
	    			if (database.delete(DATABASETABLE_NAME, "databaseName=?",
	    					new String[]{name})!=-1) {
	    				flag = true;
	    				Log.e(LOG_TAG, "--!!!ɾ��databaseName��¼�ɹ�:"+name);
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

}
