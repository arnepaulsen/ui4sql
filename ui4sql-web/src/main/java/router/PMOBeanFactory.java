/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package router;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.FileSystemResource;

import plugins.Plugin;

/*
 * REturn a plugin from the Spring beanfactory
 *   and the xml definition 
 */
public class PMOBeanFactory {

	public PMOBeanFactory() {
	
		// i'll think of something
	}
	
	public Plugin  getPlugin(String pluginName) {
		
		

		
		 
		//System.out.println(getClassPath());
		

		/*
		 * using FileSystem.... not as good as ClassPath, but at least it works.
		 */
				
		// BeanFactory bf = new XmlBeanFactory(
        //         new FileSystemResource("/home/arne/workspace/pmo/WebContent/META-INF/spring/" + pluginName + ".xml"));

		/*
		 * using ClassPathResource..
		 */
		
		
		String location = new String("beans/" + pluginName + ".xml");
		
		System.out.println("locating bean in classpath - " + location);
		
		
		
		
		BeanFactory bf = new XmlBeanFactory(
                 new ClassPathResource("beans/" + pluginName + ".xml"));

		 
		 Plugin plugin = (Plugin) bf.getBean(pluginName);
		
		 return plugin;
		 
		 
	}
	
	
}
