# Import Tree New

* Works off structure of ImportTree.
* Rather than uioning the 3 versions, this uses the technique git does when comparing which version to use although at a higher level. 
    * Instead of comparing at a textual basis, it compares based on the path. 



### Future Development:
* Develop a better algorithm to determine which file version to use for import statements.

### Script Pre-Requisites

* Run ImportMVP:

    `python3 import-tree-new.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Test Run:

    `python3 import-tree-new.py --left test2/left.java --right test2/right.java --base test2/base.java --output result.java`

    * Test case files obtained from [https://github.com/cucumber/cucumber-jvm]
        * Commit Hash: 817f9292d
        * Left Parent Hash: cd2e0197
        * Right Parent Hash: 80df4b06
        * Base Hash: 632a8579
        * File Name: core/src/main/java/io/cucumber/core/plugin/TestNGFormatter.java
