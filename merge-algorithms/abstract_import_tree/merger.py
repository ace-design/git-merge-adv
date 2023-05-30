import subprocess
from init import writefile


# merger.py is used as a starting point to run different merge algorithm. 
# Merge algorithm for import_merge is actually defined in the Tree structure. 

def import_merge(lang,base,right,left):
    import_tree=lang.gen_tree(base, right, left)
    result=import_tree.find_paths(lang)
    return result


def git_merge(base,right,left,lang):

    #Temporary files to perform git 3-way merge algorithm.

    writefile("base_content."+lang,base)
    writefile("right_content."+lang,right)
    writefile("left_content."+lang,left)

    git_rest=subprocess.run(['git', 'merge-file', '-p','left_content.'+lang, 'base_content.'+lang,'right_content.'+lang],capture_output=True, text=True)
    
    return git_rest
    