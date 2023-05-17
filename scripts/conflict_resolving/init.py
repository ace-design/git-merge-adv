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

def extactfiles(info,repo):
        # parent = subprocess.check_output(f"git log --pretty=%p -n 1 {hash} --abbrev=8", shell=True).decode('utf-8').strip('\n').split(' ')

        os.chdir(str(os.getcwd())+"/"+str(repo))
        subprocess.run(['git','checkout', info[1]])
        subprocess.run(['cp', info[4],'../results/left.java'])
        subprocess.run(['git','checkout', info[2]])
        subprocess.run(['cp', info[4],'../results/right.java'])
        subprocess.run(['git','checkout', info[3]])
        subprocess.run(['cp', info[4],'../results/base.java'])
        subprocess.run(['git','checkout', info[0]])
        subprocess.run(['cp', info[4],'../results/desired.java'])
        try:
            subprocess.run(['git', 'reset','--hard','master'])
        except:
            subprocess.run(['git', 'reset','--hard','main'])




def runSpork():
        os.chdir(str(os.getcwd())+"/../"+"results")
        spk_res = subprocess.run(['java', '-jar', str(os.getcwd())+'/../spork-0.5.1.jar', 'left.java', 'base.java', 'right.java'],capture_output=True, text=True)
        with open("spork_result.java", "w") as res:
             res.write(spk_res.stdout)
        print(spk_res.stdout)

def runGit(repo, info):
    os.chdir(str(os.getcwd())+"/../"+repo)
    subprocess.run(['git', 'checkout', info[1]])
    subprocess.run(['git', 'merge', '--no-commit', '--no-ff',info[2]], capture_output=False)
    subprocess.run(['cp', info[4],'../results/git.java'])
    subprocess.run(['git', 'merge', '--abort'])
    try:
        subprocess.run(['git', 'reset','--hard','master'])
    except:
        subprocess.run(['git', 'reset','--hard','main'])


def runJDime():
    os.chdir(str(os.getcwd())+"/../"+"results")
    subprocess.run(['cp', '../JDime.properties', 'JDime.properties'])
    subprocess.run(['cp', '../JDimeLogging.properties', 'JDimeLogging.properties'])
    with open("jdime.java", "w") as res:
        res.write("")
    subprocess.run(['sudo','docker','run','--rm', '-v',os.getcwd()+':/wkdir', 'acedesign/jdimew', '-mode', 'structured','--output','jdime.java','left.java', 'base.java', 'right.java','-f'])
    subprocess.run(['rm','JDime.properties'])
    subprocess.run(['rm','JDimeLogging.properties'])


def main():
    parser = argparse.ArgumentParser(description="For taking file of Conflict Hashes")
    parser.add_argument("--filename", type=str, help="Name of file with confict hashes")
    parser.add_argument("--line", type=int, help="line of file with confict hashes")
    parser.add_argument("--repo", type=str, help="name of clone of repo for testing")
    args = parser.parse_args()
    file_name=args.filename
    line_num = args.line
    repo = args.repo
    try:
        subprocess.run("rm -r results")
    except:
         pass
    subprocess.run(["mkdir", "results"])

    info = getinfo(file_name,line_num)
    extactfiles(info,repo)
    runSpork()
    runGit(repo,info)
    runJDime()





if __name__ == "__main__":
    main()
