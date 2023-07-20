import bisect
import math

class End:
    def __init__(self,dir,leftstartline=None, leftendline=None):
        self.leftendline = leftendline
        self.leftstartline = leftstartline
        if (" as " in dir):
            self.real_name=dir.split(" as ")[0]
            self.rename=dir.split(" as ")[1]
        else:
            self.real_name=dir
            self.rename=""
        self.versions=set()
    
    def get_dir(self):
        return self.rename
    
    def get_start(self):
        return self.leftstartline
    
    def get_full_dir(self):
        if self.rename=="":
            return self.real_name
        else:
            return (self.real_name+" as "+self.rename)
    
    def add_version(self,version):
        self.versions.add(version)
    
    def get_version(self):
        return self.versions

class Pack:
    def __init__(self,dir,start):
        self.path=dir
        self.start_line=start
        self.children=[]

    def get_children(self):
        return self.children

    def add_child(self,end):
        for child in self.children:
            if (child.get_full_dir()==end.get_full_dir()):
                return child
        self.children.append(end)
        return end
    def get_start(self):
        return self.start_line
    
    def get_full_dir(self):
        return self.path

class MainRoot:
    def __init__(self):
        self.children=set()
        self.order=[]
    
    def add_child(self,child):
        if (child not in self.order):
            bisect.insort(self.order,child,key=lambda x: x.get_start())
    
    def get_children(self):
        return self.order

class Class:
    def __init__(self,name,full_name,indent,version,start_line,nod=None):
        self.version=set()
        self.version.add(version)
        self.ranking=math.ceil(indent/4)
        self.start_line=start_line
        self.class_name=name
        self.full_name=full_name
        self.order=[]
        self.methods=[]
        self.sub_classes=[]
        self.declarations=[]
        self.comments=[]
        self.selected=False
        self.node = nod
    
    def add_method(self,method,version,before):
        if (method not in self.methods):
            if (type(before)!=str):
                for old_method in self.methods:
                    if (old_method.get_name()==before.get_name()):
                        index=self.methods.index(old_method)
                        self.methods.insert(index,method)
                        return
                self.methods.insert(0,method)
            else:
                self.methods.append(method)
        else:
            existing_method_idx=self.methods.index(method)
            self.methods[existing_method_idx].add_version(version)


    def add_sub_classes(self,new_class):
        if (new_class not in self.sub_classes):
            self.sub_classes.append(new_class)

    def add_declaration(self,new_declaration):
        if (new_declaration not in self.declarations):
            self.declarations.append(new_declaration)
    
    def add_version(self,version):
        self.version.add(version)

    def add_comment(self,comment):
        if (comment not in self.comments):
            self.comments.append(comment)
        # if method_signature in self.comments.keys():
        #     if (comment not in self.comments[method_signature]):
        #         self.comments[method_signature].append(comment)
        # else:
        #     self.comments[method_signature]=[comment]
    
    def set_selected(self):
        self.selected=True

    def get_everything(self):
        for method in self.methods:
            bisect.insort(self.order,method,key=lambda x: x.get_start())
        for subclass in self.sub_classes:
            bisect.insort(self.order,subclass,key=lambda x: x.get_start())
        for comment in self.comments:
            bisect.insort(self.order,comment,key=lambda x: x.get_start())
        for field in self.declarations:
            bisect.insort(self.order,field,key=lambda x: x.get_start())


        return self.order

        

    def get_start(self):
        return self.start_line

    def get_methods(self):
        return reversed(self.methods)
    
    def get_sub_classes(self):
        return self.sub_classes
    
    def get_ranking(self):
        return self.ranking
    
    def get_comments(self):
        return self.comments
    
    def get_class_name(self):
        return self.class_name
    
    def get_full_name(self):
        return self.full_name
    
    def get_declarations(self):
        return self.declarations
    
    def get_version(self):
        return self.version
    
    def is_selected(self):
        return self.selected
    
    def __eq__(self,obj):
        return (type(obj)==Class and self.full_name==obj.get_full_name())
    
    def __hash__(self):
        return hash(self.get_full_name())

class Field:
    def __init__(self,variable,declaration,version,class_val,start):
        self.identifier=variable
        self.full_declaration=declaration
        self.version=set()
        self.version.add(version)
        self.objclass=class_val
        self.start_line=start
        self.selected=False


    def add_version(self,version):
        self.version.add(version)

    def set_selected(self):
        self.selected=True

    def get_start(self):
        return self.start_line
    
    def get_declaration(self):
        return self.full_declaration
    
    def get_name(self):
        return self.identifier

    def get_super(self):
        return self.objclass
    
    def get_version(self):
        return self.version
    
    def is_selected(self):
        return self.selected
    
    def __eq__(self,obj):
        return (self.full_declaration==obj.get_declaration() and self.objclass==obj.get_super())
    
    def __hash__(self):
        return hash(self.get_method()) 
    

class Method:
    def __init__(self,name,des,signature,version,super_class,start,astnode=None):
        self.name=name
        self.full_method=des
        self.signature=signature
        self.super=super_class
        self.start_line=start
        self.version=set()
        self.version.add(version)
        self.selected=False
        self.astnode = astnode
    
    
    def add_version(self,version):
        self.version.add(version)

    def set_selected(self):
        self.selected=True

    def get_start(self):
        return self.start_line

    def overwrite_method(self,content):
        self.full_method=content

    def get_method(self):
        return self.full_method
    
    def get_version(self):
        return self.version
    
    def get_name(self):
        return self.name
    
    def get_signature(self):
        return self.signature
    
    def get_super(self):
        return self.super
    
    def is_selected(self):
        return self.selected

    def __eq__(self,obj):
        return (type(obj)==Method and self.full_method==obj.get_method() and self.super==obj.get_super())
    
    def __hash__(self):
        return hash(self.get_method()) 

class Comment:
    def __init__(self,comment_body,super,indent,start):
        self.comment=comment_body
        self.super_class=super
        self.start_line=start
        self.indent=indent
        self.selected=True


    def get_comment(self):
        return self.comment
    
    def get_indent(self):
        return self.indent
    
    def get_start(self):
        return self.start_line
    
    def is_selected(self):
        return self.selected
    
    def __eq__(self,obj):
        return (type(obj)==Comment) and self.comment==obj.get_comment()
    
    def __hash__(self):
        return hash(self.comment) 