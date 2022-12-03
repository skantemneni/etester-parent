package com.etester.data.domain.util.email;

/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2012, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.etester.data.domain.user.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotifierService implements WebuserNotifier {

	public static final String DEFAULT_MAIL_FROM_ADDRESS = "contactus@etester.com";
	public static final String DEFAULT_NEW_WEBUSER_SUBJECT = "eTester.com - Verify your user account.";
	public static final String DEFAULT_RESET_PASSWORD_SUBJECT = "eTester.com - Confirm your new password.";
	public static final String DEFAULT_NEW_CHANNEL_ACTIVATION_CODE_SUBJECT = "eTester.com - Channel Activation Code";

	
	//	public static final String DEFAULT_MESSAGE_SUBMITTED_SENDER = "contact.etester@gmail.com";
	// cause "com.sun.mail.smtp.SMTPSendFailedException: 554 Message rejected: Email address is not verified. The following identities failed the check in region US-EAST-1: contact.etester@gmail.com"
	public static final String DEFAULT_MESSAGE_SUBMITTED_SENDER = "contactus@etester.com";
	
	public static final String DEFAULT_MESSAGE_SUBMITTED_SUBJECT = "eTester.com MESSAGE - ";
	
    @Autowired 
    private JavaMailSender mailSender;
    
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired 
    private TemplateEngine templateEngine;

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /** 
     * Send notifyResetPassword HTML mail 
     */
    @Override
    public void notifyResetPassword (final String accountOwnerName, final String userName, final String recipientEmail, final String activationLink, final Locale locale) {
        // Prepare the evaluation context
        final Context ctx = new Context(locale);
        ctx.setVariable("accountOwnerName", accountOwnerName == null ? "User" : accountOwnerName);
        ctx.setVariable("userName", userName == null ? "User" : userName);
        ctx.setVariable("resetDate", new Date());
        ctx.setVariable("recipientEmail", recipientEmail);
        ctx.setVariable("activationLink", activationLink);
        
        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        
        try {
        	message.setSubject(DEFAULT_RESET_PASSWORD_SUBJECT);
        	message.setFrom(DEFAULT_MAIL_FROM_ADDRESS);
        	message.setTo(recipientEmail);

        	// Create the HTML body using Thymeleaf
        	final String htmlContent = this.templateEngine.process("passwordReset.html", ctx);
        	message.setText(htmlContent, true /* isHtml */);
        
            // Send email
            this.mailSender.send(mimeMessage);
        } catch (MailException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        } catch (MessagingException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        }
    }

    /** 
     * Send notifyResetPassword HTML mail 
     */
    @Override
    public void notifyNewWebuser (final String accountOwnerName, final String userName, final String recipientEmail, final String activationLink, final Locale locale) {
        // Prepare the evaluation context
        final Context ctx = new Context(locale);
        ctx.setVariable("accountOwnerName", accountOwnerName == null ? "User" : accountOwnerName);
        ctx.setVariable("userName", userName == null ? "User" : userName);
        ctx.setVariable("creationDate", new Date());
        ctx.setVariable("recipientEmail", recipientEmail);
        ctx.setVariable("activationLink", activationLink);
        
        
        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        
        try {
        	message.setSubject(DEFAULT_NEW_WEBUSER_SUBJECT);
        	message.setFrom(DEFAULT_MAIL_FROM_ADDRESS);
        	message.setTo(recipientEmail);

        	// Create the HTML body using Thymeleaf
        	final String htmlContent = this.templateEngine.process("newWebuser.html", ctx);
        	message.setText(htmlContent, true /* isHtml */);
        
            // Send email
            this.mailSender.send(mimeMessage);
        } catch (MailException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        } catch (MessagingException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        }
    }

    
    
    
    /* 
     * Send HTML mail with attachment. 
     */
    public void sendMailWithAttachment(
            final String recipientName, final String recipientEmail, final String attachmentFileName, 
            final byte[] attachmentBytes, final String attachmentContentType, final Locale locale) 
            throws MessagingException {
        
        // Prepare the evaluation context
        final Context ctx = new Context(locale);
        ctx.setVariable("name", recipientName);
        ctx.setVariable("subscriptionDate", new Date());
        ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
        
        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = 
                new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
        message.setSubject("Example HTML email with attachment");
        message.setFrom(DEFAULT_MAIL_FROM_ADDRESS);
        message.setTo(recipientEmail);

        // Create the HTML body using Thymeleaf
        final String htmlContent = this.templateEngine.process("email-withattachment.html", ctx);
        message.setText(htmlContent, true /* isHtml */);
        
        // Add the attachment
        final InputStreamSource attachmentSource = new ByteArrayResource(attachmentBytes);
        message.addAttachment(
                attachmentFileName, attachmentSource, attachmentContentType);
        
        // Send mail
        this.mailSender.send(mimeMessage);
        
    }

    
    
    /* 
     * Send HTML mail with inline image
     */
    public void sendMailWithInline(
            final String recipientName, final String recipientEmail, final String imageResourceName, 
            final byte[] imageBytes, final String imageContentType, final Locale locale)
            throws MessagingException {
        
        // Prepare the evaluation context
        final Context ctx = new Context(locale);
        ctx.setVariable("name", recipientName);
        ctx.setVariable("subscriptionDate", new Date());
        ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
        ctx.setVariable("imageResourceName", imageResourceName); // so that we can reference it from HTML
        
        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = 
                new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
        message.setSubject("Example HTML email with inline image");
        message.setFrom(DEFAULT_MAIL_FROM_ADDRESS);
        message.setTo(recipientEmail);

        // Create the HTML body using Thymeleaf
        final String htmlContent = this.templateEngine.process("email-inlineimage.html", ctx);
        message.setText(htmlContent, true /* isHtml */);
        
        // Add the inline image, referenced from the HTML code as "cid:${imageResourceName}"
        final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
        message.addInline(imageResourceName, imageSource, imageContentType);
        
        // Send mail
        this.mailSender.send(mimeMessage);
        
    }

	public void notifyChannelRedumptionCode(String emailAddress,
			String redumptionCode, String firstName, String lastName,
			String purchaserMessage) {
        // Prepare the evaluation context
		String name = (firstName == null ? "" : (firstName + " ")) + (lastName == null ? "" : (lastName + " "));
		if (name == null || name.trim().length() == 0) {
			name = "User";
		}
        final Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("name", name);
        ctx.setVariable("recipientEmail", emailAddress);
        ctx.setVariable("redumptionCode", redumptionCode);
        ctx.setVariable("purchaserMessage", purchaserMessage);
        ctx.setVariable("activationLink", "http://etester.com");
        
        
        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        
        try {
        	message.setSubject(DEFAULT_NEW_CHANNEL_ACTIVATION_CODE_SUBJECT);
        	message.setFrom(DEFAULT_MAIL_FROM_ADDRESS);
        	message.setTo(emailAddress);

        	// Create the HTML body using Thymeleaf
        	final String htmlContent = this.templateEngine.process("channelActivationCode.html", ctx);
        	System.out.println("htmlContent: \n" + htmlContent);
        	message.setText(htmlContent, true /* isHtml */);
        
            // Send email
            this.mailSender.send(mimeMessage);
        } catch (MailException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        } catch (MessagingException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        }
	}

	public void processSubmittedEmail(SendEmailData emailPayload, String recepientEmailAddresses) {
        final Context ctx = new Context(Locale.ENGLISH);
        // first validate we have the recepientEmailAddresses set for this site....(may not be set in dev/test environments)
        if (recepientEmailAddresses == null || recepientEmailAddresses.trim().length() == 0) {
        	return;
        }
//        ctx.setVariable("name", name);
//        ctx.setVariable("recipientEmail", "sesi.kantemneni@gmail.com");
        ctx.setVariable("username", emailPayload.getUsername());
        ctx.setVariable("email", emailPayload.getEmail());
        ctx.setVariable("phone", emailPayload.getPhone());
        ctx.setVariable("subject", emailPayload.getSubject());
        ctx.setVariable("message", emailPayload.getMessage());
        ctx.setVariable("problemTypeCode", emailPayload.getProblemTypeCode());
        ctx.setVariable("problemTypeDescription", emailPayload.getProblemTypeDescription());

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        
        try {
        	message.setSubject(DEFAULT_MESSAGE_SUBMITTED_SUBJECT + emailPayload.getSubject());
        	message.setFrom(DEFAULT_MESSAGE_SUBMITTED_SENDER);
        	// message.setTo parses the "comma-seperated-list" of recepientEmailAddresses...no need for us to do the same...
        	InternetAddress[] parsedRecepientEmailAddresses = InternetAddress.parse(recepientEmailAddresses);
        	if (parsedRecepientEmailAddresses == null || parsedRecepientEmailAddresses.length == 0) {
        		throw new MailParseException("Incorrect Email Address: '" + recepientEmailAddresses + "'");
        	}
        	message.setTo(parsedRecepientEmailAddresses);

        	// Create the HTML body using Thymeleaf
        	final String htmlContent = this.templateEngine.process("email-from-web-general.html", ctx);
        	System.out.println("htmlContent: \n" + htmlContent);
        	message.setText(htmlContent, true /* isHtml */);
        
            // Send email
            this.mailSender.send(mimeMessage);
        } catch (MailException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        } catch (MessagingException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        }
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// User Alert Messages
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String DEFAULT_ALERT_MESSAGE_SUBJECT = "eTester.com - Alert.";

	public void sendEmailMessageAlert(User user, String heading, String content, String link, String alertPriorityString, Locale locale) {
		if (user == null) {
        	System.out.println ("User information Missing.  No message sent.");
        	return;
		}
        // Prepare the evaluation context
        final Context ctx = new Context(locale);
        String studentName = user.getFirstName() + " " + user.getLastName();
        String recipientEmail = user.getEmailAddress();
        if (recipientEmail != null) {
        	ctx.setVariable("studentName", studentName == null ? "Student" : studentName);
        	ctx.setVariable("messageHeading", heading == null ? "Message Heading" : heading);
        	ctx.setVariable("messageContent", content == null ? "Message Content" : content);
            ctx.setVariable("messageCreationDate", new Date());
        	ctx.setVariable("additionalInfoLink", link);
        	ctx.setVariable("alertPriorityString", alertPriorityString);
        
        	// Prepare message using a Spring helper
        	final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        	final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        
        	try {
        		message.setSubject(DEFAULT_ALERT_MESSAGE_SUBJECT);
        		message.setFrom(DEFAULT_MAIL_FROM_ADDRESS);
        		message.setTo(recipientEmail);

        		// Create the HTML body using Thymeleaf
        		final String htmlContent = this.templateEngine.process("email-alert.html", ctx);
        		message.setText(htmlContent, true /* isHtml */);
        
        		// Send email
        		this.mailSender.send(mimeMessage);
        	} catch (MailException me) {
        		System.out.println ("Error sending email: " + me.getMessage());
        		me.printStackTrace();
        	} catch (MessagingException me) {
        		System.out.println ("Error sending email: " + me.getMessage());
        		me.printStackTrace();
        	}
        } else {
        	System.out.println ("User '" + user.getUsername() + "' does not have an Email Address");
        }
        
	}

}
