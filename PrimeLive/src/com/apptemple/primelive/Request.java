package com.apptemple.primelive;

import java.io.Serializable;

public class Request implements Serializable {

	//?? : What is the need for serialVersionUID ; identify
	private static final long serialVersionUID = -6024727336820833747L;
	private RequestType mRequestType = RequestType.NONE; //?? : No need for memory allocation
	private int mPrimeNumber = 0;		//Total no of Prime Numbers expected
	boolean mIsHighPriority = false;
	String mRequestId;
	int mStartAt = 0;
	
	public String getRequestId() {
		return mRequestId;
	}
	
	public int getNo() {
		return mPrimeNumber;
	}
	
	public int startAt() {
		return mStartAt;
	}
	
	public void setStartNo(int startAt) {
		mStartAt = startAt;
	}
	
	private void setRequestId() {
		if(mRequestType == RequestType.SU_HOME_PAGE) {
			mRequestId = "SUBID_HOMEPAGE";
		}else if(mRequestType == RequestType.SU_PRIME_NUMBER) {
			mRequestId = "SUBID_" + mPrimeNumber;
		}else {
			mRequestId = "SUBID_NONE"; 
		}
	}
	
	public Request(RequestType requestType) {
		mRequestType = requestType;
		setRequestId();
	}
	
	public Request(RequestType requestType, int primeNumber) {
		mRequestType = requestType;
		mPrimeNumber = primeNumber;
		setRequestId();
	}

	//?? : Explore the option to comment this
	public RequestType getRequestType() {
		return mRequestType;
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
