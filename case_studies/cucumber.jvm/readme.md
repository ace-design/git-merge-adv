# Cucumber-jvm

### Source: [https://github.com/cucumber/cucumber-jvm]


## Rationale:
* Project is mainly written in java, so it provides a good starting point to analyze some of the limitations of JDime and Spork. There is a greater Java conflict sample space.

### Good Import Conflicts:
 Case | Line | Commmit Hash | Git | Spork | JDime
--- | --- | --- | --- | --- | ---
C1| 83 | `817f9292d` | Conflict | Handled | Handled
C2 | 191 | `f601fc15d` | Conflict | Handled Incorrectly | Handled
C3 | 193 | `f601fc15d` | Conflict | Conflict & Incorrect | Handled (import)

### Interesting Cases:
 Case | Line | Commmit Hash | Odd Case
--- | --- | --- | --- |
I1 | 161 | `309895a13` | JDime: Entire code is conflict
I2 | 218 | `54265d5a8` | JDime: Entire code is conflict
I3 | 242 | `6803d2839` | JDime: Entire code is conflict
