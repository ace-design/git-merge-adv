# Developed Merge Algorithms


### (1) ImportMVP: 
* Does a slective Union of the import statements from all files keeps uncommon part of left and right and eliminates the uncommon part from the base, and runs git 3-way merge algoritm on the body of the code.

### (2) ImportTree:
* Defined tree structure compares imports based on a path directories, instead of just characters.
* Again runs git 3-way merge algorithm on code body.

### (3) ImportTreeNew:
* Works off structure of ImportTree.
* Rather than uioning the 3 versions, this uses the technique git does when comparing which version to use although at a higher level. 
    * Instead of comparing at a textual basis, it compares based on the path. 

### (4) Abstract_import_tree:
* Functionally, works the same as ImportTreeNew.
* Refactored to make it open for extention (for other languages).
* Less coupling between languages, base merge algorithm is centralized in the tree.py file. 


### (5) CompressedTree:
* Works on a new structure based out of a tree where the main class, attribute or package is stored as end node and rest of the path as the starter node.
* The amazing thing about this approach is that the structure could be commonly utilised by different languages such as python and java
* Resolves import merge conflicts for both python as well as Java

### (6) MethodUnion:
* Import merge works the same way as CompressedTree, however body is merged using our heuristics as well.
* Running git merge on methods/segments where our heuristic doesn't work. 
* Import merge and body merge run the same heuristics. 
* Uses tree-sitter to generate the Java CST, and Python AST to generate the AST for Python code. These are then queried and restructured into our abstract data structure.

