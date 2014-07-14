/*******************************************************************************
 * Copyright (c) 2009, 2010 Jesper Steen Moller, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation
 *     Jesper Steen Moller - bug 281028 - avg/min/max/sum work
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.utils;

import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDecimal;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInteger;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSUntypedAtomic;

public class NumericTypePromoter extends TypePromoter {

	@Override
	protected boolean checkCombination(Class<? extends AnyType> newType) {
		if (newType == getTargetType()) {
			return true;
		}

		// Note: Double or float will override everything
		if (newType == XSDouble.class || getTargetType() == XSDouble.class) {
			setTargetType(XSDouble.class);
		} else if (newType == XSFloat.class || getTargetType() == XSFloat.class) {
			setTargetType(XSFloat.class);
		// If we're still with integers, stick with it
		} else if (newType == XSInteger.class && getTargetType() == XSInteger.class) {
			setTargetType(XSInteger.class);
		} else {
			// Otherwise, switch to decimals
			setTargetType(XSDecimal.class);
		}
		return true;
	}

	@Override
	public AnyAtomicType doPromote(AnyAtomicType value) throws DynamicError {
		if (getTargetType().isInstance(value)) {
			return value;
		}

		if (getTargetType() == XSFloat.class) {
			return new XSFloat(value.getStringValue());
		} else if (getTargetType() == XSDouble.class) {
			return new XSDouble(value.getStringValue());
		} else if (getTargetType() == XSInteger.class) {
			return new XSInteger(value.getStringValue());
		} else if (getTargetType() == XSDecimal.class) {
			return new XSDecimal(value.getStringValue());
		} else if (getTargetType() == XSString.class) {
			return new XSString(value.getStringValue());
		}

		return null;
	}

	@Override
	protected Class<? extends AnyType> substitute(Class<? extends AnyType> typeToConsider) {
		if (typeToConsider == XSUntypedAtomic.class) return XSDouble.class;
		if (isDerivedFrom(typeToConsider, XSFloat.class)) return XSFloat.class;
		if (isDerivedFrom(typeToConsider, XSDouble.class)) return XSDouble.class;
		if (isDerivedFrom(typeToConsider, XSInteger.class)) return XSInteger.class;
		if (isDerivedFrom(typeToConsider, XSDecimal.class)) return XSDecimal.class;
		return null;
	}

	private boolean isDerivedFrom(Class<? extends AnyType> typeToConsider, Class<? extends AnyType> superType) {
		return superType.isAssignableFrom(typeToConsider);
	}
	
}
