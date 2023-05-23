# Import MVP

* Minimal Viable Product for merging two files with a focus on the import statements.
* Similar to git's two way merge: Finds the longest commmon subsequences of imports, then compiles all differences and unions both into a new file. 
* Git's 3-way merge algorithm is used on the body of code.

### Script Pre-Requisites

* Run ImportMVP:

    `python3 mvp.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Test Run:

    `python3 mvp.py --left test/left.java --right test/right.java --base test/base.java --output result.java`


    * Test case files obtained from [https://github.com/cucumber/cucumber-jvm]
        * Commit Hash: 817f9292d
        * Left Parent Hash: cd2e0197
        * Right Parent Hash: 80df4b06
        * Base Hash: 632a8579
        * File Name: core/src/main/java/io/cucumber/core/plugin/TestNGFormatter.java