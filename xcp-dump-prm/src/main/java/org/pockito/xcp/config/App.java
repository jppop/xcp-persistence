package org.pockito.xcp.config;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {

	private static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		try {
			CommandLineHelper cliHelper = new CommandLineHelper(args);
			cliHelper.parse();
			
		    String repoName = cliHelper.getRepoName();
		    String username = cliHelper.getUserName();
		    String password = cliHelper.getPassword();
			String[] namespace = cliHelper.getNamespaces();

			XcpConfigTool configTool = new XcpConfigTool(AppConfig.instance.getXcpParameterRepo());

		    if (cliHelper.hasImportOption()) {
				
			} else {

			    OutputStream output;
			    if (cliHelper.hasFileOpt()) {
			    	String filename = cliHelper.getFilename();
			    	output = new FileOutputStream(filename);
			    } else {
			    	output = System.out;
			    }
				configTool.exportConfig(repoName, username, password, namespace, output);

			}
		} catch (Exception e) {
			logger.error("Fatal error", e);
			System.out.println("Error: " + e.getMessage());
		}
	}

}
