import argparse
import subprocess
import re
import os
import math

def parsing():
    parser = argparse.ArgumentParser(description='Enter path to case study, which merge algorithm and language of case study')
    parser.add_argument('--cs', type=str, help="Path to case study folder")
    parser.add_argument('--algo', type=str, help="Name of merge algorithm")
    parser.add_argument('--lang',type=str,help="Name of language")
    parser.add_argument('--purpose',type=str,help="Enter all for general, or spec for specific")
    args = parser.parse_args()
    return args

def main():
    case_study=parsing().cs
    algo=parsing().algo
    lang=get_suffix(parsing().lang)
    purpose=parsing().purpose

    output_path=case_study.strip("../")

    if (purpose=="spec"):
        get_case_study(case_study,output_path)
        exec_algo(algo,output_path,lang)

        run_gumtree(output_path,lang,algo)

        if (lang=="java"):
            run_gumtree_spork(output_path)
            run_gumtree_jdime(output_path)
    else:
        for subdir, dirs, files in os.walk(output_path):
            try:   
                for d in dirs:
                    if ("importC" in d or "conflict" in d):
                        print("\nExecute "+os.path.join(subdir, d)+":")
                        print(os.path.join(subdir, d),lang)
                        exec_algo(algo,os.path.join(subdir, d),lang)

                        run_gumtree(os.path.join(subdir,d),lang,algo)

                        if (lang=="java"):
                            run_gumtree_spork(os.path.join(subdir,d))
                            run_gumtree_jdime(os.path.join(subdir,d))
            except Exception as e:
                print(f"error in executing the casestudy {d} in {subdir}")
                print(e)

    # for dir in os.walk(output_path)


def get_suffix(lang):
    match lang:
        case "java":
            return "java"
        case "python":
            return "py"
        case _:
            print("Language not accepted")
            exit(0)



def exec_algo(algo,case_study,lang):
    try:
        # action=input("\nEnter 0 if testing new algorithm\nEnter 1 if executing gumtree again due to previous errors.\n")
        algo_path="../merge-algorithms/"+algo+'/init.py'
        subprocess.run(['mkdir','-p',case_study+"/demo_result/"])
        subprocess.run(['python3', algo_path,'--left',case_study+"/left."+lang,'--right',case_study+"/right."+lang,'--base',case_study+"/base."+lang,'--out',case_study+"/demo_result/"+algo+"."+lang,'--file',case_study+"/base."+lang])
        # if (action=="0"):
        #     algo_path="../merge-algorithms/"+algo+'/init.py'
        #     subprocess.run(['mkdir','-p',case_study+"/demo_result/"])
        #     subprocess.run(['python3', algo_path,'--left',case_study+"/left."+lang,'--right',case_study+"/right."+lang,'--base',case_study+"/base."+lang,'--out',case_study+"/demo_result/"+algo+"."+lang,'--file',case_study+"/base."+lang])
        # elif (action=="1"):
        #     pass
        # else:
        #     print("Not a valid answer. Ended program.")
        #     exit(0)
    except:
        print("path not found")
        exit(0)


# Copies desired case study to demo folder.
def get_case_study(case_study,output_path):
    try:
        if (os.path.exists(output_path)):
            pass
        else:
            subprocess.run(['mkdir','-p',output_path])
            adjusted=output_path.rfind("/")
            subprocess.run(['cp','-r',case_study,output_path[:adjusted]])
    except:
        print("path not found")
        exit(0)

# Looks for all import deletions, moves and insertions found in gumtree textdiff output.
def search_gumtree(result,new):
    if (len(result)==1 and result[0]==''):
        raise RuntimeError("Error in gumtree. Two possible reasons:\n 1. PythonParser not configured (see readme) \n 2. Syntax error in desired files preventing gumtree from running (solve errors manually, then try again)")

    dict={
    'deletions':re.compile(r'\ndelete-(tree|node)'),
    'moves':re.compile(r'\nmove-(tree|node)'),
    'insertions':re.compile(r'\ninsert-(tree|node)'),
    'diff_path':re.compile(r'\nupdate-(tree|node)')
    }

    data={'deletions':0,'insertions':0,'moves':0,'diff_path':0}

    for val in result:
        for key,rx in dict.items():
            match=rx.search(val)
            if (match):
                if (key=='diff_path'):
                    try:
                        range=re.findall(r'\[.*?\]',val)[0]
                        end=range.split(',')[1].strip(']')
                        if (int(end)<2000):
                            data[key]+=1
                    except:
                        pass
                elif (('import' in val) or ("Import" in val)) and ("operator: ," not in val):
                    data[key]+=1
    total=0
    for value in data.values():
        total+=math.pow(value,2)
    total=math.sqrt(total)

    with open(new,'w') as writer:
        for key in data.keys():
            writer.write(key+": "+str(data[key])+"\n")
        writer.write("Overall: "+str(total))

