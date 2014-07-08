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

lexer grammar XPathLexer;

fragment
Whitespace
	:	[ \t\r\n]
	;

fragment
StringLiteral
	:	'"' ('""' | ~'"')* '"'
	|	'\'' ('\'\'' | ~'\'')* '\''
	;

fragment
Digits
	:	[0-9]+
	;

fragment
DoubleLiteral
	:	(	'.' Digits
		|	Digits ('.' [0-9]*)?
		)
		[eE] [+-]? Digits
	;

fragment
DecimalLiteral
	:	'.' Digits
	|	Digits '.' [0-9]*
	;

fragment
HighSurrogate
	:	[\uD800-\uDBFF]
	;

fragment
LowSurrogate
	:	[\uDC00-\uDFFF]
	;

fragment
Surrogate
	:	HighSurrogate LowSurrogate
	;

fragment
Letter
	:	[A-Z]
	|	'_'
	|	[a-z]
	|	[\u00C0-\u00D6]
	|	[\u00D8-\u00F6]
	|	[\u00F8-\u02FF]
	|	[\u0370-\u037D]
	|	[\u037F-\u1FFF]
	|	[\u200C-\u200D]
	|	[\u2070-\u218F]
	|	[\u2C00-\u2FEF]
	|	[\u3001-\uD7FF]
	|	[\uF900-\uFDCF]
	|	[\uFDF0-\uFFFD]
	|	Surrogate
	;

fragment
NCNameChar
	:	Letter
	|	[0-9]
	|	'.'
	|	'-'
	|	'_'
	;

fragment
NCName
	:	(Letter | '_') NCNameChar*
	;

BlockComment
	:	'(:'
		(	BlockComment
		|	'(' {_input.LA(1) != ':'}?
		|	':' {_input.LA(1) != ')'}?
		|	~[:(]
		)*
		':)'
		-> channel(HIDDEN)
	;

LBRACKET	:	'[';
RBRACKET	:	']';
LPAR		:	'(';
RPAR		:	')';

PLUS		:	'+';
MINUS		:	'-';
STAR		:	'*';
PIPE		:	'|';

EQUALS		:	'=';
NOTEQUALS	:	'!=';
LESSTHAN	:	'<';
LESSEQUAL	:	'<=';
GREATER		:	'>';
GREATEREQUAL:	'>=';

LESS_LESS	:	'<<';
GREATER_GREATER	:	'>>';

FORWARD_SLASH	:	'/';
FORWARD_SLASHSLASH	:	'//';

COLONCOLON	:	'::';
AT_SYM		:	'@';
DOTDOT		:	'..';
COLON		:	':';
COMMA		:	',';
DOLLAR		:	'$';
DOT			:	'.';
QUESTIONMARK:	'?';

CHILD		:	'child';
DESCENDANT	:	'descendant';
ATTRIBUTE	:	'attribute';
SELF		:	'self';
DESCENDANT_OR_SELF	:	'descendant-or-self';

FOLLOWING_SIBLING	:	'following-sibling';
FOLLOWING	:	'following';
NAMESPACE	:	'namespace';
PARENT		:	'parent';

ANCESTOR	:	'ancestor';
PRECEDING_SIBLING	:	'preceding-sibling';
PRECEDING	:	'preceding';
ANCESTOR_OR_SELF	:	'ancestor-or-self';

EQ			:	'eq';
NE			:	'ne';
LT			:	'lt';
LE			:	'le';
GT			:	'gt';
GE			:	'ge';

IDIV		:	'idiv';
DIV			:	'div';
MOD			:	'mod';

UNION		:	'union';
INTERSECT	:	'intersect';
EXCEPT		:	'except';

INSTANCE	:	'instance';
TREAT		:	'treat';
CASTABLE	:	'castable';
CAST		:	'cast';
AS			:	'as';
OF			:	'of';
IS			:	'is';

FOR			:	'for';
IN			:	'in';
RETURN		:	'return';
SATISFIES	:	'satisfies';
TO			:	'to';
SOME		:	'some';
EVERY		:	'every';
IF			:	'if';
THEN		:	'then';
ELSE		:	'else';
AND			:	'and';
OR			:	'or';

EMPTY_SEQUENCE	:	'empty-sequence';
ITEM		:	'item';
NODE		:	'node';
DOCUMENT_NODE	:	'document-node';
TEXT		:	'text';
COMMENT		:	'comment';

PROCESSING_INSTRUCTION	:	'processing-instruction';
SCHEMA_ATTRIBUTE	:	'schema-attribute';
ELEMENT		:	'element';
SCHEMA_ELEMENT	:	'schema-element';
TYPESWITCH	:	'typeswitch';

STRING		:	StringLiteral;
INTEGER		:	Digits;
DOUBLE		:	DoubleLiteral;
DECIMAL		:	DecimalLiteral;
NCNAME		:	NCName;

WHITESPACE
	:	Whitespace+
		-> channel(HIDDEN)
	;

ERR_CHAR
	:	.
	;
