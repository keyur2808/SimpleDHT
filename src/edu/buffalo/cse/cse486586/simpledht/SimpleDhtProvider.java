package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {
	SimpleDhtProvider provider;
	//static final String []REOTE_PORT={"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT = 10000;
    public static String  myHash;
    public static String my_port;
    public static MessageStore messagesdb;
    public static SQLiteDatabase db;
    int algo=SQLiteDatabase.CONFLICT_REPLACE;
    static ArrayList<ChordNodes> nodelist;
        static String successor;
        static String predecessor;
        static String nodeFirst;
        static String nodeLast;
        static String nodeLastHash;
	static String nodeFirstHash;
	static String predecessorHash;
	static String successorHash;
	//static Message msg;
	private int ringsize=1;
	Uri mUri;
	private ArrayList<SerialCursorRow>answerCursor;
	private int Sum=0;
    private ArrayList<String> Count;
    @Override
    public boolean onCreate() {
    	answerCursor= new ArrayList<SerialCursorRow>();
    	Count=new ArrayList<String>();
		try {
		 Setup_Ports();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
       	messagesdb=new MessageStore(this.getContext());
        if (messagesdb!=null){
     	   db=messagesdb.getWritableDatabase();
     	   Log.v("create","DataStringbase Ncreated");
     	   
        }
        else
        	return false;
        Socket_create();
       Log.e(my_port,"Port");
       if (!my_port.equals("11108")){
        send_Hello();        	
        }
        else 
        {
        	nodelist=new ArrayList<ChordNodes>();
        	ChordNodes cnodes = null;
			cnodes = new ChordNodes(my_port,myHash);
			if (cnodes!=null)
			nodelist.add(cnodes);
        }
        successor=my_port;
        predecessor=my_port;
        nodeFirst=my_port;
        nodeLast=my_port;
        nodeLastHash=myHash;
        nodeFirstHash=myHash;
        predecessorHash=myHash;
        successorHash=myHash;
    
        Log.e("Initialized","Successfuly");
        return true;
    
    }
	
    private void Setup_Ports() throws NoSuchAlgorithmException {
    	TelephonyManager tel = (TelephonyManager)this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
    	String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        my_port = String.valueOf((Integer.parseInt(portStr) * 2));
        myHash=genHash(String.valueOf((Integer.parseInt(portStr))));
               		
	}

    private void send_Hello(){
    	Message msg=new Message("Hello",my_port);
    	msg.setDest_port("11108");
        if (msg.getDest_port()!=null)new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, null);
        
    }
    
    
	public void SetNeighbors() throws InterruptedException, NoSuchAlgorithmException {
    String predecessorset="";
	String successorset="";
	ringsize=nodelist.size();
	if (nodelist.size()<=1)return;
	nodeFirstHash=nodelist.get(0).getHashValue();
	nodeLastHash=nodelist.get(nodelist.size()-1).getHashValue();
	nodeFirst=nodelist.get(0).getPort();
    nodeLast=nodelist.get(nodelist.size()-1).getPort();
    Log.e(nodeFirstHash,nodeLastHash);
	int i;
    for (i=0;i<nodelist.size();i++){
    if (i==0){
    	predecessorset=nodelist.get(nodelist.size()-1).getPort();
    	successorset=nodelist.get(i+1).getPort();
    	
    	
    }	
    else{
    	if (i==(nodelist.size()-1)){
    
    	predecessorset=nodelist.get(i-1).getPort();
        successorset=nodelist.get(0).getPort();
        }	 
    else{
    	predecessorset=nodelist.get(i-1).getPort();
    	successorset=nodelist.get(i+1).getPort();
    }
    }
    if (nodelist.get(i).getPort().equals("11108")){
        predecessor=predecessorset;
        successor=successorset; 
        Integer tmp;
        tmp=Integer.parseInt(predecessor)/2;
        predecessorHash=genHash(tmp.toString());
        tmp=Integer.parseInt(successor)/2;
        successorHash=genHash(tmp.toString());	
    	Log.e(predecessor+successor,predecessorHash+successorHash);
        continue;	
    }
    Message msg=new Message("Neighbors",my_port);
    msg.setSuccessor(successorset);
    msg.setPredecessor(predecessorset);
    msg.setDest_port(nodelist.get(i).getPort());
    msg.setEndNodes(nodelist.get(0).getPort(),nodelist.get(nodelist.size()-1).getPort());
    msg.setRingSize(nodelist.size());
    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, null);
    Log.e(predecessorset,successorset);
    }
    
    }
	

   
   @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
       Sum=0;
       Count.clear();
	   if (selection.equals("@"))
       {
    	   
    	   return db.delete("Messages","1", selectionArgs);
       }
       if (selection.equals("*"))
       {
    	  int temp= db.delete("Messages","1", selectionArgs);
    	  if (predecessor.equals(my_port))return temp;  
    	  Message msgs=new Message("Delete",my_port);
		    msgs.setDest_port(successor);
		    msgs.setKey(selection);
		    new ClientTask().doInBackground(msgs);
		    try {
		    	synchronized(Count){
		    	Sum=temp;
		    	Count.wait();
		    	Count.add(String.valueOf(Sum));
				return Integer.parseInt(Count.get(0));		     
		    }
		    } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
       }
       try {
    	   if (
   				
   				( (genHash(selection).compareTo(predecessorHash)>0) && (genHash(selection).compareTo(myHash)<=0) )
   		                               || 
   		        ( (genHash(selection).compareTo(nodeLastHash)>0) && (my_port.equals(nodeFirst)) )
   		                               ||
   		        ( (genHash(selection).compareTo(nodeFirstHash)<0) && (my_port.equals(nodeFirst)) )
   		                               ||
   		        (predecessor.equals(my_port))	
   				
   			)
			{  
			
	        return db.delete("Messages","key='"+selection+"'", selectionArgs);	
				   		
			}
			else{
				Log.e("Sending","Successor");
				Message msgs=new Message("Delete",my_port);
			    msgs.setDest_port(successor);
			    msgs.setKey(selection);
			    new ClientTask().doInBackground(msgs);
			    synchronized(Count){
			    	Sum=0;
			    	Count.wait();
			    	Count.add(String.valueOf(Sum));
			    	return Integer.parseInt(Count.get(0));		     
			    }
			   
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("Interrupted","Delete");
			e.printStackTrace();
		}

       return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stubNeighbors
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
    		
    		String keys=values.getAsString("key");
    	try {
    		if (
    				
    				( (genHash(keys).compareTo(predecessorHash)>0) && (genHash(keys).compareTo(myHash)<=0) )
    		                               || 
    		        ( (genHash(keys).compareTo(nodeLastHash)>0) && (my_port.equals(nodeFirst)) )
    		                               ||
    		        ( (genHash(keys).compareTo(nodeFirstHash)<0) && (my_port.equals(nodeFirst)) )
    		                               ||
    		        (predecessor.equals(my_port))	
    				
    			)
         	{
    			
    			db.insertWithOnConflict("Messages", null, values,algo);
		     	Log.v("insert", values.toString());
		    }
			else{
				Log.e("Successor","Sending");
				Message msgs=new Message("Insert",my_port);
			    msgs.setDest_port(successor);
			    msgs.setKeyValue(values.getAsString("key"),values.getAsString("value"));
			    new ClientTask().doInBackground(msgs);
			    
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
        
    }

    
    
    
	@Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
			 		
    	if  (selection.equals("@")){	
    	Cursor queryCursor=db.query("Messages", null, null, selectionArgs, null, null, null);
        if(queryCursor==null)Log.v("query","Databases Query");
        Log.e("Cursorsize", String.valueOf(queryCursor.getCount()));
        return queryCursor;
    	}
    	if  (selection.equals("*")){
    		Message msgs;
    		Cursor queryCursor=db.query("Messages", null,null, selectionArgs, null, null, null);
            if(queryCursor==null)Log.v("query","Databases Query");
            if (predecessor.equals(my_port))return queryCursor;
            msgs=new Message("Query",my_port);
            msgs.setDest_port(successor);
            msgs.setKey(selection);
		    ArrayList<SerialCursorRow>cursorMap=genList(queryCursor);
		    
		    new ClientTask().doInBackground(msgs);
		    
		    try{
			    	synchronized(answerCursor){
			    		answerCursor.addAll(cursorMap);
			    		answerCursor.wait(5000);
			    		String []columnNames={"key","value"};
			    		MatrixCursor cr=new MatrixCursor(columnNames);
			    	
			    		for (int i=0;i<answerCursor.size();i++){
			    			String []columnValues={answerCursor.get(i).getKey(),answerCursor.get(i).getValue()};
			    			if (i==answerCursor.size()-1)
			    			{
			    				Log.e("Row",answerCursor.get(i).getKey()+answerCursor.get(i).getValue());
			    			}
			    			cr.addRow(columnValues);	
			    			}
			    	 		answerCursor.clear();
			    	 		if (cr!=null)Log.e("Cursorsize",String.valueOf(cr.getCount()));
			    	 		return cr;
			    }
			    	}
			    	catch (InterruptedException e) {
					    e.printStackTrace();
					}

			   // Log.e("Creating","Cursor");
			   // Log.e("Cursor size", ((Integer)answerCursor.size()).toString());
			   
		    
		    }
    	
    	try {
			if (
    				
    				( (genHash(selection).compareTo(predecessorHash)>0) && (genHash(selection).compareTo(myHash)<=0) )
    		                               || 
    		        ( (genHash(selection).compareTo(nodeLastHash)>0) && (my_port.equals(nodeFirst)) )
    		                               ||
    		        ( (genHash(selection).compareTo(nodeFirstHash)<0) && (my_port.equals(nodeFirst)) )
    		                               ||
    		        (predecessor.equals(my_port))	
    				
    			)


			{  
			Cursor queryCursor=db.query("Messages", null, "key='"+selection+"'", selectionArgs, null, null, null);
	        Log.e(selection, selection);
			if(queryCursor==null)Log.v("queryn","Databases Query");
	        return queryCursor;	
				   		
			}
			else{
				Log.e("Sending","Successor");
				Message msgs=new Message("Query",my_port);
			    msgs.setDest_port(successor);
			    msgs.setKey(selection);
			    new ClientTask().doInBackground(msgs);
			    try
			    {
			    	synchronized(answerCursor){
			    		answerCursor.clear();
			    		answerCursor.wait(5000);
			    		String []columnNames={"key","value"};
			    		MatrixCursor cr=new MatrixCursor(columnNames);
			    	
			    		for (int i=0;i<answerCursor.size();i++){
			    			String []columnValues={answerCursor.get(i).getKey(),answerCursor.get(i).getValue()};
			    			if (i==answerCursor.size()-1)
			    			{
			    				Log.e("Row",answerCursor.get(i).getKey()+answerCursor.get(i).getValue());
			    			}
			    			cr.addRow(columnValues);	
			    			}
			    		answerCursor.clear();
			    		return cr;
			    	
			    		
			    }
			    	}
			    	catch (InterruptedException e) {
					    e.printStackTrace();
					}

			    }
			   // Log.e("Creating","Cursor");
			   //Log.e("Cursorsize",String.valueOf(cr.getCount()));
			    
			
					
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	return null;
    	
    }

	private ArrayList<SerialCursorRow>genList(Cursor cr){
   	 ArrayList<SerialCursorRow>cursorMap=new ArrayList<SerialCursorRow>();
   	 int keyIndex = cr.getColumnIndex("key");
		 int valueIndex = cr.getColumnIndex("value");
		 int count=cr.getCount();
		 cr.moveToFirst();
		 for (int i=0;i<count;i++){
		 SerialCursorRow resultCursor=new SerialCursorRow(cr.getString(keyIndex),cr.getString(valueIndex));
		 cursorMap.add(resultCursor);
		 cr.moveToNext();
		 } 
		 return cursorMap;
   	
    }
	
	
	
	
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        @SuppressWarnings("resource")
		Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

      
    
 void Socket_create() {
	 try {
     	ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
         new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
     } catch (IOException e) 
     {
     	Log.e("Provider", "Can't create a ServerSocket");
         return;
     }
	     
 }    

 private class ServerTask extends AsyncTask<ServerSocket, Message, Void> {

     private int count=0;

	@Override
     protected Void doInBackground(ServerSocket... sockets) {
         ServerSocket serverSocket = sockets[0];
 while(true){
         Message message = null;
			try{
         	Socket clientSocket=serverSocket.accept();
     	    ObjectInputStream inSocket=new ObjectInputStream(clientSocket.getInputStream());
             try {
            	 if (inSocket!=null)
					message = (Message)inSocket.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}	    
            if (message!=null)
            {

            	if (!message.getmyPort().equals(my_port)){
             		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
             		Log.e("Msg","Rcvd");
             		if (message.getMessage().equals("Insert")){
             			ContentValues cv=new ContentValues();
             			cv.put("key", message.getKey());
             			cv.put("value",message.getValue());
             			insert(mUri, cv);
               		}
             		if (message.getMessage().equals("Query")){
             			try {
        					Log.e("Query","Processing");
             				Message_Handler(mUri,message);
        				} catch (NoSuchAlgorithmException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
             			
               		}
             		if (message.getMessage().equals("Delete")){
             			try {
             				Log.e("Delete","Processing");
             				Message_Handler2(mUri,message);
             			}catch (NoSuchAlgorithmException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
             		            			
               		}

             		if (message.getMessage().equals("DeleteResult")){
             			Log.e("Count","CountReceived");
             			synchronized(Count){
             			Sum=message.getDeletecount();
             			Count.notify();
             			}
             		}
             		
             		if (message.getMessage().equals("DeleteAllResult")){
             			Log.e("Count","CountReceived");
             			synchronized(Count){
             			count++;
             			Sum+=message.getDeletecount();
             			if (count==ringsize-1){
             				count=0;
             				Count.notify();
                			}
             			
             			}
             		}
             		
             		if (message.getMessage().equals("Result")){
             			Log.e("Result","AnswerReceived");
                    	ArrayList<SerialCursorRow>cursorMap=message.getCursorMap();
                    	synchronized(answerCursor){
                    		answerCursor.addAll(cursorMap);
                    		answerCursor.notifyAll();
                		}
                         		
                	 }
             		if (message.getMessage().equals("Result*")){
             			Log.e("Result","AnswerReceived");
                    	ArrayList<SerialCursorRow>cursorMap=message.getCursorMap();
                    	synchronized(answerCursor){
                    		answerCursor.addAll(cursorMap);
                     	}
                    	count++;
                    	if (count==ringsize-1){
                    		synchronized(answerCursor){
                        		Log.e("Notify",my_port);
                    			answerCursor.notify();
                        		
                        		count=0;
                         	}	
                    	}
                         		
                	 }
             		if (message.getMessage().equals("SendReq")){
             		 SendData(message);
             		}
             		if (message.getMessage().equals("Data")){
                		 InsertData(message);
                		}
             		               	
             	}
         
            	publishProgress(message);	
            	clientSocket.close();                	
            }
             
                 	
           }
         catch (IOException e){Log.e("Chord", "Socket reading failed");} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			
                 	
         }       
         
 	}
 
	private void InsertData(Message message) {
		ArrayList<SerialCursorRow>insertList=message.getCursorMap();
		ContentValues cv=new ContentValues();
		for (int i=0;i<insertList.size();i++){
			cv.put("key", insertList.get(i).getKey());
			cv.put("value",insertList.get(i).getValue());
			db.insert("Messages",null,cv);
			cv.clear();
		}
	}

	private void SendData(Message message) throws NoSuchAlgorithmException {
	Cursor cr=db.query("Messages",null,null,null,null,null,null);	
	ArrayList<SerialCursorRow>cursorMap=sendCursor(cr,message.getMyHash());
	Message messageresp=new Message("Data",my_port);
	messageresp.setDest_port(message.getmyPort());
	messageresp.setCursorMap(cursorMap);
	new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,messageresp,null);
	}

	
	
     private void Message_Handler2(Uri mUri, Message message) throws NoSuchAlgorithmException {
    	 if (!message.getKey().equals("*")){
    		 if (valid(message.getKey())){
    		  int i=db.delete("Messages","key='"+message.getKey()+"'", null);
    		  Message msgs=new Message("DeleteResult",my_port);
    		  msgs.setDest_port(message.getmyPort());
    		  msgs.setDeletecount(i);
    		  new ClientTask().doInBackground(msgs);   
   		     }
    		 else{
    			 Message messageresp=new Message("Delete",message.getmyPort());
		    	 messageresp.setDest_port(successor);
		    	 messageresp.setKey(message.getKey());
		    	 new ClientTask().doInBackground(messageresp);
		    	 	 
    		 }
    	 }
    	 else{	
    		 int i=db.delete("Messages","1",null);
    		 Message messageresp=new Message("DeleteAllResult",my_port);
   	    	 messageresp.setDest_port(message.getmyPort());
   	    	 messageresp.setDeletecount(i);
   	    	 new ClientTask().doInBackground(messageresp);
   	    	 message.setDest_port(successor);
   	    	 new ClientTask().doInBackground(message);
    	
    	 }
		   
		
	}

	

    private void Message_Handler(Uri mUri,Message message) throws NoSuchAlgorithmException{
   	 
		 if (!message.getKey().equals("*")){
	    	 if (valid(message.getKey())){
	    	 Cursor queryCursor=db.query("Messages", null, "key='"+message.getKey()+"'", null, null, null, null);
	    	 if (queryCursor!=null){
	         ArrayList<SerialCursorRow>cursorMap=sendCursor(queryCursor,null);    	  
	    	 Message messageresp=new Message("Result",my_port);
	    	 messageresp.setDest_port(message.getmyPort());
	    	 messageresp.setCursorMap(cursorMap);
	    	 new ClientTask().doInBackground(messageresp);
	    	 }
	    	 }
	    	 else{
	    		 Message messageresp=new Message("Query",message.getmyPort());
		    	 messageresp.setDest_port(successor);
		    	 messageresp.setKey(message.getKey());
		    	 new ClientTask().doInBackground(messageresp);
		    	 	 
	    	 }
	    	 }	 
		 
		 else{
			 Cursor queryCursor=db.query("Messages", null,null, null, null, null, null);
			 if (queryCursor!=null){
   	         ArrayList<SerialCursorRow>cursorMap=sendCursor(queryCursor,null);    	  
   	    	 Message messageresp=new Message("Result*",my_port);
   	    	 messageresp.setDest_port(message.getmyPort());
   	    	 messageresp.setCursorMap(cursorMap);
   	    	 new ClientTask().doInBackground(messageresp);
   	    	 message.setDest_port(successor);
   	    	 new ClientTask().doInBackground(message);
   	     }
		 }
  	 	 
}
	
 
     private boolean valid(String selection) throws NoSuchAlgorithmException{
    	Log.e("Inside","Valid");
    	 if (
 				
 				( (genHash(selection).compareTo(predecessorHash)>0) && (genHash(selection).compareTo(myHash)<=0) )
 		                               || 
 		        ( (genHash(selection).compareTo(nodeLastHash)>0) && (my_port.equals(nodeFirst)) )
 		                               ||
 		        ( (genHash(selection).compareTo(nodeFirstHash)<0) && (my_port.equals(nodeFirst)) )
 		                               ||
 		        (predecessor.equals(my_port))	
 				
 			)return true;
    	 return false;
 
     }
     
     private ArrayList<SerialCursorRow>sendCursor(Cursor cr,String hash) throws NoSuchAlgorithmException{
    	 ArrayList<SerialCursorRow>cursorMap=new ArrayList<SerialCursorRow>();
    	 int keyIndex = cr.getColumnIndex("key");
 		 int valueIndex = cr.getColumnIndex("value");
 		 int count=cr.getCount();
 		 cr.moveToFirst();
		 for (int i=0;i<count;i++){
		 if (hash!=null){ 
		 if (genHash(cr.getString(keyIndex)).compareTo(hash)<=0){	 
		 SerialCursorRow resultCursor=new SerialCursorRow(cr.getString(keyIndex),cr.getString(valueIndex));
		 cursorMap.add(resultCursor);
		 cr.moveToNext();
		 db.delete("Messages","where key='"+cr.getString(keyIndex)+"'", null);
		 }
		 }
		 else{
			 SerialCursorRow resultCursor=new SerialCursorRow(cr.getString(keyIndex),cr.getString(valueIndex));
			 cursorMap.add(resultCursor);
			 cr.moveToNext();
			  
		 }
		 }
		 return cursorMap;
    	
     }
     
 	protected void onProgressUpdate(Message...messages) {
   	 if (messages[0].getMessage().equals("Hello")){//send join request
        ChordNodes cnode = null;
		try {
			Integer temp=Integer.parseInt(messages[0].getmyPort())/2;
			if (temp!=null)cnode = new ChordNodes(messages[0].getmyPort(),genHash(temp.toString()));
			Log.e(my_port,my_port);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			Log.e(my_port,my_port);
			e.printStackTrace();
		}
        nodelist.add(cnode);
        Collections.sort(nodelist);
        try {
			try {
				SetNeighbors();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        return;
   	 }//Set nbrs ports and hash
   	 if (messages[0].getMessage().equals("Neighbors")){
   		 predecessor=messages[0].getPredecessor();
   		 successor=messages[0].getSuccessor();
   		 nodeFirst=messages[0].getNodeFirst();
   		 nodeLast=messages[0].getNodeLast();
   		 ringsize=messages[0].getRingSize();
   		 Integer tmp;
   		 
   		 try {
   			 
   			tmp=(Integer.parseInt(predecessor)/2);
   			predecessorHash=genHash(tmp.toString());
				tmp=(Integer.parseInt(successor)/2);
				successorHash=genHash(tmp.toString());
				tmp=(Integer.parseInt(nodeFirst)/2);
				nodeFirstHash=genHash(tmp.toString());
				tmp=(Integer.parseInt(nodeLast)/2);
				nodeLastHash=genHash(tmp.toString());
				Log.e("End",nodeFirstHash+" "+nodeLastHash);
				Log.e("Nb",predecessorHash+" "+successorHash);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   		 
   		 Log.e("Neighborsn",successor+predecessor);
   		Message msg=new Message("SendReq",my_port);
   		msg.setDest_port(successor);
   		msg.setMyHash(myHash);
   		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,null);
   		 return;
   	 }
   	 
      	   }
 }
 
  
 
private class ClientTask extends AsyncTask<Message, Void, Void> {
     
	 @Override 
 	protected  Void doInBackground(Message... msgs) {
 	    	 try {
 	        	Log.e("Sending",msgs[0].getDest_port());
 	        	Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
 	        			Integer.parseInt(msgs[0].getDest_port()));
     	 	            	
 	        	ObjectOutputStream outSocket=new ObjectOutputStream(socket.getOutputStream());
 	            outSocket.writeObject(msgs[0]);
 	            outSocket.flush();
 	            outSocket.close();
 	            socket.close();
 	            	    	 }
 	    	    catch (UnknownHostException e) {
 		            Log.e("Error", "ClientTask UnknownHostException");
 		        } catch (IOException e) {
 		            Log.e("Error", "ClientTask socket IOException");
 		            
 		        }        	
 	    return null;
 	       
 	      }
}
 
 
 private Uri buildUri(String content, String authority) {
	 	Uri.Builder uriBuilder = new Uri.Builder();
	     uriBuilder.authority(authority);
	     uriBuilder.scheme(content);
	     return uriBuilder.build();
	 	}
	
  } 









