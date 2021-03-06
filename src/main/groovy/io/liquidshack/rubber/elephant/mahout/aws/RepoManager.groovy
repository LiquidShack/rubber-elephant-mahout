package io.liquidshack.rubber.elephant.mahout.aws

import com.amazonaws.services.ecr.AmazonECRClient
import com.amazonaws.services.ecr.model.CreateRepositoryRequest
import com.amazonaws.services.ecr.model.CreateRepositoryResult
import com.amazonaws.services.ecr.model.DescribeRepositoriesRequest
import com.amazonaws.services.ecr.model.DescribeRepositoriesResult
import com.amazonaws.services.ecr.model.Repository
import com.amazonaws.services.ecr.model.RepositoryNotFoundException

import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException

/*
 * 
 */
class RepoManager extends AbstractAwsTask {

	@Override
	void runCommand() {
		logger.quiet 'Running task [RepoManager]'

		String region = getAwsExt().region
		String repositoryName = getAwsExt().repositoryName
		assert region?.trim() && region != 'null': new RubberElephantMahoutException('`region` needs to be set')
		assert repositoryName?.trim() && repositoryName != 'null': new RubberElephantMahoutException('`repositoryName` needs to be set')

		logger.lifecycle "Looking for repository: $repositoryName"
		AmazonECRClient client = getEcrClient();
		DescribeRepositoriesRequest request = new DescribeRepositoriesRequest().withRepositoryNames(repositoryName)
		Repository repository;
		try {
			DescribeRepositoriesResult response = client.describeRepositories(request)
			repository = response.repositories.get(0)
		}
		catch (RepositoryNotFoundException e) {
			if (create) {
				repository = create()
			}
			else {
				throw new RubberElephantMahoutException('Could not find repository ' + repositoryName)
			}
		}

		setRepository(repository)
		logger.lifecycle 'Using repository: ' + repository
	}

	private Repository create(String repositoryName) {

		AmazonECRClient client = getEcrClient();
		CreateRepositoryRequest request = new CreateRepositoryRequest()
				.withRepositoryName(repositoryName)
		CreateRepositoryResult result = client.executeCreateRepository(request)

		logger.lifecycle "Created repository " + result.repository
		return result.repository
	}
}
