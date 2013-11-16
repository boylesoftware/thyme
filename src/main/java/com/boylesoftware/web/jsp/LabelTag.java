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


/**
 * Handler of the JSP tag for an input field label. Used inside a
 * {@link FormTag}.
 *
 * @author Lev Himmelfarb
 */
public class LabelTag
	extends TagSupport {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Input field name.
	 */
	private String name;


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


	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@SuppressWarnings("resource")
	@Override
	public int doStartTag()
		throws JspException {

		final FormTag formTag =
			(FormTag) findAncestorWithClass(this, FormTag.class);

		final JspWriter out = this.pageContext.getOut();
		try {
			out.print("<label");

			out.print(" for=\"");
			out.print(formTag.getId());
			out.print("_");
			out.print(this.name);
			out.print("\"");

			out.print(">");

		} catch (final IOException e) {
			throw new JspException(e);
		}

		return EVAL_BODY_INCLUDE;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	@SuppressWarnings("resource")
	@Override
	public int doEndTag()
		throws JspException {

		final JspWriter out = this.pageContext.getOut();
		try {
			out.print("</label>");
		} catch (final IOException e) {
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}
}
