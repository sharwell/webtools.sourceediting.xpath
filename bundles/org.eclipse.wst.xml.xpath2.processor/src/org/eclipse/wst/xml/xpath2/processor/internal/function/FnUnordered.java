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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

/**
 * Returns the items of $sourceSeq in a non-deterministic order.
 */
public class FnUnordered extends Function {
	/**
	 * Constructor for FnUnordered.
	 */
	public FnUnordered() {
		super(new QName("unordered"), 1);
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
		return unordered(args);
	}

	/**
	 * Unordered operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:unordered operation.
	 */
	public static ResultSequence unordered(Collection<ResultSequence> args) throws DynamicError {

		assert args.size() == 1;

		// get args
		Iterator<ResultSequence> citer = args.iterator();
		ResultSequence arg = citer.next();

		if (arg.empty())
			return ResultBuffer.EMPTY;

		// XXX lame
		ArrayList<Item> tmp = new ArrayList<Item>();
		for (Iterator<Item> i = arg.iterator(); i.hasNext();)
			tmp.add(i.next());

		Collections.shuffle(tmp);

		ResultBuffer rb = new ResultBuffer();
		for (Iterator<Item> i = tmp.iterator(); i.hasNext();)
			rb.add((AnyType) i.next());

		return rb.getSequence();
	}
}
