/*******************************************************************************
 * Copyright (c) 2009, 2010 Jesper Steen Møller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.utils;

import java.util.regex.Pattern;
import org.eclipse.wst.xml.xpath2.processor.StaticError;

/**
 * String literal utilities
 * 
 *
 */
public class LiteralUtils {

	private static final Pattern ZERO_PATTERN = Pattern.compile("^(?:0+(\\.0*)?|\\.0+)(?:[eE][+-]?[0-9]+)?$");

	/**
	 * Unquotes a quoted string, changing double quotes into single quotes as well.
     * Examples (string delimited by > and <):
     *  >"A"< becomes >A< 
     *  >'B'< becomes >B< 
	 *  >"A""B"< becomes >A"B<
	 *  >"A""B"< becomes >A"B<
	 *  >'A''''B'< becomes >A''B<
	 *  >"A''''B"< becomes >A''''B<
	 * @param quotedString A quoted string possibly containing escaped quotes
	 * @return unquoted and unescaped string
	 */
	public static String unquote(String quotedString) {
		int inputLength = quotedString.length();
		char quoteChar = quotedString.charAt(0);
		if (quotedString.indexOf(quoteChar, 1) == inputLength-1) {
			if (quotedString.indexOf('\r', 1) < 0) {
				// The trivial case where there's no quotes or carriage return
				// characters in the middle of the string.
				return quotedString.substring(1, inputLength-1);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < inputLength-1; ++i) {
			char ch = quotedString.charAt(i);
			if (ch == '\r') {
				sb.append('\n');
				if (quotedString.charAt(i + 1) == '\n') {
					// already added the \n character
					i++;
				}
			} else {
				sb.append(ch);
				if (ch == quoteChar) ++i; // Skip past second quote char (ensured by the lexer)
			}
		}
		return sb.toString();
	}

	public static String quote(String unquotedString) {
		String quotedContent = unquotedString.replace("\"", "\"\"");
		return "\"" + quotedContent + "\"";
	}

	public static double parseDouble(String text) {
		double result = Double.parseDouble(text);
		if (Double.isInfinite(result)) {
			throw new StaticError("FOAR0002", "Numeric overflow in double literal", null);
		}

		if (result == 0.0 && !ZERO_PATTERN.matcher(text).matches()) {
			throw new StaticError("FOAR0002", "Numeric underflow in double literal", null);
		}

		return result;
	}
}
