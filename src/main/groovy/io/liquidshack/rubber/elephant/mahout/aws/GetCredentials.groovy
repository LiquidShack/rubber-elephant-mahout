package io.liquidshack.rubber.elephant.mahout.aws

import com.amazonaws.services.ecr.AmazonECRClient
import com.amazonaws.services.ecr.model.AuthorizationData
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult
import com.amazonaws.services.ecr.model.Repository

import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException
import io.liquidshack.rubber.elephant.mahout.docker.Credentials

class GetCredentials extends AbstractAwsTask {

	@Override
	void runCommand() {
		logger.lifecycle 'Running task [GetCredentials]'

		Aws aws = project.extensions.getByName(AwsPlugin.AWS_EXT)
		String region = aws.region

		Repository repository = aws.repository
		assert repository : new RubberElephantMahoutException('No repository has been fetched')

		String registryId = repository.registryId

		AmazonECRClient client = getEcrClient()
		GetAuthorizationTokenRequest request = new GetAuthorizationTokenRequest().withRegistryIds(registryId)
		GetAuthorizationTokenResult result = client.getAuthorizationToken(request)
		AuthorizationData authData = result.authorizationData.first()
		String token = authData.authorizationToken

		String[] ecrCreds = new String(token.decodeBase64(), 'US-ASCII').split(':')

		Credentials credentials = new Credentials();
		credentials.user = ecrCreds[0]
		credentials.password = ecrCreds[1]
		credentials.url = authData.proxyEndpoint

		logger.lifecycle 'Created ECR credentials (for docker push)'
		logger.info 'user: ' + credentials.user
		logger.info 'password: ' + credentials.password
		logger.info 'url: ' + credentials.url
		setCredentials(credentials)
	}
}
