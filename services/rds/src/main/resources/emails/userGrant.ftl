<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkgreen;" > Request [${request.getId()}] | Role: ${role.getRoleDescription()} - Temporary Access Granted</h3>
    <div><p>Hello <b>${user.getName()}</b>

        You have been granted temporary access as <b>${role.getRoleDescription()}</b> for ${request.getDays()} days to the following databases / schemas below:
        <br/>
        <br/>
        The login that you will use will be: <b>${userName}</b>
        <br/>
        <br/>
        Your password will be provided in another email denoting the request id + the role this message is associated with.

        <ul>
            <#list request.getAwsRdsInstances() as db>
                <li><b>${db.getInstanceId()}</b> -- ${db.getName()?has_content?string(db.getName(), 'Unknown')} -- ${db.getEngine()}</li>
                <b>Schemas/Tables Included: </b>
                <#assign currentDb = schemaTables[db.getName()]>
                    <#list currentDb[role] as schemaTable>
                        <ul>${schemaTable}</ul>
                    </#list>
            </#list>
        </ul>

    </div>
    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>