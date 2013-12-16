package com.apptemple.primelive;

import java.util.Comparator;

public class UserRequest implements Comparable<UserRequest> {
	private int mHowMany = 0;
	boolean mIsComplete = false;
	private String mCreatedTime = new String(); 	//?? : Set proper time
	private String mTstamp = new String();		//?? : Set proper time
	
	UserRequest(int howMany,	int currentCount, 	boolean isComplete, 
							String createdTime, String tstamp) {
			
		mHowMany = howMany;
		mIsComplete = isComplete;
		mCreatedTime = createdTime;
		mTstamp = tstamp;
	}
	
	UserRequest(int howmany) {
		mHowMany = howmany;
	}
	
	public int getNo() {
		return mHowMany;
	}
	
	public int compareTo(UserRequest userRequest) {
		int howMany = userRequest.getNo();
		return this.mHowMany - howMany;
	}
	
	public static Comparator<UserRequest> userRequestComparator 
    = new Comparator<UserRequest>() {

		public int compare(UserRequest req1, UserRequest req2) {
		
			return req1.getNo() - req2.getNo();
		}

	};

}
