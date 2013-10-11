/*
 * Router configuration parser.
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
parser grammar RoutesParser;

@header {
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
}

@members {

/**
 * Parent provider.
 */
private RoutesRouterConfigurationProvider provider;

/**
 * Login page URI.
 */
private String loginPageURI;

/**
 * Protected pages URI patterns.
 */
private final Set<String> protectedURIPatterns = new HashSet<>();

/**
 * Public pages URI patterns.
 */
private final Set<String> publicURIPatterns = new HashSet<>();

/**
 * Current set of controller packages.
 */
private final Set<String> controllerPackages = new HashSet<>();

/**
 * Current set of entity packages.
 */
private final Set<String> entityPackages = new HashSet<>();

/**
 * Current views base.
 */
private String viewsBase = "";


/**
 * Set parent provider, to which to add the mappings. This method needs to be
 * called before the parser can be used.
 *
 * @param provider Reference to the parent provider.
 */
void setProvider(final RoutesRouterConfigurationProvider provider) {

	this.provider = provider;
}


/**
 * Get login page URI from the parsed configuration.
 *
 * @return Login page URI.
 */
String getLoginPageURI() {

	return this.loginPageURI;
}

/**
 * Get protected pages URI pattern from the parsed configuration.
 *
 * @return The pattern or {@code null}.
 */
String getProtectedURIPattern() {

	return this.combinePatterns(this.protectedURIPatterns);
}

/**
 * Get public pages URI pattern from the parsed configuration.
 *
 * @return The pattern or {@code null}.
 */
String getPublicURIPattern() {

	return this.combinePatterns(this.publicURIPatterns);
}


/**
 * Combine URI patterns in a set into a single pattern.
 *
 * @param patterns Patterns to combine.
 *
 * @return Combined pattern, or {@code null} if the patterns set is empty.
 */
private String combinePatterns(final Set<String> patterns) {

	final int numPatterns = patterns.size();

	if (numPatterns == 0)
		return null;

	if (numPatterns == 1)
		return patterns.iterator().next();

	final StringBuilder sb = new StringBuilder(256);
	for (final String p : patterns) {
		if (sb.length() > 0)
			sb.append('|');
		sb.append("(?:").append(p).append(')');
	}

	return sb.toString();
}

/**
 * Get string literal's value.
 *
 * @param literal The literal.
 *
 * @return The literal value.
 */
private String literalValue(final String literal) {

	final int litLen = literal.length();
	final StringBuilder res = new StringBuilder(litLen - 2);
	for (int i = 1; i < litLen - 1; i++) {
		char c = literal.charAt(i);
		if (c == '\\') {
			c = literal.charAt(++i);
			switch (c) {
			case '\\':
			case '"':
			case '\'': res.append(c); break;
			case 'b': res.append('\b'); break;
			case 't': res.append('\t'); break;
			case 'n': res.append('\n'); break;
			case 'f': res.append('\f'); break;
			case 'r': res.append('\r'); break;
			// TODO: more escape sequences
			}
		} else {
			res.append(c);
		}
	}

	return res.toString();
}
}


options {
	tokenVocab=RoutesLexer;
}


config
@after {
	if (this.loginPageURI == null)
		throw new InvalidRoutesException("Router configuration does not" +
				" contain information about the login page.");
}
	: declaration* initialDeclarations (declaration | mapping)* EOF
	;


initialDeclarations
	: loginPageDeclaration? declaration* protectedPagesDeclaration? declaration* publicPagesDeclaration?
	| loginPageDeclaration? declaration* publicPagesDeclaration? declaration* protectedPagesDeclaration?
	| protectedPagesDeclaration? declaration* loginPageDeclaration? declaration* publicPagesDeclaration?
	| protectedPagesDeclaration? declaration* publicPagesDeclaration? declaration* loginPageDeclaration?
	| publicPagesDeclaration? declaration* loginPageDeclaration? declaration* protectedPagesDeclaration?
	| publicPagesDeclaration? declaration* protectedPagesDeclaration? declaration* loginPageDeclaration?
	;

loginPageDeclaration
	: BEGIN_DECL_LOGIN_PAGE DECL_VALUE END_DECL {
		this.loginPageURI = $DECL_VALUE.text;
	}
	;

protectedPagesDeclaration
	: BEGIN_DECL_PROTECTED_PAGES v+=DECL_VALUE (DECL_COMMA v+=DECL_VALUE)* END_DECL {
		for (final Token t : $v)
			this.protectedURIPatterns.add(t.getText());
	}
	;

publicPagesDeclaration
	: BEGIN_DECL_PUBLIC_PAGES v+=DECL_VALUE (DECL_COMMA v+=DECL_VALUE)* END_DECL {
		for (final Token t : $v)
			this.publicURIPatterns.add(t.getText());
	}
	;

declaration
	: controllerPackagesDeclaration
	| entityPackagesDeclaration
	| viewsBaseDeclaration
	;

