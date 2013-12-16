package com.apptemple.primelive;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import android.app.Service;
import android.util.Log;

public class ThreadSync {

	Map<String, ThreadPipe> mActiveThreadsMap = new HashMap<String, ThreadPipe>();
	LinkedList<ThreadPipe> mDormantThreadQ = new LinkedList<ThreadPipe>();
	//PriorityQueue<Request> mRequestQ = new PriorityQueue<Request>(7, mComparator);
	//?? : What is the significance of numerical number
	PriorityQueue<Task> mRequestQ = new PriorityQueue<Task>(7, mComparator);
	LinkedList<Integer> mStartIdQ = new LinkedList<Integer>();
	Service mService;
	LinkedList<Task> mTaskList = new LinkedList<Task>();
	int mTotalThread = 0;
	int mStartId = 0;

	
	public static Comparator<Task> mComparator = new Comparator<Task>() {
	@Override
	public int compare(Task task1, Task task2) {
		//??? : This needs to be verified
		if(task1.isHighPriority() == true) {
			return 1;
		}else {
			return 0;
		}
	}
	};

	
	
	public ThreadSync(Service service) {
		//No of Pipes is equal to no of threads
		ThreadPipe pipe = new ThreadPipe("one", 1, 2);
		mDormantThreadQ.add(pipe);
		pipe = new ThreadPipe("two", 2, 2);
		mDormantThreadQ.add(pipe);
		/*pipe = new ThreadPipe("three", 3, 3);
		mDormantThreadQ.add(pipe);*/
		mService = service;
		mTotalThread = 2;
	}

	
	public synchronized boolean abortTask(Request request) {
		if(request.getRequestType() == RequestType.SU_ABORT_SERVICE) {
			
			Log.d("PRIME", "abortTask() : Initiating ABORT sequence...");
			
			//Notify all threads to Abort
			for(Map.Entry<String, ThreadPipe> entry : mActiveThreadsMap.entrySet()) {
				ThreadPipe threadPipe = entry.getValue();
				Log.d("PRIME", "ThreadSync : abortTask() : Notifying thread " + threadPipe.getThreadNo());
				threadPipe.notifyThread();
			}
			
			this.mRequestQ.clear();
			this.mTaskList.clear();
			
			return true;
		}else {
			return false;
		}
	}
	
	
	/*
	 * Inserts Request and
	 * 		Returns true if any thread are already running
	 * 		Creates, starts new thread and return
	 */
	public synchronized void insertRequest(Request request, int startId) {
		
		mStartId = startId;
		
		if(abortTask(request) == true) {
			return;
		}
		
		Task task = new Task(request.startAt(), request.getNo(), request.getRequestType(), this.mTotalThread);
		if(request.isHighPriority() == true) {
			task.setHighPriority();
		}else {
			task.setLowPriority();
		}
		//task.setRequestType(request.getRequestType());
		
		//Insert request in mRequestQ
		mRequestQ.add(task);
		
		//Insert startId in mStartIdQ
		//?? : This Queue is no longer needed
		mStartIdQ.add(startId);
		
		//Check if thread size is zero
		if(mActiveThreadsMap.size() == 0) {
			
			//Get one threadPipe from DormantThreadQ
			//Log.i("PRIME", "ThreadSync : ZERO thread active. Fetching thread pipe from dormant queue");
			ThreadPipe threadPipe = mDormantThreadQ.poll();
			if(threadPipe == null) {
				Log.e("PRIME", "ThreadSync : UNEXPECTED : NULL threadPipe from mDormantThreadQ");
			}else {
				//Log.i("PRIME", "ThreadSync : threadPipe retrived : '" + threadPipe.getThreadNo() + "'");
			}
			threadPipe.resetNotification();

			//Log.i("PRIME", "ThreadSync : Adding threadPipe to ActiveThread map");
			//Insert threadPipe into activeThreadQ
			ThreadPipe resultPipe = this.mActiveThreadsMap.put(threadPipe.getThreadName(), threadPipe);
			if(resultPipe != null) {
				Log.e("PRIME", "ThreadSync : threadPipe already EXISTING in activeThreadMap");
			}
			
			//Create and start new thread
			//Log.i("PRIME", "ThreadSync : Starting new thread...");
			Runnable runnable = new ServiceThread(threadPipe, this, mService);
			Thread thread = new Thread(runnable);
			thread.start();
			//Log.i("PRIME", "ThreadSync : New thread started");
						
		}else {
			//Check if priority request
			//Log.i("PRIME", "ThreadSync : One or more thread already active");
			for(Map.Entry<String, ThreadPipe> entry : mActiveThreadsMap.entrySet()) {
				ThreadPipe threadPipe = entry.getValue();
				Log.d("PRIME", "ThreadSync : Notifying thread " + threadPipe.getThreadNo());
				threadPipe.notifyThread();
				break;
			}
		}
		
	}
	
	
	public synchronized void updateTaskList(Task subTask, ThreadPipe threadPipe) {
		int maxNo = subTask.getMaxNo();
		int pos = 0;
		boolean deleteTask = false;
		boolean isTaskIdentified = false;
		
		//Log.v("PRIME", "ThreadSync : updateTaskList() : Trying to identify task with maxNo -> " + maxNo);
		for(Task task : this.mTaskList) {
			int taskMaxNo = task.getMaxNo();
			
			if(taskMaxNo == maxNo) {
				//Log.i("PRIME", "ThreadSync : updateTaskList() : Task found. Position -> " 
				//												+ pos + ". UPDATING");
				isTaskIdentified = true;
				task.decrementThreadCount();
				if(task.getThreadCount() == 0) {
					//Log.i("PRIME", "ThreadSync : updateTaskList : SubTask (" + maxNo + ") is complete.");
					deleteTask = true;
				}
				  
				break;
			}
			pos++;
		}
		
		if(isTaskIdentified == true) {
			if(deleteTask == true) {
				Log.w("PRIME", "ThreadSync : updateTaskList() : Thread " + threadPipe.getThreadNo()
						+ " : DELETEING task in Position " + pos);
				mTaskList.remove(pos);
			}
		}else {
			Log.d("PRIME", "ThreadSync : updateTaskList() : Task (" + maxNo + ") could NOT be identified");
		}
	}
		

