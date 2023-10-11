package com.lex.FeatureClassification;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BrowserLauncher {
	
	static Logger logger = LoggerFactory.getLogger(BrowserLauncher.class);
	
	//@EventListener(ApplicationReadyEvent.class)
	public void launchBrowser() {

		System.setProperty("java.awt.headless", "false");

		Desktop desktop = Desktop.getDesktop();

		try {
			logger.info("Opening URL http://localhost:8080/index");
			System.out.println("Opening URL http://localhost:8080/index");
			System.out.println("To terminate job press Ctrl+C{^C}");
			logger.info("To terminate job press Ctrl+C{^C}");
			desktop.browse(new URI("http://localhost:8080/index"));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}

}