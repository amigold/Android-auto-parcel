

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class that performs two services: reading a text file into a string,
 * and writing a string into a text file.
 * 
 * @author Steven Karas
 */
public class FileTools {

	/**
	 * Reads the contents of a given text file into a string
	 * 
	 * @param filename
	 *            the full name (including path) of the file to read
	 * @return the contents of the file, as a string
	 * @throws IOException
	 *             If an I/O error occurs
	 * @throws FileNotFoundException
	 *             if the file does not exist, or is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading
	 */
	public static String readFile(String filename)
			throws FileNotFoundException, IOException {
		File file = new File(filename);
		FileReader inputFile = new FileReader(file);
		int fileSize = (int) file.length();
		char[] fileContents = new char[fileSize];
		inputFile.read(fileContents);
		return new String(fileContents);
	}

	/**
	 * Writes the given content into the given file
	 * 
	 * @param filename
	 *            the full name (including path) of the file
	 * @param content
	 *            the contents to write into the file
	 * @throws IOException
	 *             if the file exists but is a directory rather than a regular
	 *             file, or does not exist but cannot be created, or cannot be
	 *             opened for any other reason, or an I/O error occurs
	 */
	public static void writeFile(String filename, String content)
			throws IOException {
		File file = new File(filename);
		FileWriter outputFile = new FileWriter(file);
		outputFile.write(content);
		outputFile.flush();
		outputFile.close();
	}
}