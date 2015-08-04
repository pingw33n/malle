package net.emphased.malle.javamail

import net.emphased.malle.MailSendException
import net.emphased.malle.support.InputStreamSuppliers
import org.junit.Before
import org.junit.Test

import javax.mail.internet.MimeMessage

import static net.emphased.malle.javamail.MimeMessageRawMatcher.assertMatch
import static org.assertj.core.api.Assertions.assertThat

class JavamailMessageTest extends AbstractJavamailTest {

    Javamail javamail


    @Before
    void setUp() {
        javamail = new Javamail()
                .withProperty("mail.smtp.host", "example.com")
    }

    @Test
    void "creates non-multipart MimeMessage with headers and plain text"() {

        def m = javamail.mail(false)
                .from("from@example.com")
                .to("to@example.com")
                .subject("This is a subject")
                .plain("Hello from Malle")

        MimeMessage mm = m.getMimeMessage()

        assertThat mm.getSubject() isEqualTo "This is a subject"
        assertThat mm.getContent() isEqualTo "Hello from Malle"

        MimeMessageRawMatcher.assertMatch("creates non-multipart MimeMessage with headers and plain text.eml", m)
    }

    @Test
    void "creates multipart MimeMessage with headers and plain/html text"() {

        def m = javamail.mail()
                .from("from@example.com")
                .to("to@example.com")
                .subject("This is a subject")
                .plain("Hello from Malle /plain")
                .html("Hello from Malle /html")

        assertMatch("creates multipart MimeMessage with headers and plain_html text.eml", m)
    }

    @Test
    void "creates multipart MimeMessage with inline attachments"() {

        def m = javamail.mail()
                .from("from@example.com")
                .to("to@example.com")
                .inline(InputStreamSuppliers.resource("image1.png"), "image1@example.com", "image/png")
                .html("<img src=\"cid:image1@example.com\"/>")

        assertMatch("creates multipart MimeMessage with inline attachments.eml", m)
    }

    @Test(expected = IllegalStateException)
    void "throws IllegalStateException when attempting to read attachment InputStreamS multiple times"() {
        javamail.mail()
            .plain("")
            .attachment(InputStreamSuppliers.inputStream(new ByteArrayInputStream()), "test")
            .writeTo(new ByteArrayOutputStream())
            .writeTo(new ByteArrayOutputStream())
    }

    @Test(expected = MailSendException)
    void "throws MailSendException when no recipients"() {
        javamail.mail()
                .plain("")
                .send()
    }

    @Test
    void "parses email address list"() {
        def m = javamail.mail(false)
                .to("to1@example.com, To 2 <to2@example.com>, \t\r\n\"To, 3\" <to3@example.com>, \n\"♡ Unicode ♡\" <to4@example.com>")
                .plain("")
        assertMatch("parses email address list.eml", m)
    }

    @Test
    void "charset affects existing address headers"() {
        def m = (JavamailMessage) javamail.mail()
                .plain("")
                .charset("UTF-8")
                .from("from@example.com", "from mälle")
                .replyTo("reply-to@example.com", "reply-to mälle")
                .to("to@example.com", "to mälle")
                .cc("cc@example.com", "cc mälle")
                .bcc("bcc@example.com", "bcc mälle")
                .charset("ISO-8859-1")

        def mm = m.getMimeMessage()
        ["From"     : "=?ISO-8859-1?Q?from_m=E4lle?= <from@example.com>",
         "Reply-To" : "=?ISO-8859-1?Q?reply-to_m=E4lle?= <reply-to@example.com>",
         "To"       : "=?ISO-8859-1?Q?to_m=E4lle?= <to@example.com>",
         "CC"       : "=?ISO-8859-1?Q?cc_m=E4lle?= <cc@example.com>",
         "BCC"      : "=?ISO-8859-1?Q?bcc_m=E4lle?= <bcc@example.com>",].each { k, v ->
            assertThat(mm.getHeader(k)[0].toString()).isEqualTo(v)
        }
    }
}
