package io.liquidshack.rubber.elephant.mahout.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import io.liquidshack.rubber.elephant.mahout.kubernetes.Kubernetes

class GitQuery extends AbstractGitTask {

	static final RELEASE_FORMAT = /^v(\d+)\.(\d+).*/

	@Override
	void runCommand() {
		logger.lifecycle 'Running task [GitQuery]'

		String env = System.getenv("CODEBUILD_SRC_DIR")

		if (!env?.trim() || env == 'null') {
			String environment = getEnvironment()
			if (environment?.trim() && environment != 'null') {
				logger.lifecycle 'Env [CODEBUILD_SRC_DIR] not found - but project extension environment already set (or defaulted) so leaving it alone as ' + environment
			} else {
				// This actually won't happen cause getEnvironment() defaults to dev
				logger.lifecycle 'Env [CODEBUILD_SRC_DIR] not found - using DEVELOPMENT by default. This might be running locally instead of AWS CodeBuild.'
				setEnvironment(Kubernetes.DEVELOPMENT)
			}
			return
		}
		//println 'CODEBUILD_SOURCE_REPO_URL=' + System.getenv("CODEBUILD_SOURCE_REPO_URL")
		//println 'CODEBUILD_SOURCE_VERSION=' + System.getenv("CODEBUILD_SOURCE_VERSION")

		String dir = env  + '/.git'
		logger.lifecycle "Git Directory: $dir"

		Repository repo = new FileRepositoryBuilder()
				.setGitDir(new File(dir))
				.readEnvironment()
				.build()
		Git git = new Git(repo)

		String branch = repo.getBranch()
		boolean isPullRequest = branch.startsWith('pr-')
		logger.lifecycle "Git Branch: [$branch]"
		logger.lifecycle "is Pull Request? $isPullRequest"
		String foundBranch = null

		if (!isPullRequest) {
			logger.lifecycle 'Not a Pull Request .. querying for branch this commit came from..'
			ListBranchCommand branches = git.branchList().setContains(branch)
			branches.call().each { br ->
				for (RevCommit commit : git.log().add(repo.resolve(br.name)).call()) {
					if (commit.name == branch) {
						String commitBranch = Repository.shortenRefName(br.name)
						foundBranch = commitBranch
						logger.lifecycle "User: [$commit.authorIdent]"
						if (commitBranch != 'HEAD') {
							logger.lifecycle "This commit came from branch: [$br.name], short=[$commitBranch]"
							break
						}
						else {
							logger.lifecycle 'This commit came from branch: HEAD, going to stash it but keep looking for more branches'
						}
					}
				}
			}
		}
		if (foundBranch) branch = foundBranch

		String masterBranch = getMasterBranch()
		if (!masterBranch) masterBranch = "master"
		boolean isMasterBranch = branch == masterBranch

		logger.lifecycle 'is master branch? ' + isMasterBranch

		String tag = git.describe().call()
		logger.lifecycle 'Git tag: ' + tag
		boolean isRelease = false
		String releaseFormat = getReleaseFormat() ?: RELEASE_FORMAT
		logger.lifecycle 'Release format: ' + releaseFormat
		boolean isReleaseFormat = tag.matches(releaseFormat)
		logger.lifecycle 'Tag match releaseFormat? ' + isReleaseFormat

		if (isReleaseFormat) {
			repo.getTags().each() { iTag, iRef ->
				logger.info'Tag: ' + iTag + ' : ' + iRef
				if (iTag == tag) {
					logger.lifecycle 'found matching tag in repo'
					if (branch == iRef.objectId.name) {
						logger.lifecycle 'branch matches tag ref'
						if (isMasterBranch) {
							isRelease = isReleaseFormat
						}
					}
				}
			}
		}
		logger.lifecycle 'is Release? ' + isRelease

		if (isPullRequest) {
			logger.lifecycle 'since it is a pull request, setting environment to DEVELOPMENT'
			setEnvironment(Kubernetes.DEVELOPMENT)
		}
		else if (isRelease) {
			logger.lifecycle 'since it is release, setting environment to PRODUCTION'
			logger.lifecycle 'setting versions to Git Tag: ' + tag
			setEnvironment(Kubernetes.PRODUCTION)
			setVersion(tag)
		}
		else if (!isMasterBranch) {
			// This would happen when any push happens into something other than the master branch
			logger.lifecycle "This is not from the 'master' branch [$masterBranch] so going setting environment to DEVELOPMENT"
			setEnvironment(Kubernetes.DEVELOPMENT)
		}
		else {
			logger.lifecycle 'Must be a merge here, so setting environment to QA'
			setEnvironment(Kubernetes.QA)
		}
	}
}
