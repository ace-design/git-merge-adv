# Java Demos

## Rationale:

Analysis of case studies from credible, and well-developed projects help us obtain quality results that we can use to further support our project development.

Our focus on Java will provide us with specific syntax that we can use to develop a lower level algorithm. 

## Results:

![Java Comparison](images/Comparison.png)

Tool | Deletions | Insertions | Moves | Diff Paths | Num Conflicts | Num Errors | Overall |
--- | --- | --- | --- |--- |--- |--- |--- |
CompressedTree | 484 | 864 | 517 | 129 | 134 | 0 | 1132.54 |
MethodUnion | 612 | 1312 | 659 | 103 | 67 | 0 | 1595.39 |
Spork | 1027 | 2208 | 1546 | 179 | 96 | 0 | 2891.60 |
jDime | 1133 | 1163 | 790 | 330 | 119 | 0 | 1839.41 |

## Pre-requisites:

* `base.java`: Common ancestor between left and right parents.
* `left.java`: Left parent of merge
* `right.java`: Right parent of merge
* `desired.java`: Version of file after user manually handled.
* `git.java`: Version of file after git algorithm tried to handle.
* `spork_result.java`: Version of file after spork tried to handle.
* `jdime.java`: Version of file after jdime tried to handle.


## Next Steps:

* Use the class identifier as the reference for all methods, instead of full class declaration.
* Selective union between methods with same name but signature (currently just unions all methods with slightly different signature).
* Work on ordering of methods and comments. General ordering is accurate for the most part, but large number of moves.
* Merging segments within methods (sequential sets). Currently out of our scope.

## Prerequisites:

* Deletions: Every package in desired version that isn't there in the resulting file.
* Insertions: Extra packages present in the result that are not there in the desired.
* Moves: Number of import statements in the result that are in a different spot from desired.
* Different Paths: Number of import statements in result that are from the wrong path.


### References and Acknowledgements:

* All projects chosen from Java Awesome List: [https://github.com/akullpp/awesome-java]

