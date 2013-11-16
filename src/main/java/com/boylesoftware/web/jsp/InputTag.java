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

import com.boylesoftware.web.util.EntityUtils;


/**
 * Handler of the JSP tag for an input field in a form. Used inside a
 * {@link FormTag}.
 *
 * @author Lev Himmelfarb
 */
public class InputTag
	extends AbstractInputTag {

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
	private String type = "text";


	/**
	 * Get "type" attribute.
	 *
	 * @return Attribute value.
	 */
	public String getType() {

		return this.type;
	}

	/**
	 * Set "type" attribute. Default is "text".
	 *
	 * @param type Attribute value.
	 */
	public void setType(String type) {

		this.type = type;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.jsp.AbstractInputTag#doStartTag()
	 */
	@SuppressWarnings("resource")
	@Override
	public int doStartTag()
		throws JspException {

		super.doStartTag();

		int res = SKIP_BODY;

		final JspWriter out = this.pageContext.getOut();
		try {
			switch (this.type) {

			case "textarea":
				out.print("<textarea");
				this.printCommonAttrs(out, true);
				out.print(">");
				if (this.inputValue != null)
					out.print(Utils.escapeHtml(this.inputValue));
				out.print("</textarea>");
				break;

			case "checkbox":
				out.print("<input type=\"checkbox\"");
				this.printCommonAttrs(out, false);
				out.print(" value=\"true\"");
				if ("true".equals(this.inputValue))
					out.print(" checked=\"checked\"");
				out.print("/>");
				break;

			case "password":
				out.print("<input type=\"");
				out.print(this.type);
				out.print("\"");
				this.printCommonAttrs(out, true);
				out.print("/>");
				break;

			case "select":
				out.print("<select");
				this.printCommonAttrs(out, true);
				out.print(">");
				res = EVAL_BODY_INCLUDE;
				break;

			case "radios":
				res = EVAL_BODY_INCLUDE;
				break;

			default:
				out.print("<input type=\"");
				out.print(this.type);
				out.print("\"");
				this.printCommonAttrs(out, true);
				out.print(" value=\"");
				if (this.inputValue != null)
					out.print(Utils.escapeHtmlAttr(this.inputValue));
				out.print("\"");
				out.print("/>");
			}

		} catch (final IOException e) {
			throw new JspException(e);
		}

		if (res != SKIP_BODY)
			this.pageContext.setAttribute("inputValue", this.inputValue);

		return res;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	@SuppressWarnings("resource")
	@Override
	public int doEndTag()
		throws JspException {

		if (this.type.equals("select")) {
			final JspWriter out = this.pageContext.getOut();
			try {
				out.print("</select>");
			} catch (final IOException e) {
				throw new JspException(e);
			}
		}

		return EVAL_PAGE;
	}


	/* (non-Javadoc)
	 * @see com.boylesoftware.web.jsp.AbstractInputTag#printCommonAttrs(javax.servlet.jsp.JspWriter, boolean)
	 */
	@Override
	protected void printCommonAttrs(final JspWriter out,
			final boolean checkRequired)
		throws IOException {

		super.printCommonAttrs(out, checkRequired);

		if ((this.beanClass != null) && VARLEN_TYPES.contains(this.type)) {
			final int colLen =
				EntityUtils.getColumnLength(this.beanClass, this.beanField);
			if (colLen < Integer.MAX_VALUE) {
				out.print(" maxlength=\"");
				out.print(colLen);
				out.print("\"");
			}
		}
	}
}
