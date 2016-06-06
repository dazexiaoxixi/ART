package com.example.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

	private static final String MAINTABLE_NAME = "artCheckInTable";// 表示数据库的名称
	
	private static int VERSION = 1;// 表示数据库的版本号码
	public DbOpenHelper(Context context) {
		super(context, DefineVar.databaseName, null, VERSION);
	}

	// 当数据库创建的时候，是第一次被执行,完成对数据库的表的创建
	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("------create tables "+MAINTABLE_NAME);
		//varchar int long float boolean text blob clob
		//String sql = "create table "+DefineVar.tableName+"(account varchar(50),psw varchar(100))";  
		String sql = "create table "+MAINTABLE_NAME+"(groupID varchar(12),examnieeID varchar(12)," +"startTime varchar(30) default(0),"
		+"endTime varchar(30) default(0),"+"examinationID varchar(20),"+
				"examnieeName varchar(30),examnieeIdentity varchar(50),examnieeSex varchar(5)," +"examnieePicURL varchar(100),"+
				"primary key(groupID,examnieeID))";
		db.execSQL(sql);
		System.out.println("------create tables"+MAINTABLE_NAME+"成功");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//		String sql = "alter table eHome add data varchar(10)";
		//		db.execSQL(sql);
	}

}
