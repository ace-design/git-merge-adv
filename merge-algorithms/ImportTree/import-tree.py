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


def git_merge(output):
    git_rest=subprocess.run(['git', 'merge-file', '-p','left_content.java', 'base_content.java','right_content.java'],capture_output=True, text=True)
    with open(output,"a") as res2:
        res2.write(git_rest.stdout)


def main():
    left=parsing().left
    right=parsing().right
    base=parsing().base
    output=parsing().output

    left_import,left_content=extractImports(subprocess.check_output(f"cat "+left, shell=True).decode('utf-8').split('\n'))
    right_import,right_content=extractImports(subprocess.check_output(f"cat "+right, shell=True).decode('utf-8').split('\n'))
    base_import,base_content=extractImports(subprocess.check_output(f"cat "+base, shell=True).decode('utf-8').split('\n'))

    root=Node("")
    tree=Tree(root)

    for imports in left_import:
        tree.add(imports.split("."))
    
    for imports in right_import:
        tree.add(imports.split("."))

    for imports in base_import:
        tree.add(imports.split("."))


    merge_import=tree.output()



    writefile("base_content.java",base_content)
    writefile("right_content.java",right_content)
    writefile("left_content.java",left_content)

    writefile(output,merge_import)
    
    git_merge(output)

    subprocess.run(['rm', 'base_content.java'])
    subprocess.run(['rm', 'left_content.java'])
    subprocess.run(['rm', 'right_content.java'])

    print(merge_import)


    


if __name__=="__main__":
    main()

