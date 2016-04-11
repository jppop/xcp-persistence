package org.pockito.xcp.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.pockito.xcp.config.XcpConfigTool.ProgressListener;
import org.pockito.xcp.config.domain.XcpParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {

	private static Logger logger = LoggerFactory.getLogger(App.class);
	private static boolean verbose = true;
	
	private static class ProgressConsole implements ProgressListener {

		@Override
		public void progress(String message, String status, XcpParameter param) {
			if ( verbose ) {
				System.out.println(message);
			}
			
		}
		
	}

	public static void main(String[] args) {
		try {
			
			ProgressConsole progress = new ProgressConsole();
			
			final CommandLineHelper cliHelper = new CommandLineHelper(args);
			cliHelper.parse();
			
		    final String repoName = cliHelper.getRepoName();
		    final String username = cliHelper.getUserName();
		    final String password = cliHelper.getPassword();
			final String[] typeFilter = cliHelper.getTypeFilter();

			XcpConfigTool configTool = new XcpConfigTool(AppConfig.instance.getXcpParameterRepo());

		    if (cliHelper.hasImportOption()) {
				
			    InputStream inputStream;
			    if (cliHelper.hasFileOpt()) {
			    	String filename = cliHelper.getFilename();
			    	inputStream = new FileInputStream(filename);
			    } else {
			    	inputStream = System.in;
			    }
			    Excluder excluder = null;
			    if (cliHelper.hasExcludeOpt()) {
			    	excluder = new Excluder(cliHelper.getExcludeList());
			    }
			    configTool.importConfig(repoName, username, password, inputStream, false, excluder, progress);
			} else {

			    OutputStream output;
			    if (cliHelper.hasFileOpt()) {
			    	String filename = cliHelper.getFilename();
			    	output = new FileOutputStream(filename);
			    } else {
			    	output = System.out;
			    }
				final String[] namespace = cliHelper.getNamespaces();
				configTool.exportConfig(repoName, username, password, namespace, output, typeFilter);

			}
		} catch (Exception e) {
			logger.error("Fatal error", e);
			System.out.println("Error: " + e.getMessage());
		}
	}

}
