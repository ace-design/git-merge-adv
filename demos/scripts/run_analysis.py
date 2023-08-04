import argparse
import matplotlib.pyplot as plt
import numpy as np
import os
import math
import csv


def parsing():
    parser = argparse.ArgumentParser(description='Enter which algorithm to analyze:')
    parser.add_argument('--dir',type=str,help="Enter path to case studies you want to analyze")
    args = parser.parse_args()
    return args


def main():
    dir=parsing().dir
    run_cs(dir)
    # run_overall(algo)


#Find all data associated with the desired algorithm in specified case studies path.
def run_cs(dir):


    all_algos={}



    for root,dirs,files in os.walk(dir):
        for file in files:
            if '.csv' in file:
                with open(os.path.join(root,file),'r') as reader:
                    file=file[:-4]
                    reader=csv.reader(reader)
                    next(reader)
                    if file not in all_algos.keys():
                        all_algos[file]={'deletions':0,'insertions':0,'moves':0,'diff_path':0,'num_conflicts':0,'num_errors':0}
                    for row in reader:
                        all_algos[file]['deletions']+=int(row[1])
                        all_algos[file]['insertions']+=int(row[2])
                        all_algos[file]['moves']+=int(row[3])
                        all_algos[file]['diff_path']+=int(row[4])
                        all_algos[file]['num_conflicts']+=int(row[5])
                        if row[6]=='Error':
                            all_algos[file]['num_errors']+=1

    for file in all_algos:
        all_algos[file]['overall']=math.sqrt(math.pow(all_algos[file]['deletions'],2)+math.pow(all_algos[file]['insertions'],2)+math.pow(all_algos[file]['moves'],2)+math.pow(all_algos[file]['diff_path'],2)+math.pow(all_algos[file]['num_conflicts'],2)+math.pow(10*all_algos[file]['num_errors'],2))

    print(all_algos)
    categories=['deletions','insertions','moves','diffpath','conflicts','errors','overall']
    X_axis=np.arange(len(categories))


    spacing = 0.2  # Adjust this value to increase/decrease the spacing between columns
    bar_width = (1 - spacing) / len(all_algos.keys())

    for idx, algo in enumerate(all_algos.keys()):
        plt.bar(X_axis + (idx - len(all_algos.keys()) // 2) * bar_width, all_algos[algo].values(), bar_width, label=algo)

    plt.xticks(X_axis, categories)
    plt.xlabel("Measurement")
    plt.ylabel("Quantity")
    plt.title(dir+" analysis")
    plt.legend(loc='upper center')
    plt.savefig(dir+'/images/Comparison.png')


        

if __name__=="__main__":
    main()