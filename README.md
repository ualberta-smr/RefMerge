# RefMerge

This project is a refactoring-aware merging IntelliJ plugin. 

## System requirements
* Linux
* git
* Java 8
* IntelliJ (Community Addition) Version 2020.1.2

## How to run

### 1. Create a temp directory in user.home
Go to your user.home (/home/username) and add a temporary director "temp". 
The base commit and changes to the right commit will be saved here.

For example, my temp is located at /home/max/temp.

### 2. Clone and build RefactoringMiner 
Use `Git Clone https://github.com/tsantalis/RefactoringMiner.git` to clone RefactoringMiner. 
Then build RefactoringMiner with `./gradlew distzip`. It will be under build/distributions.

### 3. Add RefactoringMiner to your local maven repository
You will need to add RefactoringMiner to your local maven repository to
use it in the build.gradle. You can use `mvn install:install-file -Dfile=<path-to-file>`
to add it to your local maven repository. You can verify that it's been installed 
by checking the path `/home/username/.m2/repository/org/refactoringminer`.

### 4. Build the project
Click on build tab in the IntelliJ IDE and select `Build Project`.

### 5. Run the plugin
Click `Run 'Plugin'` or `Debug 'Plugin'`. When it's running, click the `Tools` tab and select
`RunRefMerge`. This will run the plugin. The plugin will do everything else.
The matrix will print true/false for each handled operation, then the plugin will checkout 
the base commit and copy it to `home/username/temp/base`. After this, it checks
out the right commit and undoes handled refactorings. The content gets copied to 
`home/username/temp/right`. The same happens for the left commit, however the project
directory gets used for this one. When it merges, the merged content is saved in the project directory.
Lastly, the refactorings are replayed in the project directory. When it finishes, you can
look in the project directory to see the results.



## Additional information 

### Files

#### RefMerge
RefMerge is the main file. It starts by calling `Matrix`, which checks
for conflicting refactorings and ordering dependencies. After it finishes
this, it checks out the base commit and saves it to `temp/base`. Then
it checks out the right commit, undoes the detected rename method and
rename class refactorings, and saves it to `temp/right`.  It does the same
thing with the left commit, but it does uses the project directory to store
the changes. 

All of the refactorings that are handled should have been undone at this
point so it calls `Merge`. This performs git merge for all files that are in the 
three directories.

Lastly, it replays all of the refactorings on the merged files in the project
directory.

#### Matrix
This checks that two lists of refactorings are not conflicting. More specifically,
It checks every combination of left refactoring and right refactorings and sees
if they have a naming or inheritance conflict. 

#### Merge
Merge gets a list of files for each directory and performs `git merge-file`
on them. It does not handle additions or deletions. 

#### ReplayRefactorings
This contains all of the methods used to replay refactorings.

#### UndoRefactorings
UndoRefactorings contains all of the methods used to undo the refactorings.

#### GitUtils
GitUtils contains the methods that use git, such as resetting and checking out.

#### Utils
Utils currently has two methods. `runSystemCommand` to run things such as
`cp -r ...`, and `saveContents` to copy files from one directory to another.

### State of the project

rename class and rename method are pretty much fully implemented. There's an
additional check in the matrix that I was going to make but I wasn't able
to. If the names of the methods are the same, in the same class, then
we need to check if the signatures are the same. If they are, then
we can report that information ot the user. If they aren't, then the were
or are now overloaded and we can report that as well. 

### Issues

#### Rename Class Prompt
The only issue right now is that when you perform a class refactoring,
IntelliJ prompts you with a preview asking if it's what you want to do.
I'm not sure how I missed it initially but the plugin continues while
the prompt is up and by the time you click `ok`, there are other changes
that were made. Looking through the forum makes it sound like there's 
no way to disable this prompt, although if you press `alt+D` it accepts
the refactoring. So we should be able to send the `alt+D` signal after we
perform the refactoring. 

There's not much documentation for the IntelliJ API so there may be 
other issues that I'm not aware of, but at this point it seems to work
fine. If you have any questions about the API or implementation, don't hesitate
to send me an email at mjellis at ualberta.ca and I'll do my best to answer your 
questions.

