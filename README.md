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

