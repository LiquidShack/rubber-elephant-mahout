package io.liquidshack.rubber.elephant.mahout.aws

import org.gradle.api.tasks.Input

import com.amazonaws.services.ecr.model.Repository

class Aws {

	@Input
	String region

	@Input
	String repositoryName

	@Input
	String version

	@Input
	String namespace

	@Input
	String s3region

	@Input
	String s3bucket

	@Input
	boolean createRepo = true

	Repository repository

	String certificate
}
