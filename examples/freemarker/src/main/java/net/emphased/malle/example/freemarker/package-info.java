/**
 * Sample app that uses Javamail Malle implementation and the Freemarker support to send out an email
 * using the specified template.
 *
 * <p>
 *     Supported system properties:
 * </p>
 * <ul>
 *     <li>All Javamail standard properties. See <a href="http://docs.oracle.com/javaee/6/api/javax/mail/package-summary.html">javax.mail</a>
 *         and <a href="https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html">com.sun.mail.smtp</a> (and other) packages' Javadoc</li>.
 *     <li>{@code malle.javamail.password} - allows to specify the authentication password. See Gmail command line example below.</li>
 *     <li>{@code template} - path to the template. If prefixed with {@code classpath:} the template will be loaded from the classpath.</li>
 *     <li>Any other system properties used by template. Note that in order to reference properties with dots in the name
 *         they must be replaced with an underscore character. For instance, writing <code>${mail.user}</code>
 *         won't work but <code>${mail_user}</code> will work fine and will be replaced with the value of
 *         @{code mail.user} system property.</li>
 * </ul>
 *
 * <p>
 *     Gmail command line example using the bundled sample template:
 * </p>
 * <pre>
 *     java -Dmail.smtp.auth=true \
 *          -Dmail.smtp.starttls.enable=true \
 *          -Dmail.smtp.host=smtp.gmail.com \
 *          -Dmail.smtp.port=587 \
 *          -Dmail.user=youruser@gmail.com \
 *          -Dmalle.javamail.password=YourAppPassword \
 *          -Dtemplate=classpath:sample.ftl \
 *          -Dto=recipient@example.com \
 *          -jar malle-freemarker-example-&ltversion>-allinone.jar
 * </pre>
 */
package net.emphased.malle.example.freemarker;
