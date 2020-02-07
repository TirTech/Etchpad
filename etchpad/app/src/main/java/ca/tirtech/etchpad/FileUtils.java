package ca.tirtech.etchpad;

import android.content.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
	public static Path writeToFile(Context context, String fileName, String extension, String env, String contents) throws IOException {
		Path jsonFile = Paths.get(context.getExternalFilesDir(env).getAbsolutePath(), fileName + extension);
		Files.write(jsonFile, contents.getBytes());
		return jsonFile;
	}
	
	public static Path writeToFile(Context context, String fileName, String extension, String env, byte[] contents) throws IOException {
		Path jsonFile = Paths.get(context.getExternalFilesDir(env).getAbsolutePath(), fileName + extension);
		Files.write(jsonFile, contents);
		return jsonFile;
	}
	
	public static String readFromFile(Context context, String fileName, String extension, String joiner, String env) throws IOException {
		return String.join(joiner, Files.readAllLines(Paths.get(context.getExternalFilesDir(env).getAbsolutePath(), fileName + extension)));
	}
}
