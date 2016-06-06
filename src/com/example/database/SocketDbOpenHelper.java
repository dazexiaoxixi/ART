package com.example.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SocketDbOpenHelper extends SQLiteOpenHelper{
	private static final String SOCKETDATABASE_NAME = "Config.db";// ��ʾ���ݿ������
	private static final String SOCKETTABLE_NAME = "socketConfigTable";// ��ʾ�������
	private static final String DATABASETABLE_NAME = "databaseConfigTable";// ��ʾ�������
	private static final String USERTABLE_NAME = "userInfoTable";// ��ʾ�������
	private static int VERSION = 1;// ��ʾ���ݿ�İ汾����
	public SocketDbOpenHelper(Context context) {
		super(context, SOCKETDATABASE_NAME, null, VERSION);
	}

	// �����ݿⴴ����ʱ���ǵ�һ�α�ִ��,��ɶ����ݿ�ı�Ĵ���
	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("------create tables "+SOCKETTABLE_NAME);
		String sqlSocket = "create table "+SOCKETTABLE_NAME+"(Ip varchar(50), Port varchar(12),primary key(Ip,Port))";
		db.execSQL(sqlSocket);
		System.out.println("------create tables"+SOCKETTABLE_NAME+"�ɹ�");
		System.out.println("------create tables "+DATABASETABLE_NAME);
		String sqlDatabase = "create table "+DATABASETABLE_NAME+"(databaseName varchar(50),primary key(databaseName))";
		db.execSQL(sqlDatabase);
		System.out.println("------create tables"+DATABASETABLE_NAME+"�ɹ�");
		System.out.println("------create tables "+USERTABLE_NAME);
		String sqlUser = "create table "+USERTABLE_NAME+"(userName varchar(50),primary key(userName))";
		db.execSQL(sqlUser);
		System.out.println("------create tables"+USERTABLE_NAME+"�ɹ�");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//		String sql = "alter table eHome add data varchar(10)";
		//		db.execSQL(sql);
	}

}
