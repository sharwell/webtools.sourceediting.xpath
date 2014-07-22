/*******************************************************************************
 * Copyright (c) 2009, 2010 Standards for Technology in Automotive Retail and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Carver - bug 262765 - initial API and implementation
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/
package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

public abstract class AbstractRegExFunction extends Function {
	protected static final String validflags = "smix";

	public AbstractRegExFunction(QName name, int arity) {
		super(name, arity);
	}
	
	public AbstractRegExFunction(QName name, int min_arity, int max_arity) {
		super(name, min_arity, max_arity);
	}
	
	protected static boolean matches(String pattern, String flags, String src) {
		boolean fnd = false;
		if (pattern.indexOf("-[") != -1) {
			pattern = pattern.replaceAll("\\-\\[", "&&[^");
		}
		Matcher m = compileAndExecute(pattern, flags, src);
		while (m.find()) {
			fnd = true;
		}
		return fnd;
	}
	
	protected static Matcher regex(String pattern, String flags, String src) {
		Matcher matcher = compileAndExecute(pattern, flags, src);
		return matcher;
	}
	
	private static Matcher compileAndExecute(String pattern, String flags, String src) {
		int flag = Pattern.UNIX_LINES;
		if (flags != null) {
			for (int i = 0; i < flags.length(); i++) {
				switch (flags.charAt(i)) {
				case 'm':
					flag |= Pattern.MULTILINE;
					break;

				case 's':
					flag |= Pattern.DOTALL;
					break;

				case 'i':
					flag |= Pattern.CASE_INSENSITIVE;
					break;

				case 'x':
					pattern = removeWhitespace(pattern);
					break;

				default:
					throw DynamicError.regex_flags_error(flags);
				}
			}
		}
		
		Pattern p = Pattern.compile(pattern, flag);
		return p.matcher(src);
	}

	/**
	 * This method implements the regular expression whitespace removal
	 * algorithm described for the {@code x} flag in the XPath specification.
	 *
	 * <p>The behavior of this method is unspecified if {@code pattern} is not
	 * a valid regular expression according to the XPath specification.</p>
	 *
	 * @param pattern The regular expression containing whitespaces to remove.
	 * @return A copy of {@code pattern} with all whitespace characters removed,
	 * except for whitespace characters appearing within a character class
	 * construct.
	 */
	protected static String removeWhitespace(String pattern) {
		StringBuilder builder = new StringBuilder();
		int index = 0;
		while (index < pattern.length()) {
			index = handleOuter(pattern, index, builder);
			if (index < pattern.length()) {
				index = handleInner(pattern, index, builder);
			}
		}

		return builder.toString();
	}

	private static int handleOuter(String pattern, int index, StringBuilder result) {
		while (index < pattern.length()) {
			switch (pattern.charAt(index)) {
			case '[':
				result.append('[');
				index++;
				return index;

			case '\\':
				result.append('\\');
				index = handleOuterEscape(pattern, index + 1, result);
				break;

			default:
				if (!Character.isWhitespace(pattern.charAt(index))) {
					result.append(pattern.charAt(index));
				}

				index++;
				break;
			}
		}

		return index;
	}

	/**
	 * This method starts with the character immediately following a '\\'
	 * character.
	 */
	private static int handleOuterEscape(String pattern, int index, StringBuilder result) {
		assert pattern.charAt(index - 1) == '\\';

		while (index < pattern.length()) {
			switch (pattern.charAt(index)) {
			case '\\':
			case '[':
				result.append(pattern.charAt(index));
				index++;
				return index;

			default:
				if (!Character.isWhitespace(pattern.charAt(index))) {
					result.append(pattern.charAt(index));
					index++;
					return index;
				}

				// skip whitespace
				index++;
				break;
			}
		}

		return index;
	}

	/**
	 * This method starts with the character following the '[' that begins a
	 * character class.
	 */
	private static int handleInner(String pattern, int index, StringBuilder result) {
		assert pattern.charAt(index - 1) == '[';

		while (index < pattern.length()) {
			switch (pattern.charAt(index)) {
			case '\\':
				// escapes are even easier inside a character class, where
				// whitespace is not ignored.
				result.append('\\');
				index++;
				if (index < pattern.length()) {
					switch (pattern.charAt(index)) {
					case '\\':
					case ']':
					case '-':
						result.append(pattern.charAt(index));
						index++;
						break;

					default:
						break;
					}
				}

				break;

			case ']':
				result.append(']');
				index++;
				return index;

			case '-':
				// character class differencing
				result.append('-');
				index++;
				if (index < pattern.length() && pattern.charAt(index) == '[') {
					result.append('[');
					index = handleInner(pattern, index + 1, result);
				}

				break;

			default:
				result.append(pattern.charAt(index));
				index++;
				break;
			}
		}

		return index;
	}
}
