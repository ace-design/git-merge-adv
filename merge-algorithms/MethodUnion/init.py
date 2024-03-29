import argparse
from spec import Java,Python
import merger
import subprocess
from Node import Comment

def parsing():
    parser = argparse.ArgumentParser(description='Enter left and right parent files.')
    parser.add_argument('--left', type=str, help='path to left parent file')
    parser.add_argument('--right', type=str, help='path to right parent file' )
    parser.add_argument('--base', type=str, help='path to base file' )
    parser.add_argument('--output', type=str, help='name of output file' )
    parser.add_argument('--file', type=str, help='name of  file' )

    args = parser.parse_args()
    return args

def main():

    left=parsing().left
    right=parsing().right
    base=parsing().base
    output=parsing().output
    fl = parsing().file
    type=fl.split(".")[-1]

    #Creates new language object for language specific processing (extracting imports, methods, etc..)
    match type:
        case "java":
            lang=Java()
        case "py":
            lang=Python()
        case _:
            print("Language Not Supported")
            exit(0)

    # Seperate imports and code body for each file version. 
    left_import,left_content=lang.extractImports(subprocess.check_output(f"cat "+left, shell=True).decode('utf-8'))
    right_import,right_content=lang.extractImports(subprocess.check_output(f"cat "+right, shell=True).decode('utf-8'))
    base_import,base_content=lang.extractImports(subprocess.check_output(f"cat "+base, shell=True).decode('utf-8'))

    # Merge body using our heuristics if input code syntax is correct. Otherwise just use git merge.
    # Using syntax correctly is only relevant in python source code. Isn't used in Java.
    result=merger.body_merge(lang,base_content,right_content,left_content)
    if result == "**to_be_handled_by_git**":
        print("**to_be_handled_by_git**")
        result=merger.git_merge(base_content,right_content,left_content,lang.get_lang())

    # Determine which imports are used. Used to cross reference whether to include import in final result.
    lang.getUsages(result)
    import_result=merger.import_merge(lang,base_import,right_import,left_import)

    # Write merged import and body to final merged version.
    writefile(output,import_result)
    appendfile(output,result)
    

def writefile(name, content):
    with open(name, 'w') as output:
        for line in content:
            if line == "!!!no import anymore!!!":
                continue
            elif type(line)==Comment:
                output.write(line.get_comment()+'\n')
            else:
                output.write(line+'\n')

def appendfile(name, content):
    with open(name,"a") as res2:
        res2.write(content)


if __name__=="__main__":
    main()

