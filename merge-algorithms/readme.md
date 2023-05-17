# Developed Merge Algorithms


### ImportMVP:
* Minimal Viable Product for merging two files with a focus on the import statements.
* Similar to git's two way merge: Finds the longest commmon subsequences of imports, then compiles all differences and unions both into a new file. 
* Git's 3-way merge algorithm is used on the body of code.

### Script Pre-Requisites

* Run ImportMVP:

    `python3 mvp.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`