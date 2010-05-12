
import gradle.pluginDev.PluginUploader;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.Conf2ScopeMappingContainer;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.tasks.bundling.Jar;


/**
 * @author map
 *
 */
public class PluginDev implements Plugin<Project> {
	
	public void apply(Project project) {
		project.getConvention().getPlugins().put("pluginDev", new PluginDevConvention())
		
		// The maven Plugin is required for creating the pom-file
		project.getPlugins().apply(MavenPlugin.class);
		// We need a configuration that contains only dependencies which are not part of the gradle distribution
		def plugin = project.configurations.add('plugin')
		project.configurations.compile.extendsFrom plugin 
		
		
		project.tasks.add(name: 'pluginJar', type:Jar, dependsOn: ['createPom', 'test']).configure {
			from "${project.buildDir}/classes/main"
			metaInf {
				from "${project.buildDir}/gradle-plugin"
			}
		}
		
		project.tasks.add(name: 'createPom') << {
			project.pom({
				it.groupId = 'org.gradle.plugins'
				def mappings = it.scopeMappings.mappings
				mappings.clear()
				it.scopeMappings.addMapping(100, project.configurations.plugin, Conf2ScopeMappingContainer.COMPILE) 
			}).writeTo("${project.buildDir}/gradle-plugin/pom.xml")
		}
		
		
		project.uploadPlugin.description = "Upload the plugin jar to the plugin portal"
		project.uploadPlugin.dependsOn 'pluginJar'
		project.uploadPlugin << { 
			def jar = new File("${project.libsDir}/${project.archivesBaseName}-${project.version}.jar")
			readCredentials(project)
			PluginUploader uploader = new PluginUploader(project: project)
			uploader.upload jar
		}
	}
	
	void readCredentials(def project) {
		println '*** Gradle Plugin-Portal authentication.'
		if (project.user && project.password) {
			println '* Using username and password which have been provided by the buildscript (user & password properties)'
		}
		System.in.withReader {
			println "* username: "
			project.user = it.readLine()
			println '* password : '
			project.password = it.readLine()
		}
	}
	
}
