import copy
import ast
from tree_sitter import Language, Parser
from Node import Pack, Class, Method
from abc import ABC,abstractmethod
import os
import copy
import re
import astor

# spec.py is used as a space to extract import statements, and format the results specific to each language. 
# It acts as the adapter to the import algorithm.


# Obtains path to already cloned tree-sitter-java repo (from CompressedTree)
for dirpath, dirnames, filenames in os.walk('../'):
    for name in dirnames:
        if (name=='tree-sitter-java'):
            path=dirpath

# Dependencies required by tree-sitter for Java code. 
## For extention to more languages, add languages in similar format, and clone language repo where you execute script from. 
Language.build_library(
  # Store the library in the `build` directory
  path+'/build/my-languages.so',

  # Include one or more languages
  [
    path+'/tree-sitter-java',
  ]
)
Java_Lang = Language(path+'/build/my-languages.so', 'java')


def add_indent(string, indent):
    lines = string.split("\n")  # Split the string into individual lines
    indented_lines = [indent + line for line in lines]  # Add the indent to each line
    indented_string = "\n".join(indented_lines)  # Join the lines back together with newline characters
    return indented_string

## Abstract Class Definition (Best you can do with Python lol)
class Lang(ABC):
    # Used to generate string path for particular target from tree. 
    @abstractmethod
    def output_traverse(node,string,all_imports,suspicious):
        pass

    # @abstractmethod
    # def output_methods(writer,class_name):
    #     pass

    # Used to parse the imports to generate a more read-friendly string for tree to split. 
    @abstractmethod
    def extractImports(content):
        pass

    @abstractmethod
    def getUsages(git_content):
        pass

    @abstractmethod
    def getClasses(content):
        pass

    # @abstractmethod
    # def get_lang():
    #     pass

