package io.liquidshack.rubber.elephant.mahout.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import io.liquidshack.rubber.elephant.mahout.kubernetes.Kubernetes

class GitQuery extends AbstractGitTask {

	@Override
	void runCommand() {
		println 'Running task [GitQuery]'

		String env = System.getenv("CODEBUILD_SRC_DIR")

		if (!env?.trim() || env == 'null') {
			String environment = getEnvironment()
			if (environment?.trim() && environment != 'null') {
				println 'Env [CODEBUILD_SRC_DIR] not found - but project extension environment already set so leaving it alone as ' + environment
			}else {
				println 'Env [CODEBUILD_SRC_DIR] not found - using DEVELOPMENT by default. This might be running locally instead of AWS CodeBuild.'
				setEnvironment(Kubernetes.DEVELOPMENT)
			}
			return
		}
		//println 'CODEBUILD_SOURCE_REPO_URL=' + System.getenv("CODEBUILD_SOURCE_REPO_URL")
		//println 'CODEBUILD_SOURCE_VERSION=' + System.getenv("CODEBUILD_SOURCE_VERSION")

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

		if (!isPullRequest) {
			println 'Not a Pull Request .. querying for branch this commit came from..'
			String commitId = branch
			ListBranchCommand branches = git.branchList().setContains(commitId)
			branches.call().each { br ->
				for (RevCommit commit : git.log().add(repo.resolve(br.name)).call()) {
					if (commit.name == commitId) {
						String commitBranch = Repository.shortenRefName(br.name)
						if (commitBranch != 'HEAD') {
							println 'This commit came from branch: ' + br.name + ' : short=' + commitBranch
							branch = commitBranch
							break
						}
					}
				}
			}
		}

		String tag = git.describe().call()
		println 'Git tag: ' + tag

		boolean isRelease = false
		if (tag?.trim() && tag != 'null') {
			isRelease = tag.find(/^v(\d+)\.(\d+)/)
		}
		println 'is Release? ' + isRelease
		String masterBranch = getMasterBranch()
		if (!masterBranch) masterBranch = "master"

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
		else if (branch != masterBranch) {
			// This would happen when any push happens into something other than the master branch
			println 'This is not from the "master" branch [' +masterBranch + '] so going setting environment to DEVELOPMENT'
			setEnvironment(Kubernetes.DEVELOPMENT)
		}
		else {
			println 'Must be a merge here, so setting environment to QA'
			setEnvironment(Kubernetes.QA)
		}
	}

	public static void main(String[] args) {



		//String a = 'v1998e8d2529f5efb0dc6399b3c3930e174c1d6624'
		//boolean r = a.find(/^v(\d+)\.(\d+)/)
		//println r

		//todo find branches 998e8d2529f5efb0dc6399b3c3930e174c1d6624

		//		String dir = 'C:/Users/gdiamond1271/git/rubber-elephant-mahout/.git'

		String dir = 'C:/Users/gdiamond1271/git/lucie-mock/.git'

		Repository repo = new FileRepositoryBuilder()
				.setGitDir(new File(dir))
				.setMustExist(true)
				.readEnvironment()
				.build()
		Git git = new Git(repo)


		git.branchList().setContains("998e8d2529f5efb0dc6399b3c3930e174c1d6624").call().each { ref1 ->
			println ref1.leaf
			println ref1.name
			println ref1.leaf.name
			println 'and the repo is?'
			println Repository.shortenRefName(ref1.leaf.name)
			println ref1.objectId.name
		}

		return
		println repo.getBranch()

		println '--- x'

		println repo.getFullBranch()

		println '--- Z'

		println git.describe().call()

		println '--- A'

		Ref ref1 = repo.findRef("HEAD")
		println ref1.leaf
		println ref1.name
		println ref1.leaf.name
		println 'and the repo is?'
		println Repository.shortenRefName(ref1.leaf.name)
		println ref1.objectId.name
		println ref1.target

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
		repo.getAllRefs().each { k, v ->
			println k + ' : ' + v
		}

		ListBranchCommand bc = git.branchList()
		bc.call().each { br ->
			println 'branch: ' + br.name + " | " + br
			for (RevCommit commit : git.log().add(repo.resolve(br.name)).call()) {
				println commit.name
				println commit.getId()

				if (commit.name == '998e8d2529f5efb0dc6399b3c3930e174c1d6624') {
					println '************************************'
					println 'this came from branch: ' + br.name
					println '************************************'
				}
			}
		}

		println '************************************'

		String treeName = "refs/heads/master"; // tag or branch
		for (RevCommit commit : git.log().add(repo.resolve(treeName)).call()) {
			println commit.name
			println commit.getId()
		}

	}
}
