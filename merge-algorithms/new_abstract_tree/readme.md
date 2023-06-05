# Compressed Import Tree

* Works off structure of Abstract Import Tree.
* Tree strucutre is more compressed, to allow more focus on the end desired packages, instead of entire path. Less computation time. 



### Future Development:
* Reorganize code, and make it more readable. 

### Script Pre-Requisites

* Run ImportMVP:

    `python3 init.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Test Run:

    `python3 init.py --left test2/left.java --right test2/right.java --base test2/base.java --output result.java`

    * Test case files obtained from [https://github.com/cucumber/cucumber-jvm]
        * Commit Hash: 817f9292d
        * Left Parent Hash: cd2e0197
        * Right Parent Hash: 80df4b06
        * Base Hash: 632a8579
        * File Name: core/src/main/java/io/cucumber/core/plugin/TestNGFormatter.java
