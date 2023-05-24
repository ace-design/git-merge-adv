import argparse
import subprocess
import copy

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
            imports.append(line)
            other.remove(line)
    return imports,other


def writefile(name, content):
    with open(name, 'w') as output:
        for line in content:
            output.write(line+'\n')


def git_merge(output):
    git_rest=subprocess.run(['git', 'merge-file', '-p','left_content.java', 'base_content.java','right_content.java'],capture_output=True, text=True)
    with open(output,"a") as res2:
        res2.write(git_rest.stdout)
    # subprocess.run(['echo','git', 'merge-file','-p','left_content.java','base_content.java','right_content.java','>','git.java'])
    # content_out=subprocess.check_output(f"git merge-file -p left_content.java base_content.java right_content.java",shell=True).decode('utf-8').split('\n')

def main():
    left_parent=parsing().left
    right_parent=parsing().right
    base_parent=parsing().base
    output=parsing().output

    left_import,left_content=extractImports(subprocess.check_output(f"cat "+left_parent, shell=True).decode('utf-8').split('\n'))
    right_import,right_content=extractImports(subprocess.check_output(f"cat "+right_parent, shell=True).decode('utf-8').split('\n'))
    base_import,base_content=extractImports(subprocess.check_output(f"cat "+base_parent, shell=True).decode('utf-8').split('\n'))


    common_import=(set(left_import).intersection(right_import,base_import)).union(set(left_import).difference(base_import)).union(set(right_import).difference(base_import))
    

    writefile("base_content.java",base_content)
    writefile("right_content.java",right_content)
    writefile("left_content.java",left_content)

    writefile(output,common_import)
    
    git_merge(output)

    subprocess.run(['rm', 'base_content.java'])
    subprocess.run(['rm', 'left_content.java'])
    subprocess.run(['rm', 'right_content.java'])



if __name__=="__main__":
    main()