def search_gumtree_full(result,new,num_conflicts):
    if (len(result)==1 and result[0]==''):
        raise RuntimeError("Error in gumtree. Two possible reasons:\n 1. PythonParser not configured (see readme) \n 2. Syntax error in desired files preventing gumtree from running (solve errors manually, then try again)")


    dict={
    'deletions':re.compile(r'\ndelete'),
    'moves':re.compile(r'\nmove-tree'),
    'insertions':re.compile(r'\ninsert'),
    'diff_path':re.compile(r'\nupdate')
    }

    data={'deletions':0,'insertions':0,'moves':0,'diff_path':0,'num_conflicts':num_conflicts}

    for val in result:
        for key,rx in dict.items():
            match=rx.search(val)
            if (match):
                data[key]+=1
    total=0
    for value in data.values():
        total+=math.pow(value,2)
    total=math.sqrt(total)

    with open(new,'w') as writer:
        for key in data.keys():
            writer.write(key+": "+str(data[key])+"\n")
        writer.write("Overall: "+str(total))

# Compares spork version to desired version (Java )
def run_gumtree_spork(output_path):
    desired=output_path+"/desired.java"
    result=output_path+"/spork_result.java"

    without_git=subprocess.run(['cat',result],capture_output=True, text=True)
    new_result=output_path+"/demo_result/without_git.java"

    num_conflicts=0
    with open(new_result,'w') as writer:
        for line in without_git.stdout.split('\n'):
            if ("<<<<<<<" not in line and "=======" not in line and ">>>>>>>" not in line):
                writer.write(line+'\n')
            elif ("<<<<<<<" in line):
                num_conflicts+=1
    new=output_path+"/demo_result/spork_diff.txt"

    result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-g','java-jdt','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
    
    if (len(result)==1 and result[0]==''):
        print("Error in gumtree. Two possible reasons:\n 1. PythonParser not configured (see readme) \n 2. Syntax error in desired files preventing gumtree from running (solve errors manually, then try again)")
        exit(0)
    subprocess.run(['rm',new_result])

    search_gumtree_full(result,new,num_conflicts)
    
# Compares jdime version to desired version
def run_gumtree_jdime(output_path):
    desired=output_path+"/desired.java"
    result=output_path+"/jdime.java"
    new=output_path+"/demo_result/jdime_diff.txt"

    without_git=subprocess.run(['cat',result],capture_output=True, text=True)
    new_result=output_path+"/demo_result/without_git.java"

    num_conflicts=0
    with open(new_result,'w') as writer:
        for line in without_git.stdout.split('\n'):
            if ("<<<<<<<" not in line and "=======" not in line and ">>>>>>>" not in line):
                writer.write(line+'\n')
            elif ("<<<<<<<" in line):
                num_conflicts+=1
    
    result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-g','java-jdt','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")

    
    if (len(result)==1 and result[0]==''):
        print("Error in gumtree. Two possible reasons:\n 1. PythonParser not configured (see readme) \n 2. Syntax error in desired files preventing gumtree from running (solve errors manually, then try again)")
        exit(0)
    subprocess.run(['rm',new_result])

    search_gumtree_full(result,new,num_conflicts)

# Compares inputted algorithm version to desired version. 
def run_gumtree(output_path,lang,algo):
    desired=output_path+"/desired."+lang
    result=output_path+"/demo_result/"+algo+"."+lang
    without_git=subprocess.run(['cat',result],capture_output=True, text=True)
    new_result=output_path+"/demo_result/without_git."+lang

    num_conflicts=0
    with open(new_result,'w') as writer:
        for line in without_git.stdout.split('\n'):
            if ("<<<<<<<" not in line and "=======" not in line and ">>>>>>>" not in line):
                writer.write(line+'\n')
            elif ("<<<<<<<" in line):
                num_conflicts+=1
    new=output_path+"/demo_result/"+algo+"_diff.txt"

    match lang:
        case "py":
            result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-m','theta',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
        case "java":
            result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-g','java-jdt','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
    subprocess.run(['rm',new_result])
    # search_gumtree(result,new)
    search_gumtree_full(result,new,num_conflicts)


if __name__=="__main__":
    main()



