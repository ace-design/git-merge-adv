import copy
import ast
from tree_sitter import Language, Parser
from Node import Pack, Class, Method
from abc import ABC,abstractmethod
import os
import copy

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


## Abstract Class Definition (Best you can do with Python lol)
class Lang(ABC):
    # Used to generate string path for particular target from tree. 
    @abstractmethod
    def output_imports(node,string,all_imports,suspicious):
        pass

    @abstractmethod
    def output_body(writer,class_name):
        pass

    # Used to parse the imports to generate a more read-friendly string for tree to split. 
    @abstractmethod
    def extractImports(content):
        pass

    @abstractmethod
    def getUsages(git_content):
        pass

    @abstractmethod
    def extractBody(content):
        pass

    @abstractmethod
    def get_lang():
        pass

## Java Implementation to Abstract Class
class Java(Lang):

    global usages
    usages=set()

    global parser
    parser=Parser()
    parser.set_language(Java_Lang)

    #Used to store each main class (each object has a class tree-structure)
    #Items in list will be directly added to overall tree
    global classes
    classes=[]

    #Used for each class object reference.
    #Key is the full class declaration, value is the associated class object
    #Alternative to traversing entire tree to find subclass object.
    global class_ref  
    class_ref={}

    #Used to store all class objects as values to a particular class name key.
    #Similar to what was done for imports.
    global all_classes
    all_classes={}

    #Same idea as all_classes, but for methods.
    global all_methods
    all_methods={}

    def get_lang(self):
        return "java"

    def output_imports(self,node,string,all_imports,target,suspicious):
        # Finds the specified target node in the tree

        if (suspicious and (target.get_full_dir() not in usages) and (target.get_full_dir()[-1]!="*")):
            pass
        else:
            for item in node.get_children():
                dup=copy.deepcopy(string)
                dup+=item.get_full_dir()
                ## If the type is Pack, then it is not a leaf node. Recurse further.
                if (type(item)==Pack):
                    self.output_imports(item,dup,all_imports,target,False)
                elif (item==target):
                    all_imports.append(dup+";")


    def output_body(self,body,class_name):
        if (class_name.is_selected()):
            spacing=' '*int(class_name.get_ranking()*4)
            body+='\n'+spacing+class_name.get_full_name()+'{\n\n'
            method_spacing=spacing+' '*4

            for declaration in class_name.get_declarations():
                body+=method_spacing+declaration+'\n'

            body+='\n'
            for method in class_name.get_methods():
                if (method.is_selected()):
                    body+=method_spacing+method.get_method()+'\n'
            
            for sub_class in class_name.get_sub_classes():
                body = self.output_body(body,sub_class)
        
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
    
    def extractBody(self,content,version):
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

            indentation=int(class_details[0].start_point[1])
            new_class_name=class_details[2].text.decode()

            #Checks whether class extends/implements another one.
            if (class_details[3].text.decode()[0]!="{"):
                super_class=" "+class_details[3].text.decode()
            else:
                super_class=" "
            super_class=super_class.split('{')[0]

            #Stores full class declaration
            new_full_name=class_details[0].text.decode()+" "+class_details[1].text.decode()+" "+class_details[2].text.decode()+super_class
            
            class_obj=Class(new_class_name,new_full_name,indentation,"}",version)

            #If object with same full class declaration is in the list of classes, then class_obj references that object.
            #Equality relation for class is re-defined in Node.py.
            if (new_class_name in all_classes.keys()):
                if (class_obj not in all_classes[new_class_name]):
                    all_classes[new_class_name].append(class_obj)
                    class_ref[new_full_name]=class_obj
                else:
                    index=all_classes[new_class_name].index(class_obj)
                    class_obj=all_classes[new_class_name][index]
                    class_ref[new_full_name].add_version(version)

            else:
                all_classes[new_class_name]=[class_obj]
                class_ref[new_full_name]=class_obj

            # Checks if class is nested in another class. 
            # Adds it as subclass to main class if it is.
            nested_class_details=new_class[0].parent.parent.parent
            if (nested_class_details is None):
                if (class_obj not in classes):
                    classes.append(class_obj)
            else:
                if (nested_class_details.children[3].text.decode()[0]!="{"):
                    nested_class=" "+nested_class_details.children[3].text.decode()
                else:
                    nested_class=" "
                nested_class=nested_class.split('{')[0]
                full_nested_name=nested_class_details.children[0].text.decode()+" "+nested_class_details.children[1].text.decode()+" "+nested_class_details.children[2].text.decode()+nested_class
                class_ref[full_nested_name].add_sub_classes(class_obj)
        
        #Adds all variable declarations to associated classes.
        for field in field_captures:
            declaration=field[0].parent.text.decode()
            if (field[0].parent.parent.parent.children[3].text.decode()[0]!="{"):
                nested_class=" "+field[0].parent.parent.parent.children[3].text.decode()
            else:
                nested_class=" "
            parent_class=field[0].parent.parent.parent.children[0].text.decode()+" "+field[0].parent.parent.parent.children[1].text.decode()+" "+field[0].parent.parent.parent.children[2].text.decode()+nested_class
            class_ref[parent_class].add_declaration(declaration)

        #Adds all methods to associated classes
        for method in method_captures:
            
            #Stores method as a whole
            method_description=method[0].parent.text.decode()

            #Method signature used to identify equivalent methods.
            method_signature=" "
            index=-2
            while ("(" not in method_signature or method_signature[0]=="("):
                method_signature=method[0].parent.children[index].text.decode()+" "+method_signature
                index-=1

            #Extract which class the method belongs to.
            super_class_details=method[0].parent.parent.parent.children
            super_class=""
            index=0
            while(index<len(super_class_details) and "{" not in super_class_details[index].text.decode()):
                super_class+=super_class_details[index].text.decode()+" "
                index+=1

            method_obj=Method(method_description,version,super_class)

            #Method declaration is referenced by its signature and super class.
            #Method signatures can be the same if declared in two different classes.
            if (super_class+" "+method_signature in all_methods.keys()):
                if (method_obj not in all_methods[super_class+" "+method_signature]):
                    all_methods[super_class+" "+method_signature].append(method_obj)
                    self.add_method(classes,method_obj,super_class,version)
                else:
                    index=all_methods[super_class+" "+method_signature].index(method_obj)
                    all_methods[super_class+" "+method_signature][index].add_version(version)
            else:
                all_methods[super_class+" "+method_signature]=[method_obj]
                self.add_method(classes,method_obj,super_class,version)

        return classes
    
    #Recursively finds object for super class, and adds method object to it.
    def add_method(self,classes,new_method,super_class,version):
        for new_c in classes:
            if (new_c.get_full_name().strip(" ")==super_class.strip(" ")):
                new_c.add_method(new_method,version)
            else:
                self.add_method(new_c.get_sub_classes(),new_method,super_class,version)

    def get_method_ref(self):
        return dict(all_methods)
    
    def get_class_ref(self):
        return dict(all_classes)


class Python(Lang):
    done=[]

    def get_lang(self):
        return "python"

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
    

    def output_imports(self,node,string,all_imports,target,formatter = ""):
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
                self.output_imports(item,dup,all_imports,target,formatter)
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
    
    def extractBody(self,content):
        return super().extractBody()
                            
