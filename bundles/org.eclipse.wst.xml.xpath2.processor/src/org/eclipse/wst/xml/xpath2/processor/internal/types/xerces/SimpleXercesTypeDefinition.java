package org.eclipse.wst.xml.xpath2.processor.internal.types.xerces;

import java.util.LinkedList;
import java.util.List;

import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.eclipse.wst.xml.xpath2.api.typesystem.SimpleTypeDefinition;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.w3c.dom.Node;

public class SimpleXercesTypeDefinition extends XercesTypeDefinition implements SimpleTypeDefinition {

	private final XSSimpleTypeDefinition simpleTypeDefinition;

	public SimpleXercesTypeDefinition(XSSimpleTypeDefinition ad) {
		super(ad);
		this.simpleTypeDefinition = ad;
	}

	@Override
	public short getVariety() {
		return simpleTypeDefinition.getVariety();
	}

	@Override
	public SimpleTypeDefinition getPrimitiveType() {
		return createTypeDefinition(simpleTypeDefinition.getPrimitiveType());
	}

	@Override
	public short getBuiltInKind() {
		return simpleTypeDefinition.getBuiltInKind();
	}

	@Override
	public TypeDefinition getItemType() {
		return createTypeDefinition(simpleTypeDefinition.getItemType());
	}

	@Override
	public List<SimpleTypeDefinition> getMemberTypes() {
		XSObjectList xsMemberTypes = simpleTypeDefinition.getMemberTypes();
		List<SimpleTypeDefinition> memberTypes = new LinkedList<SimpleTypeDefinition>();
		for (int i = 0; i < xsMemberTypes.getLength(); i++) {
			memberTypes.add(createTypeDefinition((XSSimpleTypeDefinition) xsMemberTypes.item(i)));
		}
		return memberTypes;
	}

	@Override
	public short getOrdered() {
		return simpleTypeDefinition.getOrdered();
	}

	@Override
	public boolean getFinite() {
		return simpleTypeDefinition.getFinite();
	}

	@Override
	public boolean getBounded() {
		return simpleTypeDefinition.getBounded();
	}

	@Override
	public boolean getNumeric() {
		return simpleTypeDefinition.getNumeric();
	}

	@Override
	public Class<?> getNativeType() {
		return Node.class;
	}

}
