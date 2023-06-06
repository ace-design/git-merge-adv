import argparse
from spec import Java,Language,Python
import merger
import subprocess

def parsing():
    parser = argparse.ArgumentParser(description='Enter left and right parent files.')
    parser.add_argument('--left', type=str, help='path to left parent file')
    parser.add_argument('--right', type=str, help='path to right parent file' )
    parser.add_argument('--base', type=str, help='path to base file' )
    parser.add_argument('--output', type=str, help='name of output file' )

    args = parser.parse_args()
    return args

def main():

    left=parsing().left
    right=parsing().right
    base=parsing().base
    output=parsing().output

    type=left.split(".")[-1]

    match type:
        case "java":
            lang=Java()
        case "py":
            lang=Python()
        case _:
            print("Language Not Supported")
            exit(0)

    # this is a temporaray resolution , this abstaraction needs to be moved to class-fucntion  defination
    if type == "py":
        left_import,left_content=lang.extractImports(subprocess.check_output(f"cat "+left, shell=True).decode('utf-8'))
        right_import,right_content=lang.extractImports(subprocess.check_output(f"cat "+right, shell=True).decode('utf-8'))
        base_import,base_content=lang.extractImports(subprocess.check_output(f"cat "+base, shell=True).decode('utf-8'))
    else :
        left_import,left_content=lang.extractImports(subprocess.check_output(f"cat "+left, shell=True).decode('utf-8').split('\n'))
        right_import,right_content=lang.extractImports(subprocess.check_output(f"cat "+right, shell=True).decode('utf-8').split('\n'))
        base_import,base_content=lang.extractImports(subprocess.check_output(f"cat "+base, shell=True).decode('utf-8').split('\n'))

        


    result=merger.git_merge(base_content,right_content,left_content,type)


    import_result=merger.import_merge(lang,base_import,right_import,left_import)

    writefile(output,import_result)

    appendfile(output,result.stdout)
    

    clean(type)


def writefile(name, content):
    with open(name, 'w') as output:
        for line in content:
            output.write(line+'\n')

def appendfile(name, content):
    with open(name,"a") as res2:
        res2.write(content)


def clean(type):
    #Removes uncessary files that were created.
    subprocess.run(['rm', f'base_content.{type}'])
    subprocess.run(['rm', f'left_content.{type}'])
    subprocess.run(['rm', f'right_content.{type}'])



if __name__=="__main__":
    main()


