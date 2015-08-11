package net.emphased.malle.example.freemarker;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import net.emphased.malle.MailException;
import net.emphased.malle.example.AbstractExample;
import net.emphased.malle.javamail.Javamail;
import net.emphased.malle.template.freemarker.FreemarkerTemplateEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class FreemarkerExample extends AbstractExample {

    public static void main(String[] args) throws IOException {
        Configuration fc = new Configuration(Configuration.VERSION_2_3_23);
        fc.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        fc.setDefaultEncoding("UTF-8");

        String template = checkNotNull(System.getProperty("template"), "Please set 'template' property");

        System.out.println("Sending mail using '" + template + "' template");

        if (template.startsWith("classpath:")) {
            template = template.substring("classpath:".length());
            fc.setClassForTemplateLoading(FreemarkerExample.class, "/");
        } else {
            Path path = Paths.get(template);
            template = path.getFileName().toString();
            fc.setDirectoryForTemplateLoading(path.getParent().toFile());
        }

        Map<String, Object> c = new HashMap<>();
        for (Map.Entry<Object, Object> e: System.getProperties().entrySet()) {
            c.put(e.getKey().toString().replace('.', '_'), e.getValue());
        }

        try {
            new Javamail()
                    .withTemplateEngine(new FreemarkerTemplateEngine().withConfiguration(fc))
                    .mail()
                    .template(template, c)
                    .send();
        } catch (MailException e) {
            handleException(e);
        }
    }

    private FreemarkerExample() {
    }
}
