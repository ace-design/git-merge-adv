
class Node:

    def __init__(self,dir):
        self.directory=dir
        self.children=[]
    
    def get_children(self):
        return self.children
    
    def add_child(self,node):
        self.children.append(node)

    def get_dir(self):
        return self.directory

    
    