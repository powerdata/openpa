<p><a href="http://incsys.com"><img src=http://incsys.com/images/IncSys_Logo_web.png></a></p>
Open Source Power Applications and Utilities.

## Notes on Cloning

This project contains the submodule project powerdata/com.powerdata.openpa.

To clone this project with the submodules first clone this project as normal, then inside the working directory run:

    git submodule update --init
    
Now the submodule is also cloned, but not on any branch.  To make sure it is on the master branch do:
    
    git submodule foreach git checkout master