## Java Implementation to Abstract Class
class Java(Lang):

    global usages
    usages=set()

    global parser
    parser=Parser()
    parser.set_language(Java_Lang)

    global classes
    classes=[]

    global class_ref  
    class_ref={}

    global all_classes
    all_classes={}

    global all_methods
    all_methods={}

    def get_lang(self):
        return "java"

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


    def output_methods(self,body,class_name):
        if (class_name.is_selected()):
            # print(class_name.get_full_name())
            spacing=' '*int(class_name.get_ranking()*4)
            body+='\n'+spacing+class_name.get_full_name()+'{\n\n'
            method_spacing=spacing+' '*4

            for declaration in class_name.get_declarations():
                body+=method_spacing+declaration+'\n'

            body+='\n'
            # print(class_name.get_full_name())
            for method in class_name.get_methods():
                if (method.is_selected()):
                    body+=method_spacing+method.get_method()+'\n'
            
            for sub_class in class_name.get_sub_classes():
                body = self.output_methods(body,sub_class)
        
            body+=spacing+class_name.get_closer()+'\n'
        return body


    def getUsages(self,git_content):
        byte_rep=str.encode(git_content)
        tree=parser.parse(byte_rep)

        # Define instances of a method usage using tree-sitter parsed text.
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

        # Decode all usages into string format, and add to list.
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
            res=val[0].parent.parent.text

            line=res.decode()
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
    
    def getClasses(self,content,version):
        content='\n'.join(content)

        byte_rep= str.encode(content)
        tree = parser.parse(byte_rep)

        query = Java_Lang.query("""
            (class_declaration
                name: (identifier) @name)
        """)

        class_captures=query.captures(tree.root_node)

        field_query = Java_Lang.query("""
            (field_declaration
            	declarator: (variable_declarator) @name)
        """)

        field_captures=field_query.captures(tree.root_node)

        method_query = Java_Lang.query("""
            (constructor_declaration
                name: (identifier) @name)

        """)


        method_captures=method_query.captures(tree.root_node)

        method_query = Java_Lang.query("""
            (method_declaration
                name: (identifier) @name)

        """)

        method_captures+=method_query.captures(tree.root_node)

        for new_class in class_captures:
            # Tuple of class details including modifiers and name.
            class_details=new_class[0].parent.children
            # Checks any potential class it is nested into
            super_class_name=new_class[0].parent.parent.parent

            indentation=int(class_details[0].start_point[1])
            new_class_name=class_details[2].text.decode()


            if (class_details[3].text.decode()[0]!="{"):
                superclass=" "+class_details[3].text.decode()
            else:
                superclass=" "
            superclass=superclass.split('{')[0]

            new_full_name=class_details[0].text.decode()+" "+class_details[1].text.decode()+" "+class_details[2].text.decode()+superclass
            
            new_class=Class(new_class_name,new_full_name,indentation,"}",version)

            if (new_class_name in all_classes.keys()):
                if (new_class not in all_classes[new_class_name]):
                    all_classes[new_class_name].append(new_class)
                else:
                    index=all_classes[new_class_name].index(new_class)
                    new_class=all_classes[new_class_name][index]
            else:
                all_classes[new_class_name]=[new_class]

            if (new_full_name not in class_ref.keys()):
                class_ref[new_full_name]=new_class
            else:
                class_ref[new_full_name].add_version(version)

            # print(new_full_name)


            # Checks if there exists a super/parent class
            if (super_class_name is None):
                if (new_class not in classes):
                    classes.append(new_class)
            else:
                # Searches for given super/parent class in list.
                if (super_class_name.children[3].text.decode()[0]!="{"):
                    superclass=" "+super_class_name.children[3].text.decode()
                else:
                    superclass=" "
                superclass=superclass.split('{')[0]
                # if (super_class_name.children[3] is not None):
                #     superclass=" "+super_class_name.children[3].text.decode()
                # else:
                #     superclass=" "
                super_name=super_class_name.children[0].text.decode()+" "+super_class_name.children[1].text.decode()+" "+super_class_name.children[2].text.decode()+superclass
                class_ref[super_name].add_sub_classes(new_class)
        
        for field in field_captures:
            declaration=field[0].parent.text.decode()
            if (field[0].parent.parent.parent.children[3].text.decode()[0]!="{"):
                superclass=" "+field[0].parent.parent.parent.children[3].text.decode()
            else:
                superclass=" "
            parent_class=field[0].parent.parent.parent.children[0].text.decode()+" "+field[0].parent.parent.parent.children[1].text.decode()+" "+field[0].parent.parent.parent.children[2].text.decode()+superclass
            class_ref[parent_class].add_declaration(declaration)

        for method in method_captures:

            method_name=method[0].parent.text.decode()
            method_declaration=" "
            index=-2

            while ("(" not in method_declaration or method_declaration[0]=="("):
                method_declaration=method[0].parent.children[index].text.decode()+" "+method_declaration
                index-=1

            name=method[0].parent.parent.parent.children
            super_class=""
            index=0

            while(index<len(name) and "{" not in name[index].text.decode()):
                super_class+=name[index].text.decode()+" "
                index+=1
            new_method=Method(method_name,version,super_class)

            #Method declaration is referenced by its signature.
            if (super_class+" "+method_declaration in all_methods.keys()):
                if (new_method not in all_methods[super_class+" "+method_declaration]):
                    all_methods[super_class+" "+method_declaration].append(new_method)
                    self.add_method(classes,new_method,super_class,version)
                else:
                    index=all_methods[super_class+" "+method_declaration].index(new_method)
                    all_methods[super_class+" "+method_declaration][index].add_version(version)
            else:
                all_methods[super_class+" "+method_declaration]=[new_method]

                self.add_method(classes,new_method,super_class,version)

            # indentation=int(method[0].parent.parent.parent.children[0].start_point[1])
            # print("sent"+str(new_method))
            # self.add_method(classes,new_method,super_class,version)
        # body=""
        # for class_name in classes:
        #     body=self.output_methods(body,class_name)


        return classes
    
    def add_method(self,classes,new_method,super_class,version):
        for new_c in classes:
            if (new_c.get_full_name().strip(" ")==super_class.strip(" ")):
                # print(super_class)
                # print(new_c.get_full_name())
                new_c.add_method(new_method,version)
                # print(new_c.get_methods())
            else:
                self.add_method(new_c.get_sub_classes(),new_method,super_class,version)

    def get_method_ref(self):
        return dict(all_methods)
    
    def get_class_ref(self):
        return dict(all_classes)
    def initialize_body():
        return ""

