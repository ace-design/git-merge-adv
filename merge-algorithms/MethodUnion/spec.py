import copy
import ast
from tree_sitter import Language, Parser
from Node import Pack, Class, Method, Comment,Field
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

    #Used to store each main class (each object has a class tree-structure)
    #Items in list will be directly added to overall tree
    global bottom_body
    bottom_body=[]

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

    global all_fields
    all_fields={}

    global top_body
    top_body=[]

    def get_lang(self):
        return "java"

    def output_imports(self,node,string,target,suspicious):
        # Finds the specified target node in the tree

        if (suspicious and (target.get_full_dir() not in usages) and (target.get_full_dir()[-1]!="*")):
            pass
        else:
            for item in node.get_children():
                dup=copy.deepcopy(string)
                dup+=item.get_full_dir()
                ## If the type is Pack, then it is not a leaf node. Recurse further.
                if (type(item)==Pack):
                    self.output_imports(item,dup,target,False)
                elif (item==target):
                    if (dup+';' not in top_body):
                        top_body.insert(target.get_start(),dup+';')
    
    def get_top_body(self):
        return top_body


    def output_body(self,body,class_name):
        if (type(class_name)==Class and class_name.is_selected()):
            result=class_name.get_everything()

            spacing=' '*int(class_name.get_ranking()*4)
            body+='\n'+spacing+class_name.get_full_name()+'{\n\n'
            method_spacing=spacing+' '*4


            # for declaration in class_name.get_declarations():
            #     body+=method_spacing+declaration+'\n'

            for item in result:
                if item.is_selected():
                    if (type(item)==Method):
                        body+='\n'+method_spacing+item.get_method()+'\n'
                    elif (type(item)==Class):
                        body=self.output_body(body,item)
                    elif (type(item)==Comment):
                        body+='\n'+method_spacing+item.get_comment()+'\n'
                    elif (type(item)==Field):
                        body+=method_spacing+item.get_declaration()+'\n'

            body+='\n'+spacing+'}'
        elif (type(class_name)==Comment):
            spacing=class_name.get_indent()*' '
            body+='\n'+spacing+class_name.get_comment()+'\n'
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
            ((package_declaration) @name)

        """)
        captures= query.captures(tree.root_node)
        query = Java_Lang.query("""
            ((import_declaration) @name)

        """)
        captures+= query.captures(tree.root_node)
        query = Java_Lang.query("""
            ((block_comment) @name
	            (import_declaration) @name)
        """)
        comments= query.captures(tree.root_node)



        for val in captures:
            res=val[0].text
            start_point=int(val[0].start_point[0])
            end_point=start_point
            line=res.decode()
            if (line in other):
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

            imports.append([lstring,rstring,start_point,end_point])
        
        for comment_val in comments:
            if (comment_val[0].type=="block_comment"):
                starting_line=int(comment_val[0].start_point[0])
                comment_obj=Comment(comment_val[0].text.decode(),"",index,starting_line)
                if (comment_obj not in top_body):
                    top_body.insert(starting_line,comment_obj)

        return imports,other
    
    def extractBody(self,content,version):
        content='\n'.join(content)

        byte_rep= str.encode(content)
        tree = parser.parse(byte_rep)

        class_query = Java_Lang.query("""
            ((class_declaration) @name)
        """)

        class_captures=class_query.captures(tree.root_node)


        enum_query=Java_Lang.query("""
            ((enum_declaration) @name)
        """)

        class_captures=class_captures+enum_query.captures(tree.root_node)

        field_query = Java_Lang.query("""
            (field_declaration
            	declarator: (variable_declarator) @name)
        """)

        enum_contant_query = Java_Lang.query("""
            (enum_constant
                name:(identifier) @name)
        """)

        field_captures=field_query.captures(tree.root_node)+enum_contant_query.captures(tree.root_node)

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

        comments = Java_Lang.query("""
            ((block_comment) @name)
        """)

        linecomments = Java_Lang.query("""
            ((line_comment) @name)
        """)

        all_comments=comments.captures(tree.root_node)+linecomments.captures(tree.root_node)

        # for new_class in class_captures:
        #     # Tuple of class details including modifiers and name.
        #     class_details=new_class[0].parent.children

        #     indentation=int(class_details[0].start_point[1])
        #     starting_line=int(class_details[0].start_point[0])

        #     new_class_name=class_details[2].text.decode()

        #     new_full_name=""

        #     for child in class_details:
        #         if child.type!="class_body":
        #             new_full_name+=child.text.decode()+" "
        #     new_full_name=new_full_name.strip(" ")


        #     #Stores full class declaration
            
        #     class_obj=Class(new_class_name,new_full_name,indentation,version,starting_line)

        #     #If object with same full class declaration is in the list of classes, then class_obj references that object.
        #     #Equality relation for class is re-defined in Node.py.
        #     if (new_class_name in all_classes.keys()):
        #         if (class_obj not in all_classes[new_class_name]):
        #             all_classes[new_class_name].append(class_obj)
        #             class_ref[new_full_name]=class_obj
        #         else:
        #             index=all_classes[new_class_name].index(class_obj)
        #             class_obj=all_classes[new_class_name][index]
        #             class_ref[new_full_name].add_version(version)

        #     else:
        #         all_classes[new_class_name]=[class_obj]
        #         class_ref[new_full_name]=class_obj
            

        #     # Checks if class is nested in another class. 
        #     # Adds it as subclass to main class if it is.
        #     nested_class_details=new_class[0].parent.parent.parent
        #     if (nested_class_details is None):
        #         if (class_obj not in bottom_body):
        #             bottom_body.append(class_obj)
        #     else:
        #         full_nested_name=""
        #         for child in nested_class_details.children:
        #             if ("body" not in child.type and "block" not in child.type):
        #                 full_nested_name+=child.text.decode()+" "
        #             else:
        #                 full_nested_name=full_nested_name.strip(" ")
        #                 break
        #         # if (nested_class_details.children[3].text.decode()[0]!="{"):
        #         #     nested_class=" "+nested_class_details.children[3].text.decode()
        #         # else:
        #         #     nested_class=""
        #         # nested_class=nested_class.split('{')[0]
        #         # full_nested_name=nested_class_details.children[0].text.decode()+" "+nested_class_details.children[1].text.decode()+" "+nested_class_details.children[2].text.decode()+nested_class
        #         class_ref[full_nested_name].add_sub_classes(class_obj)

        for class_val in class_captures:
            class_name=""
            full_class_name=""
            indentation=int(class_val[0].start_point[1])
            starting_line=int(class_val[0].start_point[0])

            for child in class_val[0].children:
                if "body" not in child.type:
                    full_class_name+=child.text.decode()+" "
                    if (child.type=="identifier"):
                        class_name=child.text.decode()
                else:
                    break
            full_class_name=full_class_name.strip(" ")


            class_obj=Class(class_name,full_class_name,indentation,version,starting_line)

            if (class_name in all_classes.keys()):
                if (class_obj not in all_classes[class_name]):
                    all_classes[class_name].append(class_obj)
                    class_ref[full_class_name]=class_obj
                else:
                    index=all_classes[class_name].index(class_obj)
                    class_obj=all_classes[class_name][index]
                    class_ref[full_class_name].add_version(version)

            else:
                all_classes[class_name]=[class_obj]
                class_ref[full_class_name]=class_obj

            nested_class_details=class_val[0].parent.parent
            if (nested_class_details is None):
                if (class_obj not in bottom_body):
                    bottom_body.append(class_obj)
            else:
                full_nested_name=""
                for child in nested_class_details.children:
                    if ("body" not in child.type and "block" not in child.type):
                        full_nested_name+=child.text.decode()+" "
                    else:
                        full_nested_name=full_nested_name.strip(" ")
                        break
                class_ref[full_nested_name].add_sub_classes(class_obj)


        #Adds all variable declarations to associ@API(status = API.Status.STABLE)\npublic final class TestNGCucumberRunner ated classes.
        for field in field_captures:
            declaration=field[0].parent.text.decode()+","

            starting_line=int(field[0].start_point[0])
            identifier=""

            for child in field[0].parent.children:
                if (child.type=="variable_declarator"):
                    for nested_child in child.children:
                        if nested_child.type=="identifier":
                            identifier=nested_child.text.decode()
                elif (child.type=="identifier"):
                    identifier=child.text.decode()

            nested_class=""

            base=field[0].parent
            
            while (base.type!="class_declaration" and base.type!="enum_declaration"):
                base=base.parent


            for child in base.children:
                if ("body" not in child.type):
                    nested_class+=child.text.decode()+" "
                else:
                    break
            nested_class=nested_class.strip(" ")

            field_obj=Field(identifier,declaration,version,nested_class,starting_line)

            if (nested_class+identifier in all_fields.keys()):
                if (field_obj not in all_fields[nested_class+identifier]):   
                    all_fields[nested_class+identifier].append(field_obj)
                else:
                    field_obj=all_fields[nested_class+identifier][all_fields[nested_class+identifier].index(field_obj)]
                    field_obj.add_version(version)
            else:
                all_fields[nested_class+identifier]=[field_obj]

            class_ref[nested_class].add_declaration(field_obj)

        before="None"
        #Adds all methods to associated classes
        for method in method_captures:

            starting_line=int(method[0].start_point[0])
            
            #Stores method as a whole
            method_description=method[0].parent.text.decode()

            method_signature=""

            for child in method[0].parent.children:
                if child.type=="block" or "body" in child.type or child.type=="throws":
                    break
                else:
                    if (child.type!="modifiers" and "type" not in child.type):
                        method_signature+=child.text.decode()+" "
                    if (child.type=="identifier"):
                        method_name=child.text.decode()

            
            base=method[0].parent
            
            while (base.type!="class_declaration" and base.type!="enum_declaration"):
                base=base.parent

            super_class=""
            for child in base.children:
                if ("body" not in child.type):
                    super_class+=child.text.decode()+" "
                else:
                    break

            method_obj=Method(method_name,method_description,method_signature,version,super_class,starting_line)

            #Method declaration is referenced by its signature and super class.
            #Method signatures can be the same if declared in two different classes.
            if (super_class+" "+method_signature in all_methods.keys()):
                if (method_obj not in all_methods[super_class+" "+method_signature]):
                    all_methods[super_class+" "+method_signature].append(method_obj)
                    self.add_method(bottom_body,method_obj,super_class,version,before)
                else:
                    index=all_methods[super_class+" "+method_signature].index(method_obj)
                    all_methods[super_class+" "+method_signature][index].add_version(version)
            else:
                all_methods[super_class+" "+method_signature]=[method_obj]
                self.add_method(bottom_body,method_obj,super_class,version,before)
            before=method_obj


        for comment in all_comments:
            if (comment[0].type=="block_comment" and (comment[0].parent.type=="class_body") and comment[0].text.decode() not in top_body):
                main_class=""
                starting_line=int(comment[0].start_point[0])
                for child in comment[0].parent.parent.children:
                    if child.type!="block" and child.type!="class_body":
                        main_class+=child.text.decode()+" "
                    else:
                        break
                index=comment[0].start_point[1]

                comment_obj=Comment(comment[0].text.decode(),main_class,index,starting_line)
                class_ref[main_class.strip(" ")].add_comment(comment_obj)

            # elif (comment[0].type=="block_comment" and comment[0].parent.type=="program"):
            #     starting_line=int(comment[0].start_point[0])
            #     index=comment[0].start_point[1]
            #     comment_obj=Comment(comment[0].text.decode(),"",index,starting_line)
            #     bottom_body.append(comment_obj)

        return bottom_body
    
    #Recursively finds object for super class, and adds method object to it.
    def add_method(self,classes,new_method,super_class,version,before):
        for new_c in classes:
            if (type(new_c)==Class):
                if (new_c.get_full_name().strip(" ")==super_class.strip(" ")):
                    new_c.add_method(new_method,version,before)
                else:
                    self.add_method(new_c.get_sub_classes(),new_method,super_class,version,before)

    def get_method_ref(self):
        return dict(all_methods)
    
    def get_class_ref(self):
        return dict(all_classes)
    
    def initialize_body():
        return ""
    
    def get_field_ref(self):
        return dict(all_fields)


class Python(Lang):


    global methods
    methods=[]

    global codeseq
    codeseq = []
    done=[]

    global top_body
    top_body=[]

    def get_lang(self):
        return "python"

    def generateAST(self,content):
        return ast.parse(content)
    
    def get_top_body(self):
        return top_body
    

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
    

    def output_imports(self,node,string,target,formatter = ""):
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
                self.output_imports(item,dup,target,formatter)
                formatter = ''
            elif (item==target):
                if formatter == "":

                    top_body.append(dup)
                else:
                    for i,imp in enumerate(top_body):
                        if theprefix in imp:
                            match formatter:
                                case "oneline":
                                    top_body[i] = top_body[i] + ',' + target.get_full_dir()
                                case "multiline":
                                    
                                    temparray = top_body[i].split('\n')
                                    if len(temparray)==1:
                                        temimp = imp.replace(theprefix,'')
                                        top_body[i] = theprefix+ f'(\n    {temimp},\n    {target.get_full_dir()},\n)'
                                    else :
                                        new_imp = temparray[0]+'\n'
                                        temparray.pop(0)
                                        temparray.pop()
                                        for el in temparray:
                                            new_imp+=el+'\n'
                                        new_imp += f'    {target.get_full_dir()},\n)'
                                        top_body[i] = new_imp

                                            
                                case "separateline":
                                    top_body.append(dup)
                            return
                    
                    top_body.append(dup)


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


    def extractBody(self,content,version):
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
                newMethod = Method(methodName,astor.to_source(nod),methodName,version,None,0,nod)

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
                newClass = Class(className,className,0,version,0,nod)

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
