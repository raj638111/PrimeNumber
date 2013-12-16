package com.apptemple.primelive;

import java.util.ArrayList;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ServiceThread implements Runnable {
	
	ThreadPipe mThreadPipe;
	ThreadSync mThreadSync;
	Service mService;
	GlobalState mState;
	DbHandler mDbHandler;
	
	ServiceThread(ThreadPipe threadPipe, ThreadSync threadSync, Service service) {
		mThreadPipe = threadPipe;
		mThreadSync = threadSync;
		mService = service;
		mState = (GlobalState)mService.getApplicationContext();
		mDbHandler = new DbHandler(mService.getApplicationContext());
	}
	
	
	public void run() {
		
		//Log.v("PRIME", "ServiceThread : Inside run() method");
		Intent intent;
		int primeNo = 0;
		ArrayList<PrimeNo> primeList;
		int threadNo = mThreadPipe.getThreadNo(); //NN
		
		for(;;) {
			
			Task task = mThreadSync.getTask(mThreadPipe);
			if(task == null) {
				//Log.v("PRIME", "ServiceThread : Thread " + threadNo
				//		+ " : NO more task available. Shutting down");
				break;
			}
			
			
			switch(task.getRequestType()) {
			
				case SU_UNIT_WORK :
					Log.d("PRIME", "ServiceThread : Thread " + threadNo 
								+ " : SU_UNIT_WORK : " + task.getMaxNo());
					primeNo = NthPrime_12_SFPP.nthPrime(task.getMaxNo(), mThreadPipe);
					if(primeNo > 0) {
						
						mDbHandler.insertPrimeNo(task.getMaxNo(), primeNo);
						PrimeNo primeObj = new PrimeNo(task.getMaxNo(), primeNo);
						primeList = new ArrayList<PrimeNo>();
						primeList.add(primeObj);
						
						mState.addPrimeResult(primeList);
						intent = new Intent(Constants.SU_PRIME);
						//Log.v("PRIME", "ServiceThread : Thread : " + threadNo 
						//			+ " : Sending Broadcast(from SU_UNIT_WORK) : SU_PRIME");
						LocalBroadcastManager.getInstance(mService.getApplicationContext())
			    								 .sendBroadcast(intent);
					}
					break;
				case SU_INSERT_GROUP_NO :
					Log.d("PRIME", "ServiceThread : Thread " + threadNo 
										+ " : SU_INSERT_GROUP_NO : " + task.getMaxNo());
					mDbHandler.insertRequest(task.getMaxNo());
					break;
			
				//Reset request
				case SU_RESET :
					Log.d("PRIME", "ServiceThread : Thread " + threadNo 
										+ " : SU_RESET : RESETTING the application");
					mDbHandler.deleteAllRequest();
					break;
				
				//Full data request
				case SU_FULL_DATA :
					Log.d("PRIME", "ServiceThread : Thread " + threadNo 
									+ " : 'SU_FULL_DATA' request : " + task.getMaxNo());
					ArrayList<PrimeNo> primeNoList = mDbHandler.getPrimeList(task.getMaxNo());
					
					mState.addPrimeResult(primeNoList);
	    		
					//Log.i("PRIME", "ServiceThread : Thread " + threadNo 
					//				+ " : Sending Broadcast : SU_PRIME_NUMBER");
					intent = new Intent(Constants.SU_PRIME);
					LocalBroadcastManager.getInstance(mService.getApplicationContext())
	    								 .sendBroadcast(intent);
				break;
			
				//Home page request
				case SU_HOME_PAGE :
					Log.d("PRIME", "ServiceThread : Thread " + threadNo 
										+ " : 'SU_HOME_PAGE' request");
					
					//Fetch user request from table 'user_request'
					ArrayList<UserRequest> userReqList = mDbHandler.getAllRequest();
					mState.addUserRequest(userReqList);
					
					//Log.i("PRIME", "ServiceThread : Thread " + threadNo 
					//				+ " : Sending Broadcast : SU_HOME_PAGE");
					intent = new Intent(Constants.SU_HOME_PAGE);
					LocalBroadcastManager.getInstance(mService.getApplicationContext())
	    								 .sendBroadcast(intent);
							
					
					break;
				
				//Prime number request
				case SU_PRIME_NUMBER :
					
					//Log.i("PRIME", "ServiceThread : Thread " + threadNo 
					//					+ " : 'SU_PRIME_NUMBER' request");
					
					
					Date prevTime = new Date();
					Date curTime = new Date();
					primeList = new ArrayList<PrimeNo>();
				
					Log.d("PRIME", "ServiceThread : Thread " + threadNo 
							+ " : Starting At -> " + task.startAt()
							+ ", MaxNo -> " + task.getMaxNo()
							+ ", Total thread -> " + task.getTotalThread());
					
					
					for(;;) {
					
						if(task.startAt() > task.getMaxNo()) {
							task.setWorkUnitCompletion();
						}
						
						//mThreadPipe.print();
						if(task.isTaskComplete() == true) {
							Log.d("PRIME", "ServiceThread : Thread " + threadNo 
									+ " : Workunit completion : start -> " + task.startAt() 
									+ ", MaxNo -> " + task.getMaxNo());
						
							mThreadSync.updateTaskList(task, mThreadPipe);
							break;
						}
						
						int nthNo = task.startAt();
						
						//Log.v("PRIME", "ServiceThread : Thread '" + threadNo 
						//			+ "' : Calculating prime value for " + nthNo);
						
						primeNo = NthPrime_12_SFPP.nthPrime(nthNo, mThreadPipe);
						if(primeNo == 0) {
							Log.d("PRIME", "ServiceThread : Priority nofication received." 
												+ "Aborting current Task TEMPORARILY : " 
												+ "current no -> " + nthNo 
												+ "Max No -> " + task.getMaxNo() 
												+ "ThreadNo -> " + task.getThreadNo() );
							break;
						}else {
							//Log.v("PRIME", "ServiceThread : Prime no calculated : nth -> " 
							//						+ nthNo + ", Prime No -> " + primeNo);
							mDbHandler.insertPrimeNo(nthNo, primeNo);
							task.setWorkUnitCompletion();
						}
						
						PrimeNo primeObj = new PrimeNo(nthNo, primeNo);
						primeList.add(primeObj);
						
						curTime = new Date();
						long diff = curTime.getTime() - prevTime.getTime();
						long seconds = diff / 1000 % 60;
						if(seconds > 5) {
							mState.addPrimeResult(primeList);
							intent = new Intent(Constants.SU_PRIME);
							//Log.v("PRIME", "ServiceThread : Thread " + threadNo 
							//		+ " : Sending Broadcast-1 : SU_PRIME");
							LocalBroadcastManager.getInstance(mService.getApplicationContext())
				    								 .sendBroadcast(intent);
							primeList.clear();	
							prevTime = curTime;
						}
						
					}
					
					if(primeList.size() > 0) {
						mState.addPrimeResult(primeList);
						primeList.clear();
						intent = new Intent(Constants.SU_PRIME);
						//Log.v("PRIME", "ServiceThread : Thread " + threadNo 
						//		+ " : Sending Broadcast-2 : SU_PRIME");
						
			    		LocalBroadcastManager.getInstance(mService.getApplicationContext())
			    								 .sendBroadcast(intent);
					}
					
					
					break;
					
				//'Invalid' request	
				case NONE:
					Log.e("PRIME", "ServiceThread : case NONE " + mThreadPipe.getThreadNo() 
													+ " : CLOSING DOWN");
					break;
				default :
					Log.e("PRIME", "ServiceThread : run() : Invalid request"
							+ "CLOSING DOWN");
					break;
			}
			
		}
		
	}
	
}
