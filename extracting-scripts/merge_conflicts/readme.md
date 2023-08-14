# Purpose of script

The purpose of this script to to obtain a list of commits referencing merge conflicts in a github repository. The returning csv file will contain the commit hash, left parent hash, right parent hash, ancestor parent hash and the conflicting file. 

## Pre-requisites

* Run the script: 

    `python3 init.pyt --url <Clone URL> --output <Output file name>`