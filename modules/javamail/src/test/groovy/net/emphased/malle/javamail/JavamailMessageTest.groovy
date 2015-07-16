package net.emphased.malle.javamail

import net.emphased.malle.support.InputStreamSuppliers
import org.junit.Before
import org.junit.Test

import javax.mail.internet.MimeMessage

import static org.assertj.core.api.Assertions.assertThat

class JavamailMessageTest extends AbstractJavamailTest {

    Javamail javamail;


    @Before
    void setUp() {
        javamail = new Javamail();
    }

    @Test
    void "creates non-multipart MimeMessage with headers and plain text"() {

        def m = javamail.createMail(false)

        m.from("from@example.com")
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

        def m = javamail.createMail(true)
                .from("from@example.com")
                .to("to@example.com")
                .subject("This is a subject")
                .plain("Hello from Malle /plain")
                .html("Hello from Malle /html")

        MimeMessageRawMatcher.assertMatch("mp_headers_plain_html.eml", m)
    }

    @Test
    void "creates multipart MimeMessage with inline attachments"() {

        def m = javamail.createMail(true)
                .from("from@example.com")
                .to("to@example.com")
                .inline(InputStreamSuppliers.resource("image1.png"), "image1@example.com", "image/png")
                .html("<img src=\"cid:image1@example.com\"/>");

        MimeMessageRawMatcher.assertMatch("mp_inline.eml", m)
    }

    @Test(expected = IllegalStateException)
    void "throws IllegalStateException when attempting to read attachment InputStreamS multiple times"() throws Exception {
        javamail.createMail(true)
            .plain("")
            .attachment(InputStreamSuppliers.inputStream(new ByteArrayInputStream()), "test")
            .writeTo(new ByteArrayOutputStream())
            .writeTo(new ByteArrayOutputStream());
    }
}
