grammar ICSS;


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
NUMBER     : [0-9]+;
fragment HEX : [0-9A-Fa-f];
COLOR : '#' HEX HEX HEX HEX HEX HEX;
IDIDENT    : '#' [a-zA-Z\-][a-zA-Z0-9\-]*;
CLASSIDENT : '.' [a-z0-9\-]+;
LOWERIDENT : [a-z] [a-z0-9\-]*;
CAPITALIDENT : [A-Z] [A-Za-z0-9_]*;
WS: [ \t\r\n]+ -> skip;


stylesheet
    : (variableAssignment | styleRule)* EOF
    ;

styleRule
    : selector OPEN_BRACE statement* CLOSE_BRACE
    ;

selector
    : tagSelector
    | idSelector
    | classSelector
    ;

tagSelector : LOWERIDENT ;
idSelector  : IDIDENT ;
classSelector : CLASSIDENT ;

statement
    : declaration
    | variableAssignment
    | ifClause
    ;

declaration
    : property COLON expression SEMICOLON
    ;

property : LOWERIDENT ;

variableAssignment
    : CAPITALIDENT ASSIGNMENT_OPERATOR expression SEMICOLON
    ;

ifClause
    : IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE
      OPEN_BRACE statement* CLOSE_BRACE
      (ELSE OPEN_BRACE statement* CLOSE_BRACE)?
    ;


expression
    : additiveExpression
    ;

additiveExpression
    : multiplicativeExpression ((PLUS | MIN) multiplicativeExpression)*
    ;

multiplicativeExpression
    : value (MUL value)*
    ;

value
    : literal
    | variableReference
    | LPAREN expression RPAREN
    ;


literal
    : PIXELSIZE
    | PERCENTAGE
    | NUMBER
    | COLOR
    | TRUE
    | FALSE
    ;

variableReference
    : CAPITALIDENT
    ;
