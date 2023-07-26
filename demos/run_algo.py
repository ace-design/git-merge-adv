import argparse
import subprocess
import re
import os
import math
import csv 
import pandas as pd

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

    reference_path=case_study.strip("../")

    output_path=parsing().lang+'_case_studies/demo_results/'+algo+'/'

    if not os.path.isdir(output_path):
        subprocess.run(['mkdir',output_path])


    if (purpose=="spec"):
        # get_case_study(case_study,output_path)
        case_study=reference_path.split('/')[-2]+'-'+reference_path.split('/')[-1]
        output_path+=case_study
        exec_algo(algo,reference_path,output_path,lang)

        run_gumtree(reference_path,output_path,lang,algo)

        if (lang=="java"):
            run_gumtree_spork(reference_path)
            run_gumtree_jdime(reference_path)
    else:
        for subdir, dirs, files in os.walk(reference_path):
                for d in dirs:
                    try:
                        if ("importC" in d or "conflict" in d):
                            print("\nExecute "+os.path.join(subdir, d)+":")
                            print(os.path.join(subdir, d),lang)
                            added_output_path=output_path+subdir.split('/')[-1]+'-'+d
                            print(added_output_path)
                            exec_algo(algo,os.path.join(subdir, d),added_output_path,lang)

                            run_gumtree(os.path.join(subdir,d),added_output_path,lang,algo)

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



def exec_algo(algo,reference_case_study,output_path,lang):
    try:
        # action=input("\nEnter 0 if testing new algorithm\nEnter 1 if executing gumtree again due to previous errors.\n")
        algo_path="../merge-algorithms/"+algo+'/init.py'
        subprocess.run(['python3', algo_path,'--left',reference_case_study+"/left."+lang,'--right',reference_case_study+"/right."+lang,'--base',reference_case_study+"/base."+lang,'--out',output_path+"."+lang,'--file',reference_case_study+"/base."+lang])
    except:
        print("path not found")
        exit(0)



# Looks for all import deletions, moves and insertions found in gumtree textdiff output.
def search_gumtree(result):
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

    return data,total

    # with open(new,'w') as writer:
    #     for key in data.keys():
    #         writer.write(key+": "+str(data[key])+"\n")
    #     writer.write("Overall: "+str(total))

def search_gumtree_full(result,new,num_conflicts,existing_data, existing_total,output):
    if (len(result)==1 and result[0]==''):
        raise RuntimeError("Error in gumtree. Two possible reasons:\n 1. PythonParser not configured (see readme) \n 2. Syntax error in desired files preventing gumtree from running (solve errors manually, then try again)")

    dict={
    'deletions':re.compile(r'\ndelete'),
    'moves':re.compile(r'\nmove-tree'),
    'insertions':re.compile(r'\ninsert'),
    'diff_path':re.compile(r'\nupdate')
    }

    if existing_data:
        data=existing_data
        data['num_conflicts']=num_conflicts
        total=existing_total
    else:
        data={'deletions':0,'insertions':0,'moves':0,'diff_path':0,'num_conflicts':num_conflicts}
        total=0

    for val in result:
        for key,rx in dict.items():
            match=rx.search(val)
            if (match):
                try:
                    range=re.findall(r'\[.*?\]',val)[0]
                    start=int(range.split(',')[0].strip('['))
                    end=int(range.split(',')[1].strip(']'))
                    if (end-start>1 and 'import' not in val and "Import" not in val):
                        data[key]+=1
                except:
                    pass

    for value in data.values():
        total+=math.pow(value,2)
    total=math.sqrt(total)


    exists=False

    df=pd.read_csv(new)


    if output in df['project'].values:
        for key in data.keys():
            df.loc[df['project']==output, key]=data[key]
        df.to_csv(new,index=False)
    else:
        data={
            'project':[output],
            'deletions': [data['deletions']],
            'insertions': [data['insertions']],
            'moves': [data['moves']],
            'diff_path': [data['diff_path']],
            'num_conflicts': [data['num_conflicts']],
            'overall': [total] 
        }
        df=pd.DataFrame(data)
        df.to_csv(new, mode='a', index=False, header=False)
    
