from Node import Pack, End, Class, Method, Comment
import merger
from spec import Python

class Tree:

    def __init__(self,root):
        self.root=root
        self.import_root=Pack("",0)
        self.root.add_child(self.import_root)
        self.import_ref={}
        self.class_ref={}
        self.field_ref={}
        self.method_ref={}
        self.all_imports=[]

    def add_import(self,path,version):
        self.import_traverse(self.import_root,path,version)


    def import_traverse(self,node,path,version):
        parent=node.add_child(Pack(path[0],path[2]))

        child=parent.add_child(End(path[1],path[2],path[3]))

        child.add_version(version)

        if (path[1] in self.import_ref.keys()):
                self.import_ref[path[1]].add(child)
        else:
                self.import_ref[path[1]]=set()
                self.import_ref[path[1]].add(child)


    def add_body(self, body):
        for body_element in body:
            self.root.add_child(body_element)


    def find_body(self,lang):
        body=""
        if isinstance(lang,Python):
            return lang.output_body()
        for branch in self.root.get_children():
            if (type(branch) is Class or type(branch) is Comment or type(branch) is Pack):
                body=lang.output_body(body,branch)
        return body
    

    def set_classes(self,lang):
        self.class_ref=lang.get_class_ref()
        extra=[]
        for class_val in self.class_ref.keys():
            if (len(self.class_ref[class_val])>0):
                result,sus,versions=self.base_algorithm(self.class_ref,class_val)
                if (type(result) is str):
                    result=self.class_ref[class_val][0]
                result.set_selected()


    def set_methods(self,lang):
        extra=[]
        self.method_ref=lang.get_method_ref()
        for method_val in self.method_ref.keys():
            result,sus,versions=self.base_algorithm(self.method_ref,method_val)
            if (type(result) is str or sus):
                # self.method_ref[method_val][0].set_selected()
                extra.append(method_val)
            else:
                result.set_selected()
        #Run GitMerge on the ones we are uncertain with
        for method in extra:
            all_versions=self.method_ref[method]
            base=""
            left=""
            right=""
            for method_version in all_versions:
                if "base" in method_version.get_version():
                    base=method_version.get_method().split('\n')
                elif "right" in method_version.get_version():
                    left=method_version.get_method().split('\n')
                elif "left" in method_version.get_version():
                    right=method_version.get_method().split('\n')
            result=merger.git_merge(base,right,left,lang.get_lang())
            self.method_ref[method][0].overwrite_method(result)
            self.method_ref[method][0].set_selected()

            
        


    def base_algorithm(self,references,node):
        paths=list(references[node])

        if (len(paths)==1):
            versions=list(paths[0].get_version())
            if (len(versions)==1 or len(versions)==2):
                return paths[0],True,versions #updater=versions
            else:
                return paths[0],False,versions #False because 3 versions include same
        elif (len(paths)==2):
            versions=list(paths[0].get_version())
            versions_2=list(paths[1].get_version())
            if (len(versions)==2 or len(versions_2)==2):
                if (len(versions)==2):
                    if (versions_2[0]=="base"):
                        return paths[0],False,versions
                    else:
                        return paths[1],False,versions_2
                else:
                    return paths[0],False,versions
            else:
                if (versions[0]=="base"):
                    return paths[1],True,versions_2
                elif (versions_2[0]=="base"):
                    return paths[0],True,versions
                else:
                    return "Conflicting",None,None
                    # extra[current_sum]=self.map[dir]
        else:
            return "Conflicting",None,None
            # extra[current_sum]=self.map[dir]

        

    def find_imports(self,lang):
        version_ref={'right':0,'base':0,'left':0}
        current_sum=0
        extra={}

        for dir in self.import_ref.keys():
            result,sus,versions=self.base_algorithm(self.import_ref,dir)
            if (type(result) is str):
                extra[current_sum]=self.import_ref[dir]
            else:
                lang.output_imports(self.import_root,"",result,sus)
                for v in versions:
                    version_ref[v]+=1
            current_sum+=1
        
        highest=self.most_frequent(version_ref)

        for conflict_dir in extra:

            paths=list(extra[conflict_dir])

            if (len(paths)==2):
                versions=list(paths[0].get_version())
                if (versions[0]==highest):
                    lang.output_imports(self.import_root,"",paths[0],True)
                else:
                    lang.output_imports(self.import_root,"",paths[1],True)
            else:
                versions=list(paths[0].get_version())
                versions_2=list(paths[1].get_version())
                if (highest in versions):
                    lang.output_imports(self.import_root,"",paths[0],True)
                elif (highest in versions_2):
                    lang.output_imports(self.import_root,"",paths[1],True)
                else:
                    lang.output_imports(self.import_root,"",paths[2],True)


        return lang.get_top_body()
    
    
    def most_frequent(self,ref):
        ordered=sorted(ref)
        if (ordered[0]=="base"):
            highest=ordered[1]
        else:
            highest=ordered[0]
        return highest


    def confilct_resolver(self,lang,conflicted_content):
        print(conflicted_content)
        with  open("conflict.py","w") as f:
            f.write(conflicted_content)
        with  open("methodres.py","w") as f:
            f.write("")
        print("\n=======================================================================================")
        for i in self.method_ref.keys():
            print(i)
            for x in self.method_ref[i]:
                if x.selected == True:
                    with  open("methodres.py","a") as f:
                        f.write(x.method_name)
        return "Yup"
                    
    
    def set_fields(self,lang):
        self.field_ref=lang.get_field_ref()
        for field_val in self.field_ref.keys():
            if (len(self.field_ref[field_val])>0):
                result,sus,versions=self.base_algorithm(self.field_ref,field_val)
                if (type(result) is str):
                    result=self.field_ref[field_val][0]
                result.set_selected()




    def base_algorithm(self,references,node):
        paths=list(references[node])

        if (len(paths)==1):
            versions=list(paths[0].get_version())
            if (len(versions)==1 or len(versions)==2):
                return paths[0],True,versions #updater=versions
            else:
                return paths[0],False,versions #False because 3 versions include same
        elif (len(paths)==2):
            versions=list(paths[0].get_version())
            versions_2=list(paths[1].get_version())
            if (len(versions)==2 or len(versions_2)==2):
                if (len(versions)==2):
                    if (versions_2[0]=="base"):
                        return paths[0],False,versions
                    else:
                        return paths[1],False,versions_2
                else:
                    return paths[0],False,versions
            else:
                if (versions[0]=="base"):
                    return paths[1],True,versions_2
                elif (versions_2[0]=="base"):
                    return paths[0],True,versions
                else:
                    return "Conflicting",None,None
                    # extra[current_sum]=self.map[dir]
        else:
            return "Conflicting",None,None
            # extra[current_sum]=self.map[dir]

    