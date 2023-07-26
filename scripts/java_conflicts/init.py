import argparse
import csv
import subprocess
import os


def parsing():
    parser = argparse.ArgumentParser(description='Enter Git Repository Link')
    parser.add_argument('--reference', type=str, help='Enter Path to Reference CSV')
    parser.add_argument('--lang', type=str, help='Enter Language for Repo' )
    args = parser.parse_args()
    return args



def main():
    file=parsing().reference
    language=parsing().lang

    all_repos,all_hash=extract_repos(file)

    for repo in all_repos:
        print(repo)
        if not os.path.isdir("../../demos/java_case_studies/"+repo.split("/")[-1]):
            get_new_csv(repo,all_hash)
        else:
            print("Skip")

def extract_repos(file):
    all_repos=set()
    all_hash=set()
    with open(file,'r') as file:
        csvreader=csv.reader(file)
        for row in csvreader:
            all_repos.add(row[1])
            all_hash.add(row[3][0:7]+row[2].split("/")[-1].split("_")[0])
    return all_repos,all_hash

def get_new_csv(repo,reference):
    repo_name=repo.split("/")[-1]
    print("Split")
    subprocess.run(['python3','../merge_conflicts/init.pyt','--url','git@github.com:'+repo,'--output',repo_name+'.csv'])

    print("Subprocess done")

    if os.path.isfile(repo_name+".csv"):
        print("Checking")

        lines=[]
        with open(repo_name+'.csv') as file:
            csvreader=csv.reader(file)
            next(csvreader)
            count=0
            for row in csvreader:
                checker=row[0]+row[-1].split("/")[-1]
                if ".java" in row[-1] and checker in reference:
                    lines.append(count)
                count+=1

        if (len(lines)>0):
            print(lines)
            subprocess.run(['git','clone','git@github.com:'+repo])
            count=1
            for line in lines:
                find_files(repo,line,count)
                count+=1

        subprocess.run(['rm',repo_name+".csv"])

def find_files(repo,line,count):
    repo_name=repo.split("/")[-1]

    subprocess.run(['python3','../conflict_resolving/init.py','--filename',repo_name+'.csv','--line',str(line),'--repo',repo_name,'--lang','java'])

    if not os.path.isdir("../../demos/java_case_studies/"+repo_name):
        subprocess.run(['mkdir','../../demos/java_case_studies/'+repo_name])

    subprocess.run(['cp','-r','results','../../demos/java_case_studies/'+repo_name+"/conflict"+str(count)])

    # subprocess.run(['rm','-r',repo_name])








if __name__=="__main__":
    main()