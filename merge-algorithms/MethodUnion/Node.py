
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
    def __init__(self,dir):
        self.path=dir
        self.children=[]

    def get_children(self):
        return self.children

    def add_child(self,end):
        for child in self.children:
            if (child.get_full_dir()==end.get_full_dir()):
                return child
        self.children.append(end)
        return end
    
    def get_full_dir(self):
        return self.path

class MainRoot:
    def __init__(self):
        self.children=set()
    
    def add_child(self,child):
        self.children.add(child)
    
    def get_children(self):
        return self.children

class Class:
    def __init__(self,name,full_name,indent,closer,version):
        self.version=set()
        self.version.add(version)
        self.closing=closer
        self.ranking=indent/4
        self.class_name=name
        self.full_name=full_name
        self.methods=[]
        self.sub_classes=[]
        self.declarations=[]
        self.comments={}
        self.selected=False
    
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

    def add_comment(self,method_signature,comment):
        if method_signature in self.comments.keys():
            if (comment not in self.comments[method_signature]):
                self.comments[method_signature].append(comment)
        else:
            self.comments[method_signature]=[comment]
    
    def set_selected(self):
        self.selected=True

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
    
    def get_closer(self):
        return self.closing
    
    def get_version(self):
        return self.version
    
    def is_selected(self):
        return self.selected
    
    def __eq__(self,obj):
        return (type(obj)==Class and self.full_name==obj.get_full_name())
    
    def __hash__(self):
        return hash(self.get_full_name())


class Method:
    def __init__(self,name,des,signature,version,super_class):
        self.name=name
        self.full_method=des
        self.signature=signature
        self.super=super_class
        self.version=set()
        self.version.add(version)
        self.selected=False

    def add_version(self,version):
        self.version.add(version)

    def set_selected(self):
        self.selected=True

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
        return (self.full_method==obj.get_method() and self.super==obj.get_super())
    
    def __hash__(self):
        return hash(self.get_method()) 

class Comment:
    def __init__(self,comment_body,super,indent):
        self.comment=comment_body
        self.super_class=super
        self.indent=indent

    def get_comment(self):
        return self.comment
    
    def get_indent(self):
        return self.indent

    
    def __eq__(self,obj):
        return (type(obj)==Comment) and self.comment==obj.get_comment()
    
    def __hash__(self):
        return hash(self.comment) 