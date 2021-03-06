/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 273760 - wrong namespace for functions and data types
 *     David Carver - bug 282223 - implementation of xs:duration data type.
 *                  - bug 262765 - fix handling of range expression op:to and empty sequence 
 *     Jesper Moller- bug 281159 - fix document loading and resolving URIs 
 *     Jesper Moller- bug 286452 - always return the stable date/time from dynamic context
 *     Jesper Moller- bug 275610 - Avoid big time and memory overhead for externals
 *     Jesper Moller- bug 280555 - Add pluggable collation support
  *    Mukul Gandhi - bug 325262 - providing ability to store an XPath2 sequence into
 *                                 an user-defined variable.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.xerces.xs.XSModel;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeModel;
import org.eclipse.wst.xml.xpath2.processor.internal.DynamicContextAdapter;
import org.eclipse.wst.xml.xpath2.processor.internal.Focus;
import org.eclipse.wst.xml.xpath2.processor.internal.StaticContextAdapter;
import org.eclipse.wst.xml.xpath2.processor.internal.function.Function;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FunctionLibrary;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.DocType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDayTimeDuration;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDuration;
import org.eclipse.wst.xml.xpath2.processor.internal.types.xerces.XercesTypeModel;
import org.eclipse.wst.xml.xpath2.processor.util.ResultSequenceUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The default implementation of a Dynamic Context.
 * 
 * Initializes and provides functionality of a dynamic context according to the
 * XPath 2.0 specification.
 */
