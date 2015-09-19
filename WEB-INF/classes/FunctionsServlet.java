import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author alin
 * 
 */
public class FunctionsServlet extends HttpServlet {
	private native void getFunctionsJni(String binaryPath, String jsonPath);

	// local:
	private static String binaryDirPath = "/usr/local/apache-tomcat-8.0.23/webapps/ROOT/WEB-INF/classes/gsoc-binaries/";
	private static String cacheDirPath = "/usr/local/apache-tomcat-8.0.23/webapps/ROOT/WEB-INF/classes/cached-functions/";

	// gsoc1:
	// private static String binaryDirPath =
	// "/opt/tomcat8/webapps/ROOT/WEB-INF/classes/gsoc-binaries/";
	// private static String cacheDirPath =
	// "/opt/tomcat8/webapps/ROOT/WEB-INF/classes/cached-functions/";
	static {
		System.loadLibrary("dyninstParser");
	}

	/**
	 * @param fileName
	 *            name of the executable
	 * @return boolean meaning whether the executable's function list is cached
	 *         or not
	 */
	private static Boolean isFunctionCached(String fileName) {
		File cacheDir = new File(cacheDirPath);

		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}

		String[] cachedBinaries = cacheDir.list();

		if (cachedBinaries == null)
			return false;

		for (String s : cachedBinaries) {
			if (s.compareTo(fileName) == 0) {
				return true;
			}
		}

		return false;
	}

	class FunctionModel {
		private String address;
		private String name;
		private long size;

		public String toString() {
			return address + ":" + name + ", size:" + size + "\n";
		}
	}

	/**
	 * Calls the native method getFunctionsJni, gets a list of functions for the
	 * specified executable and sorts it
	 * 
	 * @param fileName
	 *            name of the executable for which to get the function list
	 * @param sortMode
	 *            one of "address", "name", "size"
	 * @param sortDirection
	 *            one of "ascending", "descending"
	 * @return the list of functions as a JSON string
	 * @throws IOException
	 *             when the result file can not be read
	 */
	private static String getFunctions(String fileName, String sortMode,
			final String sortDirection) throws IOException {
		// if the functions are not cached, parse them and save them to cache
		if (isFunctionCached(fileName) == false) {
			new FunctionsServlet().getFunctionsJni(binaryDirPath + fileName,
					cacheDirPath + fileName);
		}

		// return the cached result
		String source = FileUtils.readFileToString(new File(cacheDirPath
				+ fileName));

		if (source.startsWith("error")) {
			return source;
		}

		Gson gsonSerializer = new Gson();
		Type functionListType = new TypeToken<List<FunctionModel>>() {
		}.getType();
		List<FunctionModel> funcList = gsonSerializer.fromJson(source,
				functionListType);

		if (sortMode.compareTo("size") == 0) {
			Collections.sort(funcList, new Comparator<FunctionModel>() {

				@Override
				public int compare(FunctionModel o1, FunctionModel o2) {
					if (sortDirection.compareTo("ascending") == 0) {
						return (int) (o1.size - o2.size);
					} else {
						return (int) (o2.size - o1.size);
					}
				}
			});
		} else if (sortMode.compareTo("name") == 0) {
			Collections.sort(funcList, new Comparator<FunctionModel>() {

				@Override
				public int compare(FunctionModel o1, FunctionModel o2) {
					if (sortDirection.compareTo("ascending") == 0) {
						return o1.name.compareTo(o2.name);
					} else {
						return o2.name.compareTo(o1.name);
					}
				}
			});
		} else if (sortMode.compareTo("address") == 0) {
			// by default, functions are returned by the Dyninst framework
			// ordered by address
		}

		return gsonSerializer.toJson(funcList);
	}

	/**
	 * Called automatically by the apache web server
	 * 
	 * @param request
	 * @param response
	 *            response used for writing the response for the GET method
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Set response content type
		response.setContentType("text/plain");
		String sortMode = request.getParameter("sortby");
		String sortDirection = request.getParameter("sortdirection");
		String fileName = request.getParameter("filename");

		response.getWriter().println(
				getFunctions(fileName, sortMode, sortDirection));
	}
}
