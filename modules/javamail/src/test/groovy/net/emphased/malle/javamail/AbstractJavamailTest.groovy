package net.emphased.malle.javamail
import javax.mail.internet.InternetAddress

import static org.assertj.core.api.Assertions.assertThat

abstract class AbstractJavamailTest {

    static void assertInternetAddress(List<String> expected, actual) {
        assertThat actual isInstanceOf InternetAddress
        assertThat actual.getPersonal() isEqualTo expected[0]
        assertThat actual.getAddress() isEqualTo expected[1]
    }

    static void assertInternetAddresses(List<List<String>> expected, List<Object> actual) {
        assertThat expected.size() isEqualTo(actual.size())
        expected.eachWithIndex { e, i ->
            assertInternetAddress(e, actual[i]);
        }
    }
}
