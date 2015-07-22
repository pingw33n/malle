package net.emphased.malle.template.freemarker
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import net.emphased.malle.Encoding
import net.emphased.malle.MailMock
import net.emphased.malle.MailMockAssert
import net.emphased.malle.support.InputStreamSuppliers
import net.emphased.malle.template.MailTemplateException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class FreemarkerTemplateEngineTest {

    FreemarkerTemplateEngine t

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    void setUp() {
        Configuration fc = new Configuration(Configuration.VERSION_2_3_22)
        fc.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)
        fc.setClassForTemplateLoading(getClass(), "/")
        fc.setDefaultEncoding("UTF-8")

        t = new FreemarkerTemplateEngine()
                .withConfiguration(fc)
    }

    @Test
    void "applies template"() {
        MailMock actual = new MailMock(true)
                .withTemplateEngine(t)
                .template("applies template.ftl",
                        "to", "to@example.com",
                        "toPersonal", "Unicode ♡ Malle")

        MailMock expected = new MailMock(true)
                .bodyEncoding(Encoding.BASE64)
                .from("from@example.com", null)
                .to("to@example.com", "Unicode ♡ Malle")
                .cc("cc@example.com", "Malle ♡ Unicode")
                .cc("cc2@example.com,\r\n" +
                        "    \"CC 3\" <cc3@example.com>,\r\n" +
                        "    \"♡ Unicode ♡\" <cc4@example.com>")
                .plain("    Plain hello ☺")
                .html("<p>Html hello ☺</p>")
                .attachment(InputStreamSuppliers.resource("classpath.txt"), "classpath.txt")
                .inline(InputStreamSuppliers.resource("image1.png"), "inline.png")

        MailMockAssert.assertThat(actual).isEqualTo(expected)
    }

    @Test
    void "throws MailTemplateException when attachment or inline command has body"() {
        thrown.expect(MailTemplateException)
        thrown.expectMessage("'mail' directive doesn't support embedded attachment content")

        new MailMock(true)
                .withTemplateEngine(t)
                .template("throws MailTemplateException when attachment or inline command has body.ftl")
    }
}
