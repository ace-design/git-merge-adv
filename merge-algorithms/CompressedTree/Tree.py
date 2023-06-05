from Node import Pack, End
import copy

class Tree:

    def __init__(self,root):
        self.root=root
        self.map={}

    def add_traverse(self,node,path,version):
        parent=node.add_child(Pack(path[0]))

        for new_end in path[1:]:
            child=parent.add_child(End(new_end))
            child.add_version(version)

            if (new_end in self.map.keys()):
                self.map[new_end].add(child)
            else:
                self.map[new_end]=set()
                self.map[new_end].add(child)
    
    

    def find_paths(self,lang):
        all_import=[]
        for dir in self.map.keys():
            packs=list(self.map[dir])
            if len(packs)==1:
                lang.output_traverse(self.root, "",all_import,packs[0])
            elif len(packs)==2:
                versions=list(packs[0].get_version())
                if (len(versions)==2):
                    if (versions[0]=="left" and versions[1]=="right") or (versions[0]=="right" and versions[1]=="left"):
                        lang.output_traverse(self.root, "",all_import,packs[0])
                    else:
                        lang.output_traverse(self.root, "",all_import,packs[1])
                else:
                    lang.output_traverse(self.root, "",all_import,packs[0])



        return all_import
    

    def add(self,path,version):
        self.add_traverse(self.root,path,version)
    
