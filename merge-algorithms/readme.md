# Developed Merge Algorithms


### ImportMVP: 
* Does a slective Union of the import statements from all files keeps uncommon part of left and right and eliminates the uncommon part from the base, and runs git 3-way merge algoritm on the body of the code.

### ImportTree:
* Defined tree structure compares imports based on a path directories, instead of just characters.
* Again runs git 3-way merge algorithm on code body.

### ImportTreeNew:
* Works off structure of ImportTree.
* Rather than uioning the 3 versions, this uses the technique git does when comparing which version to use although at a higher level. 
    * Instead of comparing at a textual basis, it compares based on the path. 
### Compressed Tree:
* Works on a new structure based out of a tree where the main class, attribute or package is stored as end node and rest of the path as the starter node.
* The amazing thing about this approach is that the structure could be commonly utilised by different languages such as python and java
* Resolves import merge conflicts for both python as well as Java


### Script Pre-Requisites

* Run ImportMVP:

    `python3 mvp.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Run ImportTree:

    `python3 import-tree.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Run ImportTreeNew:

    `python3 import-tree-new.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

