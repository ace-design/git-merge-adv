import subprocess

#Parts of code given by Richard Li

def getHashes():
    all_merges=[]
    merges=subprocess.check_output(['git log --merges --pretty=format:"%h"'],shell=True).decode('utf-8').split('\n')
    return merges



def getInformation(hash):
    parent = subprocess.check_output(f"git log --pretty=%p -n 1 {hash} --abbrev=8", shell=True).decode('utf-8').strip('\n').split(' ')
    left_parent=parent[0]
    right_parent=parent[1]

    try:
        ancestor = subprocess.check_output(f"git merge-base {left_parent} {right_parent}", shell=True).decode('utf-8').strip('\n')[:8]
    except subprocess.CalledProcessError:
        ancestor="N/A"

    information={"hash":hash, "left":left_parent, "right":right_parent, "ancestor": ancestor}
    return information



def generateInfo():
    all_info=[]
    for hash in getHashes():
        all_info.append(getInformation(hash))
    
    return all_info

