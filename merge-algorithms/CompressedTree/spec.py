from abc import ABC,abstractmethod
import copy
import re
import ast
from Node import Pack

# spec.py is used as a space to extract import statements, and format the results specific to each language. 
# It acts as the adapter to the import algorithm.

class Language(ABC):
    # Used to generate string path for particular target from tree. 
    @abstractmethod
    def output_traverse(node,string,all_imports):
        pass

    # Used to parse the imports to generate a more read-friendly string for tree to split. 
    @abstractmethod
    def extractImports(content):
        pass
    # @abstractmethod
    # def generateAST(content):
    #     pass

class Java(Language):
    def output_traverse(self,node,string,all_imports,target):
        # Finds the specified target node in the tree
        for item in node.get_children():
            dup=copy.deepcopy(string)
            dup+=item.get_full_dir()
            if (type(item)==Pack):
                self.output_traverse(item,dup,all_imports,target)
            elif (item==target):
                all_imports.append(dup+";")
 

    def extractImports(self,content):
        dict={
            'import':re.compile(r'import *'),
            'package':re.compile(r'package *')
            }

        imports=[]
        other=copy.deepcopy(content)
        
        for line in content:
            for key,rx in dict.items():
                match=rx.search(line)
                if (match):
                    other.remove(line)
                    index=line.rfind(".")
                    lstring=line[0:index+1]
                    rstring=line[index+1:-1]
                    imports.append([lstring,rstring])
        return imports,other



class Python(Language):
    def sayhi(self):
        print("Hi , I am a python code!")
    def generateAST(self,content):
        return ast.parse(content)
    def getImportNodes(self,codetree):
        import_nodes = []
        top_layer = codetree.body  # Accessing the principal imports only
        for nod in top_layer:
            if isinstance(nod, ast.Import):
                import_nodes.append(nod)
            elif isinstance(nod, ast.ImportFrom):
                import_nodes.append(nod)
        return import_nodes
    def extractImports(self,content):
        importlineno = []
        formatted_imports = []
        codeTree = self.generateAST(content)
        import_nodes = self.getImportNodes(codeTree)
        restofCode = content.split('\n')

        for nod in import_nodes :
            for i in range(nod.lineno,nod.end_lineno+1):
                restofCode[i-1] = ''


            if isinstance(nod, ast.Import):
                for alias in nod.names:
                    if alias.asname == None :
                        formatted_imports.append(["import",alias.name,nod.lineno,nod.end_lineno])
                    else :
                        formatted_imports.append(["import",f'{alias.name} as {alias.asname}',nod.lineno,nod.end_lineno])
            if isinstance(nod, ast.ImportFrom):
                for alias in nod.names:
                    if alias.asname == None :
                        formatted_imports.append([f"from {nod.module} import",alias.name,nod.lineno,nod.end_lineno])
                    else :
                        formatted_imports.append([f"from {nod.module} import",f'{alias.name} as {alias.asname}',nod.lineno,nod.end_lineno])

        return formatted_imports,restofCode

    def output_traverse(self,node,string,all_imports,target):
        # Finds the specified target node in the tree
        for item in node.get_children():
            dup=copy.deepcopy(string)
            dup+=item.get_full_dir()
            if (type(item)==Pack):
                dup+=" "
                self.output_traverse(item,dup,all_imports,target)
            elif (item==target):
                all_imports.append(dup)