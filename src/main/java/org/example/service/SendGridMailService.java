package org.example.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

public class SendGridMailService {

    // ✅ ضع API KEY الحقيقية (تبدأ بـ SG.)
    private static final String SENDGRID_API_KEY = "SG.REPLACE_WITH_REAL_KEY";

    // ✅ لازم يكون Sender Verified في SendGrid
    private static final String FROM_EMAIL = "verified_sender@gmail.com";

    public void sendOtp(String toEmail, String code) {
        try {
            if (SENDGRID_API_KEY == null || SENDGRID_API_KEY.length() < 10 || !SENDGRID_API_KEY.startsWith("SG.")) {
                throw new RuntimeException("SENDGRID_API_KEY invalide. Elle doit commencer par SG.");
            }

            Email from = new Email(FROM_EMAIL);
            Email to = new Email(toEmail);

            String subject = "Code de reinitialisation";
            Content content = new Content(
                    "text/plain",
                    "Votre code est : " + code + "\nIl expire dans 10 minutes."
            );

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(SENDGRID_API_KEY);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + "\n" + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur SendGrid: " + e.getMessage(), e);
        }
    }
}
