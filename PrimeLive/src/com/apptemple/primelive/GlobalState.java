package com.apptemple.primelive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Application;
import android.util.Log;

public class GlobalState extends Application{
	
	
	
	
	//-------------------------------------------------------------------------------
	//							UserRequest Lock : Begin
	//-------------------------------------------------------------------------------
	
	//UserRequest result from Service Thread
	private ArrayList<UserRequest> mUserRequestList = new ArrayList<UserRequest>();
	//private boolean isBroadcastSent = false;
	private Lock mUserRequestLock = new ReentrantLock();
	
	public void addUserRequest(ArrayList<UserRequest> userRequestList) {
		
		this.mUserRequestLock.lock();
		try {
		
			//Log.i("PRIME", "GlobalState : addUserRequest() : Before : userRequestList -> " + userRequestList.size() 
			//										+ " mUserRequestList -> " + mUserRequestList.size());
		
			mUserRequestList.addAll(userRequestList);
		
		    userRequestList.clear();
		    
			//Log.i("PRIME", "GlobalState : addUserRequest() : After : userRequestList -> " + userRequestList.size() 
			//										+ " mUserRequestList -> " + mUserRequestList.size());
		
		}finally {
			this.mUserRequestLock.unlock();
		}
	}
	
	
	public boolean getUserResult(ArrayList<UserRequest> userRequestList) {
		this.mUserRequestLock.lock();
		boolean isAvailable = true;
		
		try {
			
			//Log.i("PRIME", "GlobalState : getUserResult() : Before : userRequestList -> " + userRequestList.size() 
			//										+ " mUserRequestList -> " + mUserRequestList.size());
			if(mUserRequestList.size() > 0) {
				userRequestList.addAll(mUserRequestList);
			}else {
				isAvailable = false;
			}
			
			mUserRequestList.clear();
		
			//Log.i("PRIME", "GlobalState : getUserResult() : After : userRequestList -> " + userRequestList.size() 
			//										+ " mUserRequestList -> " + mUserRequestList.size());
		
		}finally {
			this.mUserRequestLock.unlock();
		}
		return isAvailable;
	}
	
	
	//---------------------------------------------------------------------------------------
	//							PrimeNo result Lock : Begin
	//---------------------------------------------------------------------------------------
	
	//UserRequest result from Service Thread
	private ArrayList<PrimeNo> mPrimeList = new ArrayList<PrimeNo>();
	//private boolean isBroadcastSent = false;
	private Lock mPrimeLock = new ReentrantLock();
	
	public void clearPrimeResult() {
		this.mPrimeLock.lock();
		try {
			//Log.i("PRIME", "GlobalState : Inside clearPrimeResult()");
			mPrimeList.clear();
			
		}finally {
			this.mPrimeLock.unlock();
		}	
	}
	
	//public void addPrimeResult(List<PrimeNo> primeList) {
		
	public void addPrimeResult(List<PrimeNo> primeList) {
		this.mPrimeLock.lock();
		try {
		
			//Log.i("PRIME", "GlobalState : addPrimeResult() : Before : mPrimeList -> " + mPrimeList.size() 
			//											+ ", mPrimeList -> " + primeList.size()); 
			mPrimeList.addAll(primeList);
			Collections.sort(mPrimeList, PrimeNo.primeComparator);
	
			primeList.clear();
			
			//Log.i("PRIME", "GlobalState : addPrimeResult() : After : mPrimeList -> " + mPrimeList.size() 
			//		+ ", mPrimeList -> " + primeList.size()); 
			/*for(PrimeNo primeObj : mPrimeList) {
				Log.w("PRIME", "GlobalState : Nth No -> " + primeObj.getNthNo() );
			}
			Log.i("PRIME", "DONE");*/
		
		}finally {
			this.mPrimeLock.unlock();
		}
	}
	
	
	//public void getPrimeResult(ArrayList<PrimeNo> primeResultList) {
	public boolean getPrimeResult(ArrayList<PrimeNo> primeResultList) {
		boolean isAvailable = true;
		this.mPrimeLock.lock();
		try {
			
			//Log.i("PRIME", "GlobalState : getPrimeResult(): Before : primeResultList -> " + primeResultList.size() 
			//										+ " mPrimeList -> " + mPrimeList.size());
			if(mPrimeList.size() > 0) {
				primeResultList.addAll(mPrimeList);
				mPrimeList.clear();
			}else {
				isAvailable = false;
			}
		

			//Log.i("PRIME", "GlobalState : getPrimeResult() : After : primeResultList -> " + primeResultList.size() 
			//										+ " mPrimeList -> " + mPrimeList.size());
				
		}finally {
			this.mPrimeLock.unlock();
		}
		return isAvailable;
	}
	
	
	
	
	
	//---------------------------------------------------------------------------------------
	//							PrimeNo result Lock : End
	//---------------------------------------------------------------------------------------
	
	
	//---------------------------------------------------------------------------------------
	//							Accessed only by UI thread
	//---------------------------------------------------------------------------------------
	public ArrayList<UserRequest> mGroupList = new ArrayList<UserRequest>();
	public ArrayList<PrimeNo> mItemList = new ArrayList<PrimeNo>();

	
	
	
	
	
	//---------------------------------------------------------------------------------------
	//							OTHERS
	//---------------------------------------------------------------------------------------
	
	private boolean isActivityVisible = true;
	public void setVisibilityToTrue() {
		isActivityVisible = true;
	}
	
	public void setVisibilityToFalse() {
		isActivityVisible = false;
	}
	
	public boolean isActivityVisible() {
		return this.isActivityVisible;
	}
	
	private static GlobalState mInstance;

	@Override 
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		
	}	
	public static GlobalState getInstance() 
	{ 
		return mInstance; 
	}


}
