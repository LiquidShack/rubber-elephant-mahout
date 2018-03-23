package io.liquidshack.rubber.elephant.mahout.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.text.GStringTemplateEngine
import groovy.text.TemplateEngine

class PlaceholderReplacer {

	Logger logger = LoggerFactory.getLogger(this.class)

	static String replace(File file, Map<String, String> bindings) {
		String input = file.getText()
		def matcher = input =~ /\$\{([a-zA-Z0-9_-])\}/
		Set<String> placeholders = new HashSet<String>();
		matcher.each {
			placeholders.add(it[1])
		}
		placeholders.each { key -> logger.info "found placeholder: $key" }

		placeholders.each { key ->
			if (!bindings.containsKey(key) || bindings.get(key) == 'null' || bindings.get(key) == null) {
				String value = System.getenv(key)
				if (value !=null && value.toString() != 'null')
					bindings.put(key, value)
				else
					throw new RubberElephantMahoutException('Could not find a value for ${' + key + '}')
			}
		}

		TemplateEngine engine = new GStringTemplateEngine()
		Writable filtered = engine.createTemplate(file).make(bindings)
		StringWriter writer = new StringWriter()
		filtered.writeTo(writer)
		return filtered.toString()
	}
}
