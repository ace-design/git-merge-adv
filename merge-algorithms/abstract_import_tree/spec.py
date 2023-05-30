from abc import ABC,abstractmethod
import copy
from Node import Node
from Tree import Tree

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

    # After extracting the read-friendly strings, this is used to split and create the tree. 
    @abstractmethod
    def gen_tree(base_import, right_import, left_import):
        pass

class Java(Language):
    def output_traverse(self,node,string,all_imports,target):
        # Finds the specified target node in the tree
        if (len(node.get_children())==0):
            if (node==target):
                all_imports.append(string[:-1])
        else:
            for item in node.get_children():
                dup=copy.deepcopy(string)
                if (item.get_dir()=="import" or item.get_dir()=="static" or item.get_dir()=="package"):
                    dup+=item.get_dir()+" "
                else:
                    dup+=item.get_dir()+"."
                self.output_traverse(item,dup,all_imports,target)

    def extractImports(self,content):
        imports=[]
        other=copy.deepcopy(content)

        for line in content:
            # Narrowing search to 'import' or 'package' keyword.
            if (line[0:6]=="import" or line[0:7]=="package"):
                other.remove(line)
                line=line.replace(" ",".")
                line=line.replace("/",".")
                imports.append(line)
        return imports,other
    
    def gen_tree(self,base_import, right_import, left_import):

        root=Node("")
        tree=Tree(root)

        #Adds all imports to the tree. Tree structure ensures no duplicates.

        for imports in left_import:
            dict=imports.split(".")
            tree.add(dict,"left")

        
        for imports in right_import:
            dict=imports.split(".")
            tree.add(dict,"right")


        for imports in base_import:
            dict=imports.split(".")
            tree.add(dict,"base")


        #Writes imports in tree to given output file.
        return tree
