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

/**
 * String literal utilities
 * 
 *
 */
public class LiteralUtils {
	
	/**
	 * Unquotes a quoted string, changing double quotes into single quotes as well.
     * Examples (string delimited by &gt; and &lt;):
     *  &gt;"A"&lt; becomes &gt;A&lt; 
     *  &gt;'B'&lt; becomes &gt;B&lt; 
	 *  &gt;"A""B"&lt; becomes &gt;A"B&lt;
	 *  &gt;"A""B"&lt; becomes &gt;A"B&lt;
	 *  &gt;'A''''B'&lt; becomes &gt;A''B&lt;
	 *  &gt;"A''''B"&lt; becomes &gt;A''''B&lt;
	 * @param quotedString A quoted string possibly containing escaped quotes
	 * @return unquoted and unescaped string
	 */
	public static String unquote(String quotedString) {
		int inputLength = quotedString.length();
		char quoteChar = quotedString.charAt(0);
		if (quotedString.indexOf(quoteChar, 1) == inputLength-1) {
			// The trivial case where there's no quotes in the middle of the string.
			return quotedString.substring(1, inputLength-1);
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < inputLength-1; ++i) {
			char ch = quotedString.charAt(i);
			sb.append(ch);
			if (ch == quoteChar) ++i; // Skip past second quote char (ensured by the lexer)
		}
		return sb.toString();
	}

	public static String quote(String unquotedString) {
		String quotedContent = unquotedString.replace("\"", "\"\"");
		return "\"" + quotedContent + "\"";
	}
}
