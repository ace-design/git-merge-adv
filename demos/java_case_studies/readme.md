# Java Demos

## Rationale

Analysis of case studies from credible, and well-developed projects help us obtain quality results that we can use to further support our project development.

Our focus on Java will provide us with specific syntax that we can use to develop a lower level algorithm. 

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

