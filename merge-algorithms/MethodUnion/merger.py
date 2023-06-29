import subprocess
from Node import Pack
from Tree import Tree
from init import writefile


# merger.py is used as a starting point to run different merge algorithm. 
# Merge algorithm for import_merge is actually defined in the Tree structure. 

def import_merge(lang,base,right,left):
    import_tree=gen_tree(base, right, left)
    result=import_tree.find_paths(lang)
    return result

def body_merge(lang, base, right, left):
    lang.getClasses(base,"base")
    lang.getClasses(right,"right")
    result=lang.getClasses(left,"left")

    return result





def gen_tree(base_import, right_import, left_import):

    root=Pack("")
    tree=Tree(root)

    #Adds all imports to the tree. Tree structure ensures no duplicates.

    for imports in left_import:
        tree.add(imports,"left")

    
    for imports in right_import:
        tree.add(imports,"right")


    for imports in base_import:
        tree.add(imports,"base")


    #Writes imports in tree to given output file.
    return tree

def git_merge(base,right,left,lang):

    #Temporary files to perform git 3-way merge algorithm.

    writefile("base_content."+lang,base)
    writefile("right_content."+lang,right)
    writefile("left_content."+lang,left)

    git_rest=subprocess.run(['git', 'merge-file', '-p','left_content.'+lang, 'base_content.'+lang,'right_content.'+lang],capture_output=True, text=True)
    
    return git_rest
    