
# class Node:

#     def __init__(self,dir):
#         #name of given package/directory.
#         self.directory=dir
#         #List of all subdirectories.
#         self.children=[]
#         self.versions=[]
    
#     def get_children(self):
#         return self.children
    
#     def add_child(self,node):
#         self.children.append(node)

#     def get_dir(self):
#         return self.directory
    
#     def add_version(self,version):
#         self.versions.append(version)

#     def get_version(self):
#         return self.versions
    
class End:
    def __init__(self,dir):
        if (" as " in dir):
            self.real_name=dir.split(" as ")[0]
            self.rename=dir.split(" as ")[1]
        else:
            self.real_name=dir
            self.rename=""
        self.versions=[]
    
    def get_dir(self):
        return self.rename
    
    def get_full_dir(self):
        if self.rename=="":
            return self.real_name
        else:
            return (self.real_name+" as "+self.rename)
    
    def add_version(self,version):
        self.versions.append(version)
    
    def get_version(self):
        return self.versions

class Pack:
    def __init__(self,dir):
        self.path=dir
        self.children=[]

    def get_children(self):
        return self.children

    def add_child(self,end,version):
        for child in self.children:
            if (child.get_full_dir()==end.get_full_dir()):
                child.add_version(version)
                return -1
        self.children.append(end)
    
    def get_full_dir(self):
        return self.path


    
    