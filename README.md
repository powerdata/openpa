<p><a href="http://incsys.com"><img src=http://incsys.com/images/IncSys_Logo_web.png></a></p>
Open Source Power Applications and Utilities.

## Contents

### Decoupled PowerFlow

The Decoupled Power Flow is written in Visual Basic embeded in an Excel Spreadsheet.  It has the following objectives:
* To provide a relatively simple set of code for solving the sparse matrix decoupled power flow equations
* To provide a template so that professional software developers can build production scalable software in robust languages including C++ and Java
* To provide a model so that power system researchers have a tool to use.
* For use in training power system operators

## Notes on Cloning

This project contains the submodule project <a href="https://github.com/powerdata/com.powerdata.openpa">powerdata/com.powerdata.openpa</a>.

To clone this project with the submodules first clone this project as normal, then inside the working directory run:

    git submodule update --init
    
Now the submodule is also cloned, but not on any branch.  To make sure it is on the master branch do:
    
    git submodule foreach git checkout master
