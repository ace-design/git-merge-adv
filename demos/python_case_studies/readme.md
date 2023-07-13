# Python Demos

## Rationale

Analysis of case studies from credible, and well-developed projects help us obtain quality results that we can use to further support our project development.

Our secondary focus on Python will provide us with specific syntax that we can use to develop a slightly higher level algorithm than our previous Java one. 

## Pre-requisites:

*  `base.java`: Common ancestor between left and right parents.
*  `left.java`: Left parent of merge
* `right.java`: Right parent of merge
* `desired.java`: Version of file after user manually handled.
*  `git.java`: Version of file after git algorithm tried to handle.

## Results

* Note: Formatting still needs to be fixed. Gumtree is inaccurate because of formattting, so results need to be updated after it is fixed. 

### CompressedTree Algorithm:

* 6 Deletions: Every package in desired version is present in the result.
* 20 insertions: Extra packages present in the result that are not there in the desired.
* 103 Moves: Number of import statements in the result that are in a different spot from desired.
* 5 Different Paths: Number of import statements in result that are from the wrong path.

![My Image](images/CompressedTree.png)


### References and Acknowledgements:

* All projects chosen from Python Awesome List: [https://github.com/vinta/awesome-python]

