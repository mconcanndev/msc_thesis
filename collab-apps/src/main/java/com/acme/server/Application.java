package com.acme.server;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

import org.apache.log4j.Logger;

@ComponentScan
@EnableAutoConfiguration
public class Application {

	static final Logger logger = Logger.getLogger(Application.class);
	
    public static void main(String[] args) {

    	//ApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml");
    	//AppConfig app = new AppConfig();
    	
    	logger.debug("Start running the server.");
    	
    	SpringApplication.run(Application.class, args);
    }
}