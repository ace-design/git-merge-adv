import argparse
import subprocess
import re
import os
import math
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
    reference_path=parsing().cs
    algo=parsing().algo
    lang=get_suffix(parsing().lang)
    purpose=parsing().purpose

    output_path=parsing().lang+'_case_studies/demo_results/'+algo+'/'

    if not os.path.isdir(output_path):
        subprocess.run(['mkdir',output_path])


    if (purpose=="spec"):
        # get_case_study(case_study,output_path)
        case_study=reference_path.split('/')[-2]+'-'+reference_path.split('/')[-1]
        output_path+=case_study
        exec_algo(algo,reference_path,output_path,lang)

        if (lang=="java"):
            run_gumtree_existing(reference_path,'jdime')
            run_gumtree_existing(reference_path,'spork_result')

        run_gumtree_algo(reference_path,output_path,lang,algo)

    else:
        for subdir, dirs, files in os.walk(reference_path):
                for d in dirs:
                    try:
                        if ("importC" in d or "conflict" in d):
                            print("\nExecute "+os.path.join(subdir, d)+":")
                            added_output_path=output_path+subdir.split('/')[-1]+'-'+d
                            print(subdir.split('/'))
                            exec_algo(algo,os.path.join(subdir, d),added_output_path,lang)

                            if (lang=="java"):
                                run_gumtree_existing(os.path.join(subdir,d),'jdime')
                                run_gumtree_existing(os.path.join(subdir,d),'spork_result')

                            run_gumtree_algo(os.path.join(subdir,d),added_output_path,lang,algo)

                    except Exception as e:
                        print(f"error in executing the casestudy {d} in {subdir}")
                        print(e)
                

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
        algo_path="../merge-algorithms/"+algo+'/init.py'
        subprocess.run(['python3', algo_path,'--left',reference_case_study+"/left."+lang,'--right',reference_case_study+"/right."+lang,'--base',reference_case_study+"/base."+lang,'--out',output_path+"."+lang,'--file',reference_case_study+"/base."+lang])
    except:
        print("path not found")
        exit(0)



# Looks for all import deletions, moves and insertions found in gumtree textdiff output.
def search_gumtree_imports(result):
    syntax_error=False
    if (len(result)==1 and result[0]==''):
        syntax_error=True

    dict={
    'deletions':re.compile(r'\ndelete-(tree|node)'),
    'moves':re.compile(r'\nmove-(tree|node)'),
    'insertions':re.compile(r'\ninsert-(tree|node)'),
    'diff_path':re.compile(r'\nupdate-(tree|node)')
    }

    data={'deletions':0,'insertions':0,'moves':0,'diff_path':0}
    
    if (not syntax_error):
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


def search_gumtree_full(result,new,num_conflicts,existing_data, existing_total,output):
    syntax_error=False

    if (len(result)==1 and result[0]==''):
        syntax_error=True

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

    if (not syntax_error):
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

    df=pd.read_csv(new)


    if output in df['project'].values:
        for key in data.keys():
            df.loc[df['project']==output, key]=data[key]
        if syntax_error:
            df.loc[df['project']==output, 'overall']='Error'
        else:
            df.loc[df['project']==output, 'overall']=total
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
        if syntax_error:
            data['overall']=['Error']
        df=pd.DataFrame(data)
        df.to_csv(new, mode='a', index=False, header=False)


    if syntax_error:
        raise RuntimeError("Error in gumtree. Two possible reasons:\n 1. PythonParser not configured (see readme) \n 2. Syntax error in desired files preventing gumtree from running (solve errors manually, then try again)")

    
