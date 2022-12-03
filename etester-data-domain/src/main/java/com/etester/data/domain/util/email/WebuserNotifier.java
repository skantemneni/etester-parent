package com.etester.data.domain.util.email;

import java.util.Locale;

public interface WebuserNotifier {
    public void notifyNewWebuser(String accountOwnerName, String userName, String recipientEmail, String activationLink, Locale locale);
    public void notifyResetPassword(String accountOwnerName, String userName, String recipientEmail, String activationLink, Locale locale);
}
