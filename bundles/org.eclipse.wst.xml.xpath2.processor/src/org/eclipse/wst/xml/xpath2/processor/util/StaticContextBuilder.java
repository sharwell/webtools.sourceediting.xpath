/*******************************************************************************
 * Copyright (c) 2011, Jesper Steen Moller, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation
 *     Jesper Steen Moller  - bug 340933 - Migrate to new XPath2 API
 *     Jesper Steen Moller - bug 343804 - Updated API information
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.eclipse.wst.xml.xpath2.api.CollationProvider;
import org.eclipse.wst.xml.xpath2.api.Function;
import org.eclipse.wst.xml.xpath2.api.FunctionLibrary;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.api.StaticVariableResolver;
import org.eclipse.wst.xml.xpath2.api.typesystem.ItemType;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeModel;
import org.eclipse.wst.xml.xpath2.processor.function.FnFunctionLibrary;
import org.eclipse.wst.xml.xpath2.processor.function.XSCtrLibrary;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;
import org.w3c.dom.Node;

/**
 * @since 2.0
 */
public class StaticContextBuilder implements StaticContext {

	public static final String XPATH_FUNCTIONS_NS = "http://www.w3.org/2005/xpath-functions";

	private boolean _xpath1_compatible = false;
	private String _default_namespace = "";
	private final String _default_function_namespace = XPATH_FUNCTIONS_NS;
	private final TypeDefinition _initialContextType = null; 
	private String _defaultCollation = CollationProvider.CODEPOINT_COLLATION;

	// key: String prefix, contents: String namespace
	private final Map<String, String> _namespaces = new HashMap<String, String>();
	private final Map<String, FunctionLibrary> _functionLibraries = new HashMap<String, FunctionLibrary>();
	{
		_functionLibraries.put(XPATH_FUNCTIONS_NS, new FnFunctionLibrary());
		_functionLibraries.put(XSCtrLibrary.XML_SCHEMA_NS, new XSCtrLibrary());
	}

	private URI _base_uri;
	private final Map<QName, ItemType> _variableTypes = new HashMap<QName, ItemType>();
	private final Map<String, Short> _variableCardinality = new HashMap<String, Short>();
	private final Map<String, TypeDefinition> _collectionTypes = new HashMap<String, TypeDefinition>();

	private final Set<QName> _hiddenFunctions = new HashSet<QName>();

	private TypeModel _typeModel;
	
	public boolean isXPath1Compatible() {
		return _xpath1_compatible;
	}

	public NamespaceContext getNamespaceContext() {
		return new NamespaceContext() {
			
			public Iterator<String> getPrefixes(String ns) {
				List<String> prefixes = new LinkedList<String>();
				for (Iterator<Map.Entry<String, String>> it = _namespaces.entrySet().iterator(); it.hasNext(); ) {
					Map.Entry<String, String> entry = it.next();
					
					if (entry.getValue().equals(ns)) prefixes.add(entry.getKey());
				}
				return prefixes.iterator();
			}
			
			public String getPrefix(String ns) {
				for (Iterator<Map.Entry<String, String>> it = _namespaces.entrySet().iterator(); it.hasNext(); ) {
					Map.Entry<String, String> entry = it.next();					
					if (entry.getValue().equals(ns)) {
						return entry.getKey();
					}
				}
				return null;
			}
			
			public String getNamespaceURI(String prefix) {
				String ns = _namespaces.get(prefix);
				if (ns == null) ns = XMLConstants.NULL_NS_URI;
				return ns;
			}
		};
	}

	public String getDefaultNamespace() {
		return _default_namespace;
	}

	public String getDefaultFunctionNamespace() {
		return _default_function_namespace;
	}

	public TypeModel getTypeModel() {
		if (_typeModel != null) return _typeModel;
		
		return new TypeModel() {

			public TypeDefinition getType(Node node) {
				return null;
			}

			public TypeDefinition lookupType(String namespace, String typeName) {
				return null;
			}

			public TypeDefinition lookupElementDeclaration(String namespace, String elementName) {
				return null;
			}

			public TypeDefinition lookupAttributeDeclaration(String namespace, String attributeName) {
				return null;
			}};
	}

	public Function resolveFunction(QName name, int arity) {
		if (_hiddenFunctions.contains(name)) return null;
		FunctionLibrary flib = _functionLibraries.get(name.getNamespaceURI());
		if (flib != null) {
			return flib.resolveFunction(name.getLocalPart(), arity);
		}
		return null;
	}

	public URI getBaseUri() {
		return _base_uri;
	}

	public Map<String, FunctionLibrary> getFunctionLibraries() {
		return _functionLibraries;
	}

	public TypeDefinition getCollectionType(String collectionName) {
		return _collectionTypes.get(collectionName);
	}

	public TypeDefinition getInitialContextType() {
		return _initialContextType;
	}

	public StaticContextBuilder withNamespace(String prefix, String uri) {
		_namespaces.put(prefix, uri);
		return this;
	}

	public StaticContextBuilder withDefaultNamespace(String uri) {
		_default_namespace = uri;
		return this;
	}

	public StaticContextBuilder withXPath1Compatibility(boolean compatible) {
		_xpath1_compatible = compatible;
		return this;
	}

	public StaticContextBuilder withTypeModel(TypeModel tm) {
		_typeModel = tm;
		return this;
	}

	public StaticContextBuilder withoutFunction(QName functionToSuppress) {
		_hiddenFunctions.add(functionToSuppress);
		return this;
	}
	public StaticContextBuilder withoutFunction(QName ... functionsToSuppress) {
		for (QName name : functionsToSuppress)
			_hiddenFunctions.add(name);
		return this;
	}
	
	public TypeDefinition getDefaultCollectionType() {
		return BuiltinTypeLibrary.XS_UNTYPED;
	}

	public StaticVariableResolver getInScopeVariables() {
		return new StaticVariableResolver() {

			public boolean isVariablePresent(QName name) {
				return _variableTypes.containsKey(name);
			}

			public ItemType getVariableType(QName name) {
				return _variableTypes.get(name);
			}
		};
	}

	private static final Comparator<String> CODEPOINT_COMPARATOR = new Comparator<String>() {
		
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	};
	
	private CollationProvider _collationProvider = new CollationProvider() {
		
		public String getDefaultCollation() {
			return _defaultCollation;
		}
		
		public Comparator<String> getCollation(String uri) {
			if (CollationProvider.CODEPOINT_COLLATION.equals(uri)) return CODEPOINT_COMPARATOR;
			return null;
		}
	};

	public CollationProvider getCollationProvider() {
		return _collationProvider;
	}

	public StaticContextBuilder withCollationProvider(CollationProvider cp) {
		_collationProvider = cp;
		return this;
	}

	public StaticContextBuilder withVariable(javax.xml.namespace.QName qName,
			ItemType type) {
		_variableTypes.put(qName, type);
		return this;
	}

	public StaticContextBuilder withBaseUri(String string) throws URISyntaxException {
		_base_uri = new URI(string);		
		return this;
	}

	public StaticContextBuilder withFunctionLibrary(
			String namespace,
			FunctionLibrary fl) {
		_functionLibraries.put(namespace, fl);
		return this;
	}

	public StaticContextBuilder withDefaultCollation(String uri) {
		this._defaultCollation = uri;
		return this;
	}

	public ItemType getDocumentType(URI documentUri) {
		return null;
	}
}
