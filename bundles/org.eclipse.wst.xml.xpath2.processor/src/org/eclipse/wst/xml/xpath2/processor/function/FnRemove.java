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

package org.eclipse.wst.xml.xpath2.processor.function;

import org.eclipse.wst.xml.xpath2.processor.*;
import org.eclipse.wst.xml.xpath2.processor.types.*;

import java.util.*;
/**
 * Returns a new sequence constructed from the value of $target with the item at the
 * position specified by the value of $position removed.
 * If $position is less than 1 or greater than the number of items in $target, $target
 * is returned. Otherwise, the value returned by the function consists of all items of
 * $target whose index is less than $position, followed by all items of $target whose
 * index is greater than $position. If $target is the empty sequence, the empty
 * sequence is returned.
 */
public class FnRemove extends Function {
	/**
	 * Constructor for FnRemove.
	 */
	public FnRemove() {
		super(new QName("remove"), 2);
	}
	/**
         * Evaluate arguments.
         * @param args argument expressions.
         * @throws DynamicError Dynamic error.
         * @return Result of evaluation.
         */
	public ResultSequence evaluate(Collection args) throws DynamicError {
		return remove(args);
	}
	/**
         * Remove operation.
         * @param args Result from the expressions evaluation.
         * @throws DynamicError Dynamic error.
         * @return Result of fn:remove operation.
         */
	public static ResultSequence remove(Collection args) throws DynamicError {

		assert args.size() == 2;
			
		ResultSequence rs = ResultSequenceFactory.create_new();

		// get args
		Iterator citer = args.iterator();
		ResultSequence target = (ResultSequence) citer.next();
		ResultSequence arg2 = (ResultSequence) citer.next();

		// sanity chex
		if(arg2.size() != 1)
			DynamicError.throw_type_error();
		
		AnyType at = arg2.first();
		if( !(at instanceof XSInteger))
			DynamicError.throw_type_error();

		int position = ((XSInteger)at).int_value();

		if(position < 1)
			return target;

		if(position > target.size())
			return target;

		if(target.empty())
			return rs;

		int curpos = 1;

		for(Iterator i = target.iterator(); i.hasNext();) {
			at = (AnyType) i.next();

			if(curpos != position)
				rs.add(at);	
			
			curpos++;
		}

		return rs;
	}
}
