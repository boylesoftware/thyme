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

import com.boylesoftware.web.util.BeanUtils;
import com.boylesoftware.web.util.EntityUtils;


/**
 * Abstract parent for form input tags nested in a {@link FormTag}.
 *
 * @author Lev Himmelfarb
 */
public abstract class AbstractInputTag
	extends TagSupport {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Input field name.
	 */
	protected String inputName;

	/**
	 * Entity bean behind the input, if any.
	 */
	protected Object bean;

	/**
	 * Entity bean field name if different from the input field name.
	 */
	private String beanFieldAttr;

	/**
	 * If input is required.
	 */
	private String requiredAttr;


	/**
	 * Get "name" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getName() {

		return this.inputName;
	}

	/**
	 * Set "name" attribute.
	 *
	 * @param name Attribute value.
	 */
	public void setName(final String name) {

		this.inputName = name;
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

		return this.beanFieldAttr;
	}

	/**
	 * Set "beanField" attribute.
	 *
	 * @param beanField Attribute value.
	 */
	public void setBeanField(final String beanField) {

		this.beanFieldAttr = beanField;
	}

	/**
	 * Get "required" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getRequired() {

		return this.requiredAttr;
	}

	/**
	 * Set "required" attribute. Default is "false".
	 *
	 * @param required Attribute value.
	 */
	public void setRequired(final String required) {

		this.requiredAttr = required;
	}


	/**
	 * Reference to the parent form tag. Initialized in the
	 * {@link #doStartTag()} method.
	 */
	protected FormTag formTag;

	/**
	 * Name of the bean field corresponding to the input field, or {@code null}.
	 * Initialized in the {@link #doStartTag()} method.
	 */
	protected String beanField;

	/**
	 * Class of the bean containing the {@link #beanField}, or {@code null}.
	 * Initialized in the {@link #doStartTag()} method.
	 */
	protected Class<?> beanClass;

	/**
	 * Current value of the input field taken from either the backing bean or
	 * the request parameter. Initialized in the {@link #doStartTag()} method.
	 */
	protected String inputValue;

	/**
	 * Tells if the field is required. Initialized in the {@link #doStartTag()}
	 * method.
	 */
	protected boolean required;


	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@Override
	public int doStartTag()
		throws JspException {

		this.formTag = (FormTag) findAncestorWithClass(this, FormTag.class);

		this.beanField =
			(this.beanFieldAttr != null ? this.beanFieldAttr : this.inputName);

		this.beanClass = null;
		this.inputValue = null;
		if (this.bean == null) {
			final Object formBean = this.formTag.getBean();
			if (formBean != null)
				this.beanClass = formBean.getClass();
			this.inputValue = this.getValue(formBean, this.beanField);
		} else if (this.bean instanceof String) {
			if (!this.bean.equals("none")) {
				try {
					this.beanClass = Class.forName((String) this.bean);
					this.inputValue = this.getValue(null, this.beanField);
				} catch (final ClassNotFoundException e) {
					throw new JspException(e);
				}
			}
		} else {
			this.beanClass = this.bean.getClass();
			this.inputValue = this.getValue(this.bean, this.beanField);
		}

		this.required = Boolean.parseBoolean(this.requiredAttr) ||
				((this.requiredAttr == null) && (this.beanClass != null) &&
						!EntityUtils.isColumnNullable(this.beanClass,
								this.beanField));

		return SKIP_BODY;
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
			this.pageContext.getRequest().getParameter(this.inputName);
		if ((val == null) && (bean != null)) {
			final Object valObj =
				BeanUtils.getPropertyValue(bean, beanField);
			if (valObj != null)
				val = valObj.toString();
		}

		return val;
	}

	/**
	 * Print common attributes, including "id", "name", "autofocus" and
	 * "required".
	 *
	 * @param out The output.
	 * @param checkRequired {@code true} to analyze the bean field and determine
	 * if the input field needs to have a "required" attribute.
	 *
	 * @throws IOException If an I/O error happens writing to the output.
	 */
	protected void printCommonAttrs(final JspWriter out,
			final boolean checkRequired)
		throws IOException {

		out.print(" id=\"");
		out.print(this.formTag.getId());
		out.print("_");
		out.print(this.inputName);
		out.print("\"");

		out.print(" name=\"");
		out.print(this.inputName);
		out.print("\"");

		if (this.formTag.isHtml5() &&
				this.inputName.equals(this.formTag.getFocusFieldName()))
			out.print(" autofocus=\"autofocus\"");

		if (checkRequired && this.required)
			out.print(" required=\"required\"");
	}
}
