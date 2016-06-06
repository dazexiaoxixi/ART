package com.example.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SocketDbOpenHelper extends SQLiteOpenHelper{
	private static final String SOCKETDATABASE_NAME = "Config.db";// 表示数据库的名称
	private static final String SOCKETTABLE_NAME = "socketConfigTable";// 表示表的名称
	private static final String DATABASETABLE_NAME = "databaseConfigTable";// 表示表的名称
	private static final String USERTABLE_NAME = "userInfoTable";// 表示表的名称
	private static int VERSION = 1;// 表示数据库的版本号码
	public SocketDbOpenHelper(Context context) {
		super(context, SOCKETDATABASE_NAME, null, VERSION);
	}

	// 当数据库创建的时候，是第一次被执行,完成对数据库的表的创建
	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("------create tables "+SOCKETTABLE_NAME);
		String sqlSocket = "create table "+SOCKETTABLE_NAME+"(Ip varchar(50), Port varchar(12),primary key(Ip,Port))";
		db.execSQL(sqlSocket);
		System.out.println("------create tables"+SOCKETTABLE_NAME+"成功");
		System.out.println("------create tables "+DATABASETABLE_NAME);
		String sqlDatabase = "create table "+DATABASETABLE_NAME+"(databaseName varchar(50),primary key(databaseName))";
		db.execSQL(sqlDatabase);
		System.out.println("------create tables"+DATABASETABLE_NAME+"成功");
		System.out.println("------create tables "+USERTABLE_NAME);
		String sqlUser = "create table "+USERTABLE_NAME+"(userName varchar(50),primary key(userName))";
		db.execSQL(sqlUser);
		System.out.println("------create tables"+USERTABLE_NAME+"成功");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//		String sql = "alter table eHome add data varchar(10)";
		//		db.execSQL(sql);
	}

}
