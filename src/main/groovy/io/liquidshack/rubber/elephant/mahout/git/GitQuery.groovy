package io.liquidshack.rubber.elephant.mahout.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import io.liquidshack.rubber.elephant.mahout.kubernetes.Kubernetes

class GitQuery extends AbstractGitTask {

	@Override
	void runCommand() {
		println 'Running task [GitQuery]'

		String env = System.getenv("CODEBUILD_SRC_DIR")

		if (!env?.trim() || env == 'null') {
			println 'Env [CODEBUILD_SRC_DIR] not found - using DEVELOPMENT by default. This might be running locally instead of AWS CodeBuild.'
			setEnvironment(Kubernetes.PRODUCTION)
			return
		}

		String dir = env  + '/.git'
		println 'Git Directory: ' + dir

		Repository repo = new FileRepositoryBuilder()
				.setGitDir(new File(dir))
				.readEnvironment()
				.build()
		Git git = new Git(repo)

		String branch = repo.getBranch()
		boolean isPullRequest = branch.startsWith('pr-')
		println 'Git Branch: ' + branch
		println 'is Pull Request? ' + isPullRequest

		String tag = git.describe().call()

		println 'Git Tag: ' + tag
		boolean isRelease = false
		if (tag?.trim() && tag != 'null') {
			isRelease = tag.startsWith('v')
		}
		println 'is Release? ' + isRelease

		if (isPullRequest) {
			println 'since it is a pull request, setting environment to DEVELOPMENT'
			setEnvironment(Kubernetes.DEVELOPMENT)
		}
		else if (isRelease) {
			println 'since it is release, setting environment to PRODUCTION'
			println 'setting versions to Git Tag: ' + tag
			setEnvironment(Kubernetes.PRODUCTION)
			setVersion(tag)
		}
		else if (branch != getMasterBranch()) {
			// This would happen when merging into something other than the master branch ... not sure what else to do here
			println 'This is not from the master branch [' + getMasterBranch() + '] so going setting environment to DEVELOPMENT'
			setEnvironment(Kubernetes.DEVELOPMENT)
		}
		else {
			println 'Must be a merge here, so setting environment to QA'
			setEnvironment(Kubernetes.QA)
		}
	}

	public static void main(String[] args) {
		String tag = 'v1.1.1'
		println 'Git Tag: ' + tag
		boolean isRelease = false
		if (tag?.trim() && tag != 'null') {
			isRelease = tag.startsWith('v')
		}
		println isRelease
		return

		String dir = 'C:/Users/gdiamond1271/git/rubber-elephant-mahout/.git'

		//String dir = 'C:/Users/gdiamond1271/git/lucie-mock/.git'


		Repository repo = new FileRepositoryBuilder()
				.setGitDir(new File(dir))
				.setMustExist(true)
				.readEnvironment()
				.build()
		Git git = new Git(repo)
		println repo.getBranch()
		println '--- Z'

		println git.describe().call()

		println '--- A'

		Ref ref1 = repo.findRef("HEAD")
		println ref1.leaf
		println ref1.objectId.name

		println '--- B'

		Map<String, Ref> tags = repo.getTags()
		println 'tags:'
		tags.each { k, v ->
			println k + ' : ' + v + ' : ' + v.objectId.name

			if (v.objectId.name == ref1.objectId.name) {
				println '************************************'
				println 'found it!'
				println '************************************'
			}

		}


		//		repo.getAllRefs().each { k, v ->
		//			println k + ' : ' + v
		//		}
	}
}
