package org.pockito.xcp.config;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineHelper {

	private static final String OPT_NAMESPACES = "n";
	private static final String OPT_PASSWORD = "p";
	private static final String OPT_USERNAME = "u";
	private static final String OPT_REPO = "r";
	private static final String OPT_FILE = "f";
	private static final String OPT_IMPORT = "i";
	private static final String LONGOPT_IMPORT = "import";

	public CommandLineHelper(String[] args) {
		super();
		this.args = args;

		options.addOption("h", "help", false, "prints the help content");
		options.addOption(Option.builder(OPT_REPO).desc("Repository").required(true).longOpt("repository").hasArg()
				.build());
		options.addOption(Option.builder(OPT_USERNAME).desc("Username").required(true).longOpt("username").hasArg()
				.build());
		options.addOption(Option.builder(OPT_PASSWORD).desc("Password").required(true).longOpt("password").hasArg()
				.build());
		options.addOption(Option.builder(OPT_NAMESPACES)
				.desc("Namespaces to be imported/exported (comma separated values)").required(true)
				.longOpt("namespaces").hasArg().build());

		Option fileOption = Option.builder(OPT_FILE).desc("input or output file. Default is standard input/output")
				.required(false).longOpt("file").numberOfArgs(2).build();

		options.addOption(fileOption);

		OptionGroup cmdOptionGroup = new OptionGroup();
		cmdOptionGroup.setRequired(true);
		Option importOption = Option.builder(OPT_IMPORT).desc("udapte parameters from a file").longOpt(LONGOPT_IMPORT)
				.build();
		Option exportOption = Option.builder("e").desc("dump parameters to a file").longOpt("export").build();
		cmdOptionGroup.addOption(importOption);
		cmdOptionGroup.addOption(exportOption);
		options.addOptionGroup(cmdOptionGroup);

	}

	private String[] args;
	private CommandLine cli;
	private Options options = new Options();;

	public CommandLine cli() {
		return this.cli;
	}

	public void parse() {
		CommandLineParser parser = new DefaultParser();
		try {
			this.cli = parser.parse(options, args);
			if (this.cli.hasOption("h")) {
				help();
			}
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			help();
		}

	}

	private void help() {
		final String USAGE = "xcp-prm [-h] --export | --import [-f <filename>] -r <repository> -u <username> -p <password> -n <namspaces>";
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(120);
		formatter.printHelp(USAGE, options);
		System.exit(0);

	}

	public boolean hasImportOption() {
		checkNotNull(this.cli);
		return this.cli().hasOption(OPT_IMPORT);
	}

	public boolean hasFileOpt() {
		checkNotNull(this.cli);
		return this.cli().hasOption(OPT_FILE);
	}

	public String getFilename() {
		if (this.hasFileOpt()) {
			return this.cli().getOptionValue(OPT_FILE);
		}
		return null;
	}

	public String getRepoName() {
		checkNotNull(this.cli);
		return this.cli().getOptionValue(OPT_REPO);
	}

	public String getUserName() {
		checkNotNull(this.cli);
		return this.cli().getOptionValue(OPT_USERNAME);
	}

	public String getPassword() {
		checkNotNull(this.cli);
		return this.cli().getOptionValue(OPT_PASSWORD);
	}

	public String[] getNamespaces() {
		checkNotNull(this.cli);
		String nm = this.cli().getOptionValue(OPT_NAMESPACES);
		String[] namespaces = nm.split(",");
		return namespaces;
	};

}
