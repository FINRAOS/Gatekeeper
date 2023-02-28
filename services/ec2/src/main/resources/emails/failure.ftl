<html>
    <h1>Workflow Encountered A Problem!</h1>
    <div><h3>Request ID: ${request.getId()?long?c}</h3></div>
    <div><h3>Account: ${request.getAccount()}</h3></div>
    <div><h3>Platform: ${request.getPlatform()}</h3></div>

    <div><h4>Requestor Information</h4></div>
    <table style="width:100%;border-collapse:collapse" border="1">
        <tr>
            <th>Requestor</th>
            <th>Requestor Email</th>
            <th>User ID</th>
        </tr>

        <#list request.getUsers() as user>
            <tr style="text-align: center">
                <td>${user.getName()}</td>
                <td>${user.getEmail()! 'Unknown'}</td>
                <td>${user.getUserId()}</td>
            </tr>
        </#list>
    </table>




    <div><h4>Instances</h4></div>
    <ul>
        <#list request.getInstances() as instance>
            <li><b>${instance.getIp()?has_content?string(instance.getIp(),'Unknown IP')}</b> -- ${instance.getName()?has_content?string(instance.getName(), 'Unknown')} -- ${instance.getInstanceId()}</li>
        </#list>
    </ul>
</html>
