import subprocess
from Node import MainRoot
from Tree import Tree
from init import writefile


# merger.py is used as a starting point to run different merge algorithm. 
# Heuristics for merge agorithm is developed in Tree.py (base_algorithm).


# Root node for code tree structure.
global tree
tree=Tree(MainRoot())

# Removes unnecessary files that were created.
def clean(type):
    subprocess.run(['rm', f'base_content.{type}'])
    subprocess.run(['rm', f'left_content.{type}'])
    subprocess.run(['rm', f'right_content.{type}'])


# Temporary files to perform git 3-way merge algorithm.
def git_merge(base,right,left,language):
    writefile("base_content."+language,base)
    writefile("right_content."+language,right)
    writefile("left_content."+language,left)

    git_rest=subprocess.run(['git', 'merge-file', '-p','left_content.'+language, 'base_content.'+language,'right_content.'+language],capture_output=True, text=True)

    clean(language)
    return git_rest.stdout


def import_merge(lang,base,right,left):
    gen_import_tree(base, right, left)
    result=tree.find_imports(lang)
    return result

def body_merge(lang, base, right, left):
    res = append_body_tree(lang,base,right,left)
    if res == "**to_be_handled_by_git**":
        return "**to_be_handled_by_git**"
    #Determines which classes, methods and fields to use.
    tree.set_classes(lang)
    tree.set_methods(lang)
    tree.set_fields(lang)
    result=tree.find_body(lang)
    return result

# Compiles all unique elements in code bodies between all three versions into tree.
def append_body_tree(lang, base, right, left):
    leftclass = lang.extractBody(left,"left")
    if leftclass == "**to_be_handled_by_git**" :
        return "**to_be_handled_by_git**"
    tree.add_body(leftclass)

    rightclass = lang.extractBody(right,"right")
    if rightclass == "**to_be_handled_by_git**" :
        return "**to_be_handled_by_git**"
    tree.add_body(rightclass)

    baseclass = lang.extractBody(base,"base")
    if baseclass == "**to_be_handled_by_git**" :
        return "**to_be_handled_by_git**"
    tree.add_body(baseclass)

# Compiles all unique elements in import statements between all three versions into tree. 
def gen_import_tree(base_import, right_import, left_import):
    for imports in left_import:
        tree.add_import(imports,"left")
    for imports in right_import:
        tree.add_import(imports,"right")
    for imports in base_import:
        tree.add_import(imports,"base")



    