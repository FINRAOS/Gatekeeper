<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkred;" > Your Access has expired </h3>
    <div><p>Your access for <b>${request.getHours()! 'Unknown'}</b> hours has expired for the following <b>${request.getPlatform()! 'Unknown'}</b> instances in ${request.getAccount()}</p></div>
    <ul>
        <#list request.getInstances() as instance>
            <li><b>${instance.getIp()?has_content?string(instance.getIp(),'Unknown IP')}</b> -- ${instance.getName()?has_content?string(instance.getName(), 'Unknown')} -- ${instance.getInstanceId()}</li>
        </#list>
    </ul>
    <div>
        <p>If you need more time on the box please go to Gatekeeper and request more access.</p>
    </div>
    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admins</p></div>
</html>