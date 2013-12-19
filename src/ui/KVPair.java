package ui;

import java.util.Vector;

public class KVPair {
	String key = "";
	Vector<Object> val;
	int idx;
	
	public final static int INTEGER = 0;
	public final static int DOUBLE = 1;
	public final static int RADIO = 2;
	public final static int CHECK = 3;
	public final static int STRING = 4;
	public final static int SETTINGS = 5;
	public final static int GROUP_TITLE = 6;
}