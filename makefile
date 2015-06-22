all: binary assembly
binary: binary.c
	gcc binary.c -o binary
assembly: get_assembly.cc
	g++ -std=c++0x get_assembly.cc -o parser -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -L/usr/include/dwarf.h -ldwarf -o get_assembly
graph: get_graph.cc
	g++ -std=c++0x get_graph.cc -o parser -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -L/usr/include/dwarf.h -ldwarf -o get_graph
functions: get_functions.cc
	g++ -std=c++0x get_functions.cc -o parser -I/usr/include/dyninst -lparseAPI -linstructionAPI -lsymtabAPI -lsymLite -ldynDwarf -ldynElf -lcommon -L/usr/include/dwarf.h -ldwarf -o get_functions
draw:
	./get_graph binary | dot -Tpng > graph.png
clean:
	rm -f get_graph get_assembly get_functions
