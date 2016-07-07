/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver (STAR) - bug 262765 - added exception handling to toss correct error numbers. 
 *     Jesper Steen Moeller - bug 285145 - implement full arity checking
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * The function returns the xs:string that is obtained by replacing each
 * non-overlapping substring of $input that matches the given $pattern with an
 * occurrence of the $replacement string.
 */
public class FnReplace extends AbstractRegExFunction {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for RnReplace.
	 */
	public FnReplace() {
		super(new QName("replace"), 3, 4);
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
		return replace(args);
	}

	/**
	 * Replace operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:replace operation.
	 */
	public static ResultSequence replace(Collection<ResultSequence> args) throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get args
		Iterator<ResultSequence> argiter = cargs.iterator();
		ResultSequence arg1 = argiter.next();
		String str1 = "";
		if (!arg1.empty())
			str1 = ((XSString) arg1.first()).value();

		ResultSequence arg2 = argiter.next();
		ResultSequence arg3 = argiter.next();
		String flags = null;
		if (argiter.hasNext()) {
			ResultSequence arg4 = argiter.next();
			flags = arg4.first().getStringValue();
		}
		String pattern = ((XSString) arg2.first()).value();
		String replacement = ((XSString) arg3.first()).value();
		
		try {
			Matcher matcher = regex(pattern, flags, str1);

			if (matcher.groupCount() < 10) {
				for (int i = matcher.groupCount(); i < 9; i++) {
					pattern = pattern + "()";
				}

				matcher = regex(pattern, flags, str1);
			}

			// Validate replacement
			if (!isSyntacticallyValidReplacementString(replacement)) {
				throw new DynamicError("FORX0004", "Invalid replacement string.", null);
			}

			return new XSString(matcher.replaceAll(replacement));
		} catch (PatternSyntaxException err) {
			throw DynamicError.regex_error(null, err);
		} catch (IllegalArgumentException ex) {
			throw new DynamicError("FORX0004", "Invalid replacement string.", ex);
		}
	}

	private static boolean isSyntacticallyValidReplacementString(String replacement) {
		for (int i = indexOfNextBackslashOrDollar(replacement, 0);
			 i >= 0;
			 i = indexOfNextBackslashOrDollar(replacement, i + 2)) {

			if (i == replacement.length() - 1) {
				// If the escape character is the last character, it can't be followed by anything to make it valid.
				return false;
			}

			char next = replacement.charAt(i + 1);
			if (replacement.charAt(i) == '\\') {
				// require \\ or \$
				if (next != '\\' && next != '$') {
					return false;
				}
			} else {
				// require a digit
				if (next < '0' || next > '9') {
					return false;
				}
			}
		}

		return true;
	}

	private static int indexOfNextBackslashOrDollar(String str, int startIndex) {
		int nextBackslash = str.indexOf('\\', startIndex);
		int nextDollar = str.indexOf('$', startIndex);
		if (nextBackslash == -1) {
			return nextDollar;
		} else if (nextDollar == -1) {
			return nextBackslash;
		} else {
			return Math.min(nextBackslash, nextDollar);
		}
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
			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_NONE));
		}

		return _expected_args;
	}
}
