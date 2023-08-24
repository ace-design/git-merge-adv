import argparse
import os
import subprocess

def parsing():
    parser = argparse.ArgumentParser(description='Enter which directory you want to difference')
    parser.add_argument('--lang',type=str,help="Enter language of case studies you want to analyze")
    args = parser.parse_args()
    return args


def main():
    lang=parsing().lang
    dir=lang+"_case_studies"

    if lang=="python":
        lang='py'

    run_within(dir,lang)



def run_within(dir,lang):
    for subdir, dirs, files in os.walk(dir):
        for d in dirs:
            try:
                if ("importC" in d or "conflict" in d):
                    left_file=subdir+'/'+d+'/left.'+lang
                    right_file=subdir+'/'+d+'/right.'+lang
                    base_file=subdir+'/'+d+'/base.'+lang
                    desired_file=subdir+'/'+d+'/desired.'+lang
                    reference=subdir.replace('reference_repos','demo_results/MethodUnion')
                    generated_file=reference+'-'+d+'.'+lang

                    subprocess.run(['mkdir','-p',subdir+'/'+d+'/git_diff'])
                    if (lang=='java'):
                        jdime_file=subdir+'/'+d+'/jdime.'+lang
                        spork_file=subdir+'/'+d+'/spork_result.'+lang

                        jdime_desired=subprocess.run(['git','diff','--no-index',desired_file,jdime_file],capture_output=True,text=True).stdout
                        with open(subdir+'/'+d+'/git_diff/desired_jdime.txt','w') as file:
                            file.write(jdime_desired)

                        spork_desired=subprocess.run(['git','diff','--no-index',desired_file,spork_file],capture_output=True,text=True).stdout
                        with open(subdir+'/'+d+'/git_diff/desired_spork.txt','w') as file:
                            file.write(spork_desired)

                    desired_gen=subprocess.run(['git','diff','--no-index',desired_file,generated_file],capture_output=True,text=True).stdout
                    with open(subdir+'/'+d+'/git_diff/desired_generated.txt','w') as file:
                        file.write(desired_gen)


                    base_left=subprocess.run(['git','diff','--no-index',base_file,left_file],capture_output=True,text=True).stdout
                    with open(subdir+'/'+d+'/git_diff/base_left.txt','w') as file:
                        file.write(base_left)

                    base_right=subprocess.run(['git','diff','--no-index',base_file,right_file],capture_output=True,text=True).stdout
                    with open(subdir+'/'+d+'/git_diff/base_right.txt','w') as file:
                        file.write(base_right)

                    left_right=subprocess.run(['git','diff','--no-index',left_file,right_file],capture_output=True,text=True).stdout
                    with open(subdir+'/'+d+'/git_diff/left_right.txt','w') as file:
                        file.write(left_right)
                    


            except Exception as e:
                print(f"error in executing the casestudy {d} in {subdir}")
                print(e)

if __name__=="__main__":
    main()