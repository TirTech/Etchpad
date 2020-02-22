package ca.tirtech.etchpad.hardware;

import android.content.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for dealing with writing and reading to files.
 */
public class FileUtils {
	
	/**
	 * Write the given string into a file with the provided name.
	 *
	 * @param context   context to use for dir references
	 * @param fileName  the new file name
	 * @param extension the new file's extension
	 * @param env       the type of the system dir to write to
	 * @param contents  the new file contents
	 * @return the path to the new file
	 * @throws IOException error when writing the file
	 */
	public static Path writeToFile(Context context, String fileName, String extension, String env, String contents) throws IOException {
		return writeToFile(context, fileName, extension, env, contents.getBytes());
	}
	
	/**
	 * Write the given string into a file with the provided name.
	 *
	 * @param context   context to use for dir references
	 * @param fileName  the new file name
	 * @param extension the new file's extension
	 * @param env       the type of the system dir to write to
	 * @param contents  the new file contents
	 * @return the path to the new file
	 * @throws IOException error when writing the file
	 */
	public static Path writeToFile(Context context, String fileName, String extension, String env, byte[] contents) throws IOException {
		Path jsonFile = Paths.get(context.getExternalFilesDir(env).getAbsolutePath(), fileName + extension);
		Files.write(jsonFile, contents);
		return jsonFile;
	}
	
	/**
	 * Read from a file with the provided name.
	 *
	 * @param context   context to use for dir references
	 * @param fileName  the file name
	 * @param extension the file's extension
	 * @param joiner    string to join lines with
	 * @param env       the type of the system dir to read from
	 * @return the String contents of the file
	 * @throws IOException error when writing the file
	 */
	public static String readFromFile(Context context, String fileName, String extension, String joiner, String env) throws IOException {
		return String.join(joiner, Files.readAllLines(Paths.get(context.getExternalFilesDir(env).getAbsolutePath(), fileName + extension)));
	}
}