controllerPackagesDeclaration
	: BEGIN_DECL_CONTROLLER_PACKAGES v+=DECL_VALUE (DECL_COMMA v+=DECL_VALUE)* END_DECL {
		this.controllerPackages.clear();
		for (final Token t : $v)
			this.controllerPackages.add(t.getText());
	}
	;

entityPackagesDeclaration
	: BEGIN_DECL_ENTITY_PACKAGES v+=DECL_VALUE (DECL_COMMA v+=DECL_VALUE)* END_DECL {
		this.entityPackages.clear();
		for (final Token t : $v)
			this.entityPackages.add(t.getText());
	}
	;

viewsBaseDeclaration
	: BEGIN_DECL_VIEWS_BASE DECL_VALUE END_DECL {
		this.viewsBase = $DECL_VALUE.text;
	}
	;


mapping
locals [String viewIdPattern, Object controllerObj,
	CombinedScript routeScriptObj, CombinedScript viewScriptObj,
	CombinedScript currentScript]
@init {
	$routeScriptObj = new CombinedScript();
	$viewScriptObj = new CombinedScript();
}
	: ROUTE_ID? URI_PATTERN MAPPING_MODE? controller? routeScript? MAPPING_ARROW view viewScript? {
		System.out.println(">>> MAPPING:");
		System.out.println("    - id=[" + ($ROUTE_ID != null ? $ROUTE_ID.text.substring(1) : null) + "]");
		System.out.println("    - uriPattern=[" + $URI_PATTERN.text + "]");
		String mappingMode = "DEFAULT";
		if ($MAPPING_MODE != null) {
			switch ($MAPPING_MODE.text.charAt(1)) {
			case 'L':
				if (this.loginPageURI != null)
					throw new InvalidRoutesException("Login page is defined more than once.");
				this.loginPageURI = $URI_PATTERN.text;
			case 'S':
				mappingMode = "FORCE_SSL";
				break;
			case 'U':
				mappingMode = "FORCE_REQUIRE_AUTH";
			}
		}
		System.out.println("    - securityMode=[" + mappingMode + "]");
		System.out.println("    - commonScript=[" + $routeScript.text + "]");
		System.out.println("    - controller=[" + $controllerObj + "]");
		System.out.println("    - viewIdPattern=[" + $viewIdPattern + "]");
		System.out.println("    - viewScript=[" + $viewScript.text + "]");
	}
	;

controller
locals [List<Class<?>> argTypes, List<Object> argValues]
@init {
	$argTypes = new ArrayList<>();
	$argValues = new ArrayList<>();
}
	: MAPPING_CONTROLLER_NAME (MAPPING_LPAREN controllerArgs CTRL_ARGS_RPAREN)? {

		final String controllerClassName = $MAPPING_CONTROLLER_NAME.text;
		Class<?> controllerClass = null;
		try {
			controllerClass = Class.forName(controllerClassName);
		} catch (final ClassNotFoundException e) {
			for (final String packageName : this.controllerPackages) {
				try {
					controllerClass = Class.forName(packageName + "." +
							controllerClassName);
					break;
				} catch (final ClassNotFoundException e1) {
					// skip, try next package
				}
			}
		}
		if (controllerClass == null)
			throw new InvalidRoutesException(
				"Invalid controller class at line " + $start.getLine() + ".");

		Constructor<?> constr;
		try {
			constr = controllerClass.getConstructor(
				$argTypes.toArray(new Class<?>[$argTypes.size()]));
		} catch (final NoSuchMethodException e) {
			throw new InvalidRoutesException(
				"Controller does not have constructor with arguments " +
				$argTypes + " at line " + $start.getLine() + ".");
		}

		try {
			$mapping::controllerObj = constr.newInstance(
				$argValues.toArray(new Object[$argValues.size()]));
		} catch (final ReflectiveOperationException e) {
			throw new InvalidRoutesException(
				"Error instantiating controller at line " + $start.getLine() +
				".", e);
		}
	}
	;

controllerArgs
	: controllerArg (CTRL_ARGS_COMMA controllerArg)*
	;

controllerArg
	: v=LIT_STRING {
		$controller::argTypes.add(String.class);
		$controller::argValues.add(this.literalValue($v.text));
	}
	| v=CTRL_ARGS_LIT_BOOL {
		$controller::argTypes.add(Boolean.class);
		$controller::argValues.add(Boolean.valueOf($v.text));
	}
	| v=CTRL_ARGS_LIT_LONG {
		$controller::argTypes.add(Long.class);
		$controller::argValues.add(Long.valueOf($v.text));
	}
	| v=CTRL_ARGS_LIT_DOUBLE {
		$controller::argTypes.add(Double.class);
		$controller::argValues.add(Double.valueOf($v.text));
	}
	| v=CTRL_ARGS_LIT_FLOAT {
		$controller::argTypes.add(Float.class);
		$controller::argValues.add(Float.valueOf($v.text));
	}
	| v=CTRL_ARGS_LIT_DECIMAL {
		$controller::argTypes.add(BigDecimal.class);
		$controller::argValues.add(new BigDecimal($v.text));
	}
	| v=CTRL_ARGS_LIT_INT {
		$controller::argTypes.add(Integer.class);
		$controller::argValues.add(Integer.valueOf($v.text));
	}
	;

