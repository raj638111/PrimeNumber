package com.apptemple.primelive;

import android.util.Log;


public class ThreadPipe {
	
	private String mName = "";
	private boolean mCheckQueue = false; //?? : Analyze if this needs a lock
	private int mThreadNo= 0;
	private int mStartAt= 0;
	private int mTotalThread = 0;
	private int mMinNo = 0;
	private int mMaxNo = 0;
	boolean mIsCompleted = false;
	//RequestType mRequestType = RequestType.NONE;
	
	
	public ThreadPipe(String name, int threadNo, int totalThread) {
		mName = name;
		mThreadNo = threadNo;
		mTotalThread = totalThread;
	}
	
	
	public void print() {
		Log.d("PRIME", "ThreadPipe : " 	+ "mMinNo -> " + mMinNo 
										+ ", mMaxNo-> " + mMaxNo 
										+ ", mStartAt -> " + mStartAt 
										+ ", isCompleted -> " + mIsCompleted
										+ ", mThreadNo -> " + mThreadNo
										+ ", mTotalThread -> " + mTotalThread);
	}
	
	
	
	
	//------  Prime number specific methods : BEGIN -------------
	
	
	public void setWorkUnitCompletion() {
		//Update work unit
		mStartAt = mStartAt + this.mTotalThread;
		
		if(mStartAt > this.mMaxNo) {
			//Set task completion status
			mIsCompleted = true;
		}
	}
	
	public int getWorkUnit() {
		return mStartAt;
	}
	
	public boolean isTaskComplete() {
		return mIsCompleted;
	}
	
	public void resetCompletionStatus() {
		mIsCompleted = false;
	}
	
	int getMaxNo() {
		return this.mMaxNo;
	}
	
	int getMinNo() {
		return mMinNo;
	}
	
	
	
	//------  Prime number specific methods : END -------------

	
	public int getTotalThread() {
		return mTotalThread;
	}
	
	
	
	public int getStartNo() {
		return mStartAt;
	}
	
	public int getThreadNo() {
		return mThreadNo;
	}

	public void notifyThread() {
		mCheckQueue = true;
	}
	
	public void resetNotification() {
		mCheckQueue = false;
	}
	
	public boolean isNotification() {
		return mCheckQueue;
	}
	
	public String getThreadName() {
		return mName;
	}
	
	
	
}
