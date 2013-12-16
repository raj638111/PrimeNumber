package com.apptemple.primelive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

	void cleanupQueue(Intent intent) {
		
		if(intent.getAction().equals(Constants.SU_PRIME)) {
			
			ArrayList<PrimeNo> primeList = new ArrayList<PrimeNo>();
			GlobalState state = GlobalState.getInstance();
			
			state.getPrimeResult(primeList);
			if(primeList.size() > 0) {
			
				PrimeNo primeObj = primeList.get(0);
				String msg = "N -> " + primeObj.getNthNo() + ", Prime No -> " + primeObj.mPrimeNo;
				sendNotification(msg);
			}
			//Log.v("PRIME", "MainActivity : cleanupQueue() : Cleaning up");
			mState.clearPrimeResult();
		}
		
	}
	
	private void sendFullRequest(GlobalState state) {
		
		if(state.mGroupList.size() > 0) {
			
			UserRequest userRequest = state.mGroupList.get(state.mGroupList.size() - 1);
			
			Request request = new Request(RequestType.SU_FULL_DATA, userRequest.getNo());
	        Log.d("PRIME", "Activity : sendFullRequest() : Starting Service : SU_FULL_DATA");
	        Intent intent = new Intent(this, PrimeService.class);
	        intent.putExtra("REQUEST", request); //?? : This is a Serial object. can performance be improved?
	        if(null == startService(intent)) {
	        	Log.e("PRIME", "Activity : Unable to start service : SU_FULL_DATA");
	        }
		}
	}
	
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			GlobalState state = GlobalState.getInstance();
			if(state.isActivityVisible() == false) {
				//Log.w("PRIME", "BroadcastRX : Activity NOT in visible state");
				cleanupQueue(intent);
				return;
			}else {
				//Log.i("PRIME", "BroadcastRX : Activity is in VISIBLE state");
			}
			
			if(intent.getAction().equals(Constants.SU_HOME_PAGE)) {
				Log.d("PRIME", "BroadcastRX : SU_HOME_PAGE available");
			
				state.getUserResult(state.mGroupList);
				Collections.sort(state.mGroupList, UserRequest.userRequestComparator);
				mExpListAdapter.addItemToPrimeList();
				mExpListAdapter.notifyDataSetChanged();
			
				sendFullRequest(state);			
			}
			else if(intent.getAction().equals(Constants.SU_PRIME)) {
				
				//Log.v("PRIME", "BroadcastRX : SU_PRIME available");
				ArrayList<PrimeNo> primeList = new ArrayList<PrimeNo>();
				mState.getPrimeResult(primeList);
				
				if(primeList.size() > 0) {
					PrimeNo pm = primeList.get(0);
					//Log.i("PRIME", "BroadcastRX : Calling addItemToPrimeList(" + pm.getNthNo() + ")");
					mExpListAdapter.addItemToPrimeList(pm.getNthNo());
					
				}else {
					//Log.w("PRIME", "BroadcastRX : primeList.size() = 0");
				}
				
				for(PrimeNo primeObj : primeList) {
					int nthNo = primeObj.getNthNo();
					int primeNo = primeObj.getPrimeNo();
					
					//Log.v("PRIME", "BroadcastRX : Result : NthNo -> " + nthNo + ", Prime No -> " 
					//		+ primeNo + ", mItemList.size() -> " + state.mItemList.size());
					//Log.v("PRIME", "BroadcastRX : Result : Updating result in Adapter");
					
					PrimeNo adapterPrime;
					adapterPrime =  state.mItemList.get(nthNo - 1);
					adapterPrime.setPrimeNo(primeNo);
					
				}
				mExpListAdapter.notifyDataSetChanged();
				
			}else {
				Log.e("PRIME", "BroadcastRX : Unknown result ");
			}
			
		}
	};
	
	/* A Comparator(Anonymous class) to set Priority in PriorityQueue */
	public static Comparator<UserRequest> mComparator = new Comparator<UserRequest>() {
		@Override
		public int compare(UserRequest req1, UserRequest req2) {
			return (req1.getNo() - req2.getNo());
		}
	};
	
	
	private GlobalState mState;
	private ExpandableListView mExpListView;
	private ExpandableListAdapter mExpListAdapter;
	private TextView mSortButton;
	private EditText mEditText;
	private Activity mActivity;
	private TextView mCollapseButton;
	NotificationManager mNotificationManager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mActivity = this;
     
        //Get all views
        mExpListView = (ExpandableListView)findViewById(R.id.expandableView);
        mSortButton = (TextView)findViewById(R.id.sortId);
        mEditText = (EditText)findViewById(R.id.editText);
        mCollapseButton = (TextView)findViewById(R.id.collpaseButtonId);
        
        mState = GlobalState.getInstance();
       
        mState.setVisibilityToTrue();
        
        //this.openOptionsMenu();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

       
        //Set Listview adapter
        //prepareListData();
        mExpListAdapter = new ExpandableListAdapter(this, mState.mGroupList, mState.mItemList);
        mExpListView.setAdapter(mExpListAdapter);
    
        
        mSortButton.setOnTouchListener(new OnTouchListener() 
        {
        	@Override
        	public boolean onTouch(View v, MotionEvent event) {
        		if(event.getAction() == MotionEvent.ACTION_DOWN) {
        			
        			//Log.v("PRIME", "Activity : onTouch() : mSortButton : ACTION_DOWN");
        			v.setBackgroundColor(Color.parseColor("#EDD483"));
        			
        		}else if(event.getAction() == MotionEvent.ACTION_UP) {
        			
        			//Log.v("PRIME", "Activity : onTouch() : mSortButton : ACTION_UP");
        			v.setBackgroundColor(Color.parseColor("#86E9F4"));
        			mExpListAdapter.toggleSorting();
        			mExpListAdapter.notifyDataSetChanged();
        			
        		}
        		return true;
        	}
        });
        
        mCollapseButton.setOnTouchListener(new OnTouchListener() {
        
        	@Override
        	public boolean onTouch(View v, MotionEvent event) {
        		if(event.getAction() == MotionEvent.ACTION_DOWN) {
        			
        			//Log.v("PRIME", "Activity : onTouch() : mCollapseButton : ACTION_DOWN");
        			v.setBackgroundColor(Color.parseColor("#EDD483"));
        			
        		}else if(event.getAction() == MotionEvent.ACTION_UP) {
        			
        			//Log.v("PRIME", "Activity : onTouch() : mCollapseButton : ACTION_UP");
        			v.setBackgroundColor(Color.parseColor("#86E9F4"));
        			int count = mExpListAdapter.getGroupCount();
            		for(int inc = 0 ; inc < count ; inc++) {
            			mExpListView.collapseGroup(inc);
            		}
        		}
        		return true;
        	}
        	
        });
              
        //Set listener for 'EditText' view
        editTextListener();
        
        //Register for Broadcast (MVM???)
        IntentFilter intentFilter = new IntentFilter(Constants.SU_HOME_PAGE);
        intentFilter.addAction(Constants.SU_PRIME);
        LocalBroadcastManager.getInstance(this).registerReceiver(mIntentReceiver, intentFilter);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	//Log.v("PRIME", "Activity : onResume() : Setting to TRUE");
    	mState = (GlobalState)getApplicationContext();
    	mState.setVisibilityToTrue();
    	mState.mGroupList.clear();
    	mState.mItemList.clear();
    	this.mNotificationManager.cancel(9999);
    	
    	
        //Send service request : SU_HOME_PAGE
        Request request = new Request(RequestType.SU_HOME_PAGE);
        Log.d("PRIME", "Activity : onResume() : Starting Service : SU_HOME_PAGE");
        Intent intent = new Intent(this, PrimeService.class);
        intent.putExtra("REQUEST", request); //?? : This is a Serial object. can performance be improved?
        if(null == startService(intent)) {
        	Log.e("PRIME", "Activity : Unable to start service : SU_HOME_PAGE");
        }
        
        
        mExpListView.setOnChildClickListener(new OnChildClickListener() {
        	
        	@Override
        	public boolean onChildClick(ExpandableListView parent, View v, 
        			int groupPosition, int childPosition, long id) {
        		LinearLayout linearLayout = (LinearLayout)v;
        		
        		TextView tview = (TextView)linearLayout.findViewById(R.id.lblListItem);
        		Integer no = Integer.parseInt(tview.getText().toString());
        		
        		//Log.v("PRIME", "Activity : onChildClick() : " + tview.getText().toString());
        		Request request = new Request(RequestType.SU_UNIT_WORK, no);
		        //Create intent and call service
		        Log.d("PRIME", "BroadcastRX : Starting Service 'PrimeService' : SU_UNIT_WORK");
		        Intent reqIntent = new Intent(mActivity, PrimeService.class);
		        reqIntent.putExtra("REQUEST", request); //?? : This is a Serial object. can performance be improved?
		        if(null == mActivity.startService(reqIntent)) {
		        	Log.e("PRIME", "Activity : onChildClick() : " + 
		        			"Unable to start service 'PrimeService' : SU_UNIT_WORK");
		        }
        		
        		return true;
        	}
        });
        
    }

    
    @Override
    protected void onStop() {
    	super.onStop();
    	Log.d("PRIME", "Activity : onStop() : Setting to FALSE");
    	mState = (GlobalState)getApplicationContext();
    	mState.setVisibilityToFalse();
    	
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.d("PRIME", "Activity : onDestroy() : Activity getting destroyed");
    }
   
    @Override 
    protected void onPause() {
    	super.onPause();
    	Log.d("PRIME", "Activity : onPause() : Setting to FALSE");
    	mState = (GlobalState)getApplicationContext();
    	mState.setVisibilityToFalse();
    }
    
    int getMaxNo(int no) {
    	GlobalState state = GlobalState.getInstance();
    	if(state.mGroupList.size() > 0) {
    		int maxNo = state.mGroupList.get(state.mGroupList.size() - 1).getNo();
    		if(maxNo >= no) {
    			return 0;
    		}else {
    			return maxNo + 1;
    		}
    	}else {
    		return 1;
    	}
    		
    }
    
    
    
    @SuppressWarnings("deprecation")
	public void sendNotification(String msg) {
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.app_icon, "Prime Number result", System.currentTimeMillis());
		
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		notification.setLatestEventInfo(this, "PrimeNumber Application", msg, pIntent);
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(9999, notification);
		
		
	} 
    void editTextListener() {
    	mEditText.setOnKeyListener(new OnKeyListener() {
    		public boolean onKey(View v, int keyCode, KeyEvent event) {
    			int no = 0;
    			if(	(event.getAction() == KeyEvent.ACTION_DOWN) &&
    				(keyCode == KeyEvent.KEYCODE_ENTER)	) {
    				
    				
    					try {
    						no = Integer.parseInt(mEditText.getText().toString());
    						//Check duplicate, if no duplicate - insert new no in adapter & notify adapter
    						if(mExpListAdapter.isDuplicate(no) == false) {
    							
    							int maxNo = getMaxNo(no);
    							if(maxNo != 0) {
    							
	    							//Create request
	    							Request request = new Request(RequestType.SU_PRIME_NUMBER, no);
	    							request.setStartNo(getMaxNo(no));
	    					        
	    							//Add request to adapter & notify
	    							mExpListAdapter.addNewNodeToUserGroup(no);
	    							mExpListAdapter.notifyDataSetChanged();
	    							
	    							//Send request to Service
	    							Log.d("PRIME", "Activity : onKey() : Starting Service 'SU_PRIME_NUMBER'" + no);
	    					        Intent intent = new Intent(mActivity, PrimeService.class);
	    					        intent.putExtra("REQUEST", request); //?? : This is a Serial object. can performance be improved?
	    					        if(null == startService(intent)) {
	    					        	Log.e("PRIME", "Activity : Unable to start service 'PrimeService'");
	    					        }
    							}else {
    								//Log.v("PRIME", "No " + no + " WITHIN range.");
    								mExpListAdapter.addNewNodeToUserGroup(no);
	    							mExpListAdapter.notifyDataSetChanged();
	    							
	    							//Send request to Service
	    							Request request = new Request(RequestType.SU_INSERT_GROUP_NO, no);
	    							Log.d("PRIME", "Activity : onKey() : Starting Service 'SU_INSERT_GROUP_NO'" + no);
	    					        Intent intent = new Intent(mActivity, PrimeService.class);
	    					        intent.putExtra("REQUEST", request); //?? : This is a Serial object. can performance be improved?
	    					        if(null == startService(intent)) {
	    					        	Log.e("PRIME", "Activity : Unable to start service 'PrimeService' : SU_INSERT_GROUP_NO");
	    					        }
    								
    							}
    						  
    						}else {
    							//Log.v("PRIME", "Activity : Duplicate no");
    							Toast.makeText(MainActivity.this, "Result already available", Toast.LENGTH_SHORT).show();
    							
    						}
    						InputMethodManager inputManager = (InputMethodManager)            
				        		  mActivity.getSystemService(Context.INPUT_METHOD_SERVICE); 
				        		    inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),      
				        		    InputMethodManager.HIDE_NOT_ALWAYS);
				        	//mEditText.setText("");
				            return true;
    							
    					}catch(NumberFormatException e) {
    						Toast.makeText(MainActivity.this, "INVALID Input", Toast.LENGTH_SHORT).show();
    					}
    					finally{
    						mEditText.setText("");
    					}
    					
    			}else {
    				//Log.v("PRIME", "Activity : Hardware keyevent identified");
    			}
    			
    			return false;
    		}
    	}
    	);	

    }

    
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	Log.d("PRIME", "Activity : onReStart() : Setting to TRUE");
    	mState = (GlobalState)getApplicationContext();
    	mState.setVisibilityToTrue();
    	
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	Log.d("PRIME", "Activity : onStart() : Setting to TRUE");
    	mState = (GlobalState)getApplicationContext();
    	mState.setVisibilityToTrue();
    	
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }  
    
    
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {
    	Request request;
    	Intent reqIntent;
    	
    	switch (item.getItemId()) {  
    		case R.id.item1 :
    			 //Create request object
		        request = new Request(RequestType.SU_RESET);
		        //Create intent and call service
		        Log.d("PRIME", "BroadcastRX : Starting Service 'PrimeService' : SU_RESET");
		        reqIntent = new Intent(getApplicationContext(), PrimeService.class);
		        reqIntent.putExtra("REQUEST", request); //?? : This is a Serial object. can performance be improved?
		        if(null == startService(reqIntent)) {
		        	Log.e("PRIME", "Activity : onOptionsItemSelected-1 : " + 
		        			"Unable to start service 'PrimeService'");
		        }
		        
		        mState.mGroupList.clear();
		        mState.mItemList.clear();
		        this.mExpListAdapter.notifyDataSetChanged();
    			return true;
    		case R.id.item3 :
    			 //Create request object
		        request = new Request(RequestType.SU_ABORT_SERVICE);
		        //Create intent and call service
		        Log.d("PRIME", "BroadcastRX : Starting Service 'PrimeService' : SU_ABORT_SERVICE");
		        reqIntent = new Intent(getApplicationContext(), PrimeService.class);
		        reqIntent.putExtra("REQUEST", request); //?? : This is a Serial object. can performance be improved?
		        if(null == startService(reqIntent)) {
		        	Log.e("PRIME", "Activity : onOptionsItemSelected-2 : " + 
		        			"Unable to start service 'PrimeService'");
		        }
		        Toast.makeText(MainActivity.this, "Calculation Stopped", Toast.LENGTH_SHORT).show();
		        return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    
}


