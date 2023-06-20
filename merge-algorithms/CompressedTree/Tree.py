from Node import Pack, End

class Tree:

    def __init__(self,root):
        self.root=root
        self.map={}

    def add_traverse(self,node,path,version):

        parent=node.add_child(Pack(path[0]))

        if len(path)>2:
            child=parent.add_child(End(path[1],path[2],path[3]))
        else:     
            child=parent.add_child(End(path[1]))
        child.add_version(version)

        if (path[1] in self.map.keys()):
                self.map[path[1]].add(child)
        else:
                self.map[path[1]]=set()
                self.map[path[1]].add(child)
    

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
                    # lang.output_traverse(self.root,"",all_import,packs[1])
                    if (versions[0]=="left" or versions[0]=="right"):
                        lang.output_traverse(self.root,"",all_import,packs[0])
                    else:
                        lang.output_traverse(self.root,"",all_import,packs[1])





        return all_import
    

    def add(self,path,version):
        self.add_traverse(self.root,path,version)
    
