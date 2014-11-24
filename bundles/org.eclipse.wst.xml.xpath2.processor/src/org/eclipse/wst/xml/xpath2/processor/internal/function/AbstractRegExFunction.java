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
					flag |= Pattern.COMMENTS;
					break;

				default:
					throw DynamicError.regex_flags_error(flags);
				}
			}
		}
		
		Pattern p = Pattern.compile(pattern, flag);
		return p.matcher(src);
	}

}
