<@mail cmd='settings' body_encoding='base64'/>

<@mail cmd='from' address="from@example.com"/>

<@mail cmd='to' address="${to}" personal="${toPersonal}"/>

<@mail cmd='cc' address="cc@example.com" personal="Malle ♡ Unicode"/>

<@mail cmd='cc'>
    cc2@example.com,
    "CC 3" <cc3@example.com>,
    "♡ Unicode ♡" <cc4@example.com>
</@mail>

<@mail cmd='plain'>
    Plain hello ☺
</@mail>

<@mail cmd='html'>
    <p>Html hello ☺</p>
</@mail>

<@mail cmd='attachment' name="classpath.txt" resource="classpath.txt"/>

<@mail cmd='inline' id="inline.png" resource="image1.png"/>