# Compares jdime version to desired version
def run_gumtree_existing(reference_path,tool):
    gumtree_path=reference_path.split("java_case_studies")[0]+'dependencies/gumtree.jar'

    desired=reference_path+"/desired.java"
    desired_text=subprocess.run(['cat',desired],capture_output=True,text=True).stdout

    result=reference_path+"/"+tool+".java"
    new="java_case_studies/demo_results/"+tool+".csv"

    without_git=subprocess.run(['cat',result],capture_output=True, text=True)
    new_result="without_git.java"

    num_conflicts=0
    with open(new_result,'w') as writer:
        start=False
        divider=False
        for line in without_git.stdout.split('\n'):
            if "<<<<<<<" in line:
                num_conflicts+=1
                start=True
                current_sim=0
                incoming_sim=0
                current_text=[]
                incoming_text=[]
            elif (start and (not divider)):
                if ("=======" in line):
                    divider=True
                else:
                    if line in desired_text:
                        current_sim+=1
                    current_text.append(line)
            elif (start and divider):
                if (">>>>>>>" in line):
                    start=False
                    divider=False
                    if (current_sim>incoming_sim):
                        for val in current_text:
                            writer.write(val+'\n')
                    else:
                        for val in incoming_text:
                            writer.write(val+'\n')
                else:
                    if line in desired_text:
                        incoming_sim+=1
                    incoming_text.append(line)
            elif (not start and not divider):
                writer.write(line+'\n')


    if not os.path.isfile(new):
        columns=['project','deletions','insertions','moves','diff_path','num_conflicts','overall']
        df=pd.DataFrame(columns=columns)
        df.to_csv(new,index=False)

    result=subprocess.run(['java','-jar',gumtree_path,'textdiff','-g','java-jdt','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
    
    import_data,import_total=search_gumtree_imports(result)
    search_gumtree_full(result,new,num_conflicts,import_data,import_total,reference_path.split('/')[-2]+'-'+reference_path.split('/')[-1])
    subprocess.run(['rm',new_result])




# Compares inputted algorithm version to desired version. 
def run_gumtree_algo(reference_path,output_path,lang,algo):
    if ('java_case_studies' in reference_path):
        gumtree_path=reference_path.split("java_case_studies")[0]+'dependencies/gumtree.jar'
    else:
        gumtree_path=reference_path.split("python_case_studies")[0]+'dependencies/gumtree.jar'

    desired=reference_path+"/desired."+lang
    desired_text=subprocess.run(['cat',desired],capture_output=True,text=True).stdout
    result=output_path
    without_git=subprocess.run(['cat',result+'.'+lang],capture_output=True, text=True)
    new_result='without_git.'+lang

    num_conflicts=0
    with open(new_result,'w') as writer:
        start=False
        divider=False
        for line in without_git.stdout.split('\n'):
            if "<<<<<<<" in line:
                num_conflicts+=1
                start=True
                current_sim=0
                incoming_sim=0
                current_text=[]
                incoming_text=[]
            elif (start and not divider):
                if ("=======" in line):
                    divider=True
                else:
                    if line in desired_text:
                        current_sim+=1
                    current_text.append(line)
            elif (start and divider):
                if (">>>>>>>" in line):
                    start=False
                    divider=False
                    if (current_sim>incoming_sim):
                        for val in current_text:
                            writer.write(val+'\n')
                    else:
                        for val in incoming_text:
                            writer.write(val+'\n')
                else:
                    if line in desired_text:
                        incoming_sim+=1
                    incoming_text.append(line)
            elif (not start and not divider):
                writer.write(line+'\n')


    new=output_path[0:output_path.rfind('/')].replace('/'+algo,'')+'/'+algo+'.csv'

    if not os.path.isfile(new):
        columns=['project','deletions','insertions','moves','diff_path','num_conflicts','overall']
        df=pd.DataFrame(columns=columns)
        df.to_csv(new,index=False)


    match lang:
        case "py":
            all_result=subprocess.run(['java','-jar',gumtree_path,'textdiff','-m','theta',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
            import_data,import_total=search_gumtree_imports(all_result)
            body_result=subprocess.run(['java','-jar',gumtree_path,'textdiff','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
            search_gumtree_full(body_result,new,num_conflicts,import_data,import_total,result.split('/')[-1])
        case "java":
            all_result=subprocess.run(['java','-jar',gumtree_path,'textdiff','-g','java-jdt','-m','gumtree-simple-id',desired,new_result],capture_output=True,text=True).stdout.strip("/n").split("===")
            import_data,import_total=search_gumtree_imports(all_result)
            search_gumtree_full(all_result,new,num_conflicts,import_data,import_total,result.split('/')[-1])
    subprocess.run(['rm',new_result])


if __name__=="__main__":
    main()