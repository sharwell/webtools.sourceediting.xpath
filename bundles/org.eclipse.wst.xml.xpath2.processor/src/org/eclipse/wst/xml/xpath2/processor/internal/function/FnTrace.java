/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * The input $value is returned, unchanged, as the result of the function. In
 * addition, the inputs $value, converted to an xs:string, and $label may be
 * directed to a trace data set. The location and format of the trace data set
 * are implementation dependent. The ordering of output from invocations of the
 * fn:trace() function is implementation dependent.
 */
public class FnTrace extends Function {
	/**
	 * Constructor for FnTrace.
	 */
	public FnTrace() {
		super(new QName("trace"), 2);
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
	@Override
	public ResultSequence evaluate(Collection<ResultSequence> args, org.eclipse.wst.xml.xpath2.api.EvaluationContext ec) throws DynamicError {
		return trace(args);
	}

	/**
	 * Trace operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:trace operation.
	 */
	public static ResultSequence trace(Collection<ResultSequence> args) throws DynamicError {

		// sanity check args
		if (args.size() != 2)
			throw DynamicError.throw_type_error();

		Iterator<ResultSequence> argsi = args.iterator();

		ResultSequence arg1 = argsi.next();
		ResultSequence arg2 = argsi.next();

		if (arg2.size() != 1)
			throw DynamicError.throw_type_error();

		Item at = arg2.first();
		if (!(at instanceof XSString))
			throw DynamicError.throw_type_error();

		XSString label = (XSString) at;

		int index = 1;

		for (Iterator<Item> i = arg1.iterator(); i.hasNext(); index++) {
			at = (AnyType) i.next();

			System.out.println(label.value() + " [" + index + "] "
					+ ((AnyType)at).string_type() + ":" + at.getStringValue());

		}

		return arg1;
	}
}
