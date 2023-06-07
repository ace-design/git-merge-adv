from Node import Node
import copy

class Tree:

    def __init__(self,root):
        self.root=root
        self.map={}

    def add_traverse(self,node,path,i,version):
        # Traverses the tree using path, and adds paths that don't already exist. 
        if (i<len(path)):
            status=False
            for item in node.get_children():
                if (item.get_dir()==path[i]):
                    if (i==len(path)-1):
                        # If the value was the desired end package, we must add the version (left, right or base) to the node versions. 
                        item.add_version(version)
                    else:
                        self.add_traverse(item, path,i+1,version)
                    status=True
            if (status==False):
                new=Node(path[i])
                node.add_child(new)
                if (i==len(path)-1):
                    # If value is desired end package, then reference dictionary package string to the node.
                    if (path[i] in self.map.keys()):
                        self.map[path[i]].append(new)
                    else:
                        self.map[path[i]]=[new]
                    new.add_version(version)
                self.add_traverse(new,path,i+1,version)
        else:
            return
    

    def find_paths(self,lang):
        # List is used to compile all selected imports. 
        all_import=[]
        # Goes through all the packages from left, right and base files. 
        for dir in self.map.keys():
            # Length of 1 means there is only one node with that value in the tree. Logically must add to the merged result.
            if len(self.map[dir])==1:
                lang.output_traverse(self.root, "",all_import,self.map[dir][0])
            # Length of 2 means there are two paths that are the same in all 3 files. 
            elif len(self.map[dir])==2:
                versions=self.map[dir][0].get_version()
                # Find the node that is associated with 2 file versions. 
                if (len(versions)==2):
                    if (versions[0]=="left" and versions[1]=="right") or (versions[0]=="right" and versions[1]=="left"):
                        # If the two file versions are left and right, then base is outdared and logically choose updated path.
                        lang.output_traverse(self.root, "",all_import,self.map[dir][1])
                    else:
                        # Otherwise choose the path that isn't common. 
                        lang.output_traverse(self.root, "",all_import,self.map[dir][0])

                else:
                    lang.output_traverse(self.root, "",all_import,self.map[dir][0])



        return all_import
    

    def add(self,path,version):
        self.add_traverse(self.root,path,0,version)
    
