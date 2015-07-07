package net.emphased.malle.example;

import net.emphased.malle.MailAddressException;
import net.emphased.malle.MailAuthenticationException;
import net.emphased.malle.MailException;
import net.emphased.malle.MailSendException;

public abstract class AbstractExample {

    protected static void err(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    protected static void err(String msg, String details) {
        if (details != null) {
            msg += " (" + details + ")";
        }
        err(msg);
    }

    protected static <T> T checkNotNull(T ref, String msg) {
        checkTrue(ref != null, msg);
        return ref;
    }

    protected static void checkTrue(boolean condition, String msg) {
        if (!condition) {
            err(msg);
        }
    }

    protected static <T extends Throwable> void handleException(T e) throws T {
        if (e instanceof MailAddressException) {
            err("Invalid address", e.getMessage());
        } else if (e instanceof MailSendException) {
            err("Couldn't sent", e.getMessage());
        } else if (e instanceof MailAuthenticationException) {
            err("Couldn't authenticate", e.getMessage());
        } else if (e instanceof MailException) {
            err("Unexpected mail failure", e.getMessage());
        } else {
            throw e;
        }
    }
}
