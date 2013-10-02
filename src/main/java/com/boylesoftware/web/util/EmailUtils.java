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
package com.boylesoftware.web.util;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.boylesoftware.web.util.pool.PooledStringBuffer;
import com.boylesoftware.web.util.pool.StringBufferPool;


/**
 * Utility methods for handling e-mails.
 *
 * @author Lev Himmelfarb
 */
public final class EmailUtils {

	/**
	 * Pattern for template parameter placeholders.
	 */
	private static final Pattern TMPL_PARAM_PATTERN =
		Pattern.compile("\\$\\{([^}]+)\\}");


	/**
	 * All methods are static.
	 */
	private EmailUtils() {}


	/**
	 * Send an e-mail. The message body and subject are taken from the provided
	 * resource bundle. The following keys are used:
	 *
	 * <dl>
	 *
	 * <dt>email.&lt;tmplKey&gt;.subject</dt>
	 * <dd>The message subject.</dd>
	 *
	 * <dt>email.&lt;tmplKey&gt;.body.plain</dt>
	 * <dd>Template for plain text body.</dd>
	 *
	 * <dt>email.&lt;tmplKey&gt;.body.html</dt>
	 * <dd>Template for HTML body.</dd>
	 *
	 * <dl>
	 *
	 * The body templates may contain parameter placeholders, which are
	 * parameter names enclosed in <code>${...}</code>. The placeholders are replaced
	 * with values from the provided {@code params} map.
	 *
	 * @param mailSession JavaMail session to use.
	 * @param email Reciepient's e-mail address.
	 * @param resources Resource bundle containing the message template.
	 * @param tmplKey Message template key.
	 * @param params Parameters for the message body template. Cannot be
	 * {@code null}, but can be empty.
	 *
	 * @throws MessagingException If an error happens sending the e-mail.
	 */
	public static void sendEmail(final Session mailSession, final String email,
			final ResourceBundle resources, final String tmplKey,
			final Map<String, String> params)
		throws MessagingException {

		// build the message
		final MimeMessage msg = new MimeMessage(mailSession);
		final Multipart msgParts = new MimeMultipart("alternative");
		BodyPart msgPart = new MimeBodyPart();
		msgPart.setText(processTemplate(
				resources.getString("email." + tmplKey + ".body.plain"),
				params));
		msgParts.addBodyPart(msgPart);
		msgPart = new MimeBodyPart();
		msgPart.setContent(processTemplate(
				resources.getString("email." + tmplKey + ".body.html"), params),
				"text/html");
		msgParts.addBodyPart(msgPart);
		msg.setContent(msgParts);
		msg.setSubject(resources.getString("email." + tmplKey + ".subject"));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

		// send message
		Transport.send(msg);
	}

	/**
	 * Process the provided template by replacing all occurrences of
	 * ${paramName} constructs in it with values from the specified parameters
	 * map and return the resulting text.
	 *
	 * @param tmpl The template text.
	 * @param params Template parameters.
	 *
	 * @return Text generated from the template.
	 */
	private static String processTemplate(final String tmpl,
			final Map<String, String> params) {

		try (final PooledStringBuffer buf = StringBufferPool.get()) {
			final StringBuffer res = buf.getStringBuffer();

			final Matcher m = buf.getMatcher(TMPL_PARAM_PATTERN, tmpl);
			while (m.find()) {
				final String repl = params.get(m.group(1));
				m.appendReplacement(res,
						(repl != null ? Matcher.quoteReplacement(repl) : ""));
			}
			m.appendTail(res);

			return res.toString();
		}
	}
}
