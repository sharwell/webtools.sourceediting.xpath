/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sam Harwell - parser implementation using ANTLR
 *******************************************************************************/

parser grammar XPathParser;

options {
	tokenVocab = XPathLexer;
}

xPath
	:	expr EOF
	;

expr
	:	exprSingle (COMMA exprSingle)*
	;

exprSingle
	:	forExpr
	|	quantifiedExpr
	|	ifExpr
	|	orExpr
	;

forExpr
	:	simpleForClause RETURN exprSingle
	;

simpleForClause
	:	FOR DOLLAR varName IN exprSingle
	|	simpleForClause COMMA DOLLAR varName IN exprSingle
	;

quantifiedExpr
	:	SOME DOLLAR quantifiedExprMiddle SATISFIES exprSingle
	|	EVERY DOLLAR quantifiedExprMiddle SATISFIES exprSingle
	;

quantifiedExprMiddle
	:	varName IN exprSingle
	|	quantifiedExprMiddle COMMA DOLLAR varName IN exprSingle
	;

ifExpr
	:	IF LPAR expr RPAR THEN exprSingle ELSE exprSingle
	;

orExpr
	:	andExpr
	|	orExpr OR andExpr
	;

andExpr
	:	comparisonExpr
	|	andExpr AND comparisonExpr
	;

comparisonExpr
	:	rangeExpr
	|	rangeExpr valueComp rangeExpr
	|	rangeExpr generalComp rangeExpr
	|	rangeExpr nodeComp rangeExpr
	;

rangeExpr
	:	additiveExpr
	|	additiveExpr TO additiveExpr
	;

additiveExpr
	:	multiplicativeExpr
	|	additiveExpr PLUS multiplicativeExpr
	|	additiveExpr MINUS multiplicativeExpr
	;

multiplicativeExpr
	:	unionExpr
	|	multiplicativeExpr STAR unionExpr
	|	multiplicativeExpr DIV unionExpr
	|	multiplicativeExpr IDIV unionExpr
	|	multiplicativeExpr MOD unionExpr
	;

unionExpr
	:	intersectExceptExpr
	|	unionExpr UNION intersectExceptExpr
	|	unionExpr PIPE intersectExceptExpr
	;

intersectExceptExpr
	:	instanceOfExpr
	|	intersectExceptExpr INTERSECT instanceOfExpr
	|	intersectExceptExpr EXCEPT instanceOfExpr
	;

instanceOfExpr
	:	treatExpr
	|	treatExpr INSTANCE OF sequenceType
	;

treatExpr
	:	castableExpr
	|	castableExpr TREAT AS sequenceType
	;

castableExpr
	:	castExpr
	|	castExpr CASTABLE AS singleType
	;

castExpr
	:	unaryExpr
	|	unaryExpr CAST AS singleType
	;

unaryExpr
	:	valueExpr
	|	MINUS unaryExpr
	|	PLUS unaryExpr
	;

valueExpr
	:	pathExpr
	;

generalComp
	:	EQUALS
	|	NOTEQUALS
	|	LESSTHAN
	|	LESSEQUAL
	|	GREATER
	|	GREATEREQUAL
	;

valueComp
	:	EQ
	|	NE
	|	LT
	|	LE
	|	GT
	|	GE
	;

nodeComp
	:	IS
	|	LESS_LESS
	|	GREATER_GREATER
	;

pathExpr
	:	FORWARD_SLASH
	|	FORWARD_SLASH relativePathExpr
	|	FORWARD_SLASHSLASH relativePathExpr
	|	relativePathExpr
	;

relativePathExpr
	:	stepExpr
	|	relativePathExpr FORWARD_SLASH stepExpr
	|	relativePathExpr FORWARD_SLASHSLASH stepExpr
	;

stepExpr
	:	axisStep
	|	filterExpr
	;

axisStep
	:	forwardStep predicateList
	|	reverseStep predicateList
	;

forwardStep
	:	forwardAxis nodeTest
	|	abbrevForwardStep
	;

forwardAxis
	:	CHILD COLONCOLON
	|	DESCENDANT COLONCOLON
	|	ATTRIBUTE COLONCOLON
	|	SELF COLONCOLON
	|	DESCENDANT_OR_SELF COLONCOLON
	|	FOLLOWING_SIBLING COLONCOLON
	|	FOLLOWING COLONCOLON
	|	NAMESPACE COLONCOLON
	;

abbrevForwardStep
	:	AT_SYM nodeTest
	|	nodeTest
	;

reverseStep
	:	reverseAxis nodeTest
	|	abbrevReverseStep
	;

reverseAxis
	:	PARENT COLONCOLON
	|	ANCESTOR COLONCOLON
	|	PRECEDING_SIBLING COLONCOLON
	|	PRECEDING COLONCOLON
	|	ANCESTOR_OR_SELF COLONCOLON
	;

abbrevReverseStep
	:	DOTDOT
	;

nodeTest
	:	kindTest
	|	nameTest
	;

nameTest
	:	qName
	|	wildcard
	;

wildcard
	:	STAR
	|	nCName COLON STAR
	|	STAR COLON nCName
	;

filterExpr
	:	primaryExpr predicateList
	;

predicateList
	:	predicate*
	;

predicate
	:	LBRACKET expr RBRACKET
	;

primaryExpr
	:	literal
	|	varRef
	|	parenthesizedExpr
	|	contextItemExpr
	|	functionCall
	;

literal
	:	numericLiteral
	|	stringLiteral
	;

numericLiteral
	:	integerLiteral
	|	decimalLiteral
	|	doubleLiteral
	;

varRef
	:	DOLLAR varName
	;

