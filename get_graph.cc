#include<stdio.h>
#include<map>
#include<vector>
#include<unordered_map>
#include<sstream>
#include<memory>
#include "CodeObject.h"
#include "CFG.h"

using namespace std;
using namespace Dyninst;
using namespace ParseAPI;

int main(int argc, char *argv[]){
	map<Address, bool> seen;
	vector<Function *> funcs;
	SymtabCodeSource *sts;
	CodeObject *co;

	sts = new SymtabCodeSource(argv[1]);
	co = new CodeObject(sts);

	co->parse();
	cout << "digraph G {" << endl;

	const CodeObject::funclist &all = co->funcs();
	auto fit = all.begin();
	for(int i=0; fit != all.end(); ++fit, i++){
		Function *f = *fit;

		//generate graph only for these specific functions
		//if(strcmp(f->name().c_str(), "func1") != 0 && strcmp(f->name().c_str(), "func2") != 0 && strcmp(f->name().c_str(), "func3") != 0 && strcmp(f->name().c_str(), "main") != 0 && strcmp(f->name().c_str(), "printf") != 0)
			//continue;

		cout << "\t subgraph cluster_" << i
			<< " { \n\t\t label=\""
			<< f->name()
			<<"\"; \n\t\t color=blue;" << endl;

		cout << "\t\t\"" << hex << f->addr() << dec
			<< "\" [shape=box";

		if(f->retstatus() == NORETURN)
			cout << ",color=red";
		cout<< "]" << endl;

		cout << "\t\t\"" << hex << f->addr() << dec
			<< "\" [label = \""
			<< f->name() << "\\n" << hex << f->addr() << dec
			<< "\"];" << endl;

		stringstream edgeoutput;
		auto bit = f->blocks().begin();
		for(; bit != f->blocks().end(); ++bit){
			Block *b = *bit;
			if(seen.find(b->start()) != seen.end())
				continue;

			seen[b->start()] = true;

			cout << "\t\t\"" << hex << b->start() << dec <<
				"\";" << endl;

			auto it = b->targets().begin();
			for(; it != b->targets().end(); ++it){
				std::string s = "";
				if((*it)->type() == CALL)
					s = " [color=blue]";
				else if((*it)->type() == RET)
					s = " [color=green]";

				edgeoutput << "\t\""
					<< hex << (*it)->src()->start()
					<<"\" -> \""
					<< (*it)->trg()->start()
					<< "\"" << s << endl;
			}
		}

		cout << "\t}" << endl;

		cout << edgeoutput.str() << endl;
	}
	cout<<"}" << endl;
}
