from Node import Node
import copy

class Tree:

    def __init__(self,root):
        self.root=root

    def add_traverse(self,node,path,i):
        if (i<len(path)):
            status=False
            for item in node.get_children():
                if (item.get_dir()==path[i]):
                    self.add_traverse(item, path,i+1)
                    status=True
            if (status==False):
                new=Node(path[i])
                node.add_child(new)
                self.add_extra(new,path,i+1)
        else:
            return
    

    def add_extra(self,node,path,i):
        if (i<len(path)):
            new=Node(path[i])
            node.add_child(new)
            self.add_extra(new,path,i+1)


    
    def output_traverse(self,node,string,all_imports):
        if (len(node.get_children())==0):
            all_imports.append(string[:-1])
        else:
            for item in node.get_children():
                dup=copy.deepcopy(string)
                if (item.get_dir()=="import" or item.get_dir()=="static" or item.get_dir()=="package"):
                    dup+=item.get_dir()+" "
                else:
                    dup+=item.get_dir()+"."
                self.output_traverse(item,dup,all_imports)
        


    def output(self):
        all_imports=[]
        self.output_traverse(self.root,"", all_imports)
        return all_imports




    def add(self,path):
        self.add_traverse(self.root,path,0)
    
