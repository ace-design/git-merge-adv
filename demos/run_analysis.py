import argparse
import matplotlib.pyplot as plt
import os
import math
import csv


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
    run_cs(algo,'.')
    # run_overall(algo)
    # compile_all(dir)


# def compile_all(dir):
#     comparison={}
#     for root,dirs,files in os.walk(dir+'/images/numerical_data'):
#         for file in files:
#             if ".txt" in file:
#                 with open(os.path.join(root,file),'r') as file:
#                     reader=csv.reader(file)
#                     for row in reader:
#                         print(row)

                    # lines=reader.readlines()
                    # for line in lines:
                    #     if "Overall" in line:
                    #         comparison[file.strip(".txt")]=float(line.split(":")[-1].strip('\n'))


    # xaxis=comparison.keys()
    # yaxis=comparison.values()

    # plt.bar(xaxis,yaxis)
    # plt.title("Comparison Results")
    # plt.xlabel('Measurement')
    # plt.ylabel('Quantity')
    # plt.savefig(dir+'/images/Comparison.png')
    # plt.clf()
    # plt.cla()
    # plt.close()    
    # print(comparison)


#Find all data associated with the desired algorithm in specified case studies path.
def run_cs(algo,dir):

    data={'deletions':0,'insertions':0,'moves':0,'diff_path':0,'num_conflicts':0,'Overall':0}

    for root,dirs,files in os.walk(dir):
        for file in files:
            if file==algo+".csv":
                with open(os.path.join(root,file),'r') as file:
                    reader=csv.reader(file)
                    next(reader)
                    for row in reader:
                        data['deletions']+=int(row[1])
                        data['insertions']+=int(row[2])
                        data['moves']+=int(row[3])
                        data['diff_path']+=int(row[4])
                        data['num_conflicts']+=int(row[5])
                    # lines=reader.readlines()
                    # for line in lines:
                    #     div=line.split(": ")
                    #     if (div[0] in data.keys()):
                    #         data[div[0]]+=float(div[1].strip('\n'))
    data['Overall']=math.sqrt(math.pow(data['deletions'],2)+math.pow(data['insertions'],2)+math.pow(data['moves'],2)+math.pow(data['diff_path'],2)+math.pow(data['num_conflicts'],2))
    print(dir+" results:\n")
    print(data)

    
    xaxis=data.keys()
    yaxis=data.values()

    colors = ['red', 'green', 'pink', 'yellow','purple','blue']

    plt.bar(xaxis,yaxis,color=colors)
    plt.title(algo+" Results")
    plt.xlabel('Measurement')
    plt.ylabel('Quantity')
    plt.savefig(dir+'/images/'+algo+'.png')
    plt.clf()
    plt.cla()
    plt.close()




        

if __name__=="__main__":
    main()