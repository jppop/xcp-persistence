package org.pockito.xcp.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineHelper {

	private static final String OPT_QUIET = "q";
	private static final String OPT_NAMESPACES = "n";
	private static final String OPT_PASSWORD = "p";
	private static final String OPT_USERNAME = "u";
	private static final String OPT_REPO = "r";
	private static final String OPT_FILE = "f";
	private static final String OPT_IMPORT = "i";
	private static final String OPT_EXPORT = "e";
	private static final String OPT_TYPEFILTER = "t";
	private static final String LONGOPT_IMPORT = "import";
	private static final String OPT_EXCLUDELIST = "x";

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
				.desc("Namespaces to be imported/exported (comma separated values)").required(false)
				.longOpt("namespaces").hasArg().build());
		options.addOption(Option.builder(OPT_TYPEFILTER).desc("Config type filter (comma separated values)")
				.required(false).longOpt("type-filter").hasArg().build());

		options.addOption(Option.builder(OPT_EXCLUDELIST).desc("Exclude list")
				.required(false).longOpt("exclude").hasArg().build());

		options.addOption(Option.builder(OPT_QUIET).desc("Quiet mode")
				.required(false).longOpt("quiet").build());

		Option fileOption = Option.builder(OPT_FILE).desc("input or output file. Default is standard input/output")
				.required(false).longOpt("file").hasArg().build();

		options.addOption(fileOption);

		OptionGroup cmdOptionGroup = new OptionGroup();
		cmdOptionGroup.setRequired(true);
		Option importOption = Option.builder(OPT_IMPORT).desc("udapte parameters from a file").longOpt(LONGOPT_IMPORT)
				.build();
		Option exportOption = Option.builder(OPT_EXPORT).desc("dump parameters to a file").longOpt("export").build();
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
			if (this.hasExportOption()) {
				if (!this.cli().hasOption(OPT_NAMESPACES)) {
					throw new MissingOptionException("Missing required option:" + OPT_NAMESPACES);
				}
			} else {
				if (this.cli().hasOption(OPT_EXCLUDELIST)) {
					File file = new File(this.getExcludeList());
					if (!file.isFile() || !file.canRead()) {
						throw new FileNotFoundException(file.getAbsolutePath());
					}
				}
			}
		} catch (ParseException | FileNotFoundException e) {
			System.out.println(e.getMessage());
			help();
		}

	}

	private void help() {
		final String USAGE = "xcp-prm [-h] [-q] (--export -n <namepaces> [-t <arg>] | --import [-x <arg>]) -r <repository> -u <username> -p <password> [-f <filename>]";
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(120);
		formatter.printHelp(USAGE, options);
		System.exit(0);

	}

	public boolean hasImportOption() {
		checkNotNull(this.cli);
		return this.cli().hasOption(OPT_IMPORT);
	}

	public boolean hasExportOption() {
		checkNotNull(this.cli);
		return this.cli().hasOption(OPT_EXPORT);
	}

	public boolean hasFileOpt() {
		checkNotNull(this.cli);
		return this.cli().hasOption(OPT_FILE);
	}

	public boolean hasExcludeOpt() {
		checkNotNull(this.cli);
		return this.cli().hasOption(OPT_EXCLUDELIST);
	}

	public boolean isQuiet() {
		checkNotNull(this.cli);
		return this.cli().hasOption(OPT_QUIET);
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

	public String getExcludeList() {
		checkNotNull(this.cli);
		return this.cli().getOptionValue(OPT_EXCLUDELIST);
	}

	public String[] getNamespaces() {
		checkNotNull(this.cli);
		String nm = this.cli().getOptionValue(OPT_NAMESPACES);
		String[] namespaces = nm.split(",");
		return namespaces;
	};

	public String[] getTypeFilter() {
		checkNotNull(this.cli);
		if (this.cli().hasOption(OPT_TYPEFILTER)) {
			String filter = this.cli().getOptionValue(OPT_TYPEFILTER);
			String[] filters = filter.split(",");
			return filters;
		} else {
			return null;
		}
	};
}