class Python(Lang):


    global methods
    methods=[]

    global codeseq
    codeseq = []
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


    def getUsages(self,git_content):
        return super().getUsages()
    
    def ifinscope(self,codeast):
        structure = ''
        pattrn = r'^(Assign)*(ClassDef|FunctionDef)*(If)?$'
        for nod in codeast.body:
            structure= structure+ type(nod).__name__
        if re.match(pattrn, structure):
            return True
        else:
            return False


    def getClasses(self,content,version):
        str_content = ""
        pointer = 0
        for line in content:
                if line== "!!!no import anymore!!!":
                    continue
                str_content=str_content+line+'\n'
        content = str_content


        codeast = self.generateAST(content)
        if self.ifinscope(codeast)== False:
            return "**to_be_handled_by_git**"
        
        for nod in codeast.body:
            if isinstance(nod, ast.FunctionDef):
                methodName = nod.name
                if "function "+methodName not in codeseq:
                    codeseq.insert(pointer,"function "+methodName)
                    pointer = pointer+1
                else:
                    pointer = (codeseq.index("function "+methodName)) + 1
                methodindent= str(nod.col_offset)
                newMethod = Method(astor.to_source(nod),version,None,nod)

                #Method declaration is referenced by its signature.
                if (methodindent+" "+methodName in all_methods.keys()):
                    nodepresent = False
                    for methodObject in all_methods[methodindent+" "+methodName]:
                        if astor.to_source(methodObject.astnode) == astor.to_source(nod):
                            methodObject.add_version(version)
                            nodepresent =  True
                    if nodepresent == False:
                            all_methods[methodindent+" "+methodName].append(newMethod)
                            methods.append(newMethod)
                else:
                    all_methods[methodindent+" "+methodName]=[newMethod]
                    methods.append(newMethod)
            elif isinstance(nod, ast.ClassDef):
                className = nod.name
                if "class "+className not in codeseq:
                    codeseq.insert(pointer,"class "+className)
                    pointer = pointer+1
                else:
                    pointer = (codeseq.index("class "+methodName)) + 1
                # methodindent= str(nod.col_offset)
                newClass = Class(className,className,0,None,version,nod)

                #Method declaration is referenced by its signature.

                if (className in all_classes.keys()):
                    classnod =  all_classes[className]
                    classnod.add_version(version)
                    for child in nod.body:
                        if isinstance(child,ast.Assign):
                            if astor.to_source(child) not in classnod.declarations:
                                classnod.declarations.append(astor.to_source(child))
                        elif isinstance(child,ast.FunctionDef):
                            if child.name not in classnod.methods:
                                classnod.methods.append(child.name)
                            methodName = child.name
                        
                            methodindent= str(child.col_offset)
                            newMethod = Method(astor.to_source(child),version,None,child)
                            if (className+" "+methodName in all_methods.keys()):
                                nodepresent = False
                                for methodObject in all_methods[className+" "+methodName]:
                                    if astor.to_source(methodObject.astnode) == astor.to_source(child):
                                        methodObject.add_version(version)
                                        nodepresent =  True
                                if nodepresent == False:
                                    all_methods[className+" "+methodName].append(newMethod)
                                    methods.append(newMethod)
                            else:
                                all_methods[className+" "+methodName ]=[newMethod]
                                methods.append(newMethod)
                else:
                    all_classes[className]=[newClass]
                    for child in nod.body:
                        if isinstance(child,ast.Assign):
                            newClass.declarations.append(astor.to_source(child))
                        elif isinstance(child,ast.FunctionDef):
                            newClass.methods.append(child.name)
                            methodName = child.name
                            methodindent= str(child.col_offset)
                            newMethod = Method(astor.to_source(child),version,None,child)
                            if (className+" "+methodName in all_methods.keys()):
                                nodepresent = False
                                for methodObject in all_methods[className+" "+methodName]:
                                    if astor.to_source(methodObject.astnode) == astor.to_source(child):
                                        methodObject.add_version(version)
                                        nodepresent =  True
                                if nodepresent == False:
                                    all_methods[className+" "+methodName].append(newMethod)
                                    methods.append(newMethod)
                            else:
                                all_methods[className+" "+methodName ]=[newMethod]
                                methods.append(newMethod)



            
            elif isinstance(nod, ast.ClassDef):
                pass

            elif isinstance(nod, ast.Assign):
                code =  astor.to_source(nod)
                if code not in codeseq:
                    codeseq.insert(pointer,code)
                    pointer = pointer+1
                else:
                    pointer = (codeseq.index(code)) + 1
            elif isinstance(nod, ast.If):
                code =  astor.to_source(nod)
                if code not in codeseq:
                    codeseq.append(code)


        return methods
    
    def output_methods(self):
        print(codeseq)
        print(all_methods)
        body  =  self.initialize_body()
        codearray = [None]*len(codeseq)
        for methodname in all_methods.keys():
            if methodname.split(" ")[0]=="0":
                for nod in all_methods[methodname]:
                    if nod.selected == True:
                        pointer = codeseq.index("function "+methodname.split(" ")[1])
                        codearray[pointer] = nod.method_name
        for classname in all_classes.keys():
            classobj = all_classes[classname][0]
            if classobj.version == set("base"):
                continue
            classcode = ''
            indent = "    "
            classcode = classcode+ (astor.to_source(classobj.node)).split("\n")[0]+'\n'
            for i in classobj.declarations:
                classcode = classcode + indent+i
            for metho in classobj.methods:
                for mo in all_methods[classname+' '+metho]:
                    if mo.selected == True:
                        methodcode = mo.method_name
                # print(methodcode)
                # print(add_indent(methodcode,indent))
                classcode = classcode + add_indent(methodcode,indent)+'\n'
            

            pointer = codeseq.index("class "+classname)
            codearray[pointer] = classcode


        for i  in codearray:
            if i:
                body = body + i + '\n'
        lastele = codeseq.pop()
        if lastele[0:3] == "if ":
            body = body + lastele
        return body

    
    def get_class_ref(self):
        return {"None":[]}
    
    def get_method_ref(self):
        return dict(all_methods)
    


    def get_lang(self):
        return "py"
    
    def initialize_body(self):
        body = ""
        for code in codeseq:
            if code[0:3]!="if " and code[0:9]!="function " and code[0:6]!="class ":
                body = body+code+"\n"
                        

        return body