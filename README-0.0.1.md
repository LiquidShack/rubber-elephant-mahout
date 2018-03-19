# rubber-elephant-mahout

Sawadee!  Why Rubber Elephant Mahout?  Well, let's face it - nothing on land compares to an elephant for sheer size and elagance!
This gradle plugin project was created to fulfill a specific DevOps flow, all bundled into 1 `build.gradle` file.

### Components
This project includes 4 integration components:
* Git
* AWS (ECR, ACM, S3) 
* Docker
* Kubernetes

### Flow

The DevOps flow supported for this project, viewed from riding upon an elephants back:
*Git Event -> AWS CodeBuild -> Docker Image -> Kubernetes Deploy*

First, a Git event happens.  Project is set up with AWS WebHook to trigger on Push, Pull Request, and Release.
AWS CodeBuild then triggers the chain of events for the project.

A task in the deploy stack will query the Git repository, eg: `sh ./gradlew gitQuery`
This will determine the environment (DEVELOPMENT | QA | PRODUCTION)
* If it's a *Pull Request* it will build to DEVELOPMENT
* If it's a *Push* it will check branch.  If master branch, then build to QA, else DEVELOPMENT.  This will differentiate between code commits and merges to master.
* If it's a *Release*, determined by tag starting with `v` then it will build to PRODUCTION

Gotchas with this: Git events only allows so much tweaking of events, so *Pushing* a new branch or checking code into a personal branch will trigger the build to DEVELOPMENT.  If you check code directly into the "master", it will trigger build to QA.

Next up is the build command itself.  It will call the project `build` lifecycle to create the application .war or .jar file.

Next task is `DockerImageBuilder` which will create an image based on the *Dockerfile*

After that, we need to call some other tasks, in this order more or less, to populate the Docker and Kubernetes clients with credentials and info to publish and deploy.

`RepoManager` will get the EC2 Container Registry (ECR) repository for which we will publish the docker image to.  If the repository does not exist, the option to create it on the fly is there.  This uses the Amazon ECR Client and will use credentials that are set up in the AWS environment.

`GetCredentials` depends on `RepoManager` since the repository's registryId is part of the credentials.  This uses the Amazon ECR Client as well.

`DockerImageTagger` depends on `GetCredentials` as the image tag format is *repositoryUri:version*

`DockerImagePusher` depends on `GetCredentials` and `DockerImageTagger` as we need those supplied to push the image to the ECR repository.

`GetCertificate` gets the certificate we will need to deploy the Kubernetes service for this project.  This uses the AWS Certificate Manage (ACM) client.

`KubernetesDeploy` is the final stage which will grab the configuration files and deploy the project to the cluster (context).

These tasks can all be wrapped up into a single task, eg:
```groovy
task deploy(type: GradleBuild) {
	tasks = ['clean', 'build', 'buildImage', 'gitQuery', 'getRepository', 'getCredentials', 'tagImage', 'pushImage', 'getCertificate', 'deployImage']
}
```
then the build command simply becomes:
`./gradlew deploy`

And that's the flow in a nutshell!

### build.gradle

Declare dependencies in the *buildscript* block.
*TODO:*
1. Hoping to remove having to declare the dependencies on third parties
2. Need to deploy plugin to a repo instead of declaring as `files(...)`

```groovy
buildscript {
	repositories {
		//...
	}

	dependencies {
		classpath files('build-plugins/rubber-elephant-mahout.jar')
		classpath('com.amazonaws:aws-java-sdk-ecr:1.11.283')
		classpath('com.amazonaws:aws-java-sdk-acm:1.11.283')
		classpath('com.amazonaws:aws-java-sdk-s3:1.11.283')
		classpath('com.github.docker-java:docker-java:3.0.14')
		classpath('io.fabric8:kubernetes-client:3.1.8')
		classpath('org.eclipse.jgit:org.eclipse.jgit:4.11.0.201803080745-r')
	}
}
```

Apply the rubber elephant mahout plugins and imports.  The mahout uses type for mapping tasks, so needs the declarations to find them.

```groovy
apply plugin: 'rubber.elephant.mahout.docker'
apply plugin: 'rubber.elephant.mahout.aws'
apply plugin: 'rubber.elephant.mahout.kubernetes'
apply plugin: 'rubber.elephant.mahout.git'

import io.liquidshack.rubber.elephant.mahout.docker.DockerImageBuilder
import io.liquidshack.rubber.elephant.mahout.docker.DockerImagePusher
import io.liquidshack.rubber.elephant.mahout.docker.DockerImageTagger
import io.liquidshack.rubber.elephant.mahout.kubernetes.KubernetesDeploy
import io.liquidshack.rubber.elephant.mahout.kubernetes.EncryptConfig
import io.liquidshack.rubber.elephant.mahout.kubernetes.Kubernetes
import io.liquidshack.rubber.elephant.mahout.aws.GetCertificate
import io.liquidshack.rubber.elephant.mahout.aws.GetCredentials
import io.liquidshack.rubber.elephant.mahout.aws.RepoManager
import io.liquidshack.rubber.elephant.mahout.git.GitQuery
```

