<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkgreen;" > Access Requested </h3>
    <div><p>An access request has been created in gatekeeper for <b>${request.getHours()! 'Unknown'}</b> hours for the following users and <b>${request.getPlatform()! 'Unknown'}</b> instances in ${request.getAccount()}</p></div>
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

    <h4>Ticket for request</h4>
    <div>
        <#if request.getTicketId()??>
            <blockquote>${request.getTicketId()?has_content?string(request.getTicketId()!?replace('\n', '<br>'),'No ticket was provided')}</blockquote>
        <#else>
            <blockquote>No ticket was provided</blockquote>
        </#if>
    </div>

    <h4>Reason for request</h4>
    <div>
        <#if request.getRequestReason()??>
            <blockquote>${request.getRequestReason()?has_content?string(request.getRequestReason()!?replace('\n', '<br>'),'No reason was provided')}</blockquote>
        <#else>
            <blockquote>No reason was provided</blockquote>
        </#if>
    </div>


    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>
