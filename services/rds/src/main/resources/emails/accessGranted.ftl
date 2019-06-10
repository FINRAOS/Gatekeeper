<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkgreen;" > Your Access Request was approved </h3>
    <div><p>Your access request for <b>${request.getDays()! 'Unknown'}</b> days was approved for the following users and instances in ${request.getAccount()}</p></div>
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
    <div>
        <p>Each user on the request will be emailed separately with their corresponding pem files.</p>
    </div>


    <div><h4>Approver Comments:</h4></div>
    <div>
        <#if request.getApproverComments()??>
            <blockquote>${request.getApproverComments()?has_content?string(request.getApproverComments()!?replace('\n', '<br>'),'No reason was provided')}</blockquote>
        <#else>
            <blockquote>No reason was provided</blockquote>
        </#if>
    </div>

    <div>
        <p style="color: darkred">If you have any questions or concerns please reach out to the Gatekeeper approvers at: ${approverDL}</p>
    </div>

    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>