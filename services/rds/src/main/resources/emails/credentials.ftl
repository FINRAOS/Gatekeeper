<html>
    <h1> Gatekeeper</h1>
    <h3 style="color:darkgreen;" > Request [${request.getId()}] | Role: ${role.getRoleDescription()} - Here are your temporary credentials </h3>
    <div><p>Hello <b>${user.getName()}</b>
    <br/>
        Below is the password for each of your user(s) with role <b>${role}</b>
    <br/>
    <br/>
        <b>${password}</b>
    <br/>
    <div>
        <p>This temporary access will expire in ${request.getDays()} days time from retrieval of this message</p>
    </div>

    <div><p>Thanks!</p></div>
    <div><p>The Gatekeeper Admin Team</p></div>
</html>