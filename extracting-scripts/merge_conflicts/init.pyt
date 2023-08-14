import tempfile as file
import sys
import argparse
import subprocess
import os.path
import csv
from hash_generation import generateInfo
from conflict_identifier import identify_conflict

#Parts of code given by Richard Li

#Format: `python3 init.pyt --url <Clone URL> --output <Output file name>``

home_directory = os.getcwd()

def generate(url):
    # Clone github repo in a temporary directory

    temp_dir=file.TemporaryDirectory(".") 
    url_split = url.split('/')
    git_name = '/'.join(url_split[3:]).split('.')
    git_project_name = git_name[0]

    #clone the github repo and cd to it

    try:
        subprocess.run(['git','clone', url, f'{temp_dir.name}/{git_project_name}'])
        os.chdir(f'{temp_dir.name}/{git_project_name}')
    except FileNotFoundError:
        print("Link broken")
        sys.exit(1)    
    
    #get all merge hash-values (not specific to conflict yet)
    merge=generateInfo()

    #get all merge conflicts only
    update_merge=identify_conflict(merge)

    return update_merge


def parsing():
    parser = argparse.ArgumentParser(description='Enter Git Repository Link')
    parser.add_argument('--url', type=str, help='Git Repository Link')
    parser.add_argument('--output', type=str, help='Enter output file' )
    args = parser.parse_args()
    return args


def genCSV(filename, all_merges):
    os.chdir(home_directory)

    heading=all_merges[0].keys()

    #output all merge conflicts to csv file
    with open(filename, 'w') as output:
        writer = csv.DictWriter(output, heading)
        writer.writeheader()

        for data in all_merges:
            writer.writerow(data)




def main():
    url=parsing().url
    filename=parsing().output

    conflict_merges=generate(url)
    if (len(conflict_merges)!=0):
        genCSV(filename, conflict_merges)
    else:
        print("No Merge Conflicts")



if __name__ == "__main__":
    main()





