<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkred;" > Your Access has expired </h3>
    <div><p>Your access for <b>${request.getDays()! 'Unknown'}</b> days has expired for the following databases in ${request.getAccount()}</p></div>
    <ul>
        <#list request.getAwsRdsInstances() as db>
            <li><b>${db.getInstanceId()}</b> -- ${db.getName()?has_content?string(db.getName(), 'Unknown')} -- ${db.getEngine()}</li>
        </#list>
    </ul>

    <div>
        <p>If you need more time on the box please go to Gatekeeper and request more access.</p>
    </div>

    <div>
        <p style="color: darkred">If you have any questions or concerns please reach out to the Gatekeeper approvers at: ${approverDL}</p>
    </div>

    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>