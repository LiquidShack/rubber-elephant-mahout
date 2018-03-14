            /}/}
 ,         / / }
 \\   .-=.( (   }
  \'--"   `\\_.---,='
   '-,  \__/       \___
    .-'.-.'      \___.'
   / // /-..___,-`--'
   `" `"
  ____        _     _                 _____ _            _                 _     __  __       _                 _   
 |  _ \ _   _| |__ | |__   ___ _ __  | ____| | ___ _ __ | |__   __ _ _ __ | |_  |  \/  | __ _| |__   ___  _   _| |_ 
 | |_) | | | | '_ \| '_ \ / _ \ '__| |  _| | |/ _ \ '_ \| '_ \ / _` | '_ \| __| | |\/| |/ _` | '_ \ / _ \| | | | __|
 |  _ <| |_| | |_) | |_) |  __/ |    | |___| |  __/ |_) | | | | (_| | | | | |_  | |  | | (_| | | | | (_) | |_| | |_ 
 |_| \_\\__,_|_.__/|_.__/ \___|_|    |_____|_|\___| .__/|_| |_|\__,_|_| |_|\__| |_|  |_|\__,_|_| |_|\___/ \__,_|\__|
                                                  |_|                                                               
 
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
	Git Event -> AWS CodeBuild -> Docker Image -> Kubernetes Deploy

**Deep dive**

First, a Git event happens.  Project is set up with AWS WebHook to trigger on Push, Pull Request, and Release.
AWS CodeBuild then triggers the chain of events for the project.

First, the pre_build will query the Git repository, eg: `sh ./gradlew gitQuery`
This will determine the environment (DEVELOPMENT | QA | PRODUCTION)
* If it's a *Pull Request* it will build to DEVELOPMENT
* If it's a *Push* it will check branch.  If master branch, then build to QA, else DEVELOPMENT.  This will differentiate between code commits and merges to master.
* If it's a *Release*, determined by tag starting with `v` then it will build to PRODUCTION

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
	tasks = ['clean', 'build', 'buildImage', 'getRepository', 'getCredentials', 'tagImage', 'pushImage', 'getCertificate', 'deployImage']
}
```
then the build command simply becomes:
`./gradlew deploy`

And that's the flow in a nutshell!

### Gradle build

TODO

https://github.com/LiquidShack/rubber-elephant-mahout/releases/download/v.0.0.1/rubber-elephant-mahout.jar
