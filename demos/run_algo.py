import argparse
import subprocess
import re

def parsing():
    parser = argparse.ArgumentParser(description='Enter path to case study, which merge algorithm and language of case study')
    parser.add_argument('--cs', type=str, help="Path to case study folder")
    parser.add_argument('--algo', type=str, help="Name of merge algorithm")
    parser.add_argument('--lang',type=str,help="Name of language")
    args = parser.parse_args()
    return args

def main():
    case_study=parsing().cs
    algo=parsing().algo
    lang=get_suffix(parsing().lang)

    output_path=case_study.strip("../")
    get_case_study(case_study,output_path)
    exec_algo(algo,output_path,lang)
    run_gumtree(output_path,lang,algo)


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
        algo_path="../merge-algorithms/"+algo+'/init.py'
        subprocess.run(['mkdir','-p',case_study+"/demo_result/"])
        subprocess.run(['python3', algo_path,'--left',case_study+"/left."+lang,'--right',case_study+"/right."+lang,'--base',case_study+"/base."+lang,'--out',case_study+"/demo_result/"+algo+"."+lang])
    except:
        print("path not found")
        exit(0)



def get_case_study(case_study,output_path):
    try:
        subprocess.run(['mkdir','-p',output_path])
        adjusted=output_path.rfind("/")
        subprocess.run(['cp','-r',case_study,output_path[:adjusted]])
    except:
        print("path not found")
        exit(0)


def run_gumtree(output_path,lang,algo):
    desired=output_path+"/desired."+lang
    result=output_path+"/demo_result/"+algo+"."+lang
    new=output_path+"/demo_result/"+algo+"_diff.txt"
    result=subprocess.run(['java','-jar','gumtree.jar','textdiff',desired,result],capture_output=True,text=True).stdout.strip("/n").split("===")
    # with open(new, 'w') as devnull:
    #     subprocess.run(['java','-jar','gumtree.jar','textdiff',desired,result],stdout=devnull)

    if (len(result)==1 and result[0]==''):
        print("PythonParser cannot be detected.")
        exit(0)


    dict={
    'deletions':re.compile(r'\ndelete-(tree|node)\n---\n(i|I)mport*'),
    'moves':re.compile(r'\nmove-(tree|node)\n---\n(i|I)mport*'),
    'insertions':re.compile(r'\ninsert-(tree|node)\n---\n(i|I)mport*'),
    'diff_path':re.compile(r'\nupdate-(tree|node)\n---\nQualifiedName*')
    }

    data={'deletions':0,'insertions':0,'moves':0,'diff_path':0}


    for val in result:
        for key,rx in dict.items():
            match=rx.search(val)
            if (match):
                data[key]+=1

    with open(new,'w') as writer:
        for key in data.keys():
            writer.write(key+": "+str(data[key])+"\n")
    





if __name__=="__main__":
    main()



