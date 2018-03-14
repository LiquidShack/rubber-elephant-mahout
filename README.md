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

Sawadee!  Why Rubber Elephant Mahout?  Well, let's face it - nothing on land compares to an elephant for size and elagance!

https://github.com/LiquidShack/rubber-elephant-mahout/releases/download/v.0.0.1/rubber-elephant-mahout.jar

### flow

aws, docker, and k8s tags for config properties
1. Build Image - Build WAR and Docker Image
	Requires Docker block to define variables:
	- host
	- imageName
	- baseDirectory (optional)
	- dockerFile (optional)
	DockerImageBuilder:
	- buildArgs
	execution: gradle build buildImage

2. Get Repository - this is used to query the repository which will be needed for credentials and other stuff.  If create is set to true, then it will create the repository if it's not found and create it.
	Aws variable:
	- repositoryName
	- region
	- version
	- namespace
	- cluster
	Get Repository Variables:
	- create

3. Get Credentials - depends on Get Repository


4. DockerImageTag
	gradle tagImage -PimageId=981ca55f8a78

