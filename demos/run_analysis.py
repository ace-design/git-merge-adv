import argparse
import matplotlib.pyplot as plt
import os
import math


def parsing():
    parser = argparse.ArgumentParser(description='Enter which algorithm to analyze:')
    parser.add_argument('--algo', type=str, help="Path to case study folder")
    parser.add_argument('--dir',type=str,help="Enter path to case studies you want to analyze")
    args = parser.parse_args()
    return args


def main():
    algo=parsing().algo
    dir=parsing().dir

    run_cs(algo,dir)
    run_overall(algo)


#Find all data associated with the desired algorithm in specified case studies path.
def run_cs(algo,dir):

    data={'deletions':0,'insertions':0,'moves':0,'diff_path':0,'Overall':0}

    for root,dirs,files in os.walk(dir):
        for file in files:
            if file==algo+"_diff.txt":
                with open(os.path.join(root,file),'r') as reader:
                    lines=reader.readlines()
                    for line in lines:
                        div=line.split(": ")
                        if (div[0] in data.keys()):
                            data[div[0]]+=float(div[1].strip('\n'))
    data['Overall']=math.sqrt(math.pow(data['deletions'],2)+math.pow(data['insertions'],2)+math.pow(data['moves'],2)+math.pow(data['diff_path'],2))
    print(dir+" results:\n")
    print(data)
    
    xaxis=data.keys()
    yaxis=data.values()

    colors = ['red', 'green', 'pink', 'yellow','blue']

    plt.bar(xaxis,yaxis,color=colors)
    plt.title(algo+" Results")
    plt.xlabel('Measurement')
    plt.ylabel('Quantity')
    plt.savefig(dir+'/images/'+algo+'.png')

# Find data associated with algorithm accross all case studies in demo.
def run_overall(algo):

    print("Overall Results: \n")
    total={'deletions':0,'insertions':0,'moves':0,'diff_path':0,'Overall':0}
    for root,dirs,files in os.walk('.'):
        for file in files:
            if file==algo+"_diff.txt":
                with open(os.path.join(root,file),'r') as reader:
                    lines=reader.readlines()
                    for line in lines:
                        div=line.split(": ")
                        if (div[0] in total.keys()):
                            total[div[0]]+=float(div[1].strip('\n'))

    total['Overall']=math.sqrt(math.pow(total['deletions'],2)+math.pow(total['insertions'],2)+math.pow(total['moves'],2)+math.pow(total['diff_path'],2))
    print(total)
    xaxis=total.keys()
    yaxis=total.values()

    colors = ['red', 'green', 'pink', 'yellow','blue']

    plt.bar(xaxis,yaxis,color=colors)
    plt.title(algo+" Results")
    plt.xlabel('Measurement')
    plt.ylabel('Quantity')
    plt.savefig('images/'+algo+'.png')


        

if __name__=="__main__":
    main()