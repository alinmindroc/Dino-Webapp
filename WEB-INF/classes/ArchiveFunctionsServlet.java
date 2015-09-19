import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
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

public class ArchiveFunctionsServlet extends HttpServlet {
	private native void getArchiveFunctionsJni(String binaryPath, String jsonPath);

	// local:
	private static String binaryDirPath = "/usr/local/apache-tomcat-8.0.23/webapps/ROOT/WEB-INF/classes/gsoc-binaries/";
	private static String cacheDirPath = "/usr/local/apache-tomcat-8.0.23/webapps/ROOT/WEB-INF/classes/cached-archive-functions/";

	// gsoc1:
	// private static String binaryDirPath =
	// "/opt/tomcat8/webapps/ROOT/WEB-INF/classes/gsoc-binaries/";
	// private static String cacheDirPath =
	// "/opt/tomcat8/webapps/ROOT/WEB-INF/classes/cached-archive-functions/";

	static {
		System.loadLibrary("dyninstParser");
	}

	/**
	 * @param fileName
	 *            name of the executable
	 * @return boolean meaning whether the archive's assembly content is cached
	 *         or not
	 */
	private static Boolean isArchiveFunctionCached(String fileName) {
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

	class ArchiveFuncModel {
		private String address;
		private String name;
		private String obj;
		private int size;

		public String toString() {
			return String.format("'address': %s,'name':%s,'size':%d", address,
					name, size);
		}
	}

	/**
	 * Calls the native method getArchiveFuncsJni, gets a list of functions for
	 * the specified archive and sorts it
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
		if (isArchiveFunctionCached(fileName) == false) {
			new ArchiveFunctionsServlet().getArchiveFunctionsJni(binaryDirPath
					+ fileName, cacheDirPath + fileName);
		}
		// return the cached result
		String source = FileUtils.readFileToString(new File(cacheDirPath
				+ fileName));

		if (source.startsWith("error"))
			return source;

		Gson gsonSerializer = new Gson();
		Type archiveFunctionListType = new TypeToken<List<ArchiveFuncModel>>() {
		}.getType();
		List<ArchiveFuncModel> funcList = gsonSerializer
				.fromJson(source, archiveFunctionListType);

		if (sortMode.compareTo("size") == 0) {
			Collections.sort(funcList, new Comparator<ArchiveFuncModel>() {

				@Override
				public int compare(ArchiveFuncModel o1, ArchiveFuncModel o2) {
					if (sortDirection.compareTo("ascending") == 0) {
						return o1.size - o2.size;
					} else {
						return o2.size - o1.size;
					}
				}
			});
		} else if (sortMode.compareTo("name") == 0) {
			Collections.sort(funcList, new Comparator<ArchiveFuncModel>() {

				@Override
				public int compare(ArchiveFuncModel o1, ArchiveFuncModel o2) {
					if (sortDirection.compareTo("ascending") == 0) {
						return o1.name.compareTo(o2.name);
					} else {
						return o2.name.compareTo(o1.name);
					}
				}
			});
		} else if (sortMode.compareTo("address") == 0) {
			Collections.sort(funcList, new Comparator<ArchiveFuncModel>() {
				@Override
				public int compare(ArchiveFuncModel o1, ArchiveFuncModel o2) {
					Integer i1 = Integer.parseInt(o1.address, 16);
					Integer i2 = Integer.parseInt(o2.address, 16);
					if (sortDirection.compareTo("ascending") == 0) {
						return i1 - i2;
					} else {
						return i2 - i1;
					}
				}
			});
		} else if (sortMode.compareTo("objname") == 0) {
			Collections.sort(funcList, new Comparator<ArchiveFuncModel>() {

				@Override
				public int compare(ArchiveFuncModel o1, ArchiveFuncModel o2) {
					if (sortDirection.compareTo("ascending") == 0) {
						return o1.obj.compareTo(o2.obj);
					} else {
						return o2.obj.compareTo(o1.obj);
					}
				}
			});
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
