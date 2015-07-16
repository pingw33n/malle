package net.emphased.malle.javamail

import net.emphased.malle.MailSendException
import net.emphased.malle.support.InputStreamSuppliers
import org.junit.Before
import org.junit.Test

import javax.mail.internet.MimeMessage

import static net.emphased.malle.javamail.MimeMessageRawMatcher.assertMatch
import static org.assertj.core.api.Assertions.assertThat

class JavamailMessageTest extends AbstractJavamailTest {

    Javamail javamail;


    @Before
    void setUp() {
        javamail = new Javamail()
                .withProperty("mail.smtp.host", "example.com");
    }

    @Test
    void "creates non-multipart MimeMessage with headers and plain text"() {

        def m = javamail.createMail(false)
                .from("from@example.com")
                .to("to@example.com")
                .subject("This is a subject")
                .plain("Hello from Malle")

        MimeMessage mm = m.getMimeMessage()

        assertThat mm.getSubject() isEqualTo "This is a subject"
        assertThat mm.getContent() isEqualTo "Hello from Malle"

        MimeMessageRawMatcher.assertMatch("non_mp_headers_text.eml", m)
    }

    @Test
    void "creates multipart MimeMessage with headers and plain/html text"() {

        def m = javamail.createMail()
                .from("from@example.com")
                .to("to@example.com")
                .subject("This is a subject")
                .plain("Hello from Malle /plain")
                .html("Hello from Malle /html")

        assertMatch("mp_headers_plain_html.eml", m)
    }

    @Test
    void "creates multipart MimeMessage with inline attachments"() {

        def m = javamail.createMail()
                .from("from@example.com")
                .to("to@example.com")
                .inline(InputStreamSuppliers.resource("image1.png"), "image1@example.com", "image/png")
                .html("<img src=\"cid:image1@example.com\"/>");

        assertMatch("mp_inline.eml", m)
    }

    @Test(expected = IllegalStateException)
    void "throws IllegalStateException when attempting to read attachment InputStreamS multiple times"() {
        javamail.createMail()
            .plain("")
            .attachment(InputStreamSuppliers.inputStream(new ByteArrayInputStream()), "test")
            .writeTo(new ByteArrayOutputStream())
            .writeTo(new ByteArrayOutputStream());
    }

    @Test(expected = MailSendException)
    void "throws MailSendException when no recipients"() {
        javamail.createMail()
                .plain("")
                .send();
    }
}
