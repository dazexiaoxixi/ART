package com.example.artadmission.DB;
import android.content.ContentValues;  
import android.content.Context;  
import android.database.Cursor;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
public class InitUserNameDB extends SQLiteOpenHelper {  
	  private static final String DB_NAME = "userName.db";  
	    private static String TBL_NAME = "userNameTable";   //表格名
//	    private static final String CREATE_TBL = " create table "  
//	            + " CommRecord(_id integer primary key autoincrement,contact_id text,name text,lastTime text) ";   //创建，通话记录
	    private static final String CREATE_TBL = " create table "  
	            + TBL_NAME+" (userName varchar(20))"; 
	    		  //创建，通话记录
//	    身高|坐高|臂长|腿长
    private SQLiteDatabase db;  
    public InitUserNameDB(Context c) {  
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
    public Cursor query() {  
      SQLiteDatabase db = getWritableDatabase();  
      Cursor c = db.query(TBL_NAME, new String[]{"userName"}, null, null, null, null, null);  
      return c;  
    } 
    public void close() {  
        if (db != null)  
            db.close();  
    }  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
    }  
}