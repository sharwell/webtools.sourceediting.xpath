/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Jesper Steen Moeller - bug 282096 - clean up string storage
 *     Jesper S Moller      - Bug 281938 - no matches should return full input 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * This function breaks the $input string into a sequence of strings, treating
 * any substring that matches $pattern as a separator. The separators themselves
 * are not returned.
 */
public class FnTokenize extends AbstractRegExFunction {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnTokenize.
	 */
	public FnTokenize() {
		super(new QName("tokenize"), 2, 3);
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
		return tokenize(args);
	}

	/**
	 * Tokenize operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:tokenize operation.
	 */
	public static ResultSequence tokenize(Collection<ResultSequence> args) throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		ResultBuffer rs = new ResultBuffer();

		// get args
		Iterator<ResultSequence> argiter = cargs.iterator();
		ResultSequence arg1 = argiter.next();
		String str1 = "";
		if (!arg1.empty()) {
			str1 = ((XSString) arg1.first()).value();
		}

		ResultSequence arg2 = argiter.next();
		String pattern = ((XSString) arg2.first()).value();
		String flags = null;

		if (argiter.hasNext()) {
			ResultSequence flagRS = null;
			flagRS = argiter.next();
			flags = flagRS.first().getStringValue();
			if (validflags.indexOf(flags) == -1 && flags.length() > 0 ) {
				throw DynamicError.regex_flags_error(null);
			}
		}

		try {
			ArrayList<String> ret = tokenize(pattern, flags, str1);

			for (Iterator<String> retIter = ret.iterator(); retIter.hasNext();) {
			   rs.add(new XSString(retIter.next()));	
			}
			
		} catch (PatternSyntaxException err) {
			throw DynamicError.regex_error(null);
		}

		return rs.getSequence();
	}
	
	private static ArrayList<String> tokenize(String pattern, String flags, String src) throws DynamicError {
		Matcher matcher = regex(pattern, flags, src);
		ArrayList<String> tokens = new ArrayList<String>();
		int startpos = 0;
		int endpos = src.length();
		while (matcher.find()) {
			String delim = matcher.group();
			if (delim.length() == 0) {
				throw DynamicError.regex_match_zero_length(null);
			}
			String token = src.substring(startpos, matcher.start());
			startpos = matcher.end();
			tokens.add(token);
		}
		if (startpos < endpos) {
			String token = src.substring(startpos, endpos);
			tokens.add(token);
		}
		return tokens;
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			SeqType arg = new SeqType(new XSString(), SeqType.OCC_QMARK);
			_expected_args.add(arg);
			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_NONE));
			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_NONE));
		}

		return _expected_args;
	}
}
