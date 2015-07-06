/**
 * Sample app that uses Malle to send out an email with the specified parameters.
 *
 * <p>
 *     Supported system properties:
 * </p>
 * <ul>
 *     <li>All Javamail standard properties. See <a href="http://docs.oracle.com/javaee/6/api/javax/mail/package-summary.html">javax.mail</a>
 *         and <a href="https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html">com.sun.mail.smtp</a> (and other) packages Javadoc</li>.
 *     <li>{@code malle.javamail.password} - allows to specify the authentication password. See Gmail command line example below.</li>
 *     <li>{@code to} - a comma-separated list of recipients.</li>
 *     <li>{@code from} - the sender. If omitted, {@code mail.user} will be used and must be present.</li>
 *     <li>{@code subject} - the subject.
 *     <li>{@code plain}, {@code html} - at least one of these must specify the mail body.
 * </ul>
 *
 * <p>
 *     Gmail command line example:
 * </p>
 * <pre>
 *     java -jar malle-javamail-example.jar \
 *          -Dmail.smtp.auth=true \
 *          -Dmail.smtp.starttls.enable=true \
 *          -Dmail.smtp.host=smtp.gmail.com \
 *          -Dmail.smtp.port=587 \
 *          -Dmail.user=youruser@gmail.com \
 *          -Dmalle.javamail.password=YourAppPassword \
 *          -Dto=pingw33n@emphased.net \
 *          -Dsubject="Malle Javamail Example" \
 *          -Dplain="Hello from Malle Javamail Example!" \
 *          -Dhtml="&lt;h1>Hello from Malle Javamail Example!&lt;/h1>"
 * </pre>
 */
package net.emphased.malle.example.javamail;
