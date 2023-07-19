import subprocess
from Node import Pack,Class,MainRoot
from Tree import Tree
from init import writefile
from spec import Python


# merger.py is used as a starting point to run different merge algorithm. 
# Merge algorithm for import_merge is actually defined in the Tree structure. 

global tree
tree=Tree(MainRoot())



def git_merge(base,right,left,language):

    #Temporary files to perform git 3-way merge algorithm.

    writefile("base_content."+language,base)
    writefile("right_content."+language,right)
    writefile("left_content."+language,left)

    git_rest=subprocess.run(['git', 'merge-file', '-p','left_content.'+language, 'base_content.'+language,'right_content.'+language],capture_output=True, text=True)
    # print(git_rest.stdout.strip("\n"))
    return git_rest.stdout

def import_merge(lang,base,right,left):
    gen_tree(base, right, left)
    result=tree.find_paths(lang)
    return result

def body_merge(lang, base, right, left):
    res = append_tree(lang,base,right,left)
    if res == "**to_be_handled_by_git**":
        return "**to_be_handled_by_git**"
    tree.set_classes(lang)
    tree.set_methods(lang)

    result=tree.find_methods(lang)
    return result

def append_tree(lang, base, right, left):
    leftclass = lang.getClasses(left,"left")
    if leftclass == "**to_be_handled_by_git**" :
        return "**to_be_handled_by_git**"
    tree.add_body(leftclass)

    rightclass = lang.getClasses(right,"right")
    if rightclass == "**to_be_handled_by_git**" :
        return "**to_be_handled_by_git**"
    tree.add_body(rightclass)


    baseclass = lang.getClasses(base,"base")
    if baseclass == "**to_be_handled_by_git**" :
        return "**to_be_handled_by_git**"
    tree.add_body(baseclass)





def gen_tree(base_import, right_import, left_import):

    #Adds all imports to the tree. Tree structure ensures no duplicates.

    for imports in left_import:
        tree.add_import(imports,"left")

    
    for imports in right_import:
        tree.add_import(imports,"right")


    for imports in base_import:
        tree.add_import(imports,"base")



    