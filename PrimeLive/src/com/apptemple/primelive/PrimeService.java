package com.apptemple.primelive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PrimeService extends Service {

	ThreadSync mThreadSync;
	
	@Override
	public void onCreate() {
		//Log.v("PRIME", "Service : Inside onCreate()");
		mThreadSync = new ThreadSync(this);
		
	}
	
	/**
	 * Called every time when startService() is called
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.v("PRIME", "Service : Inside onStartCommand()");
		
		//Get request
		Request request = (Request)intent.getSerializableExtra("REQUEST");
		if(request == null) {
			Log.e("PRIME", "Service : Intent value CANNOT be null");
		}
		//Log.v("PRIME", "Service : Request received : Request type -> " + request.getRequestType());
		
		/*
		 * - Insert request into Request Queue
		 * - Create thread if needed 
		 * - If needed, inform thread about the availability of new Request 
		 */
		mThreadSync.insertRequest(request, startId);
		
		return START_NOT_STICKY;
	}
	

	
	@Override
	public IBinder onBind(Intent intent) {
		//We are not using this Service as a 'Bound' service
		//so return NULL
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d("PRIME", "Service : Service DESTROYED");	
	}
	
}
