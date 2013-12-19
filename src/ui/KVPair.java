/*******************************************************************************
 * Copyright (c) 2013 Eric Dill -- eddill@ncsu.edu. North Carolina State University. All rights reserved.
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Eric Dill -- eddill@ncsu.edu - initial API and implementation
 * 	James D. Martin -- jdmartin@ncsu.edu - Principal Investigator
 ******************************************************************************/
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
