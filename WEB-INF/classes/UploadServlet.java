//Import required java libraries
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public class UploadServlet extends HttpServlet {
	private static final String SAVE_DIR = "WEB-INF/classes/gsoc-binaries";

	/**
	 * @param part
	 *            part of a multipart/form-data POST request used for file
	 *            uploading
	 * @return name of the file containing the part
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return "";
	}

	/**
	 * Called automatically by the apache web server
	 * 
	 * @param request
	 * @param response
	 *            response used for writing the response for the POST method
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// gets absolute path of the web application
		String appPath = request.getServletContext().getRealPath("");
		// constructs path of the directory to save uploaded file
		String savePath = appPath + File.separator + SAVE_DIR;

		// creates the save directory if it does not exists
		File fileSaveDir = new File(savePath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdir();
		}

		String fileName = "";
		for (Part part : request.getParts()) {
			fileName = extractFileName(part);
			part.write(savePath + File.separator + fileName);
		}

		response.getWriter().println(fileName);
	}
}
