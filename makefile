get_binary: binary.c
	gcc binary.c -o binary
assembly: get_assembly.cc
	g++ -std=c++0x get_assembly.cc -o parser -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -L/usr/include/dwarf.h -ldwarf
graph: get_graph.cc
	g++ -std=c++0x get_graph.cc -o parser -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -L/usr/include/dwarf.h -ldwarf
functions: get_functions.cc
	g++ -std=c++0x get_functions.cc -o parser -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -L/usr/include/dwarf.h -ldwarf
draw: parser
	./parser binary | dot -Tpng > hello.png && go hello.png
clean:
	rm -f parser
