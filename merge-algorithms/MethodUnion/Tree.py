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
        version_ref={'right':0,'base':0,'left':0}
        current_sum=0
        extra={}

        for dir in self.map.keys():
            paths=list(self.map[dir])
            
            if len(paths)==1:
                versions=list(paths[0].get_version())
                if (len(versions)==1 or len(versions)==2):
                    lang.output_traverse(self.root, "",all_import,paths[0],True)
                else:
                    lang.output_traverse(self.root, "",all_import,paths[0],False)
                updater=versions

            elif len(paths)==2:

                versions=list(paths[0].get_version())
                versions_2=list(paths[1].get_version())
                if (len(versions)==2 or len(versions_2)==2):
                    if (len(versions)==2):
                        if (versions_2[0]=="base"):
                            lang.output_traverse(self.root,"",all_import,paths[0],False)
                        else:
                            lang.output_traverse(self.root,"",all_import,paths[1],False)
                            updater=versions_2
                    else:
                        lang.output_traverse(self.root,"",all_import,paths[0],False)
                        updater=versions
                else:
                    if (versions[0]=="base"):
                        lang.output_traverse(self.root,"",all_import,paths[1],True)
                        updater=versions_2
                    elif (versions_2[0]=="base"):
                        lang.output_traverse(self.root,"",all_import,paths[0],True)
                        updater=versions
                    else:
                        extra[current_sum]=self.map[dir]
            else:
                extra[current_sum]=self.map[dir]
            
            for v in updater:
                version_ref[v]+=1

            current_sum+=1

        highest=self.most_frequent(version_ref)

        for conflict_dir in extra:

            paths=list(extra[conflict_dir])

            if (len(paths)==2):
                versions=list(paths[0].get_version())
                if (versions[0]==highest):
                    lang.output_traverse(self.root,"",all_import,paths[0],True)
                else:
                    lang.output_traverse(self.root,"",all_import,paths[1],True)
            else:
                versions=list(paths[0].get_version())
                versions_2=list(paths[1].get_version())
                if (highest in versions):
                    lang.output_traverse(self.root,"",all_import,paths[0],True)
                elif (highest in versions_2):
                    lang.output_traverse(self.root,"",all_import,paths[1],True)
                else:
                    lang.output_traverse(self.root,"",all_import,paths[2],True)

            curr_path=all_import.pop(-1)
            all_import.insert(conflict_dir-1,curr_path)

        return all_import
    
    def most_frequent(self,ref):
        ordered=sorted(ref)
        if (ordered[0]=="base"):
            highest=ordered[1]
        else:
            highest=ordered[0]
        return highest


    def add(self,path,version):
        self.add_traverse(self.root,path,version)
    
