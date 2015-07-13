package net.emphased.malle.javamail
import net.emphased.malle.AddressType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import javax.mail.internet.MimeMessage

import static org.junit.runners.Parameterized.Parameter
import static org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized)
class JavamailMessageAddressTest extends AbstractJavamailTest {


    static final def MM_FROM = { MimeMessage mm -> mm.getFrom() }
    static final def MM_REPLY_TO = { MimeMessage mm -> mm.getReplyTo() }
    static final def MM_REPLY_CC = { MimeMessage mm -> mm.getAddressHeader("CC") }
    static final def MM_REPLY_BCC = { MimeMessage mm -> mm.getAddressHeader("BCC") }

    static final def ADDRESSES = [
            [null, "f1@example.com"],
            ["Personal", "f2@example.com"],
            ["Personal With Space", "f3@example.com"],
            ["Personal ♡ Unicode", "f4@example.com"],
    ]

    @Parameters(name = "{0}")
    static def params() {
        [
            ["from", AddressType.FROM, MM_FROM],
            ["replyTo", AddressType.REPLY_TO, MM_REPLY_TO],
            ["cc", AddressType.CC, MM_REPLY_CC],
            ["bcc", AddressType.BCC, MM_REPLY_BCC],
        ].collectMany({
            def mergeAddrParts = {
                it.collect({ List parts ->
                    parts[0] != null ?
                            '"' + parts[0] + '" <' + parts[1] + '>' :
                            parts[1]
                })
            }
            def (inMethod, addrType, outMethod) = it
            [
                ["$inMethod(Iterable)",                 { m, a -> m."$inMethod"(mergeAddrParts(a) as List) }, outMethod],
                ["$inMethod(String[])",                 { m, a -> m."$inMethod"(mergeAddrParts(a) as String[]) }, outMethod],
                ["$inMethod(String)",                   { m, a -> m."$inMethod"((mergeAddrParts(a) as List).join(",")) }, outMethod],
                ["$inMethod(String, String)",           { m, a -> a.each({ List ait -> m."$inMethod"(ait[1], ait[0]) }) }, outMethod],
                ["address($addrType, Iterable)",        { m, a -> m.address(addrType, mergeAddrParts(a) as List) }, outMethod],
                ["address($addrType, String[])",        { m, a -> m.address(addrType, mergeAddrParts(a) as String[]) }, outMethod],
                ["address($addrType, String)",          { m, a -> m.address(addrType, (mergeAddrParts(a) as List).join(",")) }, outMethod],
                ["address($addrType, String, String)",  { m, a -> a.each({ List ait -> m."$inMethod"(ait[1], ait[0]) }) }, outMethod],
            ]
        }).collect({ it as Object[] })
    }

    Javamail javamail

    @Parameter(0)
    public String name
    @Parameter(1)
    public def inMethod
    @Parameter(2)
    public def outMethod

    @Before
    void setUp() {
        javamail = new Javamail()
    }

    @Test
    public void test() {
        JavamailMessage m = javamail.createMailMessage(false)
                .plain("");

        inMethod(m, ADDRESSES)

        MimeMessage mm = m.getMimeMessage()

        assertInternetAddresses(ADDRESSES, outMethod(mm) as List)
    }
}
