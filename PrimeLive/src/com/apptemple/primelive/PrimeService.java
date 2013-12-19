package com.apptemple.primelive;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class PrimeService extends Service {

	ThreadSync mThreadSync;
	WakeLock wakeLock;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		
		
		
		/*PowerManager mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();*/
		
		//Log.v("PRIME", "Service : Inside onCreate()");
		mThreadSync = new ThreadSync(this);
				
		
		//Set this service to Foreground 
		Log.w("PRIME", "Service : onCreate : Making this service Foreground");
		@SuppressWarnings("deprecation")
		Notification note = new Notification(R.drawable.app_icon, "Calculating Prime...", System.currentTimeMillis());
		Intent i=new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
		 PendingIntent pi=PendingIntent.getActivity(this, 0,
                 i, 0);

		 note.setLatestEventInfo(this, "Prime number app",
				 "Service running in background..",
				 pi);
		note.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(1337, note);
		
	}
	
	/**
	 * Called every time when startService() is called
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("PRIME", "Service : Inside onStartCommand()");
		
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
		
		//return START_NOT_STICKY;
		return START_STICKY;
	}
	

	
	@Override
	public IBinder onBind(Intent intent) {
		//We are not using this Service as a 'Bound' service
		//so return NULL
		return null;
	}

	@Override
	public void onDestroy() {
		Log.w("PRIME", "Service : Service DESTROYED");
		stopForeground(true);
		//this.wakeLock.release();
	}
	
	/*@Override
	public void onPause() {
		
	}*/
	
}
