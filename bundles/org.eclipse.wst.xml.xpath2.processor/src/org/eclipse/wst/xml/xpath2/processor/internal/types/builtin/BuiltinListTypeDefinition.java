package org.eclipse.wst.xml.xpath2.processor.internal.types.builtin;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.wst.xml.xpath2.api.typesystem.SimpleTypeDefinition;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;

public class BuiltinListTypeDefinition extends BuiltinTypeDefinition implements SimpleTypeDefinition {
	
	private final BuiltinAtomicTypeDefinition itemType;

	public BuiltinListTypeDefinition(QName name, BuiltinTypeDefinition baseType, BuiltinAtomicTypeDefinition itemType) {
		super(name, null, Collection.class, baseType);
		this.itemType = itemType;
	}

	public BuiltinListTypeDefinition(String name, BuiltinTypeDefinition baseType, BuiltinAtomicTypeDefinition itemType) {
		super(name, null, Collection.class, baseType);
		this.itemType = itemType;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public short getVariety() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SimpleTypeDefinition getPrimitiveType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short getBuiltInKind() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TypeDefinition getItemType() {
		return itemType;
	}

	@Override
	public List<SimpleTypeDefinition> getMemberTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short getOrdered() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getFinite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getBounded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getNumeric() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
