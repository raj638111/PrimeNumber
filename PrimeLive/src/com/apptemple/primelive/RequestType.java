package com.apptemple.primelive;

public enum RequestType {

	NONE,				//Default 
	SU_HOME_PAGE,		//Request from a SUbscriber for home page 
	SU_PRIME_NUMBER,	//Request from a subscriber to calculate prime number
	SU_FULL_DATA,		//Get prime numbers from the database
	SU_RESET,			//Reset the application to original state
	SU_INSERT_GROUP_NO, //Insert group no into database
	SU_ABORT_SERVICE,	//Abort all the service thread & close the service
	SU_UNIT_WORK,		//Fetch prime no for a single number(ie Nth number)
	TH_TASK;			//Request from one THread to another to work on a task
	
	
}
