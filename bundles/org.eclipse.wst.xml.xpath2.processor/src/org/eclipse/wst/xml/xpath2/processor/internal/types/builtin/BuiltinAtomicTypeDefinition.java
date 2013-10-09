package org.eclipse.wst.xml.xpath2.processor.internal.types.builtin;

import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;

public class BuiltinAtomicTypeDefinition extends BuiltinTypeDefinition {

	public BuiltinAtomicTypeDefinition(String name, Class<? extends AnyType> implementationClass, Class<?> nativeType, BuiltinTypeDefinition baseType) {
		super(name, implementationClass, nativeType, baseType);
	}
	
	public boolean isAbstract() {
		return false;
	}
}
