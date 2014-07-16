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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
			if (flags.indexOf("m") >= 0) {
				flag = flag | Pattern.MULTILINE;
			}
			if (flags.indexOf("s") >= 0) {
				flag = flag | Pattern.DOTALL;
			}
			if (flags.indexOf("i") >= 0) {
				flag = flag | Pattern.CASE_INSENSITIVE;
			}
			
			if (flags.indexOf("x") >= 0) {
				//pattern = removeWhitespace(pattern);
				flag |= Pattern.COMMENTS;
			}
		}

		try {
			Pattern p = Pattern.compile(pattern, flag);
			return p.matcher(src);
		} catch (PatternSyntaxException ex) {
			throw DynamicError.regex_error(null, ex);
		}
	}

//	private static final Pattern characterClassPattern = Pattern.compile("");
//	static {
//		final String XmlChar = "[^\\[\\-\\]]";
//		final String SingleCharEsc = "\\[nrt|.?*+(){}$\\[^\\\\\\-\\]]";
//		final String charOrEsc = "(?:" + XmlChar + "|" + SingleCharEsc + ")";
//		final String charRange = charOrEsc + "\\-" + charOrEsc;
//		final String charClassEsc = "(?:";
//		final String posCharGroup = "(?:" + charRange + "|" + charClassEsc + ")+";
//		final String negCharGroup = "\\^" + posCharGroup;
//		final String charClassSub = "(?:" + posCharGroup + "|" + negCharGroup + ")-" + charClassExpr;
//		final String charGroup = "(?:" + posCharGroup + "|" + negCharGroup + "|" + charClassSub + ")";
//		final String charClassExpr = "\\[" + charGroup + "\\]";
//	}
//
//	private static String removeWhitespace(String pattern) {
//		List<Integer> startIndexes = new ArrayList<Integer>();
//		List<Integer> endIndexes = new ArrayList<Integer>();
//		Matcher matcher = characterClassPattern.matcher(pattern);
//		while (matcher.find()) {
//			startIndexes.add(matcher.start());
//			endIndexes.add(matcher.end());
//		}
//
//		StringBuilder patternBuilder = new StringBuilder();
//		int previousEnd = 0;
//		for (int i = 0; i < startIndexes.size(); i++) {
//			patternBuilder.append(pattern.substring(previousEnd, startIndexes.get(i)).replaceAll("\\s+", ""));
//			patternBuilder.append(pattern.substring(startIndexes.get(i), endIndexes.get(i)));
//			previousEnd = endIndexes.get(i);
//		}
//
//		patternBuilder.append(pattern.substring(previousEnd, pattern.length()).replaceAll("\\s+", ""));
//		return patternBuilder.toString();
//	}

}
