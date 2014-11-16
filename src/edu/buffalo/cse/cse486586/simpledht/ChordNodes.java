package edu.buffalo.cse.cse486586.simpledht;

public class ChordNodes implements Comparable<ChordNodes>{
String Port;
String HashValue;

ChordNodes(String Port,String HashValue){
	this.Port=Port;
	this.HashValue=HashValue;
}

	@Override
	public int compareTo(ChordNodes another) {
		return (HashValue.compareTo(another.HashValue));
	}


   public String getPort(){
	   return Port;
   }
   
   public String getHashValue(){
	   return HashValue;
   }
}
