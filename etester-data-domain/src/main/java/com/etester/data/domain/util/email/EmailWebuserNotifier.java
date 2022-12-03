package com.etester.data.domain.util.email;

import java.util.Locale;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.internet.MimeMessage;


public class EmailWebuserNotifier implements WebuserNotifier {
    private JavaMailSender mailSender;
    private SimpleMailMessage newWebuserMailMessage;
    private SimpleMailMessage resetPasswordMailMessage;

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setNewWebuserMailMessage(SimpleMailMessage newWebuserMailMessage) {
        this.newWebuserMailMessage = newWebuserMailMessage;
    }

    public void setResetPasswordMailMessage(SimpleMailMessage resetPasswordMailMessage) {
        this.resetPasswordMailMessage = resetPasswordMailMessage;
    }

	@Override
	public void notifyNewWebuser(final String accountOwnerName, final String userName,
			final String recipientEmail, final String activationLink, Locale locale) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage)
                throws Exception {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setFrom(newWebuserMailMessage.getFrom());
//                helper.setTo(newWebuserMailMessage.getTo());
                helper.setTo(recipientEmail);
                helper.setSubject(newWebuserMailMessage.getSubject());
                helper.setText(String.format(newWebuserMailMessage.getText(), accountOwnerName, userName, activationLink));

//                ClassPathResource config = new ClassPathResource("beans.xml");
//                helper.addAttachment("beans.xml", config);
            }
        };

        try {
        	mailSender.send(preparator);
        } catch (MailException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        }
	}

//	@Override
//	public int notifyNewWebuser(final String to_email, final String name, final String username, final String link) {
//        MimeMessagePreparator preparator = new MimeMessagePreparator() {
//                public void prepare(MimeMessage mimeMessage)
//                    throws Exception {
//                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//                    helper.setFrom(newWebuserMailMessage.getFrom());
////                    helper.setTo(newWebuserMailMessage.getTo());
//                    helper.setTo(to_email);
//                    helper.setSubject(newWebuserMailMessage.getSubject());
//                    helper.setText(String.format(newWebuserMailMessage.getText(), name, username, link));
//
////                    ClassPathResource config = new ClassPathResource("beans.xml");
////                    helper.addAttachment("beans.xml", config);
//                }
//            };
//
//        try {
//        	mailSender.send(preparator);
//        } catch (MailException me) {
//        	System.out.println ("Error sending email: " + me.getMessage());
//        	me.printStackTrace();
//        	return -1;
//        }
//        return 0;
//    }

	@Override
	public void notifyResetPassword(final String accountOwnerName, final String userName,
			final String recipientEmail, final String activationLink, Locale locale) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage)
                throws Exception {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setFrom(resetPasswordMailMessage.getFrom());
                helper.setTo(recipientEmail);
                helper.setSubject(resetPasswordMailMessage.getSubject());
                helper.setText(String.format(resetPasswordMailMessage.getText(), accountOwnerName, userName, activationLink));
            }
        };

        try {
        	mailSender.send(preparator);
        } catch (MailException me) {
        	System.out.println ("Error sending email: " + me.getMessage());
        	me.printStackTrace();
        }
	}

//	@Override
//	public int notifyResetWebuser(final String to_email, final String name, final String username, final String link) {
//        MimeMessagePreparator preparator = new MimeMessagePreparator() {
//                public void prepare(MimeMessage mimeMessage)
//                    throws Exception {
//                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//                    helper.setFrom(resetPasswordMailMessage.getFrom());
//                    helper.setTo(to_email);
//                    helper.setSubject(resetPasswordMailMessage.getSubject());
//                    helper.setText(String.format(resetPasswordMailMessage.getText(), name, username, link));
//                }
//            };
//
//        try {
//        	mailSender.send(preparator);
//        } catch (MailException me) {
//        	System.out.println ("Error sending email: " + me.getMessage());
//        	me.printStackTrace();
//        	return -1;
//        }
//        return 0;
//    }
}
