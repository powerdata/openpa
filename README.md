Open Source Power Applications and Utilities.

## Contents
This project wraps <a href="https://github.com/powerdata/com.powerdata.openpa">powerdata/com.powerdata.openpa</a> to provide dependencies and a project file for use in Eclipse


## Notes on Cloning

This project contains the submodule project <a href="https://github.com/powerdata/com.powerdata.openpa">powerdata/com.powerdata.openpa</a>.

To clone this project with the submodules first clone this project as normal, then inside the working directory run:

    git submodule update --init
    
Now the submodule is also cloned, but not on any branch.  To make sure it is on the master branch do:
    
    git submodule foreach git checkout master
