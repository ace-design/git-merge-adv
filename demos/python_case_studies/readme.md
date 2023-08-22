# Python Demos

## Rationale

Analysis of case studies from credible, and well-developed projects help us obtain quality results that we can use to further support our project development.

Our secondary focus on Python will provide us with specific syntax that we can use to develop a slightly higher level algorithm than our previous Java one. 

## Results

### Quantitative Results

![CompressedTree vs MethodUnion](images/Comparison.png)

Tool | Deletions | Insertions | Moves | Diff Paths | Num Conflicts | Num Errors | Overall |
--- | --- | --- | --- |--- |--- |--- |--- |
CompressedTree | 383 | 350 | 301 | 170 | 100 | 0 | 631.42 |
MethodUnion | 714 | 602 | 452 | 504 | 93 | 1 | 1158.05 |

### Interesting Cases

Case Study | Overall | Description |
--- | --- | --- |
cryptography/importC2 | 222.46 | Right version compiles all test classes from Left and Base version into a single class. Consequently, since our generated version unions all unique methods, it combines everything from Left, Base and Right versions of the file. This creates many insertions. Also, the AST library auto formats the codes given, which makes the generated version have a lot of syntax differences. These two things make it have a high overall score. |
blaze/importC1b | 29.91 | Another interesting case where Python's AST library auto formats the given code. Causes our generated version to have certain syntax differences from the desired version. This is also what causes the relatively large overall score.  |
blaze/importC1a | 54.59 | Right version has imported package 'data' whereas the left and base version import the same package as 'Data'. As expected, the desired version uses 'data'. Since our tool recognizes 'Data' and 'data' as the same package, the imported package uses 'Data' which is technically incorrect. Also, throughout the code both 'data' and 'Data' are used simultaneously which is also incorrect. |
algorithms/importC2 | 72.24 | Another case where the AST library changes the formatting for method declarations. This is primarly what causes the large overall score of 72.24. However, the some of it is also attributed to the fact that we don't retain comments yet for the Python merge code. |
simplecv/importC1 | 88.96 | Case study illustrates the limitations of our tool on super classes. In this case study, there is one giant super class with numerous methods. When merging, our tool has methods that are in a different order when compared to the desired version. Some of this is because of comments that are missing (which messes up the order). May need to come up with a better way to retain the original ordering of code for the generated version. |

## Next Steps

* Formatting still needs to be fixed. Gumtree is inaccurate because of formattting, so results need to be updated after it is fixed. 
* Include comments in the merged files who's body use our tool.
* Use existing Field Class for class fields, rather than strings.
* Extend python merging to encompass more than strictly classes and methods. Reduce how much Git is used for the body.

### References and Acknowledgements:

* All projects chosen from Python Awesome List: [https://github.com/vinta/awesome-python]
* Code Differencing results obtained from Gumtree: [https://github.com/GumTreeDiff/gumtree]

