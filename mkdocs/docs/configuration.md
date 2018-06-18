## Configuration

Gatekeeper requires some configuration for it to run in your environment, see below for all of the supported configuration parameters

### Global

These parameters are used by both the EC2 and RDS Gatekeeeper services 

#### Authorization
Currently Gatekeeper only supports authorization through LDAP, the application expects authentication to be done via SSO, and looks for the username in a header.

|Property                                               | Description                                                             | Type                                                          |
|-------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| gatekeeper.auth.userIdHeader                           | The header in which gatekeeper looks to extract the authenticated user | string                                                        | 
| gatekeeper.auth.ldap.isActiveDirectory                 | Whether your LDAP server is Microsoft Active Directory or not (Nested groups are not supported with Non-Active Directory LDAP servers) | boolean
| gatekeeper.auth.ldap.objectClass                       | The Object class to look for users with (ex. posixAccount, person, user) | string 
| gatekeeper.auth.ldap.usersBase                         | The base in which the Users are stored on the LDAP Server (e.g. ou=Users,dc=example,dc=org) | string
| gatekeeper.auth.ldap.userDn                            | The DN for the user that gatekeeper connects as to query ldap (e.g. cn=admin,dc=example,dc=org ) | string
| gatekeeper.auth.ldap.userPassword                      | The password to the ldap user | string
| gatekeeper.auth.ldap.server                            | The domain of the LDAP server that gatekeeper should connect to | string
| gatekeeper.auth.ldap.base                              | The base that LDAP calls will be made off of (e.g. dc=example,dc=org) | string    
| gatekeeper.auth.ldap.usersCnAttribute                  | The cn | string
| gatekeeper.auth.ldap.usersIdAttribute                  | The uid | string
| gatekeeper.auth.ldap.usersNameAttribute                | The name | string
| gatekeeper.auth.ldap.usersEmailAttribute               | The email | string
| gatekeeper.auth.ldap.usersDnAttribute                  | The dn | string
| gatekeeper.auth.ldap.pattern                           | A regular expression that is used to extract group names from the LDAP results. The regular expression must have exactly one capture ( e.g developer_([A-Za-z0-9]+)_dev) pattern | string
| gatekeeper.auth.ldap.groupsBase                        | The base where your groups are stored on your organization's LDAP server (e.g. ou=groups) | string 

#### AWS
|Property | Description | Type |
|---------|-------------|------|
| gatekeeper.accountInfoEndpoint | The Endpoint gatekeeper calls to fetch the account data for all of your aws accounts | string
| gatekeeper.accountInfoUri | The URI where gatekeeper can call your account Info service. (Defaults to "accounts") | string
| gatekeeper.aws.proxyHost | (Optional) The Proxy Host. If you are not behind a proxy you can ignore this | string
| gatekeeper.aws.proxyPort | (Optional) The Proxy Port. If you are not behind a proxy you can ignore this | integer
| gatekeeper.aws.roleToAssume | The AWS IAM role that Gatekeeper will assume to interact with AWS (e.g. Xacnt_APP_GATEKEEPER)   | string 

#### EMAIL
Gatekeeper primarily communicates out temporary credentials via email, these are the properties gatekeeper requires for email

| Property | Description | Type|
|----------|-------------|------|
| gatekeeper.email.host | The host of your mail server | string
| gatekeeper.email.port | The port that it runs on | integer
| gatekeeper.email.from | The name of the Sender for Gatekeeper emails. | string
| gatekeeper.email.team | The team that will be maintaining gatekeeper on your environment. | string 
| gatekeeper.email.approverEmails | The email address for the group who will be handling approvals | string
| gatekeeper.email.opsEmails | The email address for gatekeeper to reach out to the Ops team | string