Each component has a block for setting variables
Some of these can be overridden by passing properties into the gradle command.  This is to let you publish or deploy an existing image/tag instead of building a new one.  For example:
`./gradlew -Ptag=MYACCOUNT.dkr.ecr.us-east-2.amazonaws.com/elephant/me:v2.1.1 getCertificate deployImage`

Aws variables

Variable|Description|Required
--------|-----------|--------
region|AWS deployment region|yes
s3region|AWS S3 region can be different from deployment|yes
s3bucket|Bucket than contains the kube configs|yes
repositoryName|ECR repository name|yes
version|This will be used by the docker image tagger.  Will use release tag if available.|yes
namespace|Namespace used for AWS certificates and Kubernetes|yes

```groovy
aws {
	region = "$System.env.REGION"
	s3region = "$System.env.REGION"
	s3bucket = "some.bucket"
	repositoryName = "elephant/me"
	version = "${version}"
	namespace = "elephant"
}
```

Docker variables

Variable|Description|Required
--------|-----------|--------
imageName|Name of the docker image|yes
imageId|Can override from command|no
tag|Can override from command|no

```groovy
docker {
	imageName = "elephant"
	imageId = project.hasProperty('imageId') ? project.getProperty('imageId') : null 
	tag = project.hasProperty('tag') ? project.getProperty('tag') : null
}
```

Kubernetes variables

Variable|Description|Required
--------|-----------|--------
deployConfigs|List of deployment config file paths|yes
kubeConfigs|Map of [context : kube config], one for each environment|yes
secretElephant|Secret to decrypt config files|deprecated
contexts|Map of [environment(dev|qa|prod) : context name|yes
environment|Allow overriding environment from command line|no

```groovy
kubernetes {
	deployConfigs = [ "${project.rootDir}/infra/deploy.json",
		"${project.rootDir}/infra/hpa.json",
		"${project.rootDir}/infra/ns.json",
		"${project.rootDir}/infra/svc.json" ]
	
	kubeConfigs = [ "context-name-dev" : "config.dev", 
		"context-name-qa" : "config.qa",
		"context-name-prod" : "config.prod" ]
	
	secretElephant = "$System.env.SECRET_ELEPHANT"
	
	contexts = [ ("${Kubernetes.DEVELOPMENT}".toString()) : "k8-services.dev.ecom.devts.net",
		("${Kubernetes.QA}".toString()) : "k8-services.qa.ecom.devts.net",
		("${Kubernetes.PRODUCTION}".toString()) : "k8-services.qa.ecom.devts.net" ]

	environment = project.hasProperty('environment') ? project.getProperty('environment') : null
}
```

Git variables

Variable|Description|Required
--------|-----------|--------
masterBranch|Which branch to consider the *master* which is used to choose environment|no - defaults to "master"

```groovy
git {
	masterBranch = "master"
}
```

Setup the tasks described in the **Flow** section
The task names can be anything you want;  the task that is executed is based on the *type*, so instead of calling it *gitQuery* you have the option of calling it *OMGJumpDownFromThatElephant* if you prefer.

```groovy
task getCredentials(type: GetCredentials, dependsOn: 'getRepository') { }

task buildImage(type: DockerImageBuilder) {
	buildArgs(['WAR_FILE': "${bootWar.archiveName}"])
}

task tagImage(type: DockerImageTagger) { }

task pushImage(type: DockerImagePusher, dependsOn: 'getCredentials') { }

task getCertificate(type: GetCertificate) { }

task deployImage(type: KubernetesDeploy) { }

task gitQuery(type: GitQuery) { }
```

And can create composites such as

```groovy
task publish(type: GradleBuild) {
	tasks = ['clean', 'build', 'buildImage', 'getRepository', 'getCredentials', 'tagImage', 'pushImage']
}

task deploy(type: GradleBuild) {
	tasks = ['clean', 'build', 'gitQuery', 'buildImage', 'getRepository', 'getCredentials', 'tagImage', 'pushImage', 'getCertificate', 'deployImage']
}
```

### Gotchas
The docker pathways and mounts were not behaving, so for now the *Dockerfile* must be in a folder called *docker*.  This folder is also the mount point so your war/bootWar or jar/bootJar should also copy the war/jar file into this folder by adding a *doLast* task.

```groovy
bootWar {
	//...
	doLast {
		copy {
			from new File(project.buildDir, 'libs')
			into new File(project.rootDir, 'docker')
		}
	}
}
```

https://github.com/LiquidShack/rubber-elephant-mahout/releases/download/v.0.0.1/rubber-elephant-mahout.jar
