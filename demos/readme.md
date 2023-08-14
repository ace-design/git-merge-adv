# Algorithm Demos

## Rationale

In order to better understand to strengths and limitations of our algorithms, it is necessary for us to perform tests as "demos".
* All case studies are extracted from a curated list of well-known projects obtained from the Awesome Python and Java lists.

## Results

### Java Results

![Java Comparison](java_case_studies/images/Comparison.png)

Tool | Deletions | Insertions | Moves | Diff Paths | Num Conflicts | Num Errors | Overall |
--- | --- | --- | --- |--- |--- |--- |--- |
CompressedTree | 484 | 864 | 517 | 129 | 134 | 0 | 1132.54 |
MethodUnion | 612 | 1312 | 659 | 103 | 67 | 0 | 1595.39 |
Spork | 1027 | 2208 | 1546 | 179 | 96 | 0 | 2891.60 |
jDime | 1133 | 1163 | 790 | 330 | 119 | 0 | 1839.41 |



### Python Results

![Python Comparison](python_case_studies/images/Comparison.png)

Tool | Deletions | Insertions | Moves | Diff Paths | Num Conflicts | Num Errors | Overall |
--- | --- | --- | --- |--- |--- |--- |--- |
CompressedTree | 383 | 350 | 301 | 170 | 100 | 0 | 631.42 |
MethodUnion | 714 | 602 | 452 | 504 | 93 | 1 | 1158.05 |


## Prerequisites

### &nbsp; Script Commands:

&nbsp; &nbsp; <u> run_algo.py: </u>

&nbsp; &nbsp; &nbsp; Purpose:
* To run specified case studies using desired tool. Can run one, or multiple case studies. 
* Outputs result of each case study to [Python demo_result](python_case_studies/demo_results/) or Java [Java demo_result](java_case_studies/demo_results/) folder under specified tool. 
* All quantitative results will be added to the associated .csv files for each tool under demo_result.

&nbsp; &nbsp; &nbsp; Execution: 
```
python3 <path to run_algo.py>/run_algo.py --cs <path to case study folder> --algo <name of desired algorithm> --lang <language used in case study> --purpose <all or spec>
```

&nbsp; &nbsp; &nbsp;Flags:
* cs: Enter the path to desired case study. Can be parent directory (purpose is to run all), or specific case study directory (purpose is to run spec)
* algo: Enter the name of desired Tool (MethodUnion, CompressedTree, etc...)
* lang: Enter the language for the case study
* purpose: Specify whether you want to run multiple case studies (all) or just one (spec)



&nbsp; &nbsp; <u>run_analysis.py: </u>

&nbsp; &nbsp; &nbsp; Purpose:
* To output a graph/visual comparison between all tools that were run. 
* Graph will be outputted to [Python Images](python_case_studies/images/Comparison.png) or [Java Images](java_case_studies/images/Comparison.png) depending on the language.
* Script will also output all numerical data in terminal.

&nbsp; &nbsp; &nbsp; Execution:
```
python3 run_analysis.py --dir <path to case study>
```

&nbsp; &nbsp; &nbsp; Flags:
* dir: Enter the path to the directory 

### &nbsp; Gumtree Commands:

&nbsp; &nbsp; Python Execution:

&nbsp; &nbsp; &nbsp; Import Differencing:

```
java -jar gumtree.jar -m theta webdiff <Path to Desired Version> <Path to Generated Version>
```
&nbsp; &nbsp; &nbsp; Body Differencing:

```
java -jar gumtree.jar -m gumtree-simple-id webdiff <Path to Desired Version> <Path to Generated Version>
```

* Important Note: When trying to use Gumtree for python files, make sure you add the pythonparser to your local computer path from [https://github.com/GumTreeDiff/pythonparser]

    1. Clone the repository
    2. cd pythonparser/
    3. pip install -r requirements.txt
    4. cp pythonparser /tmp
    5. PATH=$PATH:/tmp

&nbsp; &nbsp; Python Execution:

```
java -jar gumtree.jar -m gumtree-simple-id -g java-jdt webdiff <Path to Desired Version> <Path to Generated Version>
```


## Acknowledgements

* All quantitative measurements, and visuals obtained from [https://github.com/GumTreeDiff/gumtree]

