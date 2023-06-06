# Import Tree

* Defined Tree structure allows imports to be compared based on its path, rather than a character-by-character basis. 
    * Aligns more closely with the business logic. 


### Future Development:
* Traverse entire tree to see whether package of same name already exists before adding.
* Before adding package, check whether it is even used in the code body. Check for package substring in code base. 

### Script Pre-Requisites

* Run ImportMVP:

    `python3 init.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Test Run:

    `python3 init.py --left test/left.java --right test/right.java --base test/base.java --output result.java`

    * Test case files obtained from [https://github.com/cucumber/cucumber-jvm]
        * Commit Hash: 817f9292d
        * Left Parent Hash: cd2e0197
        * Right Parent Hash: 80df4b06
        * Base Hash: 632a8579
        * File Name: core/src/main/java/io/cucumber/core/plugin/TestNGFormatter.java
