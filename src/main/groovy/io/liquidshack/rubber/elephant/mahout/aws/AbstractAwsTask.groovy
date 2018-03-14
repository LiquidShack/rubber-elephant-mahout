package io.liquidshack.rubber.elephant.mahout.aws

import com.amazonaws.services.certificatemanager.AWSCertificateManager
import com.amazonaws.services.certificatemanager.AWSCertificateManagerClientBuilder
import com.amazonaws.services.ecr.AmazonECRClient
import com.amazonaws.services.ecr.AmazonECRClientBuilder

import io.liquidshack.rubber.elephant.mahout.common.AbstractTask
import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException

abstract class AbstractAwsTask extends AbstractTask {


	AWSCertificateManager getAcmClient() {
		String region = getRegion()
		assert region && region != 'null' : new RubberElephantMahoutException('`region` needs to be set in the Aws configuration block')

		AWSCertificateManager client  = AWSCertificateManagerClientBuilder.standard()
				.withRegion(region)
				.build()
		return client
	}

	AmazonECRClient getEcrClient() {
		String region = getRegion()
		assert region && region != 'null' : new RubberElephantMahoutException('`region` needs to be set in the Aws configuration block')

		AmazonECRClient client = AmazonECRClientBuilder.standard()
				.withRegion(region)
				.build()
		return client
	}
}
