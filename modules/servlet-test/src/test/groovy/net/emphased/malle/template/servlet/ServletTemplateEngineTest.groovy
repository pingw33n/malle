package net.emphased.malle.template.servlet

import net.emphased.malle.MailMock
import net.emphased.malle.MailMockAssert
import org.apache.catalina.startup.Tomcat
import org.apache.tomcat.util.scan.StandardJarScanner
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import static org.assertj.core.util.Preconditions.checkNotNull

@Ignore
class ServletTemplateEngineTest {

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

    @Before
    void setUp() {
        String webappDir = ServletTemplateEngineTest.class.getClassLoader().getResource("webapp").getFile()
        checkNotNull(webappDir)

        tomcat = new Tomcat()
        tomcat.setPort(0)

        StandardJarScanner jarScanner = new StandardJarScanner()
        jarScanner.setScanBootstrapClassPath(true)
        jarScanner.setScanClassPath(true)

        def ctx = tomcat.addWebapp("", new File(webappDir).getAbsolutePath())
        ctx.addApplicationListener(SCL.class.name)
        //ctx.setJarScanner(jarScanner)
        ctx.setParentClassLoader()

        tomcat.start()

        t = new ServletTemplateEngine()
            .withServletContext(servletContext)
    }

    @After
    void tearDown() {
        tomcat.getServer().stop()
    }

    @Test
    void testName() {
        MailMock actual = new MailMock(true)
                .withTemplateEngine(t)
                .template("/WEB-INF/mail/test.jsp");

        MailMock expected = new MailMock(true)
                .from("from@example.com", null)
                .to("to@example.com", "Хелло Ворлд")
                .plain("Plain text проверка")
                .html("<p>Html test проверка</p>")

        MailMockAssert.assertThat(actual).isEqualTo(expected);
    }
}
