package com.apptemple.primelive;

import java.util.Comparator;

public class PrimeNo {
	int mNthNo = 0;
	int mPrimeNo = 0;
	
	public PrimeNo(int nthNo) {
		mNthNo = nthNo;
	}
	
	public PrimeNo(int nthNo, int primeNo) {
		mNthNo = nthNo;
		mPrimeNo = primeNo;
	}
	
	public int getNthNo() {
		return mNthNo;
	}
	public int getPrimeNo() {
		return mPrimeNo;
	}
	
	public void setPrimeNo(int primeNo) {
		mPrimeNo = primeNo;
	}
	
	public static Comparator<PrimeNo> primeComparator 
    = new Comparator<PrimeNo>() {

		public int compare(PrimeNo req1, PrimeNo req2) {
		
			return req2.getNthNo() - req1.getNthNo();
		}

	};

}
