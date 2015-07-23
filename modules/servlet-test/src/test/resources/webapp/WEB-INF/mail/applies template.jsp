<%@ page pageEncoding="UTF-8" trimDirectiveWhitespaces="true" session="false" %>
<%@ taglib prefix="m" uri="http://emphased.net/malle" %>

<m:settings bodyEncoding="base64"/>

<m:from address="from@example.com"/>

<m:to address="${to}">
    ${toPersonal}
</m:to>

<m:cc address="cc@example.com" personal="Malle ♡ Unicode"/>

<m:cc>
    cc2@example.com,
    "CC 3" <cc3@example.com>,
    "♡ Unicode ♡" <cc4@example.com>
</m:cc>

<m:subject>
    From Malle With ♡♡♡


</m:subject>

<m:priority value="1"/>

<m:plain>
    Plain hello ☺
</m:plain>

<m:html>
    <p>Html hello ☺</p>
</m:html>

<m:attachment name="classpath.txt" resource="classpath.txt"></m:attachment>

<m:inline id="inline.png" resource="image1.png"/>
