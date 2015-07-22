package net.emphased.malle.javamail;

import net.emphased.malle.*;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

final class Utils {

    public static MailException wrapException(MessagingException e) {
        if (e instanceof AddressException) {
            return new MailAddressException(e);
        } if (e instanceof SendFailedException) {
            return new MailSendException(e);
        } if (e instanceof AuthenticationFailedException) {
            return new MailAuthenticationException(e);
        } else {
            return new GenericMailException(e);
        }
    }

    public static <T> T[] toArray(Collection<? extends T> collection, Class<T> type) {
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(type, collection.size());
        return collection.toArray(array);
    }

    public static <T> Iterable<T> toIterable(final T[] array) {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {

                    private int i;

                    @Override
                    public boolean hasNext() {
                        return i < array.length;
                    }

                    @Override
                    public T next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        return array[i++];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    private Utils() {
    }
}
