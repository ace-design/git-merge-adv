# Developed Merge Algorithms


### ImportMVP: 
* Simply Unions the import statements from all files, and runs git 3-way merge algoritm on the body of the code.

### ImportTree:
* Defined tree structure compares imports based on a path directories, instead of just characters.
* Again runs git 3-way merge algorithm on code body.

### ImportTreeNew:
* Works off structure of ImportTree.
* Rather than uioning the 3 versions, this uses the technique git does when comparing which version to use although at a higher level. 
    * Instead of comparing at a textual basis, it compares based on the path. 


### Script Pre-Requisites

* Run ImportMVP:

    `python3 mvp.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Run ImportTree:

    `python3 import-tree.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Run ImportTreeNew:

    `python3 import-tree-new.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

