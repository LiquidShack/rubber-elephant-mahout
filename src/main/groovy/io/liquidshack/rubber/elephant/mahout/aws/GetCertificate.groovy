package io.liquidshack.rubber.elephant.mahout.aws

import com.amazonaws.services.certificatemanager.AWSCertificateManager
import com.amazonaws.services.certificatemanager.model.CertificateSummary
import com.amazonaws.services.certificatemanager.model.ListCertificatesRequest
import com.amazonaws.services.certificatemanager.model.ListCertificatesResult
import com.amazonaws.services.certificatemanager.model.RequestCertificateRequest
import com.amazonaws.services.certificatemanager.model.RequestCertificateResult

/*
 * CERTIFICATE_ARN ?= $(shell cert=$$(aws acm list-certificates --region $(REGION) --query 'CertificateSummaryList[?contains(DomainName, `$(NAMESPACE).$(CLUSTER)`)==`true`].CertificateArn' --output text); \
 * [ -n "$$cert" ] && echo $$cert || aws acm request-certificate --region $(REGION) --domain-name '*.$(NAMESPACE).$(CLUSTER)' --output text)
 */
class GetCertificate extends AbstractAwsTask {

	@Override
	void runCommand() {
		logger.lifecycle 'Running task [GetCertificate]'

		String contxt = getContext()
		String namespace = getNamespace()
		assert contxt?.trim() && contxt !='null' : '`context` needs to be set'
		assert namespace?.trim() && namespace != 'null' : '`namespace` needs to be set'

		AWSCertificateManager client  = getAcmClient()

		ListCertificatesRequest request = new ListCertificatesRequest()
		ListCertificatesResult result =	client.listCertificates(request)
		String domain = namespace + '.' + contxt

		CertificateSummary found = result.certificateSummaryList.find { certificate ->
			certificate.domainName.contains(domain)
		}
		if (found) {
			logger.lifecycle 'Found existing certificate'
			setCertificate(found.certificateArn)
		}
		else {
			RequestCertificateRequest newCertificateRequest = new RequestCertificateRequest()
					.withDomainName(domain)
			RequestCertificateResult newCertificateResult = client.requestCertificate(newCertificateRequest)
			logger.lifecycle 'Created new certificate'
			setCertificate(newCertificateResult.certificateArn)
		}
	}
}