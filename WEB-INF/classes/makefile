all: headers compile_arch_assembly compile_arch_functions compile_bin_functions compile_bin_assembly lib
headers: FunctionsServlet.java AssemblyServlet.java
	javac FilesServlet.java
	javac UploadServlet.java
	javac FunctionsServlet.java
	javah -jni FunctionsServlet
	javac AssemblyServlet.java
	javah -jni AssemblyServlet
	javac ArchiveAssemblyServlet.java
	javah -jni ArchiveAssemblyServlet
	javac ArchiveFunctionsServlet.java
	javah -jni ArchiveFunctionsServlet

#gsoc1:
#java_pathi = /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.85-2.6.1.2.el7_1.x86_64/include/
#compile_bin_functions: get_functions.cc
#	g++ -fPIC -std=c++0x -I$(java_path) -I$(java_path)/linux -I/scratch/Dyninst-8.2.1/headers -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -c get_functions.cc -L/scratch/Dyninst-8.2.1/lib -ldwarf
#compile_bin_assembly: get_assembly.cc
#	g++ -fPIC -std=c++0x -I$(java_path) -I$(java_path)/linux -I/scratch/Dyninst-8.2.1/headers -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -c get_assembly.cc -L/scratch/Dyninst-8.2.1/lib -ldwarf
#compile_arch_functions: get_functions.cc
#	g++ -fPIC -std=c++0x -I$(java_path) -I$(java_path)/linux -I/scratch/Dyninst-8.2.1/headers -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -c get_archive_functions.cc -L/scratch/Dyninst-8.2.1/lib -ldwarf
#compile_arch_assembly: get_archive_assembly.cc
#	g++ -fPIC -std=c++0x -I$(java_path) -I$(java_path)/linux -I/scratch/Dyninst-8.2.1/headers -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -c get_archive_assembly.cc -L/scratch/Dyninst-8.2.1/lib -ldwarf

#local:
compile_bin_functions: get_functions.cc
	g++ -fPIC -std=c++0x -I/usr/lib/jvm/java-7-openjdk-amd64/include/ -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -c get_functions.cc -L/usr/include/dwarf.h -ldwarf
compile_bin_assembly: get_assembly.cc
	g++ -fPIC -std=c++0x -I/usr/lib/jvm/java-7-openjdk-amd64/include/ -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -c get_assembly.cc -L/usr/include/dwarf.h -ldwarf
compile_arch_assembly: get_archive_assembly.cc
	g++ -fPIC -std=c++0x -I/usr/lib/jvm/java-7-openjdk-amd64/include/ -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -c get_archive_assembly.cc -L/usr/include/dwarf.h -ldwarf
compile_arch_functions: get_archive_functions.cc
	g++ -fPIC -std=c++0x -I/usr/lib/jvm/java-7-openjdk-amd64/include/ -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -c get_archive_functions.cc -L/usr/include/dwarf.h -ldwarf
lib:
	#-lparseAPI to resolve mangled c++ naming
	g++ -shared get_assembly.o get_functions.o get_archive_assembly.o get_archive_functions.o -o libdyninstParser.so -L/scratch/Dyninst-8.2.1/lib -lparseAPI
run:
	java -cp . -Djava.library.path=. Parser
clean:
	rm -f *.o *.so *.h *.class
