#include <jni.h>
#include <fstream>
#include "Symtab.h"
#include "Archive.h"
#include "CodeObject.h"
#include "CFG.h"
#include "InstructionDecoder.h"
#include "Instruction.h"
#include "ArchiveFunctionsServlet.h"

using namespace Dyninst;
using namespace SymtabAPI;
using namespace ParseAPI;
using namespace InstructionAPI;

JNIEXPORT void JNICALL Java_ArchiveFunctionsServlet_getArchiveFunctionsJni
(JNIEnv * env, jobject obj, jstring jArchivePath, jstring jJsonPath){
	char *archivePath = (char*) env->GetStringUTFChars(jArchivePath, 0);
	char *jsonPath = (char*) env->GetStringUTFChars(jJsonPath, 0);

	stringstream outstream;
	ofstream jsonStream;
	jsonStream.open(jsonPath);

	Instruction::Ptr instr;
	Archive *ar = NULL;
	vector<Symtab *> symTabs;

	Archive::openArchive(ar, archivePath);
	if(ar == NULL){
		const char *error = "error: archive empty";
		jsonStream << error;
		jsonStream.close();
		return;
	}

	ar->getAllMembers(symTabs);

	auto sit = symTabs.begin();
	for(; sit != symTabs.end(); ++sit){
		int func_count = 0;

		Symtab *s = *sit;
		SymtabCodeSource *sts = new SymtabCodeSource(s);

		CodeObject *co = new CodeObject(sts);

		co->parse();

		vector<CodeRegion *> regions = co->cs()->regions();

		if(regions.size() == 0){
			//no code regions
			continue;
		}

		const CodeObject::funclist &all = co->funcs();
		if(all.size() == 0){
			//no functions
			continue;
		}

		Address crtAddr;

		auto fit = all.begin();
		ParseAPI::Function *f = *fit;
		InstructionDecoder decoder(f->isrc()->getPtrToInstruction(f->addr()),
				InstructionDecoder::maxInstructionLength,
				f->isrc()->getArch());

		//iterate the ContainerWrapper through all the functions
		for(;fit != all.end(); ++fit){
			ParseAPI::Function *f = *fit;
			//if the function exists, don't output it

			Address startAddr = f->addr();
			Address lastAddr;

			auto fbl = f->exitBlocks().end();
			if(f->exitBlocks().empty() == false){
				fbl--;
				Block *b = *fbl;
				lastAddr = b->last();
			} else {
				continue;
			}

			//parseAPI sometimes returns start address higher than last for sublime_text executable
			if(startAddr >= lastAddr)
				continue;

			if(func_count != 0)
				outstream << ",\n";
			func_count++;

			outstream << "{\"address\": \"" << hex << f->addr() << "\",\"name\":\"" << f->name();
			outstream << "\",\"size\":\"" << dec << lastAddr - startAddr;
			outstream << "\",\"obj\":\"" << s->name() << "\"}";

		}
		if(func_count != 0)
			outstream << ",\n";
	}

	string resp = outstream.str();

	if(resp.size() == 0){
		const char *error = "error: no functions parsed";
		jsonStream << error;
		jsonStream.close();
		return;
	}

	resp.pop_back();
	resp.pop_back();

	resp.insert(0, "[");
	resp.append("]");

	jsonStream << resp;
	jsonStream.close();
}