#### DATABASE
| Property | Description | Type|
|----------|-------------|------|
| gatekeeper.db.url | The Postgres Database URL to connect to | String
| gatekeeper.db.port | The Port of the database | integer
| gatekeeper.db.database | The name of the database | string
| gatekeeper.db.ssl | Whether to enable SSL or not | boolean
| gatekeeper.db.sslMode | The SSL mode to use | string
| gatekeeper.db.sslCert | The SSL certificate location to use ( we provide the RDS cert in the container ) | string
| gatekeeper.db.user | The DB user to log in as | string
| gatekeeper.db.password | The DB user password | string

### EC2
These configurations are specific to Gatekeeper EC2

#### DATABASE
| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.db.schema | The schema in which gatekeeper operates its tables | gatekeeper_ec2

#### AUTHORIZATION
| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.auth.approverGroup | The LDAP group containing the gatekeeper approvers | string
| gatekeeper.auth.supportGroup | The LDAP group containing the gatekeeper support staff | string

#### APPROVALS

You will set these once for each environment you have in your company

Here's an example (taken from [config/ec2_service_config.env](config/ec2_service_config.env))

```dotenv
gatekeeper.approvalThreshold.dev.dev=45
gatekeeper.approvalThreshold.dev.qa=44
gatekeeper.approvalThreshold.dev.prod=-1
gatekeeper.approvalThreshold.dev.test=4

gatekeeper.approvalThreshold.support.dev=42
gatekeeper.approvalThreshold.support.qa=41
gatekeeper.approvalThreshold.support.prod=2
gatekeeper.approvalThreshold.support.test=5
```

This tells Gatekeeper the following:
1. For a developer role the maximum hours the user can request is:
    - <= 45 for dev environment
    - <= 44 for qa environment
    - approval is **always** required for prod
    - <= 4 for test
2. For a support role the maximum hours the user can request without requiring approval is:
   - <= 42 hours for dev environment
   - <= 41 for qa environment
   - <= 2 for prod environment
   - <= 5 for test environment


| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.approvalThreshold.dev.<environment> | The approval threshold for a given environment for the dev role | integer
| gatekeeper.approvalThreshold.support.<environment> | The approval threshold for a given environment for the support role | integer

#### Configuring SSM Documents

Gatekeeper for EC2 resources requires SSM documents to be staged to AWS before being able to generate a temporary user. There are 4 different documents that Gatekeeper requires you to provide in order to Create and Delete temporary users. These documents are:

1. Create document for Linux 
2. Delete document for Linux
3. Create document for Windows
4. Delete document for Windows

We provide examples of what we are currently using in our environment, you can find these documents located in the **aws/ssm** folder; You ultimately have control of the content in these documents should you have differing requirements in your environment. 

If you decide to write your own document, please keep in mind that gatekeeper will be passing on the following parameters to the documents. 

For create documents (linux):
1. **userName** - Gatekeeper provides The Username for the temporary user. Currently gatekeeper provides this to the SSM document in the form of **gk-$username**
2. **publicKey** - Gatekeeper generates the public key for the user and provides it to the SSM document
3. **executionTimeout** - Gatekeeper provides The maximum time to wait for the script to succeed

For windows gatekeeper functions differently as we do not want to send the user password directly to SSM (these parameters get logged to the AWS console, so the password would be exposed which is insecure), instead we rely on SSM to generate the user password on the Windows box and have the box send the username and password directly to the requestor(s) 

For create documents (windows):
1. **opsEmail** - Gatekeeper provides the email of the operations team so that they are notified if any issue occurs 
2. **teamEmail** - Gatekeeper provides the team email to notify the team that supports gatekeeper if it encounters any issues
3. **userId** - Gatekeeper provides the User Id being added
4. **userName** - Gatekeeper provides the User Name being added
5. **account** - Gatekeeper provides the account the windows instance exists in
6. **mailFrom** - Gatekeeper provides the sender of the email (who users of gatekeeper see the email is from)
7. **smtpServer** - **YOU MUST PROVIDE THIS IN THE TEMPLATE** the SMTP server that will be responsible for relaying the mail
8. **hours** - Gatekeeper provides the amount of time the request is for
9. **accessRequest** - Gatekeeper provides the access request ID 
10. **executionTimeout** - Gatekeeper provides The maximum time to wait for the script to succeed


