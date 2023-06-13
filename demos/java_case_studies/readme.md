# Java Demos

## Results

### CompressedTree Algorithm:

* 0 Deletions: 100% fewer than existing algorithms
    * 100% fewer than JDime and Spork.
* 68 insertions
    * 155.56% more than JDime and Spork
* 29 Moves: 
    * 33% more than JDime
    * 74% fewer than Spork
* 5 Different Paths
    * 150% more than JDime
    * 55% fewer than Spork

![My Image](images/CompressedTree.png)

### JDime Algorithm:

* 10 Deletions
* 21 insertions
* 9 Moves
* 4 Different Paths

![My Image](images/jdime.png)

### Spork Algorithm:

* 8 Deletions
* 21 insertions
* 86 Moves
* 11 Different Paths

![My Image](images/spork.png)


## Prerequisites:

* Deletions: Every package in desired version that isn't there in the resulting file.
* Insertions: Extra packages present in the result that are not there in the desired.
* Moves: Number of import statements in the result that are in a different spot from desired.
* Different Paths: Number of import statements in result that are from the wrong path.


## Acknowledgements:

Case Studies obtained from the following repositories:
* Cucumber-jvm: [https://github.com/cucumber/cucumber-jvm]
* TwelveMonkeys: [https://github.com/haraldk/TwelveMonkeys]

