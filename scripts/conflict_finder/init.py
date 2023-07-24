import argparse
import csv
import subprocess
import os


def parsing():
    parser = argparse.ArgumentParser(description='Enter Git Repository Link')
    parser.add_argument('--repo', type=str, help='Enter Git Clone URL' )
    parser.add_argument('--lang', type=str, help='Enter Language for Repo' )
    args = parser.parse_args()
    return args



def main():
    lang=parsing().lang
    if (lang=="python"):
        ext=".py"
    elif (lang=="java"):
        ext=".java"
    else:
        print("Not valid language")
        exit(0)

    repo=parsing().repo

    get_merges(repo,ext,lang)



def get_merges(repo,ext,lang):
    repo_name=repo.split("/")[-1][:-4]
    print("Split")
    subprocess.run(['python3','../merge_conflicts/init.pyt','--url',repo,'--output',repo_name+'.csv'])

    print("Subprocess done")

    if os.path.isfile(repo_name+".csv"):
        print("Checking")

        lines=[]
        with open(repo_name+'.csv') as file:
            csvreader=csv.reader(file)
            next(csvreader)
            for row in csvreader:
                if ext in row[-1]:
                    print(row)
                    lines.append(csvreader.line_num)

        if (len(lines)>0):
            print(lines)
            subprocess.run(['git','clone',repo])
            count=1
            for line in lines:
                find_files(repo,line,count,ext,lang)
                count+=1

        subprocess.run(['rm',repo_name+".csv"])
        # subprocess.run(['rm','-r',repo_name])


def find_files(repo,line,count,ext,lang):
    repo_name=repo.split("/")[-1][:-4]

    subprocess.run(['python3','../conflict_resolving/init.py','--filename',repo_name+'.csv','--line',str(line),'--repo',repo_name,'--lang',lang])


    if os.path.isfile("results/base"+ext) and os.path.isfile("results/left"+ext) and os.path.isfile("results/right"+ext):
        if not os.path.isdir("../../demos/"+lang+"_case_studies/"+repo_name):
            subprocess.run(['mkdir','../../demos/'+lang+'_case_studies/'+repo_name])
        print("COPIED")
        subprocess.run(['cp','-r','results','../../demos/'+lang+'_case_studies/'+repo_name+"/conflict"+str(count)])









if __name__=="__main__":
    main()