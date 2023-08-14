import argparse
import csv
import os
import subprocess

def getinfo(filename,line_num):
# Open the CSV file
    with open(filename, newline='') as csvfile:
        reader = csv.reader(csvfile)
        for i, row in enumerate(reader):
            if i+1 == line_num:
                print(row)
                return row


def extactfiles(info,repo,lang= None):
        # parent = subprocess.check_output(f"git log --pretty=%p -n 1 {hash} --abbrev=8", shell=True).decode('utf-8').strip('\n').split(' ')
        suffix = "txt"
        if lang == "python" :
             suffix = "py"
        elif lang == "java":
             suffix = "java"
        os.chdir(str(os.getcwd())+"/"+str(repo))
        main_b=subprocess.run(['git','rev-parse','--abbrev-ref','HEAD'],capture_output=True,text=True).stdout.strip('\n')
        print(info[1])
        subprocess.run(['git','checkout', info[1],'--quiet'])
        subprocess.run(['cp', info[4],f'../results/left.{suffix}'])
        subprocess.run(['git','checkout', info[2],'--quiet'])
        subprocess.run(['cp', info[4],f'../results/right.{suffix}'])
        subprocess.run(['git','checkout', info[3],'--quiet'])
        subprocess.run(['cp', info[4],f'../results/base.{suffix}'])
        subprocess.run(['git','checkout', info[0],'--quiet'])
        subprocess.run(['cp', info[4],f'../results/desired.{suffix}'])
        subprocess.run(['git', 'reset','--hard'])
        subprocess.run(['git', 'checkout',main_b])



def runSpork():
        os.chdir(str(os.getcwd())+"/../"+"results")
        spk_res = subprocess.run(['java', '-jar', str(os.getcwd())+'/../spork-0.5.1.jar', 'left.java', 'base.java', 'right.java'],capture_output=True, text=True)
        with open("spork_result.java", "w") as res:
             res.write(spk_res.stdout)

def runGit(repo, info,lang = None):
    suffix = 'txt'
    if lang == "python":
         suffix = 'py'
    elif lang == 'java' :
         suffix = 'java'

    os.chdir(str(os.getcwd())+"/../"+repo)
    main_b=subprocess.run(['git','rev-parse','--abbrev-ref','HEAD'],capture_output=True,text=True).stdout.strip('\n')
    subprocess.run(['git', 'checkout', info[1],'--quiet'])
    subprocess.run(['git', 'merge', '--no-commit', '--no-ff',info[2],'--quiet'], capture_output=False)
    subprocess.run(['cp', info[4],f'../results/git.{suffix}'])
    subprocess.run(['git', 'merge', '--abort'])
    subprocess.run(['git','reset','--hard'])
    subprocess.run(['git', 'checkout',main_b])


def runJDime():
    os.chdir(str(os.getcwd())+"/../"+"results")
    subprocess.run(['cp', '../JDime.properties', 'JDime.properties'])
    subprocess.run(['cp', '../JDimeLogging.properties', 'JDimeLogging.properties'])
    with open("jdime.java", "w") as res:
        res.write("")
    subprocess.run(['sudo','docker','run','--rm', '-v',os.getcwd()+':/wkdir', 'acedesign/jdimew', '-mode', 'structured','--output','jdime.java','left.java', 'base.java', 'right.java','-f'])
    subprocess.run(['rm','JDime.properties'])
    subprocess.run(['rm','JDimeLogging.properties'])

def getAnalysis(info):
    os.chdir(str(os.getcwd())+"/../"+"results")
    with open("analysis", "w") as res:
         res.write("Commit Hash: "+info[0]+'\n')
         res.write("Left Parent Hash: "+info[1]+'\n')
         res.write("Right Parent Hash: "+info[2]+'\n')
         res.write("Base Hash: "+info[3]+'\n')
         res.write("File Name: "+info[4]+'\n')
     


def main():
    parser = argparse.ArgumentParser(description="For taking file of Conflict Hashes")
    parser.add_argument("--filename", type=str, help="Name of file with confict hashes")
    parser.add_argument("--lang", type=str, help="Programming Language the file is written in")

    parser.add_argument("--line", type=int, help="line of file with confict hashes")
    parser.add_argument("--repo", type=str, help="name of clone of repo for testing")
    args = parser.parse_args()
    file_name=args.filename
    file_language=args.lang
    line_num = args.line
    repo = args.repo
    try:
        subprocess.run(['sudo','rm', '-r', 'results'])
    except Exception as e:
         print("exception is ",e)
    subprocess.run(["mkdir", "results"])

    info = getinfo(file_name,line_num)
    extactfiles(info,repo,file_language)
    if file_language=="java":
        runSpork()
        runJDime()
    runGit(repo,info,file_language)
    getAnalysis(info)





if __name__ == "__main__":
    main()
