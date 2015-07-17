![travis-ci](https://travis-ci.org/pingw33n/malle.svg?branch=master)

Malle is a small Java (1.7 or higher) library that aims to provide consistent and fluent interface for sending
mail messages. Implemented on top of standard [JavaMail](http://www.oracle.com/technetwork/java/javamail/index.html) 
with [FreeMarker](http://freemarker.org/) for templating support.

## Getting started

### Clone and build

```
git clone https://github.com/pingw33n/malle.git && cd malle && mvn install
```

### Add to your project

Maven:

```xml
<repositories>
    <repository>
        <id>sonatype-oss-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>net.emphased.malle</groupId>
    <artifactId>malle-javamail</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

If FreeMarker template support is needed:

```xml
<dependency>
    <groupId>net.emphased.malle</groupId>
    <artifactId>malle-freemarker</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Use

Assuming the JavaMail is properly configured with system properties, sending a mail is as simple as:

```java
new Javamail()
    .createMail()
    .from("alice@example.com", "Alice")
    .to("bob@example.com", "Bob")
    .subject("A mail from Alice")
    .html("<b>Hello Bob!</b>")
    .send();
```

If not, it's easy to configure it:

```java
MailSystem mailSystem = new Javamail()
    .withProperty("mail.smtp.auth", "true")
    .withProperty("mail.smtp.starttls.enable", "true")
    .withProperty("mail.smtp.host", "smtp.gmail.com")
    .withProperty("mail.smtp.port", "587")
    .withProperty("mail.user", "yourgmailaccount@gmail.com")
    .withProperty(Javamail.PASSWORD_PROP, "<your app password>");

mailSystem.createMail()
    .from("alice@gmail.com", "Alice")
    .to("bob@gmail.com", "Bob")
    .subject("A mail from Alice")
    .html("<b>Hello Bob!</b>")
    .send();
```

Sending some attachments:

```java
String catUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/So_happy_smiling_cat.jpg/411px-So_happy_smiling_cat.jpg";
String horseUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/Funny_Cide.jpg/444px-Funny_Cide.jpg";
mailSystem.createMail()
    .from("alice@example.com", "Alice")
    .to("bob@example.com", "Bob")
    .subject("A mail with some pics from Alice")
    .html("Hey Bob, check out these funny pics ;)")
    .attachment(InputStreamSuppliers.url(catUrl), "cat.jpg")
    .attachment(InputStreamSuppliers.url(horseUrl), "horse.jpg")
    .send();
```

Sending some inline attachments:

```java
String catUrl = ...
String horseUrl = ...
mailSystem.createMail()
    .from("alice@example.com", "Alice")
    .to("bob@example.com", "Bob")
    .subject("A mail with some inline pics from Alice")
    .html("Hey Bob, look at this cat <img src=\"cid:cat@example.com\"/> and horse <img src=\"cid:horse@example.com\"/>")
    .inline(InputStreamSuppliers.url(catUrl), "cat@example.com")
    .inline(InputStreamSuppliers.url(horseUrl), "horse@example.com")
    .send();
```

Using FreeMarker templating engine:

```java
Configuration fc = new Configuration(Configuration.VERSION_2_3_22);
fc.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
fc.setDirectoryForTemplateLoading(new File("."));
fc.setDefaultEncoding("UTF-8");

MailSystem mailSystem = new Javamail()
    ...
    .withTemplateEngine(new FreemarkerTemplateEngine().withConfiguration(fc));

mailSystem.createMail()
    .template("mytemplate.ftl",
              "from_address", "alice@example.com",
              "from_personal", "Alice",
              "to_address", "bob@example.com",
              "to_personal", "Bob")
    .send();
```

where `mytemplate.ftl` looks like this:

```html
<@mail cmd='from' address='${from_address}'>${from_personal}</@mail>

<#-- Leading and trailing whitespace inside the headers gets trimmed. -->
<@mail cmd='to' address='${to_address}'>  ${to_personal}  </@mail>

<@mail cmd='subject'>
    A message for ${to_personal} from ${from_personal} sent using Malle and Freemarker
</@mail>

<#-- Whitespace in the text/html part gets trimmed too. -->
<@mail cmd='html'><#escape x as x?html>
    <p>Hello ${to_personal},</p>
    
    <p>
        <#-- Unicode works as expected. -->
        This is a sample mail from ${from_personal} sent to you using Malle ♡ Freemarker.
    </p>
    <p>
        <#-- Inline image, see below. -->
        <img src="cid:cat.jpg"/>
    </p>
</#escape></@mail>

<#-- Only trailing whitespace in the text/plain part gets trimmed. -->
<@mail cmd='plain'>

Hello ${to_personal},

It's sad that you don't have an HTML-capable mail reader :(
</@mail>

<#-- Simple textual attachments can be created directly inside the template. -->
<@mail cmd='attachment' name='readme.txt'>
This will the attachment's content.
</@mail>

<#-- Classpath resources can be easily attached. -->
<@mail cmd='attachment' name='image.png' resource='image.png'/>

<#-- Inline resources are easy to add as well. -->
<@mail cmd='inline' id='cat.jpg'
       url='https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/So_happy_smiling_cat.jpg/411px-So_happy_smiling_cat.jpg'/>
```

Configuring with Spring XML:

```xml
<bean id="mailSystem" class="net.emphased.malle.javamail.Javamail">
    <property name="templateEngine">
        <bean class="net.emphased.malle.template.freemarker.FreemarkerTemplateEngine">
            <property name="configuration">
                <bean class="org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean"
                      p:defaultEncoding="UTF-8"
                      p:templateLoaderPath="classpath:mail"/>
            </property>
        </bean>
    </property>
    <property name="properties">
        <map>
            <entry key="mail.smtp.host" value="${mail.smtp.host}"/>
            <entry key="mail.smtp.port" value="${mail.smtp.port}"/>
            <entry key="mail.smtp.connectiontimeout" value="${mail.smtp.connectiontimeout}"/>
            <entry key="mail.smtp.timeout" value="${mail.smtp.timeout}"/>
        </map>
    </property>
</bean>
```

... and using in a usual way:

```java
public class MyBean {

    @Autowired
    private MailSystem mailSystem;

    public void sendMail() {
        mailSystem.createMail()
            ...
            .send()
    }
```

## License

The MIT License (MIT)

Copyright (c) 2015 Dmytro Lysai /pingw33n@emphased.net/

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
