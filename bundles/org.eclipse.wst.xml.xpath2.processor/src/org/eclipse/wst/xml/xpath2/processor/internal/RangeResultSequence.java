/*******************************************************************************
 * Copyright (c) 2005, 2011 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 274805 - improvements to xs:integer data type 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *     Jesper Steen Moller  - bug 340933 - Migrate to new XPath2 API
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal;

import java.math.BigInteger;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.typesystem.ItemType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.SimpleAtomicItemTypeImpl;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInteger;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A range expression can be used to construct a sequence of consecutive
 * integers.
 */
public class RangeResultSequence implements org.eclipse.wst.xml.xpath2.api.ResultSequence {

	private final int _start;
	private final int _end;
	private final int _size;

	/**
	 * set the start and end of the range result sequence
	 * 
	 * @param start
	 *            is the integer position of the start of range.
	 * @param end
	 *            is the integer position of the end of range.
	 */
	public RangeResultSequence(int start, int end) {
		_size = (end - start) + 1;

		assert _size >= 0;

		_start = start;
		_end = end;
	}

	/**
	 * iterate through range.
	 * 
	 * @return tail
	 */
	@Override
	public Iterator<Item> iterator() {
		if (empty()) {
			return ResultBuffer.EMPTY.iterator();
		}

		return new Iterator<Item>() {
			private int _next = _start;

			public void remove() {
				throw new UnsupportedOperationException("ResultSequences are immutable");
			}

			public Item next() {
				if (_next == _end) {
					throw new IllegalStateException("Reached the end of the ResultSequence");
				}

				return new XSInteger(BigInteger.valueOf(_next++));
			}

			public boolean hasNext() {
				return _next < _end;
			}
		};
	}

	/**
	 * @return item from range
	 */
	@Override
	public Item item(int index) {
		if (index < 0 || index >= _size) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of alllowed bounds (less than " + _size + ")");
		}

		return new XSInteger(BigInteger.valueOf(_start + index));
	}

	/**
	 * @return size
	 */
	@Override
	public int size() {
		return _size;
	}

	/**
	 * @return first item in range
	 */
	@Override
	public Item first() {
		return item(0);
	}

	/**
	 * @return first item in range
	 */
	@Override
	public Object firstValue() {
		return item(0).getNativeValue();
	}

	/**
	 * asks if the range is empty?
	 * 
	 * @return boolean
	 */
	@Override
	public boolean empty() {
		return size() == 0;
	}

	@Override
	public ItemType sequenceType() {
		return new SimpleAtomicItemTypeImpl(BuiltinTypeLibrary.XS_INTEGER, ItemType.OCCURRENCE_NONE_OR_MANY);
	}

	@Override
	public Object value(int index) {
		return item(index).getNativeValue();
	}

	@Override
	public ItemType itemType(int index) {
		return item(index).getItemType();
	}
}
