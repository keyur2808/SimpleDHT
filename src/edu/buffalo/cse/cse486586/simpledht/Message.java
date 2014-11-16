package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;
import java.util.ArrayList;


public class Message implements Serializable{

private static final long serialVersionUID = 1L;
private String message;
private String myPort;
private String predecessor;
private String successor;
private String key;
private String value;
private String Dest_port;
private String nodeFirst;
private String nodeLast;
private int ringSize;
private ArrayList<SerialCursorRow>cursorMap;
private String myHash;
private int deletecount;
public Message (String message,String myPort){
	this.myPort=myPort;
	this.message=message;
		
}	 

public String getMessage() {
	return message;
}

public void setMessage(String message) {
	this.message = message;
}

public String getmyPort() {
	return myPort;
}

public String getPredecessor() {
	return predecessor;
}

public void setPredecessor(String predecessor) {
	this.predecessor = predecessor;
}

public String getSuccessor() {
	return successor;
}

public void setSuccessor(String successor) {
	this.successor = successor;
}

public String getKey() {
	return key;
}

public String getValue() {
	return value;
}


public void setKeyValue(String key,String value) {
	this.key = key;
	this.value=value;
}

public String getDest_port() {
	return Dest_port;
}

public void setDest_port(String Dest_port) {
	this.Dest_port = Dest_port;
}

public void setKey(String selection) {

    this.key=selection;	
}

public void setEndNodes(String nodeFirst,String nodeLast){
	this.nodeFirst = nodeFirst;
	this.nodeLast = nodeLast;	
}

public String getNodeLast() {
	return nodeLast;
}

public void setNodeLast(String nodeLast) {
	this.nodeLast = nodeLast;
}

public String getNodeFirst() {
	return nodeFirst;
}

public void setNodeFirst(String nodeFirst) {
	this.nodeFirst = nodeFirst;
}

public int getRingSize() {
	return ringSize;
}

public void setRingSize(int ringSize) {
	this.ringSize = ringSize;
}

public ArrayList<SerialCursorRow> getCursorMap() {
	return cursorMap;
}

public void setCursorMap(ArrayList<SerialCursorRow> cursorMap) {
	this.cursorMap = cursorMap;
}

public String getMyHash() {
	return myHash;
}

public void setMyHash(String myHash) {
	this.myHash = myHash;
}

public int getDeletecount() {
	return deletecount;
}

public void setDeletecount(int deletecount) {
	this.deletecount = deletecount;
}

}
