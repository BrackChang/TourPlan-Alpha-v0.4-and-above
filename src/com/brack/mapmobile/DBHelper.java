package com.brack.mapmobile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	final private static int DB_VERSION = 1;
    final private static String DB_DATABASE_NAME = "MyDatabases.db";
    
    private String userName;
    
    public DBHelper(Context context, String userName) {
    	super(context, DB_DATABASE_NAME, null, DB_VERSION);
    	this.userName = userName;
    }
	
	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(
                "CREATE TABLE" + userName + "(" +
                        "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                		"planName CHAR, " +
                        "spots CHAR, " +
                        "spotInfo CHAR " +
                ")"
        );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS" + userName);
        onCreate(db);
	}

}
