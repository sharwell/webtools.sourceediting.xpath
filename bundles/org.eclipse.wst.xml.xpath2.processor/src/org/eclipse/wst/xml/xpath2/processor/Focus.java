/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor;

import org.eclipse.wst.xml.xpath2.processor.types.*;

/**
 * set the focus from a result sequence
 */
public class Focus {
        	private int _cp;        	// context position
	private ResultSequence _rs;	// all items in context

	/**
	 * Sets the _rs to rs and context position to 1.
	 * @param rs is a ResultSequence and is set to _rs.
 	 */
        	public Focus(ResultSequence rs) {
		_rs = rs;
		_cp = 1;
        	}

	/**
	 * Retrieves previous item from current context position.
	 * @return the item from _rs.
 	 */
       	public AnyType context_item() {
		// idexes start at 0
		return _rs.get(_cp-1);
	}

	/**
	 * Checks to see if possible to advance rs.
	 * @return the boolean.
 	 */
	public boolean advance_cp() {
		int size;

		// check if we can advance
		size = _rs.size();
		if(_cp == size)
			return false;

		_cp++;
		return true;
	}

	/**
	 * returns an integer of the current position.
	 * @return the current position of rs.
 	 */
	public int position() {
		return _cp;
	}

	/**
	 * returns the position of the last item in rs.
	 * @return the size of rs.
 	 */
	public int last() {
		return _rs.size();
	}

	/**
	 * sets the position.
	 * @param p is the position that is set.
 	 */
	public void set_position(int p) {
		_cp = p; // XXX no checks
	}
}
