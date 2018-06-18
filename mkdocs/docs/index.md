## What is Gatekeeper?
Gatekeeper is an application that we developed here at FINRA to manage temporary access to EC2 and RDS resources in AWS in an automated fashion

## How does Gatekeeper work?
### EC2
For EC2 Gatekeeper leverages AWS SSM (Amazon Simple System Manager) to create (and remove) temporary users on EC2 Instances 
### RDS
For RDS Gatekeeper leverages the AWS RDS API to be able to connect to supported RDS instances and generate users with generic sql queries specific to the engine of the RDS Instance. Currently gatekeeper supports mysql and postgres

## AWS Re:Invent 2017
See our blog post [here](https://aws.amazon.com/blogs/mt/finra-gatekeeper-amazon-ec2-access-management-system-using-amazon-ec2-systems-manager/) for more information

We also had a demo of the application in action at AWS Re:invent 2017, to see that you can find the following links below:

### Full Talk
<a href="https://www.youtube.com/watch?feature=player_embedded&v=VJf1i_b-2Kc&t=1904" target="_blank"><img src="http://img.youtube.com/vi/VJf1i_b-2Kc/hqdefault.jpg" alt="Gatekeeper @ Re:Invent 2017" width="480" height="360" border="10" /></a> 

### Demo
<a href="https://www.youtube.com/watch?feature=player_embedded&v=VJf1i_b-2Kc&t=44m21s" target="_blank"><img src="http://img.youtube.com/vi/VJf1i_b-2Kc/hqdefault.jpg" alt="Gatekeeper @ Re:Invent 2017" width="480" height="360" border="10" /></a> 

## Why Gatekeeper?
In a transient environment where application instances are constantly being torn down / spun up managing user access gets complicated. Gatekeeper resolves this by automating the creation of that user, and making sure that the user is only valid for a temporary time period. 

Since access is automated and on a temporary basis, using Gatekeeper can reduce the amount of permanent users that get set up on an instance, allowing your resources to be more secure.

Gatekeeper also stores and logs all of the access requests making user access fully auditable.

