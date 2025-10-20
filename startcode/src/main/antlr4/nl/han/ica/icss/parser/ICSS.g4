grammar ICSS;

//==================== LEXER ====================

IF  : 'if';
ELSE: 'else';
BOX_BRACKET_OPEN : '[';
BOX_BRACKET_CLOSE: ']';

OPEN_BRACE : '{';
CLOSE_BRACE: '}';
SEMICOLON : ';';
COLON     : ':';
PLUS      : '+';
MIN       : '-';
MUL       : '*';
ASSIGNMENT_OPERATOR: ':=';

LPAREN: '(';
RPAREN: ')';

TRUE : 'TRUE';
FALSE: 'FALSE';

PIXELSIZE  : [0-9]+ 'px';
PERCENTAGE : [0-9]+ '%';
SCALAR     : [0-9]+;

COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

ID_IDENT   : '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

LOWER_IDENT   : [a-z] [a-z0-9\-]*;
CAPITAL_IDENT : [A-Z] [A-Za-z0-9_]*;

WS: [ \t\r\n]+ -> skip;

//==================== PARSER ====================

stylesheet
    : (variable_assignment | stylerule)* EOF
    ;

stylerule
    : selector OPEN_BRACE statement* CLOSE_BRACE
    ;

selector
    : tag_selector
    | id_selector
    | class_selector
    ;

tag_selector
    : LOWER_IDENT
    ;

id_selector
    : ID_IDENT
    ;

class_selector
    : CLASS_IDENT
    ;

statement
    : declaration
    | variable_assignment
    | if_clause
    ;

declaration
    : property COLON expression SEMICOLON
    ;

property
    : LOWER_IDENT
    ;

variable_assignment
    : CAPITAL_IDENT ASSIGNMENT_OPERATOR expression SEMICOLON
    ;

if_clause
    : IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE statement* CLOSE_BRACE
      (ELSE OPEN_BRACE statement* CLOSE_BRACE)?
    ;

expression
    : additiveExpression
    ;

additiveExpression
    : multiplicativeExpression ( (PLUS | MIN) multiplicativeExpression )*
    ;

multiplicativeExpression
    : unaryExpression (MUL unaryExpression)*
    ;

unaryExpression
    : MIN unaryExpression                 #unaryMinus
    | primary                             #toPrimary
    ;

primary
    : literal
    | variable_reference
    | LPAREN expression RPAREN
    ;

literal
    : pixel_literal
    | percentage_literal
    | scalar_literal
    | color_literal
    | bool_literal
    ;

pixel_literal       : PIXELSIZE;
percentage_literal  : PERCENTAGE;
scalar_literal      : SCALAR;
color_literal       : COLOR;
bool_literal        : TRUE | FALSE;

variable_reference  : CAPITAL_IDENT;
