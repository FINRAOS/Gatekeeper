<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkgreen;" > Access Requested </h3>
    <div><p>An access request has been created in gatekeeper for <b>${request.getDays()! 'Unknown'}</b> days for the following users and instances in ${request.getAccount()}</p></div>
    <div><h4>Users</h4></div>
    <ul>
        <#list request.getUsers() as user>
            <li><b>${user.getName()}</b> -- ${user.getEmail()! 'Unknown'}</li>
        </#list>
    </ul>
    <div><h4>Databases</h4></div>
    <ul>
        <#list request.getAwsRdsInstances() as db>
            <li><b>${db.getInstanceId()}</b> -- ${db.getName()?has_content?string(db.getName(), 'Unknown')} -- ${db.getEngine()}</li>
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

    <div>
        <p>Please review and action this request on the gatekeeper app.</p>
    </div>

    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>