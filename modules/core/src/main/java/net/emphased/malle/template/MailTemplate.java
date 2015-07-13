package net.emphased.malle.template;

import net.emphased.malle.Mail;

import javax.annotation.Nullable;
import java.util.Map;

public interface MailTemplate {

    String getName();

    void apply(Mail message, @Nullable Map<String, ?> context);
}
