package net.emphased.malle.util;

import static net.emphased.malle.util.SimpleFormat.escape;
import static net.emphased.malle.util.SimpleFormat.format;
import static net.emphased.malle.util.SimpleFormat.unescape;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SimpleFormatTest {

    @Test
    public void formats_with_escapes() throws Exception {
        assertThat(format("{{}")).isEqualTo("{}");
        assertThat(format("{{}asd")).isEqualTo("{}asd");
        assertThat(format("asd{{}asd")).isEqualTo("asd{}asd");
        assertThat(format("asd{{}")).isEqualTo("asd{}");
        assertThat(format("{{}asd{{}")).isEqualTo("{}asd{}");
        assertThat(format("{{}{{}")).isEqualTo("{}{}");
    }

    @Test
    public void formats_without_placeholders() throws Exception {
        assertThat(format("")).isEmpty();
        assertThat(format("test test")).isEqualTo("test test");
        assertThat(format("test test", "blah", 123)).isEqualTo("test test");
    }

    @Test
    public void formats_with_placeholders() throws Exception {
        assertThat(format("{}", "test")).isEqualTo("test");
        assertThat(format("{}-pre", "test")).isEqualTo("test-pre");
        assertThat(format("post-{}", "test")).isEqualTo("post-test");
        assertThat(format("middle-{}-middle", "test")).isEqualTo("middle-test-middle");
        assertThat(format("{{}{}{{}", "test")).isEqualTo("{}test{}");
    }

    @Test
    public void escapes() throws Exception {
        assertThat(format(escape("{}"))).isEqualTo("{}");
        assertThat(format(escape("{{}"))).isEqualTo("{{}");
        assertThat(format(escape("{{{}}}"))).isEqualTo("{{{}}}");
        assertThat(format(escape("{}test{}test{}"))).isEqualTo("{}test{}test{}");
    }

    @Test
    public void escapes_and_unescapes() throws Exception {
        assertThat(unescape(escape("{}"))).isEqualTo("{}");
        assertThat(unescape(escape("{{}"))).isEqualTo("{{}");
        assertThat(unescape(escape("{{{}}}"))).isEqualTo("{{{}}}");
        assertThat(unescape(escape("{}test{}test{}"))).isEqualTo("{}test{}test{}");
    }
}
