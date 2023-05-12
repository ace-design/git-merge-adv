import subprocess
import copy
import re


#Parts of code given by Richard Li

def identify_conflict(all_merges):

    copy_merges=copy.deepcopy(all_merges)

    for merge in all_merges:
        left=merge.get('left')
        right=merge.get('right')

        subprocess.run(f"git checkout {left}", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        out = subprocess.Popen(f"git merge --no-commit --no-ff {right}", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        output=[]

        for i in range(len(out.communicate())):
            output.append(out.communicate()[i].decode('utf-8').strip('\n'))

        subprocess.run("git merge --abort", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        subprocess.run("git checkout master", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        if 'Automatic merge went well; stopped before committing as requested' in output:
            copy_merges.remove(merge)
        else:
            messages=re.findall('Merge conflict in [a-zA-Z0-9]+.*', ' '.join(output))
            message=[sub.replace('Merge conflict in ','') for sub in messages]
            copy_merges.remove(merge)
            for inst in message:
                temp=copy.deepcopy(merge)
                temp['file']=inst
                copy_merges.append(temp)

    
    return copy_merges

