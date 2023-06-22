import copy
import ast
from tree_sitter import Language, Parser
from Node import Pack
from abc import ABC,abstractmethod

# spec.py is used as a space to extract import statements, and format the results specific to each language. 
# It acts as the adapter to the import algorithm.


# Dependencies required by tree-sitter for Java code. 
## For extention to more languages, add languages in similar format, and clone language repo where you execute script from. 
Language.build_library(
  # Store the library in the `build` directory
  'build/my-languages.so',

  # Include one or more languages
  [
    'tree-sitter-java',
  ]
)
Java_Lang = Language('build/my-languages.so', 'java')


## Abstract Class Definition (Best you can do with Python lol)
class Lang(ABC):
    # Used to generate string path for particular target from tree. 
    @abstractmethod
    def output_traverse(node,string,all_imports,suspicious):
        pass

    # Used to parse the imports to generate a more read-friendly string for tree to split. 
    @abstractmethod
    def extractImports(content):
        pass

    @abstractmethod
    def getUsages(git_content):
        pass

## Java Implementation to Abstract Class
class Java(Lang):

    global usages
    usages=set()

    global parser
    parser=Parser()
    parser.set_language(Java_Lang)

    def output_traverse(self,node,string,all_imports,target,suspicious):
        # Finds the specified target node in the tree
        # print(target.get_full_dir())

        if (suspicious and (target.get_full_dir() not in usages) and (target.get_full_dir()[-1]!="*")):
            pass
        else:
            for item in node.get_children():
                dup=copy.deepcopy(string)
                dup+=item.get_full_dir()
                ## If the type is Pack, then it is not a leaf node. Recurse further.
                if (type(item)==Pack):
                    self.output_traverse(item,dup,all_imports,target,False)
                elif (item==target):
                    all_imports.append(dup+";")


    def getUsages(self,git_content):
        byte_rep=str.encode(git_content)
        tree=parser.parse(byte_rep)

        usage_query=Java_Lang.query("""
            ((type_identifier) @type
            (#match? @type ""))
        """)

        usages_byte=usage_query.captures(tree.root_node)

        usage_query=Java_Lang.query("""
            object: (identifier) @type
            (#match? @type "")
        """)

        usages_byte+=usage_query.captures(tree.root_node)

        usage_query=Java_Lang.query("""
            type: (type_identifier) @type
            (#match? @type "")
        """)

        usages_byte+=usage_query.captures(tree.root_node)

        usage_query=Java_Lang.query("""
        ((method_invocation
            name: (identifier) @type)
            (#match? @type ""))
        """)

        usages_byte+=usage_query.captures(tree.root_node)

        usage_query=Java_Lang.query("""
        ((marker_annotation
            name: (identifier) @type)
            (#match? @type ""))
        """)

        usages_byte+=usage_query.captures(tree.root_node)

        usage_query=Java_Lang.query("""
        ((method_reference
            (identifier) @type)
            (#match? @type ""))
        """)

        usages_byte+=usage_query.captures(tree.root_node)


        for usage in usages_byte:
            usages.add(usage[0].text.decode())


 
    def extractImports(self,content):
        imports=[]
        other=content.split("\n")

        byte_rep= str.encode(content)
        tree = parser.parse(byte_rep)

        query = Java_Lang.query("""
            ((package_declaration
                (scoped_identifier
                    scope: (scoped_identifier) @type))
                    (#match? @type ""))

        """)
        captures= query.captures(tree.root_node)


        query = Java_Lang.query("""
            ((import_declaration
                (scoped_identifier
                    scope: (scoped_identifier) @type))
                    (#match? @type ""))

        """)


        captures+= query.captures(tree.root_node)

        query = Java_Lang.query("""
            ((import_declaration
                (scoped_identifier
                    scope: (identifier) @type))
                    (#match? @type ""))
        """)

        captures+= query.captures(tree.root_node)

        for val in captures:
            res=val[0].parent
            while(True):
                if (res.parent is None):
                    break
                else:
                    if (res.parent.text.decode()[-1]!=';'):
                        res=res.parent
                    else:
                        res=res.parent
                        break

            line=res.text.decode()
            other.remove(line)

            #Split import statement to seperate path and desired package.
            index=line.rfind(".")
            lstring=line[0:index+1]
            rstring=line[index+1:-1]

            #Case where package is wildcard, we need one path value as reference.
            if rstring=="*":
                new_index=lstring[0:index].rfind(".")
                lstring=line[0:new_index+1]
                rstring=line[new_index+1:-1]

            imports.append([lstring,rstring])

        return imports,other



class Python(Lang):
    done=[]
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
    

    # The extraction is done in specific format to convert the list of imports into a common tree like structure that for python as well as JAVA
    def extractImports(self,content):
        formatted_imports = []
        codeTree = self.generateAST(content)
        import_nodes = self.getImportNodes(codeTree)
        restofCode = content.split('\n')

        for nod in import_nodes :
            for i in range(nod.lineno,nod.end_lineno+1):
                restofCode[i-1] = "!!!no import anymore!!!"


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
    

    def output_traverse(self,node,string,all_imports,target,formatter = ""):
        # Finds the specified target node in the tree
        for item in node.get_children():
            formatter = copy.deepcopy(formatter)

            if item.get_full_dir()[0:4] == "from":
                if len(item.get_children())>1:
                    if item.get_children()[1].leftstartline == item.get_children()[0].leftstartline :
                        if item.get_children()[1].leftstartline == item.get_children()[1].leftendline:
                            formatter = "oneline"
                        else :
                            formatter = "multiline"
                    else:
                        formatter = "separateline"

            dup=copy.deepcopy(string)
            theprefix = dup
            dup+=item.get_full_dir()
            if (type(item)==Pack):
                dup+=" "
                self.output_traverse(item,dup,all_imports,target,formatter)
                formatter = ''
            elif (item==target):
                if formatter == "":

                    all_imports.append(dup)
                else:
                    for i,imp in enumerate(all_imports):
                        if theprefix in imp:
                            match formatter:
                                case "oneline":
                                    all_imports[i] = all_imports[i] + ',' + target.get_full_dir()
                                case "multiline":
                                    
                                    temparray = all_imports[i].split('\n')
                                    if len(temparray)==1:
                                        temimp = imp.replace(theprefix,'')
                                        all_imports[i] = theprefix+ f'(\n    {temimp},\n    {target.get_full_dir()},\n)'
                                    else :
                                        new_imp = temparray[0]+'\n'
                                        temparray.pop(0)
                                        temparray.pop()
                                        for el in temparray:
                                            new_imp+=el+'\n'
                                        new_imp += f'    {target.get_full_dir()},\n)'
                                        all_imports[i] = new_imp

                                            
                                case "separateline":
                                    all_imports.append(dup)
                            return
                    
                    all_imports.append(dup)
                            
