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
                versions=list(packs[0].get_version())
                if (len(versions)==1 or len(versions)==2):
                    lang.output_traverse(self.root, "",all_import,packs[0],True)
                else:
                    lang.output_traverse(self.root, "",all_import,packs[0],False)
            elif len(packs)==2:
                versions=list(packs[0].get_version())
                versions_2=list(packs[1].get_version())
                if (len(versions)==2 or len(versions_2)==2):
                    if (len(versions)==2):
                        lang.output_traverse(self.root,"",all_import,packs[1],False)
                    else:
                        lang.output_traverse(self.root,"",all_import,packs[0],False)
                else:
                    if (versions[0]=="Base" and (versions_2[0]=="Right" or versions_2[0]=="Left")):
                        lang.output_traverse(self.root,"",all_import,packs[1],True)
                    else:
                        lang.output_traverse(self.root,"",all_import,packs[0],True)
            else:
                lang.output_traverse(self.root,"",all_import,packs[0],True)

        return all_import
    

    def add(self,path,version):
        self.add_traverse(self.root,path,version)
    
