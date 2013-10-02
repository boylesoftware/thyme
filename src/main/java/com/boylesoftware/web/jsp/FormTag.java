/*
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
package com.boylesoftware.web.jsp;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.boylesoftware.web.UserInputError;
import com.boylesoftware.web.api.Attributes;
import com.boylesoftware.web.api.UserInputErrors;


/**
 * Handler of the JSP tag used to create HTML forms.
 *
 * @author Lev Himmelfarb
 */
public class FormTag
	extends BodyTagSupport {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Form submission URL.
	 */
	private String action;

	/**
	 * Form submission HTTP method.
	 */
	private String method;

	/**
	 * Name of the form field for the initial focus.
	 */
	private String focus;

	/**
	 * Entity bean behind the form, if any.
	 */
	private Object bean;

	/**
	 * Tells if HTML5 needs to be generated.
	 */
	private boolean html5 = true;


	/**
	 * Get "action" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getAction() {

		return this.action;
	}

	/**
	 * Set "action" attribute. The default is nothing.
	 *
	 * @param action Attribute value.
	 */
	public void setAction(final String action) {

		this.action = action;
	}

	/**
	 * Get "method" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getMethod() {

		return this.method;
	}

	/**
	 * Set "method" attribute. The default is "post".
	 *
	 * @param method Attribute value.
	 */
	public void setMethod(final String method) {

		this.method = method;
	}

	/**
	 * Get "focus" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getFocus() {

		return this.focus;
	}

	/**
	 * Set "focus" attribute.
	 *
	 * @param focus Attribute value.
	 */
	public void setFocus(final String focus) {

		this.focus = focus;
	}

	/**
	 * Get "bean" attribute.
	 *
	 * @return Attribute value.
	 */
	public Object getBean() {

		return this.bean;
	}

	/**
	 * Set "bean" attribute.
	 *
	 * @param bean Attribute value.
	 */
	public void setBean(final Object bean) {

		this.bean = bean;
	}

	/**
	 * Get "html5" attribute.
	 *
	 * @return Attribute value.
	 */
	public boolean isHtml5() {

		return this.html5;
	}

	/**
	 * Set "html5" attribute.
	 *
	 * @param html5 Attribute value.
	 */
	public void setHtml5(boolean html5) {

		this.html5 = html5;
	}


	/**
	 * Focus field name.
	 */
	private String focusFieldName;


	/**
	 * Get focus field name.
	 *
	 * @return Focus field name, or {@code null}.
	 */
	String getFocusFieldName() {

		return this.focusFieldName;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 */
	@SuppressWarnings("resource")
	@Override
	public int doStartTag()
		throws JspException {

		final JspWriter out = this.pageContext.getOut();
		try {
			out.print("<form");

			out.print(" id=\"");
			out.print(this.id);
			out.print("\"");

			out.print(" name=\"");
			out.print(this.id);
			out.print("\"");

			if (this.action != null) {
				out.print(" action=\"");
				out.print(this.action);
				out.print("\"");
			}

			out.print(" method=\"");
			if (this.method != null)
				out.print(this.method);
			else
				out.print("post");
			out.print("\"");

			out.print(">");

		} catch (final IOException e) {
			throw new JspException(e);
		}

		this.focusFieldName = null;
		final UserInputErrors errors =
			(UserInputErrors) this.pageContext.getAttribute(
					Attributes.USER_INPUT_ERRORS, PageContext.REQUEST_SCOPE);
		if (errors != null) {
			final UserInputError error = errors.getFirstFieldError();
			if (error != null)
				this.focusFieldName = error.getFieldName();
		}
		if (this.focusFieldName == null)
			this.focusFieldName = this.focus;

		return EVAL_BODY_INCLUDE;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	@SuppressWarnings("resource")
	@Override
	public int doEndTag()
		throws JspException {

		final JspWriter out = this.pageContext.getOut();
		try {
			out.print("</form>");

			if (!this.html5) {
				if (this.focusFieldName != null) {
					out.println("\n<script type=\"text/javascript\">");
					out.print("var field = document.forms[\"");
					out.print(this.id);
					out.print("\"][\"");
					out.print(this.focusFieldName);
					out.println("\"];");
					out.println("field.focus();");
					out.print("if ((field.type == \"text\"");
					out.print(" || field.type == \"email\"");
					out.print(" || field.type == \"number\"");
					out.print(" || field.type == \"search\"");
					out.print(" || field.type == \"tel\"");
					out.print(" || field.type == \"url\")");
					out.println(" && field.value != \"\")");
					out.println("field.select();");
					out.print("</script>");
				}
			}

		} catch (final IOException e) {
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}
}
