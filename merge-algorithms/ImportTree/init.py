import argparse
import copy
import subprocess
from Tree import Tree
from Node import Node


def parsing():
    parser = argparse.ArgumentParser(description='Enter left and right parent files.')
    parser.add_argument('--left', type=str, help='path to left parent java file')
    parser.add_argument('--right', type=str, help='path to right parent java file' )
    parser.add_argument('--base', type=str, help='path to base java file' )
    parser.add_argument('--output', type=str, help='name of output file' )

    args = parser.parse_args()
    return args

def extractImports(content):
    imports=[]
    other=copy.deepcopy(content)

    for line in content:
        # Narrowing search to 'import' or 'package' keyword.
        if (line[0:6]=="import" or line[0:7]=="package"):
            other.remove(line)
            line=line.replace(" ",".")
            line=line.replace("/",".")
            imports.append(line)
    return imports,other


def writefile(name, content):
    with open(name, 'w') as output:
        for line in content:
            output.write(line+'\n')

def appendfile(name, content):
    with open(name,"a") as res2:
        res2.write(content)
    


def git_merge(output,base,right,left):

    #Temporary files to perform git 3-way merge algorithm.

    writefile("base_content.java",base)
    writefile("right_content.java",right)
    writefile("left_content.java",left)

    git_rest=subprocess.run(['git', 'merge-file', '-p','left_content.java', 'base_content.java','right_content.java'],capture_output=True, text=True)
    
    return git_rest




def merge_import(output, base_import, right_import, left_import, result):

    root=Node("")
    tree=Tree(root)

    #Adds all imports to the tree. Tree structure ensures no duplicates.

    for imports in left_import:
        dict=imports.split(".")
        package=dict[-1].strip(";")
        if package in result or package=="*":
            tree.add(dict)

    
    for imports in right_import:
        dict=imports.split(".")
        package=dict[-1].strip(";")
        if package in result or package=="*":
            tree.add(dict)


    for imports in base_import:
        dict=imports.split(".")
        package=dict[-1].strip(";")
        if package in result or package=="*":
            tree.add(dict)


    #Writes imports in tree to given output file.
    writefile(output,tree.output())
    

def clean():
    #Removes uncessary files that were created.
    subprocess.run(['rm', 'base_content.java'])
    subprocess.run(['rm', 'left_content.java'])
    subprocess.run(['rm', 'right_content.java'])


def main():

    left=parsing().left
    right=parsing().right
    base=parsing().base
    output=parsing().output

    left_import,left_content=extractImports(subprocess.check_output(f"cat "+left, shell=True).decode('utf-8').split('\n'))
    right_import,right_content=extractImports(subprocess.check_output(f"cat "+right, shell=True).decode('utf-8').split('\n'))
    base_import,base_content=extractImports(subprocess.check_output(f"cat "+base, shell=True).decode('utf-8').split('\n'))


    result=git_merge(output,base_content,right_content,left_content)


    merge_import(output,base_import,right_import,left_import,result.stdout)

    appendfile(output,result.stdout)
    

    clean()

    


if __name__=="__main__":
    main()

