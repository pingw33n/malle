package net.emphased.malle.template.servlet
import com.google.common.io.Resources
import net.emphased.malle.Encoding
import net.emphased.malle.MailMock
import net.emphased.malle.MailMockAssert
import net.emphased.malle.support.InputStreamSuppliers
import org.apache.catalina.startup.Tomcat
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

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
        webappDir = checkNotNull(Resources.getResource("webapp").getFile())
    }

    @Before
    void setUp() {
        // Is there a better way to make Tomcat see the TLD?
        def target = Paths.get(webappDir, "WEB-INF", "mulle.tld")
        Files.createDirectories(target.getParent())
        new FileOutputStream(target.toFile()).withCloseable { os ->
            Resources.copy(Resources.getResource("META-INF/malle.tld"), os)
        }

        tomcat = new Tomcat()
        tomcat.setPort(0)

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
                    "toPersonal", "Unicode ♡ Malle");

        MailMock expected = (MailMock) new MailMock(true)
                .bodyEncoding(Encoding.BASE64)
                .from("from@example.com", null)
                .to("to@example.com", "Unicode ♡ Malle")
                .cc("cc@example.com", "Malle ♡ Unicode")
                .cc("cc2@example.com,\n" +
                    "    \"CC 3\" <cc3@example.com>,\n" +
                    "    \"♡ Unicode ♡\" <cc4@example.com>")
                .plain("    Plain hello ☺")
                .html("<p>Html hello ☺</p>")
                .attachment(InputStreamSuppliers.bytes("    Hello there ✌".getBytes("UTF-8")), "embedded.txt")
                .attachment(InputStreamSuppliers.resource("classpath.txt"), "classpath.txt")
                .inline(InputStreamSuppliers.resource("image1.png"), "inline.png")

        MailMockAssert.assertThat(actual).isEqualTo(expected);
    }
}
