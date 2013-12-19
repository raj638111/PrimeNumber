package com.apptemple.primelive;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHandler extends SQLiteOpenHelper {
   
	private static final String DATABASE_NAME = "PrimeNumber";
	private static final int DATABASE_VERSION = 3;
	 
	private static final String TABLE_REQUEST = "user_request";
	private static final String TABLE_PRIME = "prime";
	
    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	
	/* Create Tables */
	 @Override
	 public void onCreate(SQLiteDatabase db) {
		 String CREATE_REQUEST;
		 
	     CREATE_REQUEST 	= "CREATE TABLE " + 	TABLE_REQUEST + "(" + 
													" howmany INTEGER PRIMARY KEY, " + 
													" current_count INTEGER,  " +
													" is_complete TEXT, " +
													" created_time DATETIME, " +
													" tstamp DATETIME " + 
													")";
	     db.execSQL(CREATE_REQUEST);
	     
	     CREATE_REQUEST 	= "CREATE TABLE " + 	TABLE_PRIME + "(" + 
													" nthno INTEGER PRIMARY KEY, " + 
													" primeno INTEGER  " +
													")";
	     
	     db.execSQL(CREATE_REQUEST);
	     
	 }
	 
	  
	 /* Upgrading database */
	 @Override
	 public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQUEST);
 
        // Create tables again
        onCreate(db);
    }
	 
	 
	 //-----------------------------------------------
	 //				Table : 'request'				   
	 //-----------------------------------------------
	 
	 /* Get all request from table : 'request' */
	 //?? : Do we have to use try/catch/finally for database opening and closing
	 public ArrayList<UserRequest> getAllRequest() {
		 
		 ArrayList<UserRequest> requestList = new ArrayList<UserRequest>();
		 
		 //Log.v("PRIME", "Table : request : Inside getAllRequest()");
		 
		 //String selectQuery = "SELECT  * FROM " + TABLE_REQUEST + " ORDER BY DATETIME(created_time) ASC";
		 String selectQuery = "SELECT  * FROM " + TABLE_REQUEST + " ORDER BY howmany ASC";
		 SQLiteDatabase db = this.getWritableDatabase();
		 Cursor cursor = db.rawQuery(selectQuery, null);
		 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	
            	int howMany = Integer.parseInt(cursor.getString(0));
            	int currentCount = Integer.parseInt(cursor.getString(1));
            	boolean isComplete = false;
            	if(cursor.getString(2).equals("Y")) {
            		isComplete = true;
            	}else {
            		isComplete = false;
            	}	
            	String createdTime = cursor.getString(3);
            	String tstamp = cursor.getString(4);
            	
            	
            	/*Log.v("PRIME", "Table : request : " + " howMany -> " + howMany
            										+ " currentCount -> "+ currentCount 
            										+ " isComplete -> " + isComplete 
            										+ " createdTime -> " + createdTime
            										+ " tstamp -> " + tstamp);*/
            	UserRequest request = new UserRequest(	howMany, 	currentCount, 
            											isComplete, createdTime, 
            											tstamp);
            	
            	requestList.add(request);
            
            	
            } while (cursor.moveToNext());
        }
        db.close();
		 
		return requestList; 
	 }
	 
	 
	 /* Insert new request into table 'request' */
	 void insertRequest(int number) {
		 
		 //Log.i("PRIME", "Table : request : Inside insertRequest() : No -> " 
		 //		 										+ number);
		 
		 SQLiteDatabase db = this.getWritableDatabase();
 
		 //??
		 String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
      
		 //Log.i("PRIME", "DbHandler : insertRequest() " 
		 //		 				+ "howmany -> " + number);
		 
		 ContentValues values = new ContentValues();
		 values.put("howmany", number);
		 values.put("current_count", 0);
		 values.put("is_complete", "N");
		 values.put("created_time", timeStamp);
		 values.put("tstamp", timeStamp);

		 try {
			 // Inserting Row
			 db.insertOrThrow(TABLE_REQUEST, null, values);
		 
		 }
		 catch (android.database.sqlite.SQLiteConstraintException e) {
			 Log.e("PRIME", "insertRequest : SQLiteConstraintException:" + e.getMessage());
		 }
		 catch (android.database.sqlite.SQLiteException e) {
			 Log.e("PRIME", "SQLiteException:" + e.getMessage());
		 } 
		 catch (Exception e) {
			 Log.e("PRIME", "Exception:" + e.getMessage());
		 }
		 
		 db.close(); // Closing database connection
    }
	
	
	 //-----------------------------------------------
	 //				Table : 'prime'				   
	 //-----------------------------------------------
	 public ArrayList<PrimeNo> getPrimeList(int maxNo) {
		 
		 ArrayList<PrimeNo> primeList = new ArrayList<PrimeNo>();
	
		 //Log.v("PRIME", "Table : prime : Inside getPrimeList() : " + maxNo);
		 
		 String selectQuery = "SELECT  * FROM " + TABLE_PRIME
				 								+ " where nthno <= "
				 								+ maxNo
				 								+ " ORDER BY nthno DESC";
		 SQLiteDatabase db = this.getWritableDatabase();
		 Cursor cursor = db.rawQuery(selectQuery, null);
		 
		 // looping through all rows and adding to list
		 if (cursor.moveToFirst()) {
			 do {
            	
				 int nthNo = Integer.parseInt(cursor.getString(0));
				 int primeno = Integer.parseInt(cursor.getString(1));
				 
            	if(nthNo == 0) {
            		Log.e("PRIME", "DbHandler : getPrimeList() : UNEXPECTED. nthNo = 0");
            		continue;
            	}
				 //Log.v("PRIME", "Table : prime : " 	+ " nthNo -> " + nthNo 
            	 //									+ " Prime No -> " + primeno); 
				 PrimeNo primeObj = new PrimeNo(nthNo, primeno);
				 
				 
				 primeList.add(primeObj);
            
            	
			 } while (cursor.moveToNext());
		 }
		 db.close();
		 
		 Log.v("PRIME", "Table : getPrimeList() : primeList.size() -> " + primeList.size());
		 
		return primeList; 
	 }
	 
	 /* Insert prime no into table 'prime' */

	 
	 public void insertPrimeNo(int nthNo, int primeNo) {
			 
		 //Log.v("PRIME", "Table : prime : Inside insertPrimeNo()");
		 
		 SQLiteDatabase db = this.getWritableDatabase();
 
		 
		 ContentValues values = new ContentValues();
		 values.put("nthno", nthNo);
		 values.put("primeno", primeNo);
		 try {
			 // Inserting Row
			 db.insertOrThrow(TABLE_PRIME, null, values);
		 
		 }
		 catch (android.database.sqlite.SQLiteConstraintException e) {
			 Log.e("PRIME", "insertPrimeNo() : SQLiteConstraintException:" + e.getMessage());
		}
		catch (android.database.sqlite.SQLiteException e) {
		    Log.e("PRIME", "insertPrimeNo() : SQLiteException:" + e.getMessage());
		} 
		catch (Exception e) {
		    Log.e("PRIME", "insertPrimeNo() : Exception:" + e.getMessage());
		}
		 db.close(); // Closing database connection
		 
	 }

	 //-----------------------------------------------------
	 //			Table : 'user_request' & 'prime'				   
	 //-----------------------------------------------------
	 
	 
	/* Delete all records from table 'request' */
	void deleteAllRequest() {
		//Log.i("PRIME", "Table : request : Inside deleteAllRequest()");
		SQLiteDatabase db = this.getWritableDatabase();
		 
		db.delete(TABLE_REQUEST, null, null);
		db.delete(TABLE_PRIME, null, null);
		 
		db.close(); // Closing database connection
	}
	 
	 
	
}
