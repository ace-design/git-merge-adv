
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
    
class Class:
    def __init__(self,name,full_name,indent,closer,version):
        self.version=[version]
        self.closing=closer
        self.ranking=indent/4
        self.class_name=name
        self.full_name=full_name
        self.methods=[]
        self.sub_classes=[]
        self.declarations=[]
    
    def add_method(self,method,version):
        if (method not in self.methods):
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
        self.version.append(version)

    def get_methods(self):
        return self.methods
    
    def get_sub_classes(self):
        return self.sub_classes
    
    def get_ranking(self):
        return self.ranking
    
    def get_class_name(self):
        return self.class_name
    
    def get_full_name(self):
        return self.full_name
    
    def get_declarations(self):
        return self.declarations
    
    def get_closer(self):
        return self.closing
    
    def get_versions(self):
        return self.version
    
    def __eq__(self,obj):
        return (self.full_name==obj.get_full_name())
    
    def __hash__(self):
        return hash(self.get_full_name())


class Method:
    def __init__(self,des,version):
        self.method_name=des
        self.version=[version]

    def add_version(self,version):
        self.version.append(version)

    def get_method(self):
        return self.method_name  
    
    def get_versions(self):
        return self.version

    def __eq__(self,obj):
        return (self.method_name==obj.get_method())
    
    def __hash__(self):
        return hash(self.get_method()) 
    