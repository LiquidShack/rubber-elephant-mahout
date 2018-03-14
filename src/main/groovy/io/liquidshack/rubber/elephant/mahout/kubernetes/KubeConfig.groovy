package io.liquidshack.rubber.elephant.mahout.kubernetes

import io.fabric8.kubernetes.api.model.Cluster
import io.fabric8.kubernetes.api.model.Context
import io.fabric8.kubernetes.api.model.Preferences
import io.fabric8.openshift.api.model.User

class KubeConfig {

	String apiVersion
	List<Cluster> clusters
	List<Context> contexts
	String kind
	List<Preferences> preferences
	List<User> users
}
