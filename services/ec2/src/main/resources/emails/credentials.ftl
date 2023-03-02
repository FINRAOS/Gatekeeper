<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkgreen;" > Request [${request.getId()?long?c}] - Here is your temporary credential </h3>
    <div><p>Hello <b>${user.getName()}</b>

        You have been granted temporary access to the following <b>${request.getPlatform()! 'Unknown'}</b> instances in ${request.getAccount()}</p></div>
    <ul>
        <#list request.getInstances() as instance>
            <li><b>${instance.getIp()?has_content?string(instance.getIp(),'Unknown IP')}</b> -- ${instance.getName()?has_content?string(instance.getName(), 'Unknown')} -- ${instance.getInstanceId()}</li>
            <#if instance.getStatus()!="Online">
                <ul>
                    <li style="color:darkred;">User could not be added because SSM is unable to run.</li>
                </ul>
            <#elseif instanceStatus[instance.getInstanceId()] != "Success">
                <ul>
                    <li style="color:darkred;">SSM execution failed on this instance. Please submit another request.</li>
                </ul>
            </#if>
        </#list>
    </ul>
    <div>
        <p>The accompanying username will be sent in a separate email. This temporary credential will expire in ${request.getHours()} hours time from retrieval of this message</p>
    </div>

    <#if changeDisclaimer??>
        <#if changeDisclaimer != "">
            <p style="color: darkred">
                ${changeDisclaimer}
            </p>
        </#if>
    </#if>

    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admins</p></div>
</html>
