import argparse
from spec import Java,Python
import merger
import subprocess

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

    match type:
        case "java":
            lang=Java()
        case "py":
            lang=Python()
        case _:
            print("Language Not Supported")
            exit(0)
            


    left_import,left_content=lang.extractImports(subprocess.check_output(f"cat "+left, shell=True).decode('utf-8'))
    right_import,right_content=lang.extractImports(subprocess.check_output(f"cat "+right, shell=True).decode('utf-8'))
    base_import,base_content=lang.extractImports(subprocess.check_output(f"cat "+base, shell=True).decode('utf-8'))


    result=lang.getClasses(left_content+right_content+base_content)


    git_result=merger.git_merge(base_content,right_content,left_content,type)
    
    lang.getUsages(git_result.stdout)


    import_result=merger.import_merge(lang,base_import,right_import,left_import)

    writefile(output,import_result)

    with open(output,'a') as writer:
        for class_name in result:
            write_methods(writer,class_name)

    

    clean(type)


def write_methods(writer,class_name):
        spacing=' '*int(class_name.get_ranking()*4)
        writer.write(spacing+class_name.get_full_name()+'{\n')
        method_spacing=spacing+' '*4
        for method in class_name.get_methods():
            writer.write(method_spacing+method.get_method()+'\n')
        
        for sub_class in class_name.get_sub_classes():
            write_methods(writer,sub_class)
            
        writer.write(spacing+'}\n')


def writefile(name, content):
    with open(name, 'w') as output:
        for line in content:
            if line == "!!!no import anymore!!!":
                continue
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


