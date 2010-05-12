
package gradle.pluginDev

import org.gradle.api.GradleException;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod 


import org.apache.commons.httpclient.Header 
import org.apache.commons.httpclient.HttpClient 
import org.slf4j.Logger 
import org.slf4j.LoggerFactory;

/**
 * This class basically uploads a plugin archive to the 
 * plugin portal. 
 * @author map
 */
class PluginUploader {
	Logger log = LoggerFactory.getLogger(PluginUploader)
	
	def project
	def client = new HttpClient()
	      
	def upload(def file) {
		
		
		def postUrl = "$project.pluginPortalUrl/${project.name}/${project.version}"
		log.info "uploading the plugin to: $postUrl"
		def post = new PostMethod(postUrl)
		post.setRequestHeader('username', project.user)
		post.setRequestHeader('password', project.password)
		
		def entity = new FileRequestEntity(file, 'application'); 
		post.setRequestEntity(entity);
		def responseCode = client.executeMethod(post)
		def response = ""//post.getResponseBodyAsString()
		new InputStreamReader(post.getResponseBodyAsStream()).eachLine() {
             response += it + "\n";
		}
		println response
			
		if (responseCode != 201) {
			throw new GradleException(response)
		}
	}
}
