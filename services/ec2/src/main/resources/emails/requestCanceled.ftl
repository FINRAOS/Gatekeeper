<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkgreen;" > Access Request Canceled </h3>
    <div><p>Access request with ID: <b>${request.getId()}</b> for <b>${request.getHours()! 'Unknown'}</b> hours has been canceled for the following users and <b>${request.getPlatform()! 'Unknown'}</b> instances in ${request.getAccount()}</p></div>
    <div><h4>Users</h4></div>
    <ul>
        <#list request.getUsers() as user>
            <li><b>${user.getName()}</b> -- ${user.getEmail()! 'Unknown'}</li>
        </#list>
    </ul>
    <div><h4>Instances</h4></div>
    <ul>
        <#list request.getInstances() as instance>
            <li><b>${instance.getIp()?has_content?string(instance.getIp(),'Unknown IP')}</b> -- ${instance.getName()?has_content?string(instance.getName(), 'Unknown')} -- ${instance.getInstanceId()}</li>
        </#list>
    </ul>
    <div>
        <p>No further action is required.</p>
    </div>

    <div>
        <p style="color: darkred">If you have any questions or concerns please reach out to the Gatekeeper approvers at: ${approverDL}</p>
    </div>

    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>