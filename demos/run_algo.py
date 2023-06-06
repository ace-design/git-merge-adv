import argparse
import subprocess

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


if __name__=="__main__":
    main()



