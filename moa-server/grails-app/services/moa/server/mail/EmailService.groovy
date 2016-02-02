package moa.server.mail

import moa.Submitter

import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Created by sajjan on 7/30/15.
 */
class EmailService {
    def sendMessage(String to, String subject, String body) {
        def host = "smtp.mandrillapp.com"
        def username = "ssmehta@ucdavis.edu"
        def password = "_kz61FdVFNHrmiNbzuTOlQ"

        Properties props = System.getProperties()
        props.put("mail.smtp.host", host)
        props.put("mail.smtp.user", username)
        props.put("mail.smtp.password", password)
        props.put("mail.smtp.port", "587")
        props.put("mail.smtp.auth", "true")

        Session session = Session.getDefaultInstance(props, null)

        MimeMessage message = new MimeMessage(session)
        message.setFrom(new InternetAddress("mona@fiehnlab.ucdavis.edu"))
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))
        message.setSubject(subject)
        message.setText(body)

        Transport transport = session.getTransport("smtp")
        transport.connect(host, username, password)
        transport.sendMessage(message, message.getAllRecipients())
        transport.close()
    }

    def sendDownloadEmail(String emailAddress, long queryCount, long id) {
        Submitter user = Submitter.findByEmailAddress(emailAddress)

        String body = "Dear "+ user.firstName +",\n\n";
        body += "Thank you for your query download request.  $queryCount spectra were exported.  "
        body += "Your download results will be available from the following link for the next 7 days:\n"
        body += "http://mona.fiehnlab.ucdavis.edu/rest/spectra/search/download/$id\n\n"
//        body += "You may also download your query in JSON format from:\n"
//        body += "http://mona.fiehnlab.ucdavis.edu/rest/spectra/search/download/$id/json\n\n"
        body += "Best Regards,\nMoNA Development Team"

        sendMessage(emailAddress, 'Your MoNA download results are ready!', body)
    }
}