package com.example.artadmission.DB;
import com.example.artadmission.define_var;

import android.content.ContentValues;  
import android.content.Context;  
import android.database.Cursor;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
public class DBHelper extends SQLiteOpenHelper {  
	private static final String DB_NAME = define_var.userNameStr+".db";  
	private static String TBL_NAME = "record";   //表格名
	private static final String CREATE_TBL = " create table "  
			+ TBL_NAME+" (subject varchar(20),roomNum varchar(20),examType varchar(10),groupNum varchar(20),stdNum varchar(20)," +
			"startTime varchar(30),endTime varchar(30),height varchar(10),standHeight varchar(10),armLength  varchar(10)," +
			"legLength varchar(10),score varchar(20),submitOK varchar(5),primary key(subject,roomNum,groupNum,stdNum,examType))"; 
	//	    身高|坐高|臂长|腿长
	private SQLiteDatabase db;  
	public DBHelper(Context c) {  
		super(c, DB_NAME, null, 2);    
	}  
	@Override  
	public void onCreate(SQLiteDatabase db) {  //第一次创建调用
		this.db = db;  
		db.execSQL(CREATE_TBL); 
	}  
	public void insert(ContentValues values) {  
		SQLiteDatabase db = getWritableDatabase();  
		db.insert(TBL_NAME, null, values);  
		db.close();  
	}  
	public Cursor query(String subject,String roomNum,String groupNum,String stdNum) {  
		SQLiteDatabase db = getWritableDatabase();  
		Cursor cursor = db.query(TBL_NAME, new String[]{"subject","roomNum","groupNum","stdNum","examType"}, "subject=? and roomNum=? and groupNum=? and stdNum=? and examType=?"
				, new String[]{subject,roomNum,groupNum,stdNum,define_var.examTypeStr}, null, null, null); 
		//  Cursor c = db.query(TBL_NAME, new String[] {"lastTime"}, null, null, null, null, null);
		return cursor;  
	} 
	public Cursor query() {  
		SQLiteDatabase db = getWritableDatabase();  
		Cursor c = db.query(TBL_NAME, new String[] {"subject","roomNum","groupNum","stdNum","startTime","endTime","score","submitOK","examType"}, null, null, null, null, null);
		return c;  
	} 
	public void update(String startTime,String endTime,String stdNum) {  
		ContentValues values = new ContentValues();
		SQLiteDatabase db = getWritableDatabase(); 
		values.put("startTime", startTime);
		values.put("endTime", endTime);
		try {			
			db.update("record", values,"subject=? and roomNum=? and groupNum=? and stdNum=? and examType=?"
					, new String[]{define_var.subject,define_var.room,define_var.group,stdNum,define_var.examTypeStr}); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}  
	public void close() {  
		if (db != null)  
			db.close();  
	}  
	@Override  
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
	}  
}