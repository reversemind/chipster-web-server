package fi.csc.chipster.toolbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import fi.csc.microarray.description.SADLDescription.Name;

public class SADLReplacements {
	
	private static final Logger logger = Logger.getLogger(SADLReplacements.class);

	public static Name[] processNames(Collection<Name> options) throws IOException {
		ArrayList<Name> newOptions = new ArrayList<>();
		for (Name option : options) {
			List<String> replaced = processReplacements(option.getID());
			if (replaced.size() == 1 && option.getID().equals(replaced.get(0))) {
				// there is no replacement, keep the original Name object
				newOptions.add(option);
			} else {
				// create Name objects for all replacement strings
				newOptions.addAll(getNames(replaced));
			}
		}
		return newOptions.toArray(new Name[0]);
	}
	
	public static String[] processStrings(Collection<String> options) throws IOException {
		ArrayList<String> newOptions = new ArrayList<>();
		for (String option : options) {
			return processReplacements(option).toArray(new String[0]);
		}
		return newOptions.toArray(new String[0]);
	}

	private static List<String> processReplacements(String option) throws IOException {
		String FILES = "FILES";
		String SYMLINK_TARGET = "SYMLINK_TARGET";

		// split by white space
		String[] tokens = parseReplacements(option);
		if (FILES.equals(tokens[0])) {
			return getFiles(tokens[1], tokens[2]);
			
		} else if (SYMLINK_TARGET.equals(tokens[0])) {			
			try {
				return Arrays.asList(new String[] {getSymlinkTarget(tokens[1])});
			} catch (IOException e) {
				logger.warn("failed to get the symlink target of " + tokens[1]);
			} 
		}
		return Arrays.asList(new String[] {option});
	}	
	
	private static String[] parseReplacements(String value) {
		String[] tokens = value.split("\\s+");
		for (int i = 0; i < tokens.length; i++) {
			System.out.println(i + " " + tokens[i]);
		}
		return tokens;
	}

	private static ArrayList<Name> getNames(Collection<String> ids) {
		ArrayList<Name> names = new ArrayList<>();
		for (String id : ids) {
			Name name = new Name();
			name.setID(id);
			names.add(name);
		}
		return names;
	}
	
	private static ArrayList<String> getFiles(String path, String endsWith) throws IOException {
		ArrayList<String> basenames = new ArrayList<>();
		File dir = new File(path);
		if (!dir.exists()) {
			throw new IOException("failed to get SADL options from files, file not found: " + path);
		}
		for (String fileName : dir.list()) {
			if (fileName.endsWith(endsWith)) {
				String basename = fileName.substring(0, fileName.length() - endsWith.length());
				basenames.add(basename);
			}
		}
		return basenames;
	}
	
	private static String getSymlinkTarget(String path) throws IOException {
		Path symlink = new File(path).toPath();		
		if (Files.isSymbolicLink(symlink)) {
			return Files.readSymbolicLink(symlink).getFileName().toString();
		} else {
			throw new IOException("not a symlink: " + path);
		}		
	}
}
