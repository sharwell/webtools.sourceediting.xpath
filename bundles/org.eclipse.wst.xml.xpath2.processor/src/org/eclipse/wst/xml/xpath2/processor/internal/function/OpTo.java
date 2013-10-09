/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 274805 - improvements to xs:integer data type 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.RangeResultSequence;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInteger;

/**
 * Support for To operation.
 */
public class OpTo extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for OpTo.
	 */
	public OpTo() {
		super(new QName("to"), 2);
	}

	/**
	 * Evaluate arguments.
	 * 
	 * @param args
	 *            argument expressions.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of evaluation.
	 */
	public ResultSequence evaluate(Collection<ResultSequence> args, org.eclipse.wst.xml.xpath2.api.EvaluationContext ec) throws DynamicError {
		assert args.size() == 2;

		// Iterator i = args.iterator();

		// return op_to( (ResultSequence) i.next(), (ResultSequence) i.next());
		return op_to(args);
	}

	/**
	 * Op-To operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of operation.
	 */
	public static ResultSequence op_to(Collection<ResultSequence> args) throws DynamicError {
		// convert arguments
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get arguments
		Iterator<ResultSequence> iter = cargs.iterator();
		ResultSequence r = iter.next();
		int one = ((XSInteger) r.first()).int_value().intValue();
		r = iter.next();
		if (r.first() == null) {
			return ResultBuffer.EMPTY;
		}
		int two = ((XSInteger) r.first()).int_value().intValue();

		if (one > two)
			return ResultBuffer.EMPTY;

		// inclusive first and last
		if (one == two) {
			return new XSInteger(BigInteger.valueOf(one));
		}
		/*
		 * for(one++; one <= two; one++) { rs.add(new XSInteger(one)); }
		 * 
		 * return rs;
		 */
		return new RangeResultSequence(one, two);
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();

			SeqType st = new SeqType(new XSInteger());

			_expected_args.add(st);
			_expected_args.add(st);
		}
		return _expected_args;
	}
}