@Deprecated
public class DefaultDynamicContext extends org.eclipse.wst.xml.xpath2.processor.internal.DefaultStaticContext implements
		DynamicContext {

	private Focus _focus;
	private XSDuration _tz;
	private Map<URI, Document> _loaded_documents;
	private GregorianCalendar _current_date_time;
	private String _default_collation_name = CODEPOINT_COLLATION;
	private CollationProvider _collation_provider;

	/**
	 * Constructor.
	 * 
	 * @param schema
	 *            Schema information of document. May be null
	 * @param doc
	 *            Document [root] node of XML source.
	 */
	public DefaultDynamicContext(XSModel schema, Document doc) {
		this(new XercesTypeModel(schema));
	}

	/**
	 * Constructor.
	 * 
	 * @param schema
	 *            Schema information of document. May be null
	 * @param doc
	 *            Document [root] node of XML source.
	 * @since 2.0
	 */
	public DefaultDynamicContext(TypeModel schema) {
		super(schema);

		_focus = null;
		_tz = new XSDayTimeDuration(0, 5, 0, BigDecimal.ZERO, true);
		_loaded_documents = new HashMap<URI, Document>();
	}
	/**
	 * Reads the day from a TimeDuration type
	 * 
	 * @return an xs:integer _tz
	 * @since 1.1
	 */
	@Override
	public XSDuration tz() {
		return _tz;
	}

	/**
	 * Gets the Current stable date time from the dynamic context.
	 * @since 1.1
	 * @see org.eclipse.wst.xml.xpath2.processor.DynamicContext#get_current_time()
	 */
	@Override
	public GregorianCalendar current_date_time() {
		if (_current_date_time == null) {
			_current_date_time = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		}
		return _current_date_time;
	}
	
	/**
	 * Changes the current focus.
	 * 
	 * @param f
	 *            focus to set
	 */
	@Override
	public void set_focus(Focus f) {
		_focus = f;
	}

	/**
	 * Return the focus
	 * 
	 * @return _focus
	 */
	@Override
	public Focus focus() {
		return _focus;
	}

	/**
	 * Retrieve context item that is in focus
	 * 
	 * @return an AnyType result from _focus.context_item()
	 */
	@Override
	public AnyType context_item() {
		return _focus.context_item();
	}

	/**
	 * Retrieve the position of the focus
	 * 
	 * @return an integer result from _focus.position()
	 */
	@Override
	public int context_position() {
		return _focus.position();
	}

	/**
	 * Retrieve the position of the last focus
	 * 
	 * @return an integer result from _focus.last()
	 */
	@Override
	public int last() {
		return _focus.last();
	}

	/**
	 * Retrieve the variable name
	 * 
	 * @return an AnyType result from get_var(name) or return NULL
	 * @since 2.0
	 */
	@Override
	public Object get_variable(QName name) {
		// XXX: built-in variables
		if ("fs".equals(name.prefix())) {
			if (name.local().equals("dot"))
				return context_item();

			return null;
		}
		return get_var(name);
	}

	/**
	 * 
	 * @return a ResultSequence from funct.evaluate(args)
	 */
	@Override
	public ResultSequence evaluate_function(QName name, Collection<org.eclipse.wst.xml.xpath2.api.ResultSequence> args)
			throws DynamicError {
		Function funct = function(name, args.size());

		assert funct != null;

		final org.eclipse.wst.xml.xpath2.api.DynamicContext dc = new DynamicContextAdapter(this);
		final org.eclipse.wst.xml.xpath2.api.StaticContext sc = new StaticContextAdapter(this);
		EvaluationContext ec = new EvaluationContext() {
			
			@Override
			public org.eclipse.wst.xml.xpath2.api.DynamicContext getDynamicContext() {
				return dc;
			}
			
			@Override
			public AnyType getContextItem() {
				return _focus.context_item();
			}
			
			@Override
			public int getContextPosition() {
				return _focus.position();
			}
		
			@Override
			public int getLastPosition() {
				return _focus.last();
			}
			
			@Override
			public org.eclipse.wst.xml.xpath2.api.StaticContext getStaticContext() {
				return sc;
			}
		};

		return ResultSequenceUtil.newToOld(funct.evaluate(args, ec));
	}

	/**
	 * Adds function definitions.
	 * 
	 * @param fl
	 *            Function library to add.
	 * 
	 */
	@Override
	public void add_function_library(FunctionLibrary fl) {
		super.add_function_library(fl);
		fl.set_dynamic_context(new DynamicContextAdapter(this));
	}

	/**
	 * get document
	 * 
	 * @return a ResultSequence from ResultSequenceFactory.create_new()
	 * @since 1.1
	 */
	@Override
	public ResultSequence get_doc(URI resolved) {
		Document doc;
		if (_loaded_documents.containsKey(resolved)) {
			 //tried before
			doc = _loaded_documents.get(resolved);
		} else {
			doc = retrieve_doc(resolved);
			_loaded_documents.put(resolved, doc);
		}

		if (doc == null)
			return null;

		return ResultSequenceFactory.create_new(new DocType(doc, getTypeModel(doc)));
	}
	/**
	 * @since 1.1
	 */
	@Override
	public URI resolve_uri(String uri) {
		try {
			URI realURI = URI.create(uri);
			if (realURI.isAbsolute()) {
				return realURI;
			} else {
				URI baseURI = URI.create(base_uri().getStringValue());
				return baseURI.resolve(uri);
			}
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}

	// XXX make it nice, and move it out as a utility function
	private Document retrieve_doc(URI uri) {
		try {
			DOMLoader loader = new XercesLoader();
			loader.set_validating(false);

			Document doc = loader.load(new URL(uri.toString()).openStream());
			doc.setDocumentURI(uri.toString());
			return doc;
		} catch (DOMLoaderException e) {
			return null;
		} catch (FileNotFoundException e) {
			return null;
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Sets the value of a variable.
	 * 
	 * @param var
	 *            Variable name.
	 * @param val
	 *            Variable value.
	 */
	@Override
	public void set_variable(QName var, AnyType val) {
		super.set_variable(var, val);
	}
	
	
	/*
	 * Set a XPath2 sequence into a variable.
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public void set_variable(QName var, ResultSequence val) {
		super.set_variable(var, val);
	}

	/**
	 * @since 1.1
	 */
	public void set_default_collation(String _default_collation) {
		this._default_collation_name = _default_collation;
	}

	/**
	 * @since 1.1
	 */
	@Override
	public String default_collation_name() {
		return _default_collation_name;
	}

	private static Comparator<String> CODEPOINT_COMPARATOR = new Comparator<String>() {
		
		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	};
	
	/**
	 * @since 1.1
	 * 
	 */
	@Override
	public Comparator<String> get_collation(String uri) {
		if (CODEPOINT_COLLATION.equals(uri)) return CODEPOINT_COMPARATOR;
		
		return _collation_provider != null ? _collation_provider.get_collation(uri) : null;
	}
	
	/**
	 * 
	 * 
	 * @param provider
	 * @since 1.1
	 */
	public void set_collation_provider(CollationProvider provider) {
		this._collation_provider = provider;
	}
	
	/**
	 * Use {@code focus().position()} to retrieve the value.
	 * @deprecated  This will be removed in a future version use focus().position().
	 */
	@Deprecated
	@Override
	public int node_position(Node node) {
	  // unused parameter!
	  return _focus.position();	
	}
	
	/**
	 * @since 2.0
	 */
	@Override
	public TypeModel getTypeModel(Node node) {
		return super.getTypeModel(node);
	}
}
