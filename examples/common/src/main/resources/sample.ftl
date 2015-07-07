<#assign from = '${from!mail_user}'/>
<#assign to_firstName = '${to_firstName!"Mr"}'/>
<#assign to_lastName = '${to_lastName!"X"}'/>

<@mail key='from' address='${from!mail_user}'/>
<#-- Leading and trailing whitespace in the header values gets trimmed. -->
<@mail key='to' address='${to}'>
    ${to_firstName} ${to_lastName}
</@mail>

<@mail key='subject'>
    A message for ${to_firstName} ${to_lastName} sent using Malle and Freemarker
</@mail>

<#-- Whitespace in the text/html part gets trimmed too_ -->
<@mail key='html'><#escape x as x?html>
    <p>Hello ${to_firstName} ${to_lastName},</p>

    <p>
        <#-- Unicode works as expected. -->
        This is a sample mail from ${from} sent to you using Malle ♡ Freemarker.
    </p>
</#escape></@mail>

<#-- Only trailing whitespace in the text/plain part gets trimmed. -->
<@mail key='plain'>
Hello ${to_firstName} ${to_lastName},

It's sad that you don't have an HTML-capable mail reader :(
</@mail>

<#-- Simple textual attachments can be created with template too. -->
<@mail key='attachment' filename='wowowow.txt'>
This will go inside attachment.
</@mail>
