from abc import ABC,abstractmethod
import copy
import re
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
    def extractImports(self,content):
        return super().extractImports()
    

