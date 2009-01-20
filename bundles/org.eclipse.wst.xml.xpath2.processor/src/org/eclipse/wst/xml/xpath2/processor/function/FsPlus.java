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
import java.lang.reflect.*;
/**
 * Class for Plus function.
 */
public class FsPlus extends Function {
	/**
	 * Constructor for FsPlus.
	 */
	public FsPlus() {
		super(new QName("plus"), 2);
	}
	/**
         * Evaluate arguments.
         * @param args argument expressions.
         * @throws DynamicError Dynamic error.
         * @return Result of evaluation.
         */
	public ResultSequence evaluate(Collection args) throws DynamicError {
		assert args.size() == arity();

		return fs_plus(args);
	}

	/**
	 * Convert arguments for operation.
	 * @param args input arguments.
	 * @throws DynamicError Dynamic error.
	 * @return Result of conversion.
	 */
	private static Collection convert_args(Collection args) throws DynamicError {
		Collection result = new ArrayList();

                // atomize arguments 
                for(Iterator i = args.iterator(); i.hasNext();) {
                        ResultSequence rs = FnData.atomize((ResultSequence)i.next());
                        
			if(rs.empty())
                                return new ArrayList();

                        if(rs.size() > 1)
                                throw new DynamicError(TypeError.invalid_type(null));

                        AnyType arg = rs.first();

                        if(arg instanceof UntypedAtomic) {
                                arg = new XSDouble(arg.string_value());
			}	

			
			rs = ResultSequenceFactory.create_new();
			rs.add(arg);
			result.add(rs);
                }

		return result;
	}
	/**
         * General operation on the arguments.
         * @param args input arguments.
         * @throws DynamicError Dynamic error.
         * @return Result of the operation.
         */
	public static ResultSequence fs_plus(Collection args) throws DynamicError {
		return do_math_op(args, MathPlus.class, "plus");
	}
	/**
         * Unary operation on the arguments.
         * @param args input arguments.
         * @throws DynamicError Dynamic error.
         * @return Result of the operation.
         */
	public static ResultSequence fs_plus_unary(Collection args) throws DynamicError {
		
		// make sure we got only one arg
		if(args.size() != 1)
			DynamicError.throw_type_error();
		ResultSequence arg = (ResultSequence) args.iterator().next();

		// make sure we got only one numeric atom
		if(arg.size() != 1)
			DynamicError.throw_type_error();
		AnyType at = arg.first();
		if( !(at instanceof NumericType) )
			DynamicError.throw_type_error();

		// no-op
		return arg;	
	}

	// voodoo
	/**
	 * Mathematical operation on the arguments.
	 * @param args input arguments.
	 * @param type type of arguments.
	 * @param mname Method name for template simulation.
	 * @throws DynamicError Dynamic error.
	 * @return Result of operation.
	 */
	public static ResultSequence do_math_op(Collection args, 
						Class type,
						String mname) throws DynamicError {

		// sanity check args + convert em
		if(args.size() != 2)
			DynamicError.throw_type_error();
		
		Collection cargs = convert_args(args);

		ResultSequence result = ResultSequenceFactory.create_new();
	
		if(cargs.size() == 0)
			return result;

		// make sure arugments are good [at least the first one]
		Iterator argi = cargs.iterator();
		AnyType arg = ((ResultSequence)argi.next()).first();
		ResultSequence arg2 = (ResultSequence)argi.next();

		if( !(type.isInstance(arg)) )
			DynamicError.throw_type_error();

		// here is da ownage
		try {
			Class margsdef[] = { ResultSequence.class };
			Method method = null;
		
			method  = type.getMethod(mname, margsdef);

			Object margs[] = { arg2 };
			return (ResultSequence) method.invoke(arg, margs);

		} catch(NoSuchMethodException err) {
			System.out.println("NoSuchMethodException: " +
		 			   err.getMessage());
			assert false;
		} catch(IllegalAccessException err) {
			System.out.println("IllegalAccessException: " +
					   err.getMessage());
			assert false;
		} catch(InvocationTargetException err) {
			Throwable ex = err.getTargetException();

			if(ex instanceof DynamicError)
				throw (DynamicError) ex;
			else {
				ex.printStackTrace();
				System.exit(1);
			}	
		}
		return null; // unreach!
	}
}
