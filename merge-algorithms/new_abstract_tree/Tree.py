from Node import Pack, End
import copy

class Tree:

    def __init__(self,root):
        self.root=root
        self.map={}

    def add_traverse(self,node,path,version):
        status=False
        parent=Pack(path[0])
        for item in node.get_children():
            if (item.get_full_dir()==path[0]):
                parent=item
                status=True
                break
        if status==False:
            node.add_child(parent,version)
        for new_end in path[1:]:
            new=End(new_end)
            exit=parent.add_child(new,version)
            if (exit!=-1):
                if (new_end in self.map.keys()):
                    self.map[new_end].append(new)
                else:
                    self.map[new_end]=[new]
    

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
        self.add_traverse(self.root,path,version)
    
