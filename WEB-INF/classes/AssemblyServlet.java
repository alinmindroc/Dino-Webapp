import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AssemblyServlet extends HttpServlet {
	private native void getAssemblyJni(String binaryPath, String jsonPath);

	// local:
	private static String binaryDirPath = "/usr/local/apache-tomcat-8.0.23/webapps/ROOT/WEB-INF/classes/gsoc-binaries/";
	private static String cacheDirPath = "/usr/local/apache-tomcat-8.0.23/webapps/ROOT/WEB-INF/classes/cached-assembly/";

	// gsoc1:
	// private static String binaryDirPath =
	// "/opt/tomcat8/webapps/ROOT/WEB-INF/classes/gsoc-binaries/";
	// private static String cacheDirPath =
	// "/opt/tomcat8/webapps/ROOT/WEB-INF/classes/cached-assembly/";

	static {
		System.loadLibrary("dyninstParser");
	}

	/**
	 * @param fileName
	 *            name of the executable
	 * @return boolean meaning whether the executable's assembly content is
	 *         cached or not
	 */
	private static Boolean isAssemblyCached(String fileName) {
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

	/**
	 * Calls the native method getAssemblyJni and returns the plain result
	 * 
	 * @param fileName
	 *            name of the executable for which to get the assembly code
	 * @return
	 * @throws IOException
	 */
	private static String getAssembly(String fileName) throws IOException {
		// if the functions are not cached, parse them and save them to cache
		if (isAssemblyCached(fileName) == false) {
			new AssemblyServlet().getAssemblyJni(binaryDirPath + fileName,
					cacheDirPath + fileName);
		}

		// return the cached result
		return FileUtils.readFileToString(new File(cacheDirPath + fileName));
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

		String fileName = request.getParameter("filename");
		String functionName = request.getParameter("functionname");
		String address = request.getParameter("address");

		if (isAssemblyCached(fileName) == false)
			getAssembly(fileName);

		if (getAssembly(fileName).startsWith("error")) {
			response.getWriter().println(getAssembly(fileName));
			return;
		}

		Gson gsonSerializer = new Gson();
		Type assemblyMapType = new TypeToken<List<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> funcsList = gsonSerializer.fromJson(new FileReader(
				cacheDirPath + fileName), assemblyMapType);

		for (Map<String, String> funcMap : funcsList) {
			if (funcMap.get(functionName) != null
					&& funcMap.get(functionName).contains(address)) {
				response.getWriter().println(funcMap.get(functionName));
				return;
			}
		}
	}
}
