package support;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ModelFileFilter extends FileFilter {

	public final static String xlsx = "xlsx";
	public final static String xls = "xls";
	public final static String txt = "txt";
	public final static String csv = "csv";
	public final static String cnf = "cnf";

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}
		String extension = getExtension(file);
		if (extension != null) {
			if (extension.equals(xlsx) || extension.equals(xls) ) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "ModelFileFilter";
	}

	/**
	 * Returns the file's extension as a string.
	 * @param file the file for which the extension is returned.
	 * @return the extension of the file as a string.
	 */
	public static String getExtension(File file) {
		String extension = null;
		String s = file.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 &&  i < s.length() - 1) {
			extension = s.substring(i+1).toLowerCase();
		}
		return extension;
	}

}