parenthesizedExpr
	:	LPAR RPAR
	|	LPAR expr RPAR
	;

contextItemExpr
	:	DOT
	;

functionCall
	:	unreservedQName LPAR RPAR
	|	unreservedQName LPAR functionCallMiddle RPAR
	;

functionCallMiddle
	:	exprSingle
	|	functionCallMiddle COMMA exprSingle
	;

singleType
	:	atomicType
	|	atomicType QUESTIONMARK
	;

sequenceType
	:	itemType
	|	itemType occurrenceIndicator
	|	EMPTY_SEQUENCE LPAR RPAR
	;

occurrenceIndicator
	:	QUESTIONMARK
	|	STAR
	|	PLUS
	;

itemType
	:	atomicType
	|	kindTest
	|	ITEM LPAR RPAR
	;

atomicType
	:	qName
	;

kindTest
	:	documentTest
	|	elementTest
	|	attributeTest
	|	schemaElementTest
	|	schemaAttributeTest
	|	pITest
	|	commentTest
	|	textTest
	|	anyKindTest
	;

anyKindTest
	:	NODE LPAR RPAR
	;

documentTest
	:	DOCUMENT_NODE LPAR RPAR
	|	DOCUMENT_NODE LPAR elementTest RPAR
	|	DOCUMENT_NODE LPAR schemaElementTest RPAR
	;

textTest
	:	TEXT LPAR RPAR
	;

commentTest
	:	COMMENT LPAR RPAR
	;

pITest
	:	PROCESSING_INSTRUCTION LPAR RPAR
	|	PROCESSING_INSTRUCTION LPAR nCName RPAR
	|	PROCESSING_INSTRUCTION LPAR stringLiteral RPAR
	;

attributeTest
	:	ATTRIBUTE LPAR RPAR
	|	ATTRIBUTE LPAR attribNameOrWildcard RPAR
	|	ATTRIBUTE LPAR attribNameOrWildcard COMMA typeName RPAR
	;

attribNameOrWildcard
	:	attributeName
	|	STAR
	;

schemaAttributeTest
	:	SCHEMA_ATTRIBUTE LPAR attributeDeclaration RPAR
	;

attributeDeclaration
	:	attributeName
	;

elementTest
	:	ELEMENT LPAR RPAR
	|	ELEMENT LPAR elementNameOrWildcard RPAR
	|	ELEMENT LPAR elementNameOrWildcard COMMA typeName RPAR
	|	ELEMENT LPAR elementNameOrWildcard COMMA typeName QUESTIONMARK RPAR
	;

elementNameOrWildcard
	:	elementName
	|	STAR
	;

schemaElementTest
	:	SCHEMA_ELEMENT LPAR elementDeclaration RPAR
	;

elementDeclaration
	:	elementName
	;

attributeName
	:	qName
	;

elementName
	:	qName
	;

typeName
	:	qName
	;

integerLiteral
	:	INTEGER
	;

decimalLiteral
	:	DECIMAL
	;

doubleLiteral
	:	DOUBLE
	;

stringLiteral
	:	STRING
	;

varName
	:	qName
	;

qName
	:	nCName
	|	nCName COLON nCName
	;

prefix
	:	nCName
	;

localPart
	:	nCName
	;

unreservedQName
	:	unreservedNCName
	|	nCName COLON nCName
	;

nCName
	:	NCNAME
	|	TO
	|	CHILD
	|	DESCENDANT
	|	ATTRIBUTE
	|	SELF
	|	DESCENDANT_OR_SELF
	|	FOLLOWING_SIBLING
	|	FOLLOWING
	|	NAMESPACE
	|	PARENT
	|	ANCESTOR
	|	PRECEDING_SIBLING
	|	PRECEDING
	|	ANCESTOR_OR_SELF
	|	UNION
	|	INTERSECT
	|	EXCEPT
	|	INSTANCE
	|	OF
	|	AS
	|	TREAT
	|	CASTABLE
	|	CAST
	|	IS
	|	FOR
	|	IN
	|	RETURN
	|	SATISFIES
	|	SOME
	|	EVERY
	|	IF
	|	THEN
	|	ELSE
	|	AND
	|	OR
	|	DIV
	|	IDIV
	|	COMMENT
	|	DOCUMENT_NODE
	|	ELEMENT
	|	EMPTY_SEQUENCE
	|	ITEM
	|	NODE
	|	PROCESSING_INSTRUCTION
	|	SCHEMA_ATTRIBUTE
	|	SCHEMA_ELEMENT
	|	TEXT
	|	TYPESWITCH
	|	MOD
	|	EQ
	|	NE
	|	LT
	|	LE
	|	GT
	|	GE
	;

unreservedNCName
	:	NCNAME
	|	TO
	|	CHILD
	|	DESCENDANT
	|	SELF
	|	DESCENDANT_OR_SELF
	|	FOLLOWING_SIBLING
	|	FOLLOWING
	|	NAMESPACE
	|	PARENT
	|	ANCESTOR
	|	PRECEDING_SIBLING
	|	PRECEDING
	|	ANCESTOR_OR_SELF
	|	UNION
	|	INTERSECT
	|	EXCEPT
	|	INSTANCE
	|	OF
	|	AS
	|	TREAT
	|	CASTABLE
	|	CAST
	|	IS
	|	FOR
	|	IN
	|	RETURN
	|	SATISFIES
	|	SOME
	|	EVERY
	|	THEN
	|	ELSE
	|	AND
	|	OR
	|	DIV
	|	IDIV
	|	MOD
	|	EQ
	|	NE
	|	LT
	|	LE
	|	GT
	|	GE
	;
