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
import java.util.Collection;
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

/**
 * Find class given the class name an and a collection of packages, in which the
 * class may be.
 *
 * @param className Class name (qualified or unqualified).
 * @param packageNames Names of packages, in which to try to find the class.
 *
 * @return The class, or {@code null} if not found.
 */
private Class<?> findClass(final String className,
	final Collection<String> packageNames) {

	Class<?> res = null;
	try {
		res = Class.forName(className);
	} catch (final ClassNotFoundException e) {
		for (final String packageName : packageNames) {
			try {
				res = Class.forName(packageName + "." + className);
				break;
			} catch (final ClassNotFoundException e1) {
				// skip, try next package
			}
		}
	}

	return res;
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
	: loginPageDeclaration? declaration*
		protectedPagesDeclaration? declaration*
		publicPagesDeclaration?
	| loginPageDeclaration? declaration*
		publicPagesDeclaration? declaration*
		protectedPagesDeclaration?
	| protectedPagesDeclaration? declaration*
		loginPageDeclaration? declaration*
		publicPagesDeclaration?
	| protectedPagesDeclaration? declaration*
		publicPagesDeclaration? declaration*
		loginPageDeclaration?
	| publicPagesDeclaration? declaration*
		loginPageDeclaration? declaration*
		protectedPagesDeclaration?
	| publicPagesDeclaration? declaration*
		protectedPagesDeclaration? declaration*
		loginPageDeclaration?
	;

loginPageDeclaration
	: BEGIN_DECL_LOGIN_PAGE DECL_VALUE END_DECL {

		this.loginPageURI = $DECL_VALUE.text;
	}
	;

protectedPagesDeclaration
	: BEGIN_DECL_PROTECTED_PAGES
		v+=DECL_VALUE (DECL_COMMA v+=DECL_VALUE)* END_DECL {

		for (final Token t : $v)
			this.protectedURIPatterns.add(t.getText());
	}
	;

publicPagesDeclaration
	: BEGIN_DECL_PUBLIC_PAGES
		v+=DECL_VALUE (DECL_COMMA v+=DECL_VALUE)* END_DECL {

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
	: BEGIN_DECL_CONTROLLER_PACKAGES
		v+=DECL_VALUE (DECL_COMMA v+=DECL_VALUE)* END_DECL {

		this.controllerPackages.clear();
		for (final Token t : $v)
			this.controllerPackages.add(t.getText());
	}
	;

entityPackagesDeclaration
	: BEGIN_DECL_ENTITY_PACKAGES
		v+=DECL_VALUE (DECL_COMMA v+=DECL_VALUE)* END_DECL {

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
locals [String viewIdPattern, Object controllerObj]
	: ROUTE_ID? URI_PATTERN MAPPING_MODE?
		controller? routeScript=script[true]?
		MAPPING_ARROW view viewScript=script[false]? {

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
		if ($ctx.routeScript != null) {
			System.out.println("    - commonScript=" + $routeScript.scriptObj +
				" [" + $routeScript.text + "]");
		}
		System.out.println("    - controller=[" + $controllerObj + "]");
		System.out.println("    - viewIdPattern=[" + $viewIdPattern + "]");
		if ($ctx.viewScript != null) {
			System.out.println("    - viewScript=" + $viewScript.scriptObj +
				" [" + $viewScript.text + "]");
		}
	}
	;

controller
locals [List<Class<?>> argTypes, List<Object> argValues]
@init {
	$argTypes = new ArrayList<>();
	$argValues = new ArrayList<>();
}
	: MAPPING_CONTROLLER_NAME
		(MAPPING_LPAREN controllerArgs CTRL_ARGS_RPAREN)? {

		Class<?> controllerClass =
			this.findClass($MAPPING_CONTROLLER_NAME.text,
			this.controllerPackages);
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


script[boolean routeScript]
returns [SequenceScript scriptObj]
@init {
	$scriptObj = new SequenceScript();
}
	: BEGIN_SCRIPT (scriptStatement[$routeScript])* END_SCRIPT
	;

scriptStatement[boolean routeScript]
	: scriptConditionalConstruct[$routeScript]
	| scriptAssignStatement
	| {$routeScript}? scriptAbortStatement
	| {$routeScript}? scriptForbidStatement
	;

scriptConditionalConstruct[boolean routeScript]
	: SCRIPT_KW_IF scriptCondition ifScript=script[$routeScript]
		(SCRIPT_KW_ELSE elseScript=script[$routeScript])? {

		$script::scriptObj.addSubscript(
			new ConditionalStatement(
				$scriptCondition.conditionObj,
				$ifScript.scriptObj,
				($ctx.elseScript != null ? $elseScript.scriptObj : null)
			)
		);
	}
	;

scriptAbortStatement
	: SCRIPT_KW_ABORT k=(SCRIPT_KW_IF | SCRIPT_KW_UNLESS) scriptCondition {

		$script::scriptObj.addSubscript(
			new AbortStatement(
				$k.text.equals("if"),
				$scriptCondition.conditionObj
			)
		);
	}
	;

scriptForbidStatement
	: SCRIPT_KW_FORBID k=(SCRIPT_KW_IF | SCRIPT_KW_UNLESS) scriptCondition {

		$script::scriptObj.addSubscript(
			new ForbidStatement(
				$k.text.equals("if"),
				$scriptCondition.conditionObj
			)
		);
	}
	;

scriptAssignStatement
	: SCRIPT_NAME SCRIPT_ASSIGN scriptValueExpr {

		$script::scriptObj.addSubscript(
			new AssignStatement(
				$SCRIPT_NAME.text,
				$scriptValueExpr.valueExprObj
			)
		);
	}
	;

scriptCondition
returns [Condition conditionObj]
	: SCRIPT_LPAREN scriptConditionExpr SCRIPT_RPAREN {
		$conditionObj = $scriptConditionExpr.conditionObj;
	}
	;

scriptConditionExpr
returns [Condition conditionObj]
	: SCRIPT_OP_NOT<assoc=right> c11=scriptConditionExpr {
		$conditionObj =
			new NotCondition($c11.conditionObj);
	}
	| c21=scriptConditionExpr SCRIPT_OP_AND c22=scriptConditionExpr {
		$conditionObj =
			new AndCondition($c21.conditionObj, $c22.conditionObj);
	}
	| c31=scriptConditionExpr SCRIPT_OP_OR c32=scriptConditionExpr {
		$conditionObj =
			new OrCondition($c31.conditionObj, $c32.conditionObj);
	}
	| v41=scriptValueExpr SCRIPT_OP_EQ v42=scriptValueExpr {
		$conditionObj =
			new EqualsCondition($v41.valueExprObj, $v42.valueExprObj);
	}
	| v51=scriptValueExpr SCRIPT_OP_NE v52=scriptValueExpr {
		$conditionObj =
			new NotEqualsCondition($v51.valueExprObj, $v52.valueExprObj);
	}
	| v6=scriptValueExpr {
		$conditionObj =
			new ValueCondition($v6.valueExprObj);
	}
	| m7=(SCRIPT_COND_GET | SCRIPT_COND_POST | SCRIPT_COND_DELETE) {
		$conditionObj =
			new RequestMethodCondition($m7.text);
	}
	| SCRIPT_LPAREN c8=scriptConditionExpr SCRIPT_RPAREN {
		$conditionObj = $c8.conditionObj;
	}
	;

scriptValueExpr
returns [ValueExpression valueExprObj]
	: scriptPersistencyExpr {
		$valueExprObj = $scriptPersistencyExpr.valueExprObj;
	}
	| scriptQName {
		$valueExprObj = new ModelReferenceValueExpression($scriptQName.text);
	}
	| scriptLiteral {
		$valueExprObj = $scriptLiteral.valueExprObj;
	}
	;

scriptPersistencyExpr
returns [ValueExpression valueExprObj]
	: SCRIPT_KW_NEW e1=scriptEntity {
		$valueExprObj = new NewEntityValueExpression($e1.entityClass);
	}
	| SCRIPT_KW_REF e2=scriptEntity
		SCRIPT_LPAREN v2=scriptValueExpr SCRIPT_RPAREN {
		$valueExprObj =
			new EntityRefValueExpression($e2.entityClass, $v2.valueExprObj);
	}
	| e3=scriptEntity SCRIPT_LPAREN v3=scriptValueExpr SCRIPT_RPAREN {
		$valueExprObj =
			new EntityValueExpression($e3.entityClass, $v3.valueExprObj);
	}
	| scriptQuery {
		$valueExprObj = $scriptQuery.valueExprObj;
	}
	;

scriptQuery
returns [EntityQueryValueExpression valueExprObj]
locals [List<EntityQueryTweak> tweaks]
@init {
	$tweaks = new ArrayList<>();
}
	: scriptEntity SCRIPT_COLON scriptQueryName=scriptQName
		SCRIPT_LPAREN scriptQueryParams SCRIPT_RPAREN
		scriptQueryOp* (SCRIPT_DOT SCRIPT_QOP_LIST)? {

		$valueExprObj = new EntityQueryValueExpression(
			$scriptEntity.entityClass,
			$scriptQueryName.text,
			$tweaks.toArray(new EntityQueryTweak[$tweaks.size()]),
			($SCRIPT_QOP_LIST != null)
		);
	}
	;

scriptQueryParams
	: scriptQueryNamedParams
	| scriptQueryPosParams
	|
	;

scriptQueryNamedParams
	: scriptQueryNamedParam (SCRIPT_COMMA scriptQueryNamedParam)*
	;

scriptQueryNamedParam
	: SCRIPT_NAME SCRIPT_COLON scriptValueExpr {

		$scriptQuery::tweaks.add(
			new NamedParamEntityQueryTweak(
				$SCRIPT_NAME.text,
				$scriptValueExpr.valueExprObj
			)
		);
	}
	;

scriptQueryPosParams
locals [int nextParamPos]
@init {
	$nextParamPos = 1;
}
	: scriptQueryPosParam { $nextParamPos++; }
		(SCRIPT_COMMA scriptQueryPosParam { $nextParamPos++; })*
	;

scriptQueryPosParam
	: scriptValueExpr {

		$scriptQuery::tweaks.add(
			new PositionalParamEntityQueryTweak(
				$scriptQueryPosParams::nextParamPos,
				$scriptValueExpr.valueExprObj
			)
		);
	}
	;

scriptQueryOp
	: SCRIPT_DOT SCRIPT_QOP_MAXRESULTS
		SCRIPT_LPAREN v1=scriptValueExpr SCRIPT_RPAREN {

		$scriptQuery::tweaks.add(
			new MaxResultsEntityQueryTweak($v1.valueExprObj)
		);
	}
	| SCRIPT_DOT SCRIPT_QOP_FIRSTRESULT
		SCRIPT_LPAREN v2=scriptValueExpr SCRIPT_RPAREN {

		$scriptQuery::tweaks.add(
			new FirstResultEntityQueryTweak($v2.valueExprObj)
		);
	}
	;

scriptEntity
returns [Class<?> entityClass]
	: scriptQName {

		$entityClass = this.findClass($scriptQName.text, this.entityPackages);
		if ($entityClass == null)
			throw new InvalidRoutesException(
				"Unknown entity at line " + $start.getLine() + ".");
	}
	;

scriptQName
	: SCRIPT_NAME (SCRIPT_DOT SCRIPT_NAME)*
	;

scriptLiteral
returns [ValueExpression valueExprObj]
	: LIT_STRING {
		$valueExprObj = new LiteralValueExpression(
			this.literalValue($LIT_STRING.text)
		);
	}
	| SCRIPT_LIT_BOOL {
		$valueExprObj = new LiteralValueExpression(
			Boolean.valueOf($SCRIPT_LIT_BOOL.text)
		);
	}
	| SCRIPT_LIT_LONG {
		$valueExprObj = new LiteralValueExpression(
			Long.valueOf($SCRIPT_LIT_LONG.text)
		);
	}
	| SCRIPT_LIT_INT {
		$valueExprObj = new LiteralValueExpression(
			Integer.valueOf($SCRIPT_LIT_INT.text)
		);
	}
	| SCRIPT_LIT_DOUBLE {
		$valueExprObj = new LiteralValueExpression(
			Double.valueOf($SCRIPT_LIT_DOUBLE.text)
		);
	}
	| SCRIPT_LIT_FLOAT {
		$valueExprObj = new LiteralValueExpression(
			Float.valueOf($SCRIPT_LIT_FLOAT.text)
		);
	}
	| SCRIPT_LIT_DECIMAL {
		$valueExprObj = new LiteralValueExpression(
			new BigDecimal($SCRIPT_LIT_DECIMAL.text)
		);
	}
	;
