package net.emphased.malle.template;

import java.util.Map;

import net.emphased.malle.MailMessage;

public interface MailTemplate {

    String getName();

    void apply(MailMessage message, Map<String, ?> context);
}
