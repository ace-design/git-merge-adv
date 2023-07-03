import subprocess
from Node import Pack,Class,MainRoot
from Tree import Tree
from init import writefile


# merger.py is used as a starting point to run different merge algorithm. 
# Merge algorithm for import_merge is actually defined in the Tree structure. 

global tree
tree=Tree(MainRoot())

def import_merge(lang,base,right,left):
    gen_tree(base, right, left)
    result=tree.find_paths(lang)
    return result

def body_merge(lang, base, right, left):
    append_tree(lang,base,right,left)
    result=tree.find_methods(lang)
    return result

def append_tree(lang, base, right, left):
    tree.add_body(lang.getClasses(base,"base"))
    tree.add_body(lang.getClasses(right,"right"))
    tree.add_body(lang.getClasses(left,"left"))



def gen_tree(base_import, right_import, left_import):

    #Adds all imports to the tree. Tree structure ensures no duplicates.

    for imports in left_import:
        tree.add_import(imports,"left")

    
    for imports in right_import:
        tree.add_import(imports,"right")


    for imports in base_import:
        tree.add_import(imports,"base")


def git_merge(base,right,left,lang):

    #Temporary files to perform git 3-way merge algorithm.

    writefile("base_content."+lang,base)
    writefile("right_content."+lang,right)
    writefile("left_content."+lang,left)

    git_rest=subprocess.run(['git', 'merge-file', '-p','left_content.'+lang, 'base_content.'+lang,'right_content.'+lang],capture_output=True, text=True)
    
    return git_rest
    