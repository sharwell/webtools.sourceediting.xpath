/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.eclipse.wst.xml.xpath2.processor;

/**
 *
 * @author samu6505
 */
public class StaticTypeError extends StaticError {
	public static final String INCOMPATIBLE_TYPES = "XPST0005";

	public StaticTypeError() {
		this(INCOMPATIBLE_TYPES, null);
	}

	public StaticTypeError(String err, Throwable cause) {
		super(INCOMPATIBLE_TYPES, err, cause);
	}
}
