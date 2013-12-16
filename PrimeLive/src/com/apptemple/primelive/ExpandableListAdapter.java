package com.apptemple.primelive;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
	 
    private Context mContext;
    boolean mDescending = false;
    UserRequest mRequest;
    PrimeNo mPrimeNo;
    GlobalState mState;
    
    public ExpandableListAdapter(Context context, ArrayList<UserRequest> header,
    		ArrayList<PrimeNo> primeList) {
    	
    	
        this.mContext = context;
        mState = GlobalState.getInstance();
    }
    

    public boolean toggleSorting() {
    	if(mDescending == true) {
    		mDescending = false;
    	}else {
    		mDescending = true;
    	}
    	return mDescending;
    }
    
    /*@Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
    	return false;
    }*/
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
    	if(mDescending == true) {
    		mRequest = mState.mGroupList.get(groupPosition);
    		mPrimeNo = mState.mItemList.get(mRequest.getNo() - 1 - childPosition);
    	}else {
    		mPrimeNo = mState.mItemList.get(childPosition);
    	}
    		
    	//final String childText = (String) getChild(groupPosition, childPosition);
    	 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
          
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        TextView primeView = (TextView)convertView.findViewById(R.id.primeNoId);
        
        if(mPrimeNo.mPrimeNo <= 0) {
        	primeView.setText("...");
        }else {
        	primeView.setText("" + mPrimeNo.mPrimeNo);
        }
 
        txtListChild.setText("" + mPrimeNo.mNthNo);
        
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
    	UserRequest request = mState.mGroupList.get(groupPosition);
    	int childCount = request.getNo();
    	
    	//Log.i("PRIME", "Adapter1 : Inside getChildrenCount() : Group position -> " 
    	//				+ groupPosition + " Child count -> " + childCount);
    	
    	return childCount;
    }
 
 
    @Override
    public int getGroupCount() {
    	
    	//Log.i("PRIME", "Adapter : getGroupCount() : Group count -> " 
    	//								+ mState.mGroupList.size());
    	return mState.mGroupList.size();
    }
 
    
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
    	
    	//UserRequest request = (UserRequest)getGroup(groupPosition);
    	UserRequest request = mState.mGroupList.get(groupPosition);
    	if(convertView == null) {
    		LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);    
    		convertView = infalInflater.inflate(R.layout.list_group, null);
    	}
    	
    	TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText("" + request.getNo());
  
        return convertView;
    }
    
    
    public void addItemToPrimeList(int maxNo) {
    	int itemSize = mState.mItemList.size();
		//Log.i("PRIME", "Adapter1 : Max number -> " + maxNo + " mItemList.size() -> " + itemSize);
		
		if(itemSize < maxNo) {
			//Log.i("PRIME", "Adapter1 : Arg : Creating additional items");
			for(int inc = itemSize + 1 ; inc <= maxNo ; inc++) {
				mState.mItemList.add(new PrimeNo(inc));
				//Log.i("PRIME", "Adapter1 : New item added : " + inc);
			}
		} 	
    }
    
    public void addItemToPrimeList() {
    	int headerSize = mState.mGroupList.size();
    	UserRequest userRequest;
    	if(headerSize > 0) {
    		userRequest = mState.mGroupList.get(headerSize - 1);
    	}else {
    		//Log.v("PRIME", "Adapter1 : No items to add for PrimeList");
    		return;
    	}
    	
		int maxNo = userRequest.getNo();
		int itemSize = mState.mItemList.size();
		//Log.v("PRIME", "Adapter : mGroupList.size() -> " + mState.mGroupList.size() 
		//				+ ", Max number -> " + maxNo + " mItemList.size() -> " + itemSize);
		
		if(itemSize < maxNo) {
			//Log.i("PRIME", "Adapter1 : Creating additional items");
			for(int inc = itemSize + 1 ; inc <= maxNo ; inc++) {
				mState.mItemList.add(new PrimeNo(inc));
				//Log.i("PRIME", "Adapter : New item added : " + inc);
			}
		}
	
    }
    
    void addNewNodeToUserGroup(int no) {
		mState.mGroupList.add(new UserRequest(no));
		Log.d("PRIME", "Adapter : New no added to mGroupList");
		Collections.sort(mState.mGroupList, UserRequest.userRequestComparator);
		addItemToPrimeList(no);
	}
    
    public boolean isDuplicate(int no) {
    	boolean isDuplicate = false;
    	
    	//Log.v("PRIME", "Adapter : Inside isDuplicate() : Notifying dataset changed");
    	
    	for(UserRequest userRequest : mState.mGroupList) {
    		if(userRequest.getNo() == no) {
    			Log.d("PRIME", "Adapter : Duplicate no. Not adding to mGroupList");
    			isDuplicate = true;
    			break;
    		}
    	}
    	
    	return isDuplicate;
    }
 
   
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    

    @Override
    public long getGroupId(int groupPosition) {
    	//Log.i("PRIME", "Adapter1 : Inside getGroupId : groupPosition -> " + groupPosition);
        return groupPosition;
    }

    
    //@Override
    public Object getGroup(int groupPosition) {
    	//Log.i("PRIME", "Adapter1 : Inside getGroup() : Group position -> " + groupPosition);
    	return mState.mGroupList.get(groupPosition);
    }
   
    
    @Override
    public boolean hasStableIds() {
        return false;
    }
  
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
    	//Log.i("PRIME", "Adapter1 : INside getChild() : Group position -> " + groupPosition + " childPosition -> " + childPosition);
    	return mState.mItemList.get(childPosition);  
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
    	//Log.i("PRIME", "Adapter1 : Inside getChildId()");
        return childPosition;
    }

    
    
}