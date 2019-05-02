<html>
    <h1> Gatekeeper</h1>
    <h3  style="color:darkred;"> Temporary account could not be created </h3>
    <div>
        <p>Hello <b>${user.getName()}</b>. A temporary account could not be created on the following <b>${request.getPlatform()! 'Unknown'}</b> instances in ${request.getAccount()} </p>
    </div>
    <ul>
        <#list instances as instanceId>
            <li><b>${instanceId}</b></li>
        </#list>
    </ul>
    <div>
        <p>Please verify the instances are properly configured and submit a new request if necessary.</p>
    </div>
    <div>
        <p>Thanks!</p>
    </div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>