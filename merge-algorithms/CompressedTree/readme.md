# Compressed Import Tree

* Works off structure of Abstract Import Tree.
* Tree strucutre is more compressed, to allow more focus on the end desired packages, instead of entire path. Less computation time. 



### Future Development:
* Improve accuracy of CompressedTree algorithm on existing case studies. 

### Script Pre-Requisites:

1. Clone tree-sitter Java repository in directory you are running the script from

    `git clone https://github.com/tree-sitter/tree-sitter-python`


### Execution:

* Run Compressed Import Tree:

    `python3 init.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file> --file <name of file>`


### Acknowledgements:

* py-tree-sitter: [https://github.com/tree-sitter/py-tree-sitter]