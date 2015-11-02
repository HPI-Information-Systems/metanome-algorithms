package de.uni_potsdam.hpi.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class FileUtils {

	public static String CHARSET_NAME = "ISO-8859-1";

	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void close(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isRoot(File directory) {
		File rootSlash = new File("/");
		File rootBackslash = new File("\\");
		if (directory.getAbsolutePath().equals(rootSlash.getAbsolutePath()) || directory.getAbsolutePath().equals(rootBackslash.getAbsolutePath()))
			return true;		
		return false;
	}

	public static void deleteDirectory(File directory) {
		if (isRoot(directory))
			return;
		
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory())
						deleteDirectory(files[i]);
					else
						files[i].delete();
				}
			}
		}
		directory.delete();
	}
	
	public static void cleanDirectory(File directory) {
		if (isRoot(directory))
			return;
		
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory())
						deleteDirectory(files[i]);
					else
						files[i].delete();
				}
			}
		}
	}

	public static void createFile(String filePath, boolean recreateIfExists) throws IOException {
		File file = new File(filePath);
		File folder = file.getParentFile();
		
		if ((folder != null) && !folder.exists()) {
			folder.mkdirs();
			while (!folder.exists()) {}
		}
		
		if (recreateIfExists && file.exists())
			file.delete();
		
		if (!file.exists()) {
			file.createNewFile();
			while (!file.exists()) {}
		}
	}

	public static CSVReader buildCSVFileReader(String filePath, char separator, boolean skipHeadline) throws FileNotFoundException {
		int line = (skipHeadline) ? 1 : 0;
		return new CSVReader(new InputStreamReader(new FileInputStream(filePath), Charset.forName(FileUtils.CHARSET_NAME)), separator, '\"', line);
	}

	public static CSVWriter buildCSVFileWriter(String filePath, char separator, boolean append) throws IOException {
		FileUtils.createFile(filePath, !append);
		return new CSVWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath), append), Charset.forName(FileUtils.CHARSET_NAME)), separator, '\"', '\\');
	}

	public static BufferedReader buildFileReader(String filePath) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), Charset.forName(FileUtils.CHARSET_NAME)));
	}
	
	public static BufferedWriter buildFileWriter(String filePath, boolean append) throws IOException {
		FileUtils.createFile(filePath, !append);
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath), append), Charset.forName(FileUtils.CHARSET_NAME)));
	}

	public static void writeToFile(String content, String filePath) throws IOException {
		Writer writer = null;
		try {
			writer = FileUtils.buildFileWriter(filePath, false);
			writer.write(content);
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
	
}