# Compares spork version to desired version (Java )
def run_gumtree_spork(reference_path):
    desired=reference_path+"/desired.java"
    result=reference_path+"/spork_result.java"
    new="java_case_studies/demo_results/spork.csv"

    without_git=subprocess.run(['cat',result],capture_output=True, text=True)
    new_result="without_git.java"

    num_conflicts=0
    with open(new_result,'w') as writer:
        for line in without_git.stdout.split('\n'):
            if ("<<<<<<<" not in line and "=======" not in line and ">>>>>>>" not in line):
                writer.write(line+'\n')
            elif ("<<<<<<<" in line):
                num_conflicts+=1


    if not os.path.isfile(new):
        columns=['project','deletions','insertions','moves','diff_path','num_conflicts','overall']
        df=pd.DataFrame(columns=columns)
        df.to_csv(new,index=False)

    result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-g','java-jdt','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
    
    import_data,import_total=search_gumtree(result)
    search_gumtree_full(result,new,num_conflicts,import_data,import_total,reference_path.split('/')[-2]+'-'+reference_path.split('/')[-1])

    # if (len(result)==1 and result[0]==''):
    #     print("Error in gumtree. Two possible reasons:\n 1. PythonParser not configured (see readme) \n 2. Syntax error in desired files preventing gumtree from running (solve errors manually, then try again)")
    #     exit(0)
    subprocess.run(['rm',new_result])

    
# Compares jdime version to desired version
def run_gumtree_jdime(reference_path):
    desired=reference_path+"/desired.java"
    result=reference_path+"/jdime.java"
    new="java_case_studies/demo_results/jdime.csv"

    without_git=subprocess.run(['cat',result],capture_output=True, text=True)
    new_result="without_git.java"

    num_conflicts=0
    with open(new_result,'w') as writer:
        for line in without_git.stdout.split('\n'):
            if ("<<<<<<<" not in line and "=======" not in line and ">>>>>>>" not in line):
                writer.write(line+'\n')
            elif ("<<<<<<<" in line):
                num_conflicts+=1


    if not os.path.isfile(new):
        columns=['project','deletions','insertions','moves','diff_path','num_conflicts','overall']
        df=pd.DataFrame(columns=columns)
        df.to_csv(new,index=False)

    result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-g','java-jdt','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
    
    import_data,import_total=search_gumtree(result)
    search_gumtree_full(result,new,num_conflicts,import_data,import_total,reference_path.split('/')[-2]+'-'+reference_path.split('/')[-1])

    # if (len(result)==1 and result[0]==''):
    #     print("Error in gumtree. Two possible reasons:\n 1. PythonParser not configured (see readme) \n 2. Syntax error in desired files preventing gumtree from running (solve errors manually, then try again)")
    #     exit(0)
    subprocess.run(['rm',new_result])



# Compares inputted algorithm version to desired version. 
def run_gumtree(reference_path,output_path,lang,algo):
    desired=reference_path+"/desired."+lang
    result=output_path
    without_git=subprocess.run(['cat',result+'.java'],capture_output=True, text=True)
    new_result='without_git.'+lang

    num_conflicts=0
    with open(new_result,'w') as writer:
        for line in without_git.stdout.split('\n'):
            if ("<<<<<<<" not in line and "=======" not in line and ">>>>>>>" not in line):
                writer.write(line+'\n')
            elif ("<<<<<<<" in line):
                num_conflicts+=1

    new=output_path[0:output_path.rfind('/')].replace('/'+algo,'')+'/'+algo+'.csv'
    print(new)



    if not os.path.isfile(new):
        columns=['project','deletions','insertions','moves','diff_path','num_conflicts','overall']
        df=pd.DataFrame(columns=columns)
        df.to_csv(new,index=False)


    match lang:
        case "py":
            import_result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-m','theta',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
            import_data,import_total=search_gumtree(import_result)
            body_result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
            search_gumtree_full(body_result,new,num_conflicts,import_data,import_total,result.split('/')[-1])
        case "java":
            import_result=subprocess.run(['java','-jar','gumtree.jar','textdiff','-g','java-jdt','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
            import_data,import_total=search_gumtree(import_result)
            search_gumtree_full(import_result,new,num_conflicts,import_data,import_total,result.split('/')[-1])
    subprocess.run(['rm',new_result])
    # search_gumtree(result,new)
    # search_gumtree_full(result,new,num_conflicts)


if __name__=="__main__":
    main()



