let me explain the full process we follow for updating SSL certificates in OCI for both internal and external load balancers.  

i start with monitoring. In the Governance dashboard, under *SSL Cert Expiry*, we have two segments: one for external LB certificates expiry and one for internal LB certificates expiry. Any certificate that is due to expire within the next 30 days will show with an amber warning light. Along with that, we also receive email alerts that include the compartment name, the load balancer name, and the exact expiry date of the certificate.  

For example, if the certificate for *LB_ASBAV_PSH_OTM_External* is expiring, the first step is to raise a request with the procurement team for a new SSL certificate. To support this request, we need to attach a CSR file. These are generated using the script available on our jump server, located under `/root/ssl_root/script`.  

Before running the CSR generation script, we must identify which URL is associated with the load balancer. There are two ways to do this. The first is through the OCI console: open the respective load balancer, go to the hostname section, and check the URL. The second way is by using the script I created, `check_lb_url.py`, which you run with the LB name. This script will display the associated URL along with any SAN entries. Once we have the URL, we also check it in a browser to confirm the expiry date and ensure it belongs to the correct SAN entry.  

After confirming the URL, we generate the CSR and private key using the script.


(As an additional step, I also take a backup of the CSR and private key. For this, I created a script called ssl_file_copy.py which backs up the existing CSR and private key files. The reason for taking this backup is that sometimes the DBA team or Fusion team may need to reuse the old private key. In certain cases, after attaching a new certificate to the load balancer, we face issues and need to roll back to the old certificate. While OCI Certificate Service Managed Certificates do provide the old certificate, they do not allow us to download the private key. Since we cannot share the private key directly, this backup process ensures we have the old CSR and private key safely stored, and we can use them if a rollback is required.)


 The CSR file is then attached to the procurement request. Within two to four days, the procurement team provides us with the new SSL certificate.  

Once we receive the certificate, the next step is to raise a change request for implementation. At this stage, we also inform the dependent teams, such as DBA and Fusion, so they are aware of the change.  

=========================


Up until last month, we were using *Load Balancer Managed Certificates* for updates. However, we are now moving to *Certificate Service Managed Service*.

(OCI Certificate Service: Centralized store, Version controlling, Secure storage, Easy integration) )

 Let me explain how that works. In OCI, navigate to *Identity & Security → Certificates*. I have already imported all LB SSL certificates compartment‑wise, and for clarity and easy maintenance, I name each certificate the same as its load balancer  name . To renew, select the correct certificate, click **Renew Certificate**, and then provide the new certificate details: paste the SSL certificate, the CA certificate, and the private key. OCI Certificate Service will automatically generate a new version of the certificate.  

Right now, not all load balancers have been migrated to Certificate Service Managed Service. Most are still using Load Balancer Managed Certificates. Our plan is to migrate them gradually during their expiry cycle. That means whenever a certificate is expiring, we will create the new certificate in Certificate Service Managed, then switch the LB resource from Load Balancer Managed to Certificate Service Managed. This is a one‑time task per load balancer. Once all LBs are migrated, future renewals will be seamless — OCI will automatically point the LB to the latest certificate version without manual intervention.  


==================================================================================================

David, would you like me to explain the steps for LB creation in detail, or just highlight the major issues we face with LB?


to create a Load Balancer in OCI, we first need the requirement details provided by the DBA. (These include backend servers, ports, and the load balancing policy.)

Next, we go to the OCI Console, under Networking, and select Load Balancer → Create Load Balancer. Here we choose the compartment where the LB will be created.

Next comes naming. We follow the convention: LB_ASBAV_EnvironmentName_ApplicationCategory_internal/external. As per the DBA’s instructions, we specify whether the Load Balancer is internal or external, which corresponds to whether it is private or public. A public LB is exposed to the internet, while a private LB is only accessible inside the VCN. After that, we attach the already created VCN and Subnet, again based on the details provided by the DBA. This step essentially gives the Load Balancer its address within the cloud network.

After that, we configure the backends. Based on the DBA’s sheet, we select the backend servers and choose the load balancing policy. Most of the time, we use Weighted Round Robin, which distributes traffic sequentially based on weights assigned to each server.

Along with backends, we configure health checks. Health checks are critical because they continuously monitor whether backend servers are alive and responding. If a server fails the health check, the LB automatically stops sending traffic to it until it recovers. The health check uses the protocol and port provided by the DBA, for example HTTP on port 80 or 7003. This ensures the LB only sends traffic to healthy servers.


Next, we configure the listener. The listener is like the front door of the Load Balancer. It waits for incoming traffic on a specific IP, port, and protocol. We give it a name using the convention LS_ASBAV_BackendPort_ListenerPort so it’s clear which ports are involved. The listener is also where we configure SSL/TLS if secure connections are required.


After that, we move to logging. Logging is important for troubleshooting and auditing. We enable both Access Logs and Error Logs. Access Logs record every request received by the LB, useful for auditing and traffic analysis. Error Logs capture connection failures and backend health issues, which help in troubleshooting. 

Once all of this is done, we press Submit, and the Load Balancer is created successfully.

After the Load Balancer is created, the next step is to configure Backend Sets and Hostnames. Since we already discussed how to create a backend set earlier, we can skip repeating that part here. What we need to focus on now is updating and creating the hostname. Once the hostname is set up, we attach both the backend set and the hostname to the listener that was configured previously. This final step completes the setup, making sure the Load Balancer knows exactly which servers to route traffic to and under which hostname they should be identified.