	private synchronized Task getTaskFromTaskList(int threadNo) {
		RequestType requestType = RequestType.NONE;
		Task subTask = null;
		
		//----- Check for task in mTaskList ----------
		//Log.i("PRIME", "ThreadSync : Inside getTaskFromTaskList()");
		for(Task task : this.mTaskList) {
			
			//Get Subtask for this thread
			subTask = task.getSubTask(threadNo);
			if(subTask == null) {
				break;
			}
			
			//Check if the Subtask is complete
			if(subTask.isTaskComplete() == true) {
				Log.w("PRIME", "ThreadSync : getTaskFromlist() : Thread " + threadNo 
						+ " : Task alread completed : MaxNo -> " + subTask.getMaxNo());
			}else {
				Log.d("PRIME", "ThreadSync : getTaskFromList() : Thread " + threadNo 
							+ " : Task avaialble : MaxNo -> " + subTask.getMaxNo());
				break;
			}
			subTask = null;
		}

		
		return subTask;
	}
	
	
	public synchronized void startOtherThreads(ThreadPipe thisThreadPipe) {

		int threadNo = thisThreadPipe.getThreadNo();
		
		for(ThreadPipe threadPipe : this.mDormantThreadQ) {
			//Log.v("PRIME", "ThreadSync : " + threadNo + " : Starting thread : " + threadPipe.getThreadNo());
			Runnable runnable = new ServiceThread(threadPipe, ThreadSync.this, mService);
			Thread thread = new Thread(runnable);
			
			thread.start();	
			Log.d("PRIME", "ThreadSync : " + threadNo + " : Thread Started : " + threadPipe.getThreadNo());
			
			//Add threadPipe to ActiveMap
			this.mActiveThreadsMap.put(threadPipe.getThreadName(), threadPipe);
		}
		
		//Make all thread pipes Empty in mDormantThreadQ
		this.mDormantThreadQ.clear();
	}
	
	
	public synchronized Task getGroupNoTask(Task parentTask) {
	
		Task task = new Task(parentTask);
		task.setRequestType(RequestType.SU_INSERT_GROUP_NO);
		
		return task;
	}
	