view
	: MAPPING_VIEW_ID {
		$mapping::viewIdPattern = this.viewsBase + $MAPPING_VIEW_ID.text;
	}
	;


routeScript
@init {
	$mapping::currentScript = $mapping::routeScriptObj;
}
	: BEGIN_SCRIPT (routeScriptStatement)* END_SCRIPT
	;

viewScript
@init {
	$mapping::currentScript = $mapping::viewScriptObj;
}
	: BEGIN_SCRIPT (viewScriptStatement)* END_SCRIPT
	;

routeScriptStatement
	: routeScriptConditionalConstruct
	| scriptAbortStatement
	| scriptForbidStatement
	| scriptAllowStatement
	| scriptAssignStatement
	;

viewScriptStatement
	: viewScriptConditionalConstruct
	| scriptAssignStatement
	;

routeScriptConditionalConstruct
	: SCRIPT_KW_IF scriptCondition routeScript (SCRIPT_KW_ELSE routeScript)?
	;

viewScriptConditionalConstruct
	: SCRIPT_KW_IF scriptCondition viewScript (SCRIPT_KW_ELSE viewScript)?
	;

scriptAbortStatement
	: SCRIPT_KW_ABORT (SCRIPT_KW_IF | SCRIPT_KW_UNLESS) scriptCondition
	;

scriptForbidStatement
	: SCRIPT_KW_FORBID (SCRIPT_KW_IF | SCRIPT_KW_UNLESS) scriptCondition
	;

scriptAllowStatement
	: SCRIPT_KW_ALLOW (SCRIPT_KW_IF | SCRIPT_KW_UNLESS) scriptCondition
	;

scriptAssignStatement
	: SCRIPT_NAME SCRIPT_ASSIGN scriptValueExpr
	;

scriptCondition
	: SCRIPT_LPAREN scriptConditionExpr SCRIPT_RPAREN
	;

scriptConditionExpr
	: SCRIPT_OP_NOT<assoc=right> scriptConditionExpr
	| scriptConditionExpr SCRIPT_OP_AND scriptConditionExpr
	| scriptConditionExpr SCRIPT_OP_OR scriptConditionExpr
	| scriptValueExpr SCRIPT_OP_EQ scriptValueExpr
	| scriptValueExpr SCRIPT_OP_NE scriptValueExpr
	| scriptValueExpr
	| (SCRIPT_COND_GET | SCRIPT_COND_POST | SCRIPT_COND_DELETE)
	| SCRIPT_LPAREN scriptConditionExpr SCRIPT_RPAREN
	;

scriptValueExpr
	: scriptPersistencyExpr
	| scriptQName
	| scriptLiteral
	;

scriptPersistencyExpr
	: SCRIPT_KW_NEW scriptEntityName
	| SCRIPT_KW_REF scriptEntityName SCRIPT_LPAREN scriptValueExpr SCRIPT_RPAREN
	| scriptEntityName SCRIPT_COLON scriptQueryName SCRIPT_LPAREN scriptQueryParams SCRIPT_RPAREN scriptQueryOps
	| scriptEntityName SCRIPT_LPAREN scriptValueExpr SCRIPT_RPAREN
	;

scriptEntityName
	: scriptQName
	;

scriptQueryName
	: scriptQName
	;

scriptQueryParams
	: scriptQueryNamedParam (SCRIPT_COMMA scriptQueryNamedParam)*
	| scriptQueryPosParam (SCRIPT_COMMA scriptQueryPosParam)*
	|
	;

scriptQueryNamedParam
	: SCRIPT_NAME SCRIPT_COLON scriptValueExpr
	;

scriptQueryPosParam
	: scriptValueExpr
	;

scriptQueryOps
	: scriptQueryOp* (SCRIPT_DOT SCRIPT_QOP_LIST)?
	;

scriptQueryOp
	: SCRIPT_DOT SCRIPT_QOP_MAXRESULTS SCRIPT_LPAREN scriptValueExpr SCRIPT_RPAREN
	| SCRIPT_DOT SCRIPT_QOP_FIRSTRESULT SCRIPT_LPAREN scriptValueExpr SCRIPT_RPAREN
	;

scriptQName
	: SCRIPT_NAME (SCRIPT_DOT SCRIPT_NAME)*
	;

scriptLiteral
	: LIT_STRING
	| SCRIPT_LIT_BOOL
	| SCRIPT_LIT_LONG
	| SCRIPT_LIT_INT
	| SCRIPT_LIT_DOUBLE
	| SCRIPT_LIT_FLOAT
	| SCRIPT_LIT_DECIMAL
	;
