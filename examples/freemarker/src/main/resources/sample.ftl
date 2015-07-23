<#assign from = '${from!mail_user}'/>
<#assign to_firstName = '${to_firstName!"Mr"}'/>
<#assign to_lastName = '${to_lastName!"X"}'/>

<@mail cmd='from' address='${from}'/>
<@mail cmd='to' address='${to}' personal='${to_firstName} ${to_lastName}'/>

<#-- Leading and trailing whitespace in the header values gets trimmed. -->
<@mail cmd='subject' value='A message for ${to_firstName} ${to_lastName} sent using Malle and Freemarker'/>

<#-- Whitespace in the text/html part gets trimmed. -->
<@mail cmd='html'><#escape x as x?html>
    <p>Hello ${to_firstName} ${to_lastName},</p>

    <p>
        <#-- Unicode works as expected. -->
        This is a sample mail from ${from} sent to you using Malle ♡ Freemarker.
    </p>

    <p>
        <img src="cid:cat.jpg"/>
    </p>
</#escape></@mail>

<#-- Only trailing whitespace in the text/plain part gets trimmed. -->
<@mail cmd='plain'>
Hello ${to_firstName} ${to_lastName},

It's sad that you don't have an HTML-capable mail reader :(
</@mail>

<@mail cmd='attachment' name='image1.png' resource='image1.png'/>

<@mail cmd='inline' id='cat.jpg' url='https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/So_happy_smiling_cat.jpg/411px-So_happy_smiling_cat.jpg'/>
