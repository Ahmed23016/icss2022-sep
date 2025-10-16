grammar level01;

stylesheet
    :   ruleSet+ EOF
    ;

ruleSet
    :   selector OPEN_BRACE declaration+ CLOSE_BRACE
    ;

selector
    :   HASH IDENT              #idSelector
    |   DOT IDENT               #classSelector
    |   IDENT                   #elementSelector
    ;

declaration
    :   property COLON value SEMICOLON
    ;

property
    :   IDENT
    ;

value
    :   PIXELSIZE
    |   NUMBER
    |   COLOR
    |   STRING
    |   IDENT
    ;

// ---------- Lexer ----------

HASH        : '#';
DOT         : '.';
OPEN_BRACE  : '{';
CLOSE_BRACE : '}';
COLON       : ':';
SEMICOLON   : ';';

IDENT
    :   [a-zA-Z_] [a-zA-Z0-9_-]*
    ;

PIXELSIZE
    :   DIGIT+ 'px'
    ;

NUMBER
    :   DIGIT+ ('.' DIGIT+)?
    ;

COLOR
    :   '#' HEXDIGIT HEXDIGIT HEXDIGIT (HEXDIGIT HEXDIGIT HEXDIGIT)?
    ;

STRING
    :   '"' (~["\r\n])* '"'
    |   '\'' (~['\r\n])* '\''
    ;

fragment DIGIT    : [0-9];
fragment HEXDIGIT : [0-9a-fA-F];

WS      : [ \t\r\n\f]+ -> skip ;
COMMENT : '/*' .*? '*/' -> skip ;
