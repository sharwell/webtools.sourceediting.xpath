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
 * Class for Modular function.
 */
public class FsMod extends Function {
	/**
	 * Constructor for FsMod.
	 */
	public FsMod() {
		super(new QName("mod"), 2);
	}
	/**
         * Evaluate arguments.
         * @param args argument expressions.
         * @throws DynamicError Dynamic error.
         * @return Result of evaluation.
         */
	@Override
	public ResultSequence evaluate(Collection args) throws DynamicError {
		assert args.size() == arity();

		return fs_mod(args);
	}

	/**
         * General operation on the arguments.
         * @param args input arguments.
         * @throws DynamicError Dynamic error.
         * @return Result of the operation.
         */
	public static ResultSequence fs_mod(Collection args) throws DynamicError {
		return FsPlus.do_math_op(args, MathMod.class, "mod");
	}
}