	public synchronized Task getTask(ThreadPipe threadPipe) {
		
		RequestType requestType = RequestType.NONE;
		Task task = null;
		
		
		//-------Check if task available in  mRequestQ --------
		//Log.v("PRIME", "ThreadSync : getTask() : Checking if task available in both queues");
		
		task = this.mRequestQ.poll();
		
		if(task != null) {
			Log.d("PRIME", "ThreadSync : getTask() : Task found in mRequestQ : " 
												+ task.getRequestType());
			requestType = task.getRequestType();
			if(requestType == RequestType.SU_PRIME_NUMBER) {
				//Log.i("PRIME", "ThreadSync : getTask() : Moving SU_PIRME_NUMBER " + 
				//											"to mTaskList");
				
				//Create sub-task and add it to this task(Main task)
				task.setSubTask(this.getSubTasks(task));
				
				//Add the task to TaskList
				this.mTaskList.add(task);
				
				//Create new task for inserting group no into table 'request'
				task = getGroupNoTask(task);
				
				// Start other threads to work on prime no calculation 
				startOtherThreads(threadPipe);
				
			}
		}else {

			//----- Check for task in mTaskList ----------
			//Log.v("PRIME", "ThreadSync : getTask() : Task NOT found in mRequestQ. " 
			//								+ "Identifying task from mTaskList");
			task = getTaskFromTaskList(threadPipe.getThreadNo());
			if(task != null) {
				//Log.i("PRIME", "ThreadSync : getTask() : Task found");
			}else {
				Log.w("PRIME", "ThreadSync : getTask() : Thread " + threadPipe.getThreadNo() 
						+ " : Task not found in mTaskList. WINDING up"); 
				cleanup(threadPipe);	
			}
			
		}
			
		return task;
	}
	
	private synchronized LinkedList<Task> getSubTasks(Task task) {
		
		Task subTask;
		LinkedList<Task> subTaskList = new LinkedList<Task>();
		
		Log.d("INFO", "ThreadSync : getSubTasks() : CREATING subtasks");
		
		
		for(int inc = 0 ; inc < this.mTotalThread ; inc++) {
			subTask = new Task(task.getMinNo(), task.getMaxNo(), 
								RequestType.SU_PRIME_NUMBER, this.mTotalThread);
			subTask.setStartingPoint(inc + 1);
			subTaskList.add(subTask);
		}
		
		return subTaskList;
		
	}
	
	private synchronized void cleanup(ThreadPipe threadPipe) {

		//Log.i("PRIME", "ThreadSync : Inside cleanup()");
		
		threadPipe.resetCompletionStatus();
		
		//Remove node from ActiveMap
		ThreadPipe pipe = this.mActiveThreadsMap.remove(threadPipe.getThreadName());
		if(pipe == null) {
			Log.e("PRIME", "ThreadSync : cleanup() : UNEXPECTED : 'null' pipe value");
			return;
		}
		
		//Add node to DormantQueue
		this.mDormantThreadQ.add(pipe);
		
		//Set notification variable to false
		pipe.resetNotification();
		
		if(this.mActiveThreadsMap.size() == 0) {
			mService.stopSelfResult(this.mStartId);
			
		}
		
	}
	
	
}
