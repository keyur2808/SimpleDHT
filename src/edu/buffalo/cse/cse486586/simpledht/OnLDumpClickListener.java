package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class OnLDumpClickListener implements OnClickListener {
    TextView tv;
    ContentResolver cr;
    Uri uri;
    String projection="@";
	
    public OnLDumpClickListener(TextView tv, ContentResolver contentResolver){
		this.tv=tv;
		this.cr=contentResolver;
		this.uri=buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
	}
	
	@Override
	public void onClick(View arg0) {
		Cursor resultCursor=cr.query(uri,null,projection ,null,null);
		int count=resultCursor.getCount();
		int keyIndex = resultCursor.getColumnIndex("key");
		int valueIndex = resultCursor.getColumnIndex("value");
		String key,value;
		resultCursor.moveToFirst();
		for (int i=0;i<count;i++){
		key = resultCursor.getString(keyIndex);
		value = resultCursor.getString(valueIndex);
		tv.append(key+" "+value+"\n");
          resultCursor.moveToNext();	
		}
	}


	private Uri buildUri(String content, String authority) {
	 	Uri.Builder uriBuilder = new Uri.Builder();
	     uriBuilder.authority(authority);
	     uriBuilder.scheme(content);
	     return uriBuilder.build();
	 	}


}