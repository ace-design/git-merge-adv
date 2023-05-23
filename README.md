

<div align="center">
  <a href="https://github.com/ace-design">
    <img src="https://ace-design.github.io/img/logo.png">
  </a>
  <h1>Git Merge ADV</h1>
</div>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#project-description">Project Description</a>
        <ul>
        <li><a href="#Rationale">Rationale</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#script-prerequisites">Script Prerequisites</a></li>
      </ul>
    <li>
      <a href="#contacts">Contacts</a>
    </li>
    </li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
    </li>
    <li><a href="#licence">Licence</a></li>
  </ol>
</details>

## Project Description

### Rationale

Source code merging has always been an issue for developers. When colloborating on complex projects, developers must inevitably use multiple branches, which is the cause for numerous merge conflicts. These conflicts often waste a lot of their time. 

Existing 3-way merge algorithms developed by Git rely on textual difference, which often leads it to incorrectly resolve the conflicts or cause merge conflicts. 

Other 3-way merge algorithms such as JDime or Spork take a strucutred approach which use Abstract Syntax Trees, however are limited in the scope in which they can be used.

As such, our purpose is to develop a new 3-way merge algorithm that takes both a textual and strucutured approach.
* To do so, we will be focusing mainly on the syntax of Python and Java code for now.

### Built with:

* Python
* Java


## Getting Started

### Script Prerequisites

* Run ImportMVP:

    `python3 mvp.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Run ImportTree:

    `python3 import-tree.py --left <left parent path> --right <right parent path> --base <base file path> --out <output file>`

* Run Merge Conflict Script:

    `python3 init.pyt --url <Clone URL> --output <Output file name>`

* Run Conflict Resolving Script:

    `python3 init.py --filename <merge-conflicts.csv> --line <line-number in CSV> --repo <name of Clone repo>`


## Contacts

* Sebastien Mosser - mossers@mcmaster.ca
* Madhur Jain - 21112002mj@gmail.com
* Nirmal Chaudhari - chaudn12@mcmaster.ca


## Acknowledgements
* Parts of scripts obtained from [https://github.com/ace-design/git-corpus]
* All projects chosen from Java Awesome List: [https://github.com/akullpp/awesome-java]
* Cucumber Test Cases [https://github.com/cucumber/cucumber-jvm]
* TwelveMonkeys Test Cases [https://github.com/haraldk/TwelveMonkeys]
* OpenHTMLtoPDF Test cases [https://github.com/danfickle/openhtmltopdf]


## Licence
Distributed under the MIT License. See LICENSE.txt for more information.
