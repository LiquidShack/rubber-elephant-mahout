package io.liquidshack.rubber.elephant.mahout.aws

import com.amazonaws.services.ecr.model.Repository

import io.liquidshack.rubber.elephant.mahout.common.RubberElephantMahoutException

class ImageTagGenerator extends AbstractAwsTask {

	@Override
	void runCommand() {
		println 'Running task [ImageTagGenerator]'

		Repository repository = getRepository()
		String version = getVersion()
		assert repository : new RubberElephantMahoutException('`repository` has not been set')
		assert version?.trim() && version != 'null' : new RubberElephantMahoutException('`version` has not been set')

		setTag(repository, version)
		println 'Generated Tag: ' + getTag()
	}
}
