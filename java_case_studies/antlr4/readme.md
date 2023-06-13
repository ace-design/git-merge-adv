# Cucumber-jvm

### Source: [https://github.com/antlr/antlr4]


## Rationale:
* Project is mainly written in java, so it provides a good starting point to analyze some of the limitations of JDime and Spork. There is a greater Java conflict sample space.

### Good Import Conflicts:
 Case | Line | Commmit Hash | Git | Spork | JDime
--- | --- | --- | --- | --- | ---
C1| 774 | `2f902da3d` | Conflict | Handled | Handled
C2| 780 | `b14ca5644` | Conflict | Handled | Handled
C3| 781 | `b14ca5644` | Conflict | Handled | Handled
C4| 826 | `18f5354d1` | Conflict | Handled | Incorrect
C5| 842 | `aed26c690` | Conflict | Handled | Handled
C6| 845 | `c893f2af0` | Conflict | Handled | Incorrect
C7| 857 | `9ef612798` | Conflict | Incorrect | Incorrect
C8| 865 | `1a2094b2d` | Conflict | Incorrect | Incorrect



### Interesting Cases:
 Case | Line | Commmit Hash | Odd Case
--- | --- | --- | --- |