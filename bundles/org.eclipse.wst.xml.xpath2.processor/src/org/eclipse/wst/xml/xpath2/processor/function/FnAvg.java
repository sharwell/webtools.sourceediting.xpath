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
 * Returns the average of the values in the input sequence $arg, that is, the sum of
 * the values divided by the number of values.
 */
public class FnAvg extends Function {
	/**
	 * Constructor for FnAvg.
	 */
	public FnAvg() {
		super(new QName("avg"), 1);
	}
	/**
	 * Evaluate arguments.
	 * @param args argument expressions.
         * @throws DynamicError Dynamic error.
         * @return Result of evaluation.
         */
	public ResultSequence evaluate(Collection args) throws DynamicError {
		return avg(args);
	}
	/**
         * Average value operation.
         * @param args Result from the expressions evaluation.
         * @throws DynamicError Dynamic error.
         * @return Result of fn:avg operation.
         */
	public static ResultSequence avg(Collection args) throws DynamicError {

		ResultSequence arg = get_arg(args);
	
		if(arg.empty())
			return ResultSequenceFactory.create_new();

		int elems = 0;

		MathPlus total = null;
		for(Iterator i = arg.iterator(); i.hasNext();) {
			AnyType at = (AnyType) i.next();

			if( !(at instanceof MathPlus))
				DynamicError.throw_type_error();

			if(total == null)
				total = (MathPlus) at;
			else {
				ResultSequence res = total.plus(ResultSequenceFactory.create_new(at));
				assert res.size() == 1;

				total = (MathPlus) res.first();
			}
			elems++;
		}

		if( !(total instanceof MathDiv))
			DynamicError.throw_type_error();
		
		MathDiv avg = (MathDiv) total;
		ResultSequence res = avg.div(ResultSequenceFactory.create_new(new XSDouble(elems)));
		
		return res;
	}
	/**
	 * Obtain input argument for operation.
	 * @param args input expressions.
	 * @throws DynamicError Dynamic error.
	 * @return Resulting expression from the operation.
	 */
	public static ResultSequence get_arg(Collection args) throws DynamicError {
		assert args.size() == 1;

		ResultSequence arg = (ResultSequence) args.iterator().next();

		if(arg.empty())
			return arg;

		AnyType first = arg.first();

		Class durtype = null;
		if(first instanceof XDTDayTimeDuration) 
			durtype = XDTDayTimeDuration.class;
		else if(first instanceof XDTYearMonthDuration)
			durtype = XDTYearMonthDuration.class;
		else
			durtype = null;

		// duration
		if(durtype != null) {
			for(Iterator i = arg.iterator(); i.hasNext();) {
				AnyType at = (AnyType) i.next();

				if(!durtype.isInstance(at))
					DynamicError.throw_type_error();
			}
			return arg;
		}

		ResultSequence rs = ResultSequenceFactory.create_new();
		// XXX promote everything to double
		for(Iterator i = arg.iterator(); i.hasNext();) {
			AnyType at = (AnyType) i.next();

			XSDouble d = null;
			if(at instanceof UntypedAtomic) {
				d = XSDouble.parse_double(((UntypedAtomic)at).string_value());
			}
			else if(at instanceof NumericType) {
				d = XSDouble.parse_double( ((NumericType)at).string_value());
			}
			else
				DynamicError.throw_type_error();
			
			if(d == null)
				throw DynamicError.cant_cast(null);
			
			rs.add(d);
		}
		return rs;
	}
}