For delete documents:
1. **userName** - Gatekeeper provides the Username for the temporary user that should be removed.
2. **executionTimeout** - Gatekeeper provides the maximum time to wait for the script to succeed.

For convenience, we have provided SSM scripts with the script part left blank if you need to create your own script to set up a user / remove them. You can learn more about SSM [here](https://docs.aws.amazon.com/systems-manager/latest/userguide/create-ssm-doc.html)

The following are configuration properties which tell gatekeeper which documents to run for which action:

| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.ssm.linux.create.documentName | For Linux: The name of the SSM document Gatekeeper will call to create a user | string
| gatekeeper.ssm.linux.create.timeout | For Linux: The Amount of time Gatekeeper should wait for the ssm create call to complete | integer
| gatekeeper.ssm.linux.create.waitInterval | For Linux: The Interval that Gatekeeper polls the SSM create for completion | integer
| gatekeeper.ssm.linux.delete.documentName | For Linux: The name of the SSM document Gatekeeper will call to delete a user | string
| gatekeeper.ssm.linux.delete.timeout | For Linux: The Amount of time Gatekeeper should wait for the ssm delete call to complete | integer 
| gatekeeper.ssm.linux.delete.waitInterval | For Linux: The Interval that Gatekeeper polls the SSM delete for completion | integer
| gatekeeper.ssm.windows.create.documentName | For Windows: The name of the SSM document Gatekeeper will call to create a user | string
| gatekeeper.ssm.windows.create.timeout | For Windows: The Amount of time Gatekeeper should wait for the ssm create call to complete | integer
| gatekeeper.ssm.windows.create.waitInterval | For Windows: The Interval that Gatekeeper polls the SSM create for completion | integer
| gatekeeper.ssm.windows.delete.documentName | For Windows: The name of the SSM document Gatekeeper will call to delete a user
| gatekeeper.ssm.windows.delete.timeout | For Windows: The Amount of time Gatekeeper should wait for the ssm delete call to complete | integer 
| gatekeeper.ssm.windows.delete.waitInterval | The Interval that Gatekeeper polls the SSM delete for completion | integer

### RDS
These configurations are specific to Gatekeeper RDS

| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.db.schema | The schema in which gatekeeper operates its tables  | string
| gatekeeper.requiredSecurityGroup | The Security Group in which Gatekeeper RDS requires for connectivity | string 
| gatekeeper.rds.postgresMinServerVersion | The minimum postgrs server to use | string 
| gatekeeper.rds.ssl | Whether Gatekeeeper-RDS should use SSL or not to connect | boolean
| gatekeeper.rds.connectTimeout | The timeout (in milliseconds) to wait for a connection | Integer
| gatekeeper.rds.sslMode | The mode of SSL verification to use while Gatekeeper RDS connects to an instance | string
| gatekeeper.rds.sslCert | The SSL Cert to use for gatekeeper to connect (We provide the RDS cert) | string
| gatekeeper.rds.user | The gatekeeper user to log into the RDS instance. | string
| gatekeeper.rds.password | The password for the gatekeeper user | string

| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.auth.approverGroup | The LDAP group containing Gatekeeper Approvers for RDS | string
| gatekeeper.auth.dbaGroupsPattern | A regular expression to extract group(s) of DBAS from ldap groups, requires one capture | string
| gatekeeper.auth.opsGroupsPattern | A regular expression to extract group(s) of Ops Members from ldap groups. requires one capture | string
| gatekeeper.auth.devGroupsPattern | A regular expression to extract group(s) of Dev Members from ldap groups. requires one capture | string

#### APPROVALS

Like the EC2 service, the RDS approval criteria needs to be set, however this configuration is handled at a more granular level.

Here is an example from [config/rds_service_config.env](config/rds_service_config.env) 

```dotenv
    gatekeeper.approvalThreshold.dev.readonly.dev=120
    gatekeeper.approvalThreshold.dev.readonly.qa=100
    gatekeeper.approvalThreshold.dev.readonly.prod=-1

    gatekeeper.approvalThreshold.dev.datafix.dev=180
    gatekeeper.approvalThreshold.dev.datafix.qa=180
    gatekeeper.approvalThreshold.dev.datafix.prod=-1
    
    gatekeeper.approvalThreshold.dev.dba.dev=180
    gatekeeper.approvalThreshold.dev.dba.qa=2
    gatekeeper.approvalThreshold.dev.dba.prod=-1
    
    gatekeeper.approvalThreshold.ops.readonly.dev=-1
    gatekeeper.approvalThreshold.ops.readonly.qa=-1
    gatekeeper.approvalThreshold.ops.readonly.prod=180
    
    gatekeeper.approvalThreshold.ops.datafix.dev=-1
    gatekeeper.approvalThreshold.ops.datafix.qa=-1
    gatekeeper.approvalThreshold.ops.datafix.prod=1
    
    gatekeeper.approvalThreshold.ops.dba.dev=-1
    gatekeeper.approvalThreshold.ops.dba.qa=-1
    gatekeeper.approvalThreshold.ops.dba.prod=-1
    
    gatekeeper.approvalThreshold.dba.readonly.dev=180
    gatekeeper.approvalThreshold.dba.readonly.qa=180
    gatekeeper.approvalThreshold.dba.readonly.prod=180
    
    gatekeeper.approvalThreshold.dba.datafix.dev=180
    gatekeeper.approvalThreshold.dba.datafix.qa=180
    gatekeeper.approvalThreshold.dba.datafix.prod=180
    
    gatekeeper.approvalThreshold.dba.dba.dev=180
    gatekeeper.approvalThreshold.dba.dba.qa=180
    gatekeeper.approvalThreshold.dba.dba.prod=-1
```
Here's an explanation of what this configuration translates to in Gatekeeper:

1. For Dev Role 
    - Requesting readonly
        - < 120 days for dev environment
        - < 180 days for qa environment
        - approval is **always** required for prod
    - Requesting datafix
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - approval is **always** required for prod
    - Requesting dba
        - <= 180 days for dev environment
        - <= 2 days for qa environment
        - approval is **always** required for prod
          
2. For Ops Role
    - Requesting readonly
        - approval is **always** required for dev
        - approval is **always** required for qa
        - <= 180 days for prod
    - Requesting datafix
        - approval is **always** required for dev
        - approval is **always** required for qa
        - <= 1 days for prod
    - Requesting dba
        - approval is **always** required for dev
        - approval is **always** required for qa
        - approval is **always** required for prod
3. For DBA Role
    - Requesting readonly
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - <= 180 days for prod environment
    - Requesting datafix
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - <= 180 days for prod environment
    - Requesting dba
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - approval is **always** required for prod

You can see the format for these properties below:

| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.approvalThreshold.dev.readonly.<environment>| The threshold in which devs can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dev.datafix.<environment>| The threshold in which devs can request datafix access before requiring approval | integer
| gatekeeper.approvalThreshold.dev.dba.<environment>| The threshold in which devs can request dba access before requiring approval | integer
| gatekeeper.approvalThreshold.ops.readonly.<environment>| The threshold in which ops can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.ops.datafix.<environment>| The threshold in which ops can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.ops.dba.<environment>| The threshold in which ops can request dba access before requiring approval  | integer
| gatekeeper.approvalThreshold.dba.readonly.<environment>| The threshold in which dbas can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dba.datafix.<environment>| The threshold in which dbas can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dba.dba.<environment>| The threshold in which dbas can request readonly access before requiring approval | integer
| gatekeeper.overridePolicy.maxDays | The maximum amount of time a user can request temporary access for | integer
| gatekeeper.overridePolicy.<user_role>.<db_role>.<environment> | for a given user role, db role and environment, this Overrides the maximum amount of days a user can request temporary access for | integer