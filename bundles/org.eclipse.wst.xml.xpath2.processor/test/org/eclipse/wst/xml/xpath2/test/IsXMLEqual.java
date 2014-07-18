/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.eclipse.wst.xml.xpath2.test;

import java.io.IOException;

import org.custommonkey.xmlunit.Diff;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.xml.sax.SAXException;

/**
 *
 * @author sam
 */
public class IsXMLEqual<T> extends BaseMatcher<T> {
	private final T expectedValue;
	private final boolean fragment;

	public IsXMLEqual(T expectedValue, boolean fragment) {
		this.expectedValue = expectedValue;
		this.fragment = fragment;
	}

	@Override
	public boolean matches(Object item) {
		return areXMLEqual(item, expectedValue);
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(expectedValue);
	}

	private boolean areXMLEqual(Object item, T expectedValue) {
		if (expectedValue instanceof String && item instanceof String) {
			Diff diff;
			try {
				String left = (String)expectedValue;
				String right = (String)item;
				if (fragment) {
					left = "<dummy>" + left + "</dummy>";
					right = "<dummy>" + right + "</dummy>";
				}

				diff = new Diff(left, right);
			} catch (SAXException ex) {
				return false;
			} catch (IOException ex) {
				return false;
			}

			return diff.similar();
		}

		return false;
	}

	@Factory
	public static <T> Matcher<T> xmlEqualTo(T operand) {
		return new IsXMLEqual<T>(operand, false);
	}

	@Factory
	public static <T> Matcher<T> xmlFragmentEqualTo(T operand) {
		return new IsXMLEqual<T>(operand, true);
	}
}
