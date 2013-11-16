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
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import javax.el.ValueExpression;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Handler of the JSP tag used to render list of input options in an
 * {@link InputTag} with type "select" or "radios".
 *
 * @author Lev Himmelfarb
 */
public class OptionsTag
	extends TagSupport {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Parent input type.
	 */
	private static enum InputType {

		/**
		 * Select drop-down.
		 */
		SELECT,

		/**
		 * Radio buttons group.
		 */
		RADIOS
	}


	/**
	 * The options.
	 */
	private Object options;

	/**
	 * Option value expression.
	 */
	private ValueExpression optionValueExpr;

	/**
	 * Option label expression.
	 */
	private ValueExpression optionLabelExpr;


	/**
	 * Get "options" attribute.
	 *
	 * @return Attribute value.
	 */
	public Object getOptions() {

		return this.options;
	}

	/**
	 * Set "options" attribute.
	 *
	 * @param options Attribute value.
	 */
	public void setOptions(final Object options) {

		this.options = options;
	}

	/**
	 * Get "optionValueExpr" attribute.
	 *
	 * @return Attribute value.
	 */
	public ValueExpression getOptionValueExpr() {

		return this.optionValueExpr;
	}

	/**
	 * Set "optionValueExpr" attribute.
	 *
	 * @param optionValueExpr Attribute value.
	 */
	public void setOptionValueExpr(final ValueExpression optionValueExpr) {

		this.optionValueExpr = optionValueExpr;
	}

	/**
	 * Get "optionLabelExpr" attribute.
	 *
	 * @return Attribute value.
	 */
	public ValueExpression getOptionLabelExpr() {

		return this.optionLabelExpr;
	}

	/**
	 * Set "optionLabelExpr" attribute.
	 *
	 * @param optionLabelExpr Attribute value.
	 */
	public void setOptionLabelExpr(final ValueExpression optionLabelExpr) {

		this.optionLabelExpr = optionLabelExpr;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@SuppressWarnings("resource")
	@Override
	public int doStartTag()
		throws JspException {

		if (this.options == null)
			return SKIP_BODY;

		final InputTag inputTag =
			(InputTag) findAncestorWithClass(this, InputTag.class);
		final InputType inputType =
			(inputTag.getType().equals("radios") ? InputType.RADIOS :
				InputType.SELECT);

		final JspWriter out = this.pageContext.getOut();
		try {
			if (this.options instanceof Collection) {
				int optionInd = 0;
				for (final Object option : (Collection<?>) this.options)
					this.printOption(out, inputType, inputTag, option,
							optionInd++);
			} else if (this.options instanceof Map) {
				int optionInd = 0;
				for (final Map.Entry<?, ?> option :
					((Map<?, ?>) this.options).entrySet())
					this.printOption(out, inputType, inputTag, option,
							optionInd++);
			} else if (this.options.getClass().isArray()) {
				final int len = Array.getLength(this.options);
				for (int optionInd = 0; optionInd < len; optionInd++)
					this.printOption(out, inputType, inputTag,
							Array.get(this.options, optionInd), optionInd);
			} else {
				this.printOption(out, inputType, inputTag, this.options, 0);
			}
		} catch (final IOException e) {
			throw new JspException(e);
		}

		return SKIP_BODY;
	}


	/**
	 * Print the option element to the JSP output.
	 *
	 * @param out The JSP output.
	 * @param inputType Parent input type.
	 * @param inputTag Parent input tag.
	 * @param option The option object.
	 * @param optionInd Option index (zero-based).
	 *
	 * @throws IOException If an I/O error happens writing to the JSP output.
	 */
	private void printOption(final JspWriter out, final InputType inputType,
			final InputTag inputTag, final Object option, final int optionInd)
		throws IOException {

		this.pageContext.setAttribute("option", option);

		Object optionValue;
		if (this.optionValueExpr != null) {
			optionValue = this.optionValueExpr.getValue(
					this.pageContext.getELContext());
			if (optionValue != null)
				optionValue = optionValue.toString();
		} else if (option instanceof Option) {
			optionValue = ((Option) option).getValue();
		} else if (option instanceof Map.Entry) {
			optionValue = ((Map.Entry<?, ?>) option).getKey();
			if (optionValue != null)
				optionValue = optionValue.toString();
		} else if (option == null) {
			optionValue = null;
		} else {
			optionValue = option.toString();
		}

		Object optionLabel;
		if (this.optionLabelExpr != null) {
			optionLabel = this.optionLabelExpr.getValue(
					this.pageContext.getELContext());
			if (optionLabel != null)
				optionLabel = optionLabel.toString();
		} else if (option instanceof Option) {
			optionLabel = ((Option) option).getLabel();
		} else if (option instanceof Map.Entry) {
			optionLabel = ((Map.Entry<?, ?>) option).getValue();
			if (optionLabel != null)
				optionLabel = optionLabel.toString();
		} else {
			optionLabel = optionValue;
		}

		switch (inputType) {

		case RADIOS:
			out.print("<label><input type=\"radio\"");
			out.print(" id=\"");
			out.print(inputTag.formTag.getId());
			out.print("_");
			out.print(inputTag.inputName);
			out.print("_");
			out.print(optionInd);
			out.print("\"");
			out.print(" name=\"");
			out.print(inputTag.inputName);
			out.print("\"");
			if (inputTag.required)
				out.print(" required=\"required\"");
			out.print(" value=\"");
			if (optionValue != null)
				out.print(Utils.escapeHtmlAttr((String) optionValue));
			out.print("\"");
			if ((inputTag.inputValue != null) &&
					inputTag.inputValue.equals(optionValue))
				out.print(" checked=\"checked\"");
			out.print("/> ");
			if (optionLabel != null)
				out.print(Utils.escapeHtml((String) optionLabel));
			out.print("</label>");
			break;

		default:
			out.print("<option");
			if (optionLabel != optionValue) {
				out.print(" value=\"");
				if (optionValue != null)
					out.print(Utils.escapeHtmlAttr((String) optionValue));
				out.print("\"");
			}
			if ((inputTag.inputValue != null) &&
					inputTag.inputValue.equals(optionValue))
				out.print(" selected=\"selected\"");
			out.print(">");
			if (optionLabel != null)
				out.print(Utils.escapeHtml((String) optionLabel));
			out.print("</option>");
		}
	}
}
