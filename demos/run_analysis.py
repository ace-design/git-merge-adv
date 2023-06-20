import argparse
import matplotlib.pyplot as plt
import os


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

    data={'deletions':0,'insertions':0,'moves':0,'diff_path':0}

    for root,dirs,files in os.walk(dir):
        for file in files:
            if file==algo+"_diff.txt":
                with open(os.path.join(root,file),'r') as reader:
                    lines=reader.readlines()
                    for line in lines:
                        div=line.split(": ")
                        data[div[0]]+=int(div[1].strip('\n'))

    print(dir+" results:\n")
    print(data)
    
    xaxis=data.keys()
    yaxis=data.values()

    colors = ['red', 'green', 'pink', 'yellow']

    plt.bar(xaxis,yaxis,color=colors)
    plt.title(algo+" Results")
    plt.xlabel('Measurement')
    plt.ylabel('Quantity')
    plt.savefig(dir+'/images/'+algo+'.png')

# Find data associated with algorithm accross all case studies in demo.
def run_overall(algo):

    print("Overall Results: \n")
    total={'deletions':0,'insertions':0,'moves':0,'diff_path':0}
    for root,dirs,files in os.walk('.'):
        for file in files:
            if file==algo+"_diff.txt":
                with open(os.path.join(root,file),'r') as reader:
                    lines=reader.readlines()
                    for line in lines:
                        div=line.split(": ")
                        total[div[0]]+=int(div[1].strip('\n'))

    print(total)
    xaxis=total.keys()
    yaxis=total.values()

    colors = ['red', 'green', 'pink', 'yellow']

    plt.bar(xaxis,yaxis,color=colors)
    plt.title(algo+" Results")
    plt.xlabel('Measurement')
    plt.ylabel('Quantity')
    plt.savefig('images/'+algo+'.png')


        

if __name__=="__main__":
    main()