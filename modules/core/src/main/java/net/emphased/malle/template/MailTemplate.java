package net.emphased.malle.template;

import net.emphased.malle.MailMessage;

import javax.annotation.Nullable;
import java.util.Map;

public interface MailTemplate {

    String getName();

    void apply(MailMessage message, @Nullable Map<String, ?> context);
}
