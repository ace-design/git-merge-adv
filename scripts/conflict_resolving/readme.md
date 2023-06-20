# Purpose of Script
This script will automatically extract the left , right and base version of a file for a given merge conflict for a given repository. This will then run the automerge tools such as spork and Jdime(intended for Java files) on the extracted files and give out the results. This will also extract the resolution of the merge conflict that was actually done by developer hence provinding different versions of same file to compare and analyse the merge confilcts and the merge algorithms available.


## Pre-requisites

 - Clone the repository in the current folder
 - Copy the `<merge-conflicts.csv>` file from the `../merge-conflicts` folder into current directory
 - Run `python3 init.py --filename <merge-conflicts.csv> --line <line-number in CSV> --repo <name of Clone repo> --lang <Programming Language>`


## References and Acknowledgement

Jar file for spork has been taken from [https://github.com/KTH/spork](https://github.com/KTH/spork)
