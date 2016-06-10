package com.example.artadmission.DB;
import android.content.ContentValues;  
import android.content.Context;  
import android.database.Cursor;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
public class IPDB extends SQLiteOpenHelper {  
	  private static final String DB_NAME ="IP.db";  
	    private static String TBL_NAME = "socket";   //�����
//	    private static final String CREATE_TBL = " create table "  
//	            + " CommRecord(_id integer primary key autoincrement,contact_id text,name text,lastTime text) ";   //������ͨ����¼
	    private static final String CREATE_TBL = " create table "  
	            + TBL_NAME+" (ip varchar(50),port varchar(20))"; 
	    		  //������ͨ����¼
//	    ���|����|�۳�|�ȳ�
    private SQLiteDatabase db;  
    public IPDB(Context c) {  
       super(c, DB_NAME, null, 2);    
    }  
    @Override  
    public void onCreate(SQLiteDatabase db) {  //��һ�δ�������
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
      Cursor c = db.query(TBL_NAME, new String[]{"ip","port"}, null,null, null, null, null);  
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