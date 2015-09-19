## Dino WebApp - GSoC 2015


####Overview:  

Dino's main functionality is letting the user upload an executable file or static archive and
showing functions and assembly code from that file, together with histograms
and the possibility of getting a visual diff of two functions.

The **Dyninst framework** was used for parsing executable files: http://www.dyninst.org/

The webapp is based on the Apache web server, with the back-end written in
Java (using JNI to link with the C++ Dyninst framework).


####Source code:  

All the servlets return JSON objects.

1. **UploadServlet.java**:
    This implements a POST method which lets the user upload an executable on
    the server. The executable will be parsed only the first time when the user
    demands it, so the UploadServlet only has to save the file in a specified
    directory. The directory containing the executable files is ```ROOT/WEB-INF/classes/gsoc-binaries```  

2. **FilesServlet.java**:
    Implements a GET method which returns all the files in the gson-binaries
    directory.

 The other four servlets can be categorized in two ways:
  * The ArchiveFuncsServlet and ArchiveServlet will be parsing static archive
    files (.a), and the FunctionsServlet and AssemblyServlet will be parsing
    executable files and shared objects(.so). This difference exists because
		the Dyninst framework treats differently archive files from executables
		and shared objects. Mainly, an archive file contains many object files
		(.o), and it can contain many functions with the same name (which will be
		defined by their start addresses)
  * The ArchiveFuncsServlet and FunctionsServlet will return a list of
    functions in a file, and the ArchiveServlet and AssemblyServlet will
		return the assembly code of a specific function.
		This design was chosen to acomodate the frontend, which first shows a list
		of functions to the user. When the user selects one of those functions, one
		of the assembly servlets is invoked to return the assembly code.

3. **FunctionsServlet**: implements a GET method which returns a list of
    functions and gets three parameters: file name, a string which represents
    how will the functions be sorted (one of 'name', 'size', or 'address'), and
    the sorting direction ('ascending' or 'descending').
    The getFunctions method does all the work, mainly: it invokes the JNI method
    'getFunctionsJni', reads the result from the destination file, packs it in a
    JSON list and then sorts it by the specified arguments.

    It also contains an 'isFunctionCached' method which checks if the parser
    result is cached (written in a file in the cached-functions directory) before
    calling the JNI method.

4. **AssemblyServlet**: implements a GET method which reads a file containing
    all the functions in the specified executable file, puts it in a map and
		returns the assembly of the function with the desired name.

    It uses an 'isAssemblyCached' method with the same functionality as the
		previous one.

5. **ArchiveFuncsServlet**: implements a GET method which returns a list of
    functions in an archive file. Functions in an archive file cand also be
		sorted by object name, which is the object file (.o) containing the
		respective function.

6. **ArchiveServlet**: implements a GET method which reads a file containing all
    the functions in the specified archive file, puts it in a map and returns the
    assembly of the function with the desired name. However, as in an archive
    file there can be more than one function with the same name, this servlet
    also receives the name of the object file containing the function in order
		to identify it.

The link with the Dyninst framework is done through JNI. That means that each
servlet, excluding UploadServlet and FilesServlet contains a native method
which executes code written in C++ which can call functions from the Dyninst
framework.

For each servlet there is a C++ source file which contains the implementation
of the native function. Those C++ sources are then compiled into a shared
object and used when needed by Java:

* FunctionsServlet: **get_functions.cc**  
* AssemblyServlet: **get_assembly.cc**  
* ArchiveFuncsServlet: **get_archive_funcs.cc**  
* ArchiveServlet: **get_archive.cc**  

Mainly, each C++ program receives the name of an executable and the name of a
file where to cache the result of the parsing in JSON format. The JSON
serializing process is done by hand (unfortunately), but this way, there is no
need to keep parsed data in a separate data structure inside the C++ program,
each line read from the executable file is immediately printed in the output
file.


####How to build:  

1. install apache tomcat, preferably in ```/usr/local```. If this path is not used for the directory containing the server, you have to change the paths in the servlet files  

2. For the upload functionality to work:
 * enable multipart parsing inside ```context.xml```
 * increase maxpostsize inside ```server.xml```  

3. install the dyninst framework:  
  1.libdyninst: Installing libdyninst and libdyninst-dev 8.1.2: http://www.dyninst.org/downloads/dyninst-8.x  

  2.boost: Download boost: http://sourceforge.net/projects/boost/files/boost/1.57.0/ Copy boost directory where libdyninst was installed:  
```cp -r boost_1_57_0/boost/ /usr/include/dyninst/  ```  

  3.libelf, libdwarf:  
```sudo apt-get install libelf-dev```   
Build libdwarf from sources and copy the dynamic library libdwarf.so (/dwarf-20130207/libdwarf/libdwarf.so) in /usr/lib:    
http://askubuntu.com/questions/502749/install-libdwarf-so-on-ubuntu  

4. copy the WEB-INF directory into ```<apache_root>/webapps/ROOT/```  
5. run ```make all```


####Future development notes:
* The frontend for this web app is at [dino-webapp-ui](https://github.com/alinmindroc/dino-webapp-ui)
* The ```/tmp/dino``` directory used for storing the executables and cached results is not the best chosen, as it
is cleared at every machine startup.
* An example of workflow in the web app is: When the user chooses an executable file, if it is not cached in the ```cached-functions``` directory, the C++ parser is invoked from JNI. This parses the executable and writes the result to a file in the ```cached-functions``` dir. After the method is done, the file is read inside Java and the result is returned at the ```/api/functions``` endpoint. 
* Possible improvements would be:
  * Using another solution to print functions' assembly lines. Angular's double binding can freeze the browser tab when showing functions containing more than 10000 instructions
  * When printing histograms for functions, the frequencies are computed on client-side, based on an array of defined functions in javascript (stats.js). It would be nice to get this data from the server, based on the types of instructions that the Dyninst version supports.
  * Show functions in a tree-like structure after the user chooses an executable file, with callee functions branching from callers.
  * Provide a control panel where an user with elevated privileges could see statistics of usage, uploaded files, parsing time, or delete executables. Right now, deleting executables can be done only by going into the ```gsoc-binaries``` directory and removing them.
  * Provide a 'Source view' which shows a mapping between a function's assembly and its source code. The back-end is written in the plugin: https://github.com/alinmindroc/dino-plugin
  * Inside the C++ files: don't create JSON content manually, instead save the results in an array and then output it 
using a JSON serializer. It would be unwise to communicate the results from C++ directly to JNI without caching them in separate files. This would mean that every time an executable/function is chosen from the UI, the parser would be invoked, and this can take up to 10 seconds for big executables. 
  * Contribute to Dyninst; some features which would have been nice for this project would have been:
    * platform-independent parsing (i.e. right now you have to recompile the whole Dyninst library in order to parse Windows executables on Linux)
    * support for AVX instructions
    * better documentation



