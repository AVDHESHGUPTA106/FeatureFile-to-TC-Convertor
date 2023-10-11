package com.lex.FeatureClassification;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.client.RestTemplate;

import com.lex.FeatureClassification.advice.RestTemplateResponseErrorHandler;

@SpringBootApplication
public class FeatureClassificationApplication {
	
	static Logger logger = LoggerFactory.getLogger(FeatureClassificationApplication.class);
	
	@Autowired
	RestTemplateResponseErrorHandler restTemplateResponseErrorHandler;
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
		return restTemplateBuilder.errorHandler(new RestTemplateResponseErrorHandler()).build();
	}

	public static void main(String[] args) throws IOException {
		
		System.setProperty("java.awt.headless", "false");
		JDialog dialog = new JDialog();
		String infoDialogMsg = "<html><body><font color='blue'>Please wait, PR report generator application will be launching...</font></body></html>";
		infoDialog(dialog, infoDialogMsg);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if (!isTcpPortAvailable(8080)) {
				ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
				Resource[] resources = resolver.getResources("classpath*:kill.bat");
				if(null!=resources && resources.length>0) {
					InputStream inputStream = resources[0].getInputStream();
					File batchFile = File.createTempFile(resources[0].getFilename(), ".bat");
					try {
						FileUtils.copyInputStreamToFile(inputStream, batchFile);
					} finally {
						IOUtils.close(inputStream);
					}
					ProcessBuilder pb = new ProcessBuilder("cmd", "/c", batchFile.getName());
					pb.directory(batchFile.getParentFile());
					Process p = pb.start();
					logger.info(p.toString());
					bootedApplication(args, dialog);
				}
			}else { bootedApplication(args, dialog); }
			
		}catch (Exception ex) {
			dialog.setVisible(false);
			String errorStr = "<html><body><p style='width: 300px;'>" + String
					.format("Due to some system error %s, application not able to open browser", ex.getMessage())
					+ "</p></body></html>";
			JOptionPane.showMessageDialog(null, errorStr, ex.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			logger.error("Error - ",ex.fillInStackTrace());
			System.exit(0);
		}
	}

	/**
	 * 
	 * @param args
	 * @param dialog
	 */
	private static void bootedApplication(String[] args, JDialog dialog) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				dialog.setVisible(false);
				JDialog chdialog = new JDialog();
				infoDialog(chdialog, "<html><body><font color='green'>Application booted successfully... launching browser now...</font></body></html>");
				SpringApplication.run(FeatureClassificationApplication.class, args);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						try {
							openClientInterface(chdialog);
							chdialog.setVisible(false);
						} catch (InterruptedException | IOException | URISyntaxException e) {
							e.printStackTrace();
						}

					}

				}, 500);
			}
		}, 2000);
	}
	
	/**
	 * 
	 * @param dialog
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	private static void openClientInterface(JDialog dialog) throws InterruptedException, IOException, URISyntaxException {
		System.out.println("Opening URL http://localhost:8080/index");
		logger.info("Opening URL http://localhost:8080/index");
		System.out.println("To terminate job press Ctrl+C{^C}");
		logger.info("To terminate job press Ctrl+C{^C}");
		openWebpage("http://localhost:8080/index/", dialog);
	}
	
	/**
	 * 
	 * @param url
	 * @param dialog
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public static void openWebpage(String url, JDialog dialog) throws IOException, URISyntaxException {
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI("http://localhost:8080/index"));
		} else {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
		}
		dialog.setVisible(false);
	}

	/**
	 * 
	 * @param port
	 * @return
	 */
	public static boolean isTcpPortAvailable(int port) {
		try (ServerSocket serverSocket = new ServerSocket()) {
			// setReuseAddress(false) is required only on OSX,
			// otherwise the code will not work correctly on that platform
			serverSocket.setReuseAddress(false);
			serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port), 1);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * 
	 * @param dialog
	 * @param infoDialogMsg
	 */
	private static void infoDialog(final JDialog dialog, String infoDialogMsg) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				dialog.setTitle("PR Report Generator");
				dialog.setLayout(new GridBagLayout());
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(20, 20, 20, 20);
				gbc.weightx = 1;
				gbc.gridy = 0;
				dialog.add(new JLabel(infoDialogMsg), gbc);
				gbc.gridy = 1;
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			}
		});
	}

}
