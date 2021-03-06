/*******************************************************************************
 * Copyright (c) 2011 Jesper Moller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jesper Moller - initial API and implementation
 *     Lukasz Wycisk - bug 361804 - StaticContextAdapter returns mock function
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.eclipse.wst.xml.xpath2.api.CollationProvider;
import org.eclipse.wst.xml.xpath2.api.Function;
import org.eclipse.wst.xml.xpath2.api.FunctionLibrary;
import org.eclipse.wst.xml.xpath2.api.StaticVariableResolver;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeModel;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeItemTypeImpl;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.SimpleAtomicItemTypeImpl;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;
import org.w3c.dom.Node;

public class StaticContextAdapter implements
		org.eclipse.wst.xml.xpath2.api.StaticContext {
	@SuppressWarnings("deprecation")
	private final org.eclipse.wst.xml.xpath2.processor.StaticContext sc;

	@Deprecated
	public StaticContextAdapter(
			org.eclipse.wst.xml.xpath2.processor.StaticContext sc) {
		this.sc = sc;
	}

	@Override
	public boolean isXPath1Compatible() {
		return sc.xpath1_compatible();
	}

	@Override
	public StaticVariableResolver getInScopeVariables() {
		return new StaticVariableResolver() {
			
			@Override
			public boolean isVariablePresent(javax.xml.namespace.QName name) {
				return sc.variable_exists(qn(name));
			}

			@Override
			public org.eclipse.wst.xml.xpath2.api.typesystem.ItemType getVariableType(javax.xml.namespace.QName name) {
				return new SimpleAtomicItemTypeImpl(BuiltinTypeLibrary.XS_ANYTYPE);
			}
		};
	}

	private QName qn(javax.xml.namespace.QName name) {
		return new QName(name);
	}

	@Override
	public TypeDefinition getInitialContextType() {
		return BuiltinTypeLibrary.XS_UNTYPED;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Map<String, FunctionLibrary> getFunctionLibraries() {
		if (sc instanceof DefaultStaticContext) {
			DefaultStaticContext dsc = (DefaultStaticContext)sc;
			return dsc.get_function_libraries();
		}
		return Collections.emptyMap();
	}

	@SuppressWarnings("deprecation")
	@Override
	public CollationProvider getCollationProvider() {
		if (sc instanceof org.eclipse.wst.xml.xpath2.processor.DynamicContext) {
			final org.eclipse.wst.xml.xpath2.processor.DynamicContext dc = (org.eclipse.wst.xml.xpath2.processor.DynamicContext)sc;
			return new CollationProvider() {
				
				@Override
				public String getDefaultCollation() {
					return dc.default_collation_name();
				}
				
				@Override
				public Comparator<String> getCollation(String name) {
					return dc.get_collation(name);
				}
			};
		}
		
		return new CollationProvider() {
			
			@Override
			public String getDefaultCollation() {
				return null;
			}
			
			@Override
			public Comparator<String> getCollation(String name) {
				return null;
			}
		};
	}

	@Override
	public URI getBaseUri() {
		// TODO Auto-generated method stub
		try {
			return new URI(sc.base_uri().getStringValue());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return new NamespaceContext() {
			
			@Override
			public Iterator<?> getPrefixes(String arg0) {
				return Collections.emptyList().iterator();
			}
			
			@Override
			public String getPrefix(String arg0) {
				return "x";
			}
			
			@Override
			public String getNamespaceURI(String prefix) {
				String ns = sc.resolve_prefix(prefix);
				return ns != null ? ns : XMLConstants.NULL_NS_URI;
			}
		};
	}

	@Override
	public String getDefaultNamespace() {
		return sc.default_namespace();
	}

	@Override
	public String getDefaultFunctionNamespace() {
		return sc.default_function_namespace();
	}

	@Override
	public TypeModel getTypeModel() {
		return sc.getTypeModel(null);
	}

	@Override
	public Function resolveFunction(javax.xml.namespace.QName name, int arity) {
		if (sc.function_exists(new QName(name), arity)) {
			FunctionLibrary functionLibrary = getFunctionLibraries().get(name.getNamespaceURI());
			if (functionLibrary != null) {
				return functionLibrary.resolveFunction(name.getLocalPart(), arity);
			}
		}
		throw new IllegalArgumentException("Function not found "+name);
	}

	@Override
	public TypeDefinition getCollectionType(String collectionName) {
		return BuiltinTypeLibrary.XS_UNTYPED;
	}

	@Override
	public TypeDefinition getDefaultCollectionType() {
		return BuiltinTypeLibrary.XS_UNTYPED;

	}

	@Override
	public org.eclipse.wst.xml.xpath2.api.typesystem.ItemType getDocumentType(
			URI documentUri) {
		return new NodeItemTypeImpl(org.eclipse.wst.xml.xpath2.api.typesystem.ItemType.OCCURRENCE_OPTIONAL, Node.DOCUMENT_NODE);
	}
}