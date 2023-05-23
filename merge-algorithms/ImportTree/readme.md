# Import Tree

* Defined Tree structure allows imports to be compared based on its path, rather than a character-by-character basis. 
    * Aligns more closely with the business logic. 
* 

### Script Pre-Requisites

* Run ImportMVP:

    `python3 import-tree.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Test Run:

    `python3 import-tree.py --left test/left.java --right test/right.java --base test/base.java --output result.java`