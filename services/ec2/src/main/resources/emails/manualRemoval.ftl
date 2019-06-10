<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkred;" > Requesting Manual removal for Access Request</h3>
    <div><p>The system was unable to revoke temporary access for Access Request ${request.getId()} to the below <b>${request.getPlatform()! 'Unknown'}</b> instances for the following users in ${request.getAccount()}</p></div>

    <div><h4>Users</h4></div>
    <ul>
        <#list request.getUsers() as user>
            <li><b>${user.getUserId()}</b> -- ${user.getName()} -- ${user.getEmail()! 'Unknown'}</li>
        </#list>
    </ul>
    <div><h4>Instances</h4></div>
    <ul>
        <#list offlineInstances as instance>
            <li><b>${instance.getIp()?has_content?string(instance.getIp(),'Unknown IP')}</b> -- ${instance.getName()?has_content?string(instance.getName(), 'Unknown')} -- ${instance.getInstanceId()}</li>
        </#list>
    </ul>
    <div>
        <p>Can somebody please investigate the following instances and determine whether manual removal of access is needed for each user?</p>
    </div>
    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>