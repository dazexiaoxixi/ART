package com.example.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

	private static final String MAINTABLE_NAME = "artCheckInTable";// ��ʾ���ݿ������
	
	private static int VERSION = 1;// ��ʾ���ݿ�İ汾����
	public DbOpenHelper(Context context) {
		super(context, DefineVar.databaseName, null, VERSION);
	}

	// �����ݿⴴ����ʱ���ǵ�һ�α�ִ��,��ɶ����ݿ�ı�Ĵ���
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
		System.out.println("------create tables"+MAINTABLE_NAME+"�ɹ�");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//		String sql = "alter table eHome add data varchar(10)";
		//		db.execSQL(sql);
	}

}
