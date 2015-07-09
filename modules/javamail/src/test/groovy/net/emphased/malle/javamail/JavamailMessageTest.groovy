package net.emphased.malle.javamail
import spock.lang.Specification

import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class JavamailMessageTest extends Specification {

    def javamail = new Javamail()

    def "creates non-multipart MimeMessage with headers and plain text"() {
        given:

        JavamailMessage m = javamail.createMailMessage(false)

        when:

        m.from("from@example.com")
                .to("to@example.com")
                .subject("This is a subject")
                .plain("Hello from Malle")

        then:

        MimeMessage mm = m.getMimeMessage()

        mm.getFrom().length == 1
        assertInternetAddress("from@example.com", null, mm.getFrom()[0])

        mm.getRecipients(Message.RecipientType.TO).length == 1
        assertInternetAddress("to@example.com", null, mm.getRecipients(Message.RecipientType.TO)[0])

        mm.getSubject() == "This is a subject"
        mm.getContent() == "Hello from Malle"

        MimeMessageRawMatcher.assertMatch("non_mp_headers_text.eml", mm)
    }

    def "creates multipart MimeMessage with headers and plain/html text"() {
        given:

        JavamailMessage m = javamail.createMailMessage(true)

        when:

        m.from("from@example.com")
                .to("to@example.com")
                .subject("This is a subject")
                .plain("Hello from Malle /plain")
                .html("Hello from Malle /html")

        then:

        MimeMessageRawMatcher.assertMatch("mp_headers_plain_html.eml", m.getMimeMessage())
    }

    void assertInternetAddress(String address, String personal, actual) {
        assert actual instanceof InternetAddress
        assert actual.getAddress() == address
        assert actual.getPersonal() == personal
    }
}
