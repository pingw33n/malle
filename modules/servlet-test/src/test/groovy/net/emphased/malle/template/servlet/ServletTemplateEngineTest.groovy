package net.emphased.malle.template.servlet
import com.google.common.io.Resources
import net.emphased.malle.Encoding
import net.emphased.malle.MailMock
import net.emphased.malle.MailMockAssert
import net.emphased.malle.support.InputStreamSuppliers
import net.emphased.malle.template.GenericMailTemplateException
import org.apache.catalina.startup.Tomcat
import org.junit.*
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import java.nio.file.Files
import java.nio.file.Paths

import static net.emphased.malle.util.Preconditions.checkNotNull

class ServletTemplateEngineTest {

    static String webappDir
    Tomcat tomcat
    static ServletContext servletContext
    ServletTemplateEngine t

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    static class SCL implements ServletContextListener {

        @Override
        void contextInitialized(ServletContextEvent sce) {
            ServletTemplateEngineTest.servletContext = sce.servletContext
        }

        @Override
        void contextDestroyed(ServletContextEvent sce) {
        }
    }

    @BeforeClass
    static void setUpStatic() {
        String f = checkNotNull(Resources.getResource("webapp").getFile())
        webappDir = new File(f).toString()
    }

    @Before
    void setUp() {
        // Is there a better way to make Tomcat see the TLD?
        def target = Paths.get(webappDir, "WEB-INF", "malle.tld")
        Files.createDirectories(target.getParent())
        new FileOutputStream(target.toFile()).withCloseable { os ->
            Resources.copy(Resources.getResource("META-INF/malle.tld"), os)
        }

        tomcat = new Tomcat()
        tomcat.setPort(0)
        tomcat.setBaseDir(tmpFolder.newFolder("tomcat").toString())

        def ctx = tomcat.addWebapp("", new File(webappDir).getAbsolutePath())
        ctx.addApplicationListener(SCL.class.name)

        tomcat.start()

        t = new ServletTemplateEngine()
            .withServletContext(servletContext)
    }

    @After
    void tearDown() {
        tomcat.getServer().stop()
    }

    @Test
    void "applies template"() {
        MailMock actual = (MailMock) new MailMock(true)
                .withTemplateEngine(t)
                .template("/WEB-INF/mail/applies template.jsp",
                    "to", "to@example.com",
                    "toPersonal", "Unicode ♡ Malle")

        MailMock expected = (MailMock) new MailMock(true)
                .bodyEncoding(Encoding.BASE64)
                .from("from@example.com", null)
                .to("to@example.com", "Unicode ♡ Malle")
                .cc("cc@example.com", "Malle ♡ Unicode")
                .cc("cc2@example.com,\r\n" +
                    "    \"CC 3\" <cc3@example.com>,\r\n" +
                    "    \"♡ Unicode ♡\" <cc4@example.com>")
                .subject("From Malle With ♡♡♡")
                .priority(1)
                .plain("    Plain hello ☺")
                .html("<p>Html hello ☺</p>")
                .attachment(InputStreamSuppliers.resource("classpath.txt"), "classpath.txt")
                .inline(InputStreamSuppliers.resource("image1.png"), "inline.png")

        MailMockAssert.assertThat(actual).isEqualTo(expected)
    }

    @Test
    void "throws MailTemplateException when attachment or inline command has body"() {
        thrown.expect(GenericMailTemplateException);
        thrown.expectMessage("According to TLD, tag m:attachment must be empty, but is not");

        new MailMock(true)
                .withTemplateEngine(t)
                .template("/WEB-INF/mail/throws MailTemplateException when attachment or inline command has body.jsp")
    }
}
