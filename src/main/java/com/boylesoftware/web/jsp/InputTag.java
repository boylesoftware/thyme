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
import javax.servlet.jsp.tagext.TagSupport;

import com.boylesoftware.web.util.EntityUtils;


/**
 * Handler of the JSP tag for an input field in a form. Used inside a
 * {@link FormTag}.
 *
 * @author Lev Himmelfarb
 */
public class InputTag
	extends TagSupport {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Input field types, for which to try to auto-detect the maximum length.
	 */
	private static final java.util.Set<String> VARLEN_TYPES;
	static {
		VARLEN_TYPES = new java.util.HashSet<>(4);
		VARLEN_TYPES.add("text");
		VARLEN_TYPES.add("email");
		VARLEN_TYPES.add("url");
		VARLEN_TYPES.add("textarea");
	}


	/**
	 * Input field type.
	 */
	private String type;

	/**
	 * Input field name.
	 */
	private String name;

	/**
	 * Entity bean behind the input, if any.
	 */
	private Object bean;

	/**
	 * Entity bean field name if different from the input field name.
	 */
	private String beanField;

	/**
	 * If input is required.
	 */
	private String required;


	/**
	 * Get "type" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getType() {

		return this.type;
	}

	/**
	 * Set "type" attribute.
	 *
	 * @param type Attribute value.
	 */
	public void setType(String type) {

		this.type = type;
	}

	/**
	 * Get "name" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getName() {

		return this.name;
	}

	/**
	 * Set "name" attribute.
	 *
	 * @param name Attribute value.
	 */
	public void setName(String name) {

		this.name = name;
	}

	/**
	 * Get "required" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getRequired() {

		return this.required;
	}

	/**
	 * Set "required" attribute. Default is not required.
	 *
	 * @param required Attribute value.
	 */
	public void setRequired(String required) {

		this.required = required;
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
	 * Get "beanField" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getBeanField() {

		return this.beanField;
	}

	/**
	 * Set "beanField" attribute.
	 *
	 * @param beanField Attribute value.
	 */
	public void setBeanField(final String beanField) {

		this.beanField = beanField;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@Override
	public int doStartTag() {

		return SKIP_BODY;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	@SuppressWarnings("resource")
	@Override
	public int doEndTag()
		throws JspException {

		final FormTag formTag =
			(FormTag) findAncestorWithClass(this, FormTag.class);

		final String beanField =
			(this.beanField != null ? this.beanField : this.name);
		Class<?> beanClass = null;
		String val = null;
		if (this.bean == null) {
			final Object formBean = formTag.getBean();
			if (formBean != null)
				beanClass = formBean.getClass();
			val = this.getValue(formBean, beanField);
		} else if (this.bean instanceof String) {
			if (!this.bean.equals("none")) {
				try {
					beanClass = Class.forName((String) this.bean);
					val = this.getValue(null, beanField);
				} catch (final ClassNotFoundException e) {
					throw new JspException(e);
				}
			}
		} else {
			beanClass = this.bean.getClass();
			val = this.getValue(this.bean, beanField);
		}

		final JspWriter out = this.pageContext.getOut();
		try {
			switch (this.type) {

			case "textarea":
				out.print("<textarea");
				this.printAttrs(out, formTag, beanClass, beanField, true);
				out.print(">");
				if (val != null)
					out.print(Utils.escapeHtml(val));
				out.print("</textarea>");
				break;

			case "checkbox":
				out.print("<input type=\"checkbox\"");
				this.printAttrs(out, formTag, beanClass, beanField, false);
				out.print(" value=\"true\"");
				if ("true".equals(val))
					out.print(" checked=\"checked\"");
				out.print("/>");
				break;

			case "password":
				out.print("<input type=\"");
				out.print(this.type);
				out.print("\"");
				this.printAttrs(out, formTag, beanClass, beanField, true);
				out.print("/>");
				break;

			default:
				out.print("<input type=\"");
				out.print(this.type);
				out.print("\"");
				this.printAttrs(out, formTag, beanClass, beanField, true);
				out.print(" value=\"");
				if (val != null)
					out.print(Utils.escapeHtmlAttr(val));
				out.print("\"");
				out.print("/>");
			}

		} catch (final IOException e) {
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}

	/**
	 * Print common attributes.
	 *
	 * @param out The output.
	 * @param formTag The parent form tag.
	 * @param beanClass The backing bean class, or {@code null}.
	 * @param beanField The backing bean field name.
	 * @param checkRequired {@code true} to analyze the bean field and determine
	 * if the input field needs to have a "required" attribute.
	 *
	 * @throws IOException If an I/O error happens writing to the output.
	 */
	private void printAttrs(final JspWriter out, final FormTag formTag,
			final Class<?> beanClass, final String beanField,
			final boolean checkRequired)
		throws IOException {

		out.print(" id=\"");
		out.print(formTag.getId());
		out.print("_");
		out.print(this.name);
		out.print("\"");

		out.print(" name=\"");
		out.print(this.name);
		out.print("\"");

		if (formTag.isHtml5() && this.name.equals(formTag.getFocusFieldName()))
			out.print(" autofocus=\"autofocus\"");

		if ((beanClass != null) && VARLEN_TYPES.contains(this.type)) {
			final int colLen =
				EntityUtils.getColumnLength(beanClass, beanField);
			if (colLen < Integer.MAX_VALUE) {
				out.print(" maxlength=\"");
				out.print(colLen);
				out.print("\"");
			}
		}

		if (Boolean.parseBoolean(this.required) ||
				((this.required == null) && (beanClass != null) &&
					checkRequired &&
						!EntityUtils.isColumnNullable(beanClass, beanField)))
			out.print(" required=\"required\"");
	}

	/**
	 * Get current input field value either from the request parameters or from
	 * the backing bean.
	 *
	 * @param bean The backing bean, or {@code null}.
	 * @param beanField The backing bean field name.
	 *
	 * @return The value, or {@code null}.
	 */
	private String getValue(final Object bean, final String beanField) {

		String val =
			this.pageContext.getRequest().getParameter(this.name);
		if ((val == null) && (bean != null)) {
			final Object valObj =
				EntityUtils.getFieldValue(bean, beanField);
			if (valObj != null)
				val = valObj.toString();
		}

		return val;
	}
}
