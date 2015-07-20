package net.emphased.malle;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;

import java.util.Map;
import java.util.TreeMap;

public class MailMockAssert extends AbstractAssert<MailMockAssert, MailMock> {

    private MailMockAssert(MailMock actual) {
        super(actual, MailMockAssert.class);
    }

    public static MailMockAssert assertThat(MailMock actual) {
        return new MailMockAssert(actual);
    }

    @Override
    public MailMockAssert isEqualTo(Object expected) {
        if (!(expected instanceof MailMock)) {
            return super.isEqualTo(expected);
        }

        MailMock e = (MailMock) expected;

        SoftAssertions sa = new SoftAssertions();
        sa.assertThat(actual.isMultipart()).isEqualTo(e.isMultipart());
        sa.assertThat(actual.getPriority()).isEqualTo(e.getPriority());
        sa.assertThat(actual.getId()).isEqualTo(e.getId());
        sa.assertThat(sorted(actual.getAddresses())).isEqualTo(sorted(e.getAddresses()));
        sa.assertThat(sorted(actual.getBodies())).isEqualTo(sorted(e.getBodies()));
        sa.assertThat(actual.getSubject()).isEqualTo(e.getSubject());
        sa.assertThat(actual.getHeaders()).isEqualTo(e.getHeaders());
        sa.assertThat(actual.getAttachments()).isEqualTo(e.getAttachments());
        sa.assertThat(actual.getInlines()).isEqualTo(e.getInlines());

        sa.assertAll();

        return this;
    }

    private <K, V> Map<K, V> sorted(Map<K, V> map) {
        return new TreeMap<>(map);
    }

    @Override
    public MailMockAssert isNotEqualTo(Object expected) {
        if (expected instanceof MailMock) {
            throw new UnsupportedOperationException();
        } else {
            return super.isEqualTo(expected);
        }
    }
}
