/*
 * Router configuration lexer.
 *
 * author: Lev Himmelfarb
 *
 * Copyright 2013 Boyle Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
lexer grammar RoutesLexer;


BEGIN_DECL_CONTROLLER_PACKAGES: 'controllerPackages' WS* ':' -> mode(DECL) ;
BEGIN_DECL_ENTITY_PACKAGES: 'entityPackages' WS* ':' -> mode(DECL) ;
BEGIN_DECL_VIEWS_BASE: 'viewsBase' WS* ':' -> mode(DECL) ;
BEGIN_DECL_LOGIN_PAGE: 'loginPage' WS* ':' -> mode(DECL) ;
BEGIN_DECL_PROTECTED_PAGES: 'protectedPages' WS* ':' -> mode(DECL) ;
BEGIN_DECL_PUBLIC_PAGES: 'publicPages' WS* ':' -> mode(DECL) ;

ROUTE_ID: '@' ~[ \t\r\n]+ ;
URI_PATTERN: '/' ~[ \t\r\n]* -> mode(MAPPING) ;

BEGIN_SCRIPT: '{' -> pushMode(SCRIPT) ;

COMMENT: '#' .*? '\r'? '\n' -> skip ;
IWS: WS+ -> skip ;

fragment WS: [ \t\r\n] ;


mode DECL;

DECL_COMMENT: '#' .*? '\r'? '\n' -> skip ;
DECL_IWS: [ \t]+ -> skip ;

DECL_MORE: '\r'? '\n' [ \t]+ -> skip ;

DECL_VALUE: ~[ \t\r\n,]+ ;

DECL_COMMA: ',' ;

END_DECL: '\r'? '\n' -> mode(DEFAULT_MODE) ;


mode MAPPING;

MAPPING_COMMENT: '#' .*? '\r'? '\n' -> skip ;
MAPPING_IWS: [ \t\r\n]+ -> skip ;

MAPPING_MODE: '+' [LSU] ;
MAPPING_CONTROLLER_NAME: MAPPING_JAVA_NAME ('.' MAPPING_JAVA_NAME)* ;
fragment MAPPING_JAVA_NAME: [a-zA-Z_$] [a-zA-Z_$0-9]* ;
MAPPING_LPAREN: '(' -> pushMode(CTRL_ARGS) ;
MAPPING_ARROW: '=>' -> mode(MAPPING_VIEW) ;

BEGIN_ROUTE_SCRIPT: '{' -> type(BEGIN_SCRIPT), pushMode(SCRIPT) ;


mode CTRL_ARGS;

CTRL_ARGS_COMMENT: '#' .*? '\r'? '\n' -> skip ;
CTRL_ARGS_IWS: [ \t\r\n]+ -> skip ;

CTRL_ARGS_COMMA: ',' ;

CTRL_ARGS_LIT_BOOL: 'true' | 'false' ;
CTRL_ARGS_LIT_LONG: '-'? [0-9]+ [lL] ;
CTRL_ARGS_LIT_DOUBLE: '-'? [0-9]+ '.' [0-9]+ [dD];
CTRL_ARGS_LIT_FLOAT: '-'? [0-9]+ '.' [0-9]+ [fF];
CTRL_ARGS_LIT_DECIMAL: '-'? [0-9]+ '.' [0-9]* ;
CTRL_ARGS_LIT_INT: '-'? [0-9]+ ;

CTRL_ARGS_QUOT: '"' -> more, pushMode(STRING) ;

CTRL_ARGS_RPAREN: ')' -> popMode ;


mode MAPPING_VIEW;

MAPPING_VIEW_COMMENT: '#' .*? '\r'? '\n' -> skip ;
MAPPING_VIEW_IWS: [ \t\r\n]+ -> skip ;

MAPPING_VIEW_ID: ~[ \t\r\n]+ -> mode(DEFAULT_MODE) ;


mode SCRIPT;

SCRIPT_KW_IF: 'if' ;
SCRIPT_KW_ELSE: 'else' ;
SCRIPT_KW_UNLESS: 'unless' ;
SCRIPT_KW_ABORT: 'abort' ;
SCRIPT_KW_FORBID: 'forbid' ;
SCRIPT_KW_NEW: 'new' ;
SCRIPT_KW_REF: 'ref' ;

SCRIPT_OP_EQ: '==' ;
SCRIPT_OP_NE: '!=' ;
SCRIPT_OP_AND: '&' '&'? ;
SCRIPT_OP_OR: '|' '|'? ;
SCRIPT_OP_NOT: '!' ;

SCRIPT_COND_GET: 'GET' ;
SCRIPT_COND_POST: 'POST' ;
SCRIPT_COND_DELETE: 'DELETE' ;

SCRIPT_QOP_MAXRESULTS: 'maxResults' ;
SCRIPT_QOP_FIRSTRESULT: 'firstResult' ;
SCRIPT_QOP_LIST: 'list' ;

SCRIPT_LPAREN: '(' ;
SCRIPT_RPAREN: ')' ;
SCRIPT_COLON: ':' ;
SCRIPT_DOT: '.' ;
SCRIPT_COMMA: ',' ;
SCRIPT_ASSIGN: '=' ;

SCRIPT_LIT_BOOL: 'true' | 'false' ;
SCRIPT_LIT_LONG: '-'? [0-9]+ [lL] ;
SCRIPT_LIT_DOUBLE: '-'? [0-9]+ '.' [0-9]+ [dD];
SCRIPT_LIT_FLOAT: '-'? [0-9]+ '.' [0-9]+ [fF];
SCRIPT_LIT_DECIMAL: '-'? [0-9]+ '.' [0-9]* ;
SCRIPT_LIT_INT: '-'? [0-9]+ ;

SCRIPT_QUOT: '"' -> more, pushMode(STRING) ;

SCRIPT_NAME: [a-zA-Z_$] [a-zA-Z_$0-9]* ;

SCRIPT_LCURLY: '{' -> type(BEGIN_SCRIPT), pushMode(SCRIPT) ;
END_SCRIPT: '}' -> popMode ;

SCRIPT_COMMENT: '#' .*? '\r'? '\n' -> skip ;
SCRIPT_IWS: [ \t\r\n]+ -> skip ;


mode STRING;

STRING_ESC_OCT3: '\\' [0-3] [0-7] [0-7] -> more ;
STRING_ESC_OCT: '\\' [0-7] [0-7]? -> more ;
STRING_ESC_UNICODE: '\\u' [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] -> more ;
STRING_ESC: '\\' [\\"'btnfr] -> more ;
LIT_STRING: '"' -> popMode ;
STRING_TEXT: . -> more ;
