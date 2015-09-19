import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class FilesServlet extends HttpServlet {
	// local:
	private static String binaryDirPath = "/usr/local/apache-tomcat-8.0.23/webapps/ROOT/WEB-INF/classes/gsoc-binaries/";

	// gsoc1:
	// private static String binaryDirPath =
	// "/opt/tomcat8/webapps/ROOT/WEB-INF/classes/gsoc-binaries/";
	
	/**
	 * Called automatically by the apache web server.
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
		response.setContentType("text/plain");

		File baseDir = new File(binaryDirPath);

		Gson gsonSerializer = new Gson();
		String result = gsonSerializer.toJson(baseDir.list());

		response.getWriter().println(result);
	}
}
