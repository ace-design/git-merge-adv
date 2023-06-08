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
    data={'deletions':0,'insertions':0,'moves':0,'diff_path':0}

    for root,dirs,files in os.walk(dir):
        for file in files:
            if file=="CompressedTree_diff.txt":
                with open(os.path.join(root,file),'r') as reader:
                    lines=reader.readlines()
                    for line in lines:
                        div=line.split(": ")
                        data[div[0]]+=int(div[1].strip('\n'))

    print(data)
    
    xaxis=data.keys()
    yaxis=data.values()

    colors = ['green', 'blue', 'purple', 'brown']

    plt.bar(xaxis,yaxis,color=colors)
    plt.title(algo+" Results")
    plt.xlabel('Measurement')
    plt.ylabel('Quantity')
    plt.show()




    # print(data)
        

if __name__=="__main__":
    main()