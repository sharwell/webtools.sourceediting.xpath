package org.eclipse.wst.xml.xpath2.processor.internal.types.xerces;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.eclipse.wst.xml.xpath2.api.typesystem.ComplexTypeDefinition;
import org.eclipse.wst.xml.xpath2.api.typesystem.SimpleTypeDefinition;
import org.w3c.dom.NodeList;

public class ComplexXercesTypeDefinition extends XercesTypeDefinition implements ComplexTypeDefinition {

	private final XSComplexTypeDefinition complexTypeDefinition;

	public ComplexXercesTypeDefinition(XSComplexTypeDefinition ad) {
		super(ad);
		this.complexTypeDefinition = ad;
	}

	@Override
	public SimpleTypeDefinition getSimpleType() {
		XSSimpleTypeDefinition simpleType = complexTypeDefinition.getSimpleType();
		if (simpleType != null) {
			return createTypeDefinition(simpleType);
		} else return null;
	}

	@Override
	public short getDerivationMethod() {
		// TODO: Map it
		return complexTypeDefinition.getDerivationMethod();
	}

	@Override
	public boolean getAbstract() {
		return complexTypeDefinition.getAbstract();
	}

	@Override
	public short getContentType() {
		return complexTypeDefinition.getContentType();
	}

	@Override
	public boolean isProhibitedSubstitution(short restriction) {
		return complexTypeDefinition.isProhibitedSubstitution(restriction);
	}

	@Override
	public short getProhibitedSubstitutions() {
		return complexTypeDefinition.getProhibitedSubstitutions();
	}

	@Override
	public Class<?> getNativeType() {
		return NodeList.class;
	}

}
