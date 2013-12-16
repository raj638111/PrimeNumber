package com.apptemple.primelive;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.util.Log;

public class Task {

	private RequestType mRequestType = RequestType.NONE;
	private int mStartAt = 0; 	//The 'N'th number from where calculation should start
	private int mMaxNo = 0; //The 'N'th number above which calculation should not exceed
	private boolean mIsHighPriority = false;
	private int mThreadNo = 0;
	private int mTotalThread = 0;
	private int mThreadCount = 0;
	boolean mIsComplete = false;
	//?? : What about using SparseArray
	private Map<Integer, Task> mSubTaskMap = new HashMap<Integer, Task>();
	
	
	public Task(int startAt, int maxNo, RequestType requestType, int totalThread) {
		mMaxNo = maxNo;
		if(startAt <= 0) {
			mStartAt = 1;
		}else {
			mStartAt = startAt;
		}
		mRequestType = requestType;
		mTotalThread = totalThread;
		mThreadCount = totalThread;
		
	}
	
	
	public Task(Task parentTask) {
		mMaxNo = parentTask.getMaxNo();
	}
	
	public Task() {
		
	}
	
	public boolean isTaskComplete() {
		return mIsComplete;
	}
	
	public void setRequestType(RequestType requestType) {
		mRequestType = requestType;
	}
	
	/* Add Subtasks to Main task */
	public void setSubTask(LinkedList<Task> subTaskList) {

		int subTaskSize = 0;
		subTaskSize = subTaskList.size();
		if(subTaskSize == 0) {
			Log.e("PRIME", "Task : UNEXPECTED : subTaskList.size() = 0");
			return;
		}else {
			//Log.i("PRIME", "Task : Adding " + subTaskSize 
			//					+ " Subtask to Main Task");
		}
		
		for(Task subTask : subTaskList) {
			Task result = this.mSubTaskMap.put(subTask.getThreadNo(), subTask);
			if(result != null) {
				Log.e("PRIME", "Task : UNEXPECTED : This task is already mapped "
							+ subTask.getThreadNo());
			}
		}
		
	}
	
	public void decrementThreadCount() {
		if(mThreadCount <= 0) {
			Log.e("PRIME", "Task : INVALID : Thread count should be > 0");
		}else {
			mThreadCount = mThreadCount - 1;
		}
	}
	
	public int getThreadCount() {
		return mThreadCount;
	}
	
	public int getThreadNo() {
		return this.mThreadNo;
	}
	
	public void setStartingPoint(int threadNo) {
		
		
		/*Log.w("PRIME", "Task : Subtask : Before setting : " 
				+ " mThreadNo -> " + mThreadNo
				+ " mTotalThread -> " + mTotalThread
				+ " mThreadCount -> " + mThreadCount
				+ " mStartAt -> " + mStartAt);*/

		
		mThreadNo = threadNo;
		//mTotalThread = totalThreads;
		//mThreadCount = totalThreads;
		mStartAt = mStartAt + (mThreadNo - 1);
		
		
		Log.d("PRIME", "Task : Subtask : After setting : " 
							+ " mThreadNo -> " + mThreadNo
							+ " mTotalThread -> " + mTotalThread
							+ " mThreadCount -> " + mThreadCount
							+ " mStartAt -> " + mStartAt);
												
	}
	
	public void setNumbers(int startAt, int maxNo) {
		mMaxNo = maxNo;
		mStartAt = startAt;
	}
	
	
	public RequestType getRequestType() {
		return mRequestType;
	}
	
	/*public void setRequestType(RequestType requestType) {
		mRequestType = requestType;
	}*/
	
	
	public Task getSubTask(int threadNo) {
	
		Task result = this.mSubTaskMap.get(threadNo);
		if(result == null) {
			Log.e("PRIME", "Task : SubTask for threadNo '" + threadNo + "' NOT found");
		}else {
			//Log.i("PRIME", "Task : Subtask for threadNo '" + threadNo + "' FOUND");
			
		}
		return result;
	}
	
	public void setWorkUnitCompletion() {
		//Update work unit
		mStartAt = mStartAt + this.mTotalThread;
		if(mStartAt > this.mMaxNo) {
			//Set task completion status
			mIsComplete = true;
		}
	}
	
	public int getTotalThread() {
		return this.mTotalThread;
	}
	
	public int startAt() {
		return mStartAt;
	}
	
	
	public int getMinNo() {
		return mStartAt;
	}
	public int getMaxNo() {
		return mMaxNo;
	}
	
	public void setHighPriority() {
		mIsHighPriority = true;
	}
	
	public void setLowPriority() {
		mIsHighPriority = false;
	}
	
	public boolean isHighPriority() {
		return mIsHighPriority;
	}
	
}
