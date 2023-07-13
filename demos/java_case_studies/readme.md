# Java Demos

## Rationale

Analysis of case studies from credible, and well-developed projects help us obtain quality results that we can use to further support our project development.

Our focus on Java will provide us with specific syntax that we can use to develop a lower level algorithm. 

## Pre-requisites:

*  `base.java`: Common ancestor between left and right parents.
*  `left.java`: Left parent of merge
* `right.java`: Right parent of merge
* `desired.java`: Version of file after user manually handled.
*  `git.java`: Version of file after git algorithm tried to handle.
* `spork_result.java`: Version of file after spork tried to handle.
* `jdime.java`: Version of file after jdime tried to handle.

## Results

### CompressedTree Algorithm:

* 7 Deletions
* 46 insertions
* 43 Moves
* 5 Different Paths

![My Image](images/CompressedTree.png)

### JDime Algorithm:

* 15 Deletions
* 24 insertions
* 9 Moves
* 1 Different Paths

![My Image](images/jdime.png)

### Spork Algorithm:

* 13 Deletions
* 24 insertions
* 88 Moves
* 0 Different Paths

![My Image](images/spork.png)


## Prerequisites:

* Deletions: Every package in desired version that isn't there in the resulting file.
* Insertions: Extra packages present in the result that are not there in the desired.
* Moves: Number of import statements in the result that are in a different spot from desired.
* Different Paths: Number of import statements in result that are from the wrong path.


### References and Acknowledgements:

* All projects chosen from Java Awesome List: [https://github.com/akullpp/awesome-java]

