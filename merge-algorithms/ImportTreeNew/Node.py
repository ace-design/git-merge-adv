
class Node:

    def __init__(self,dir):
        #name of given package/directory.
        self.directory=dir
        #List of all subdirectories.
        self.children=[]
        self.versions=[]
    
    def get_children(self):
        return self.children
    
    def add_child(self,node):
        self.children.append(node)

    def get_dir(self):
        return self.directory
    
    def add_version(self,version):
        self.versions.append(version)

    def get_version(self):
        return self.versions
    
    