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

### 5. Set up configuration
Edit the configuration and under environment variables, set `LEFT_COMMIT` to the respective left
commit and set `RIGHT_COMMIT` to the respective right commit. In the future we will replace this
with command line arguments.

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

Alternatively, this can be run from the command line by creating a separate Java program that
calls RefMerge and passes the left and right commit hashes in.

## Adding new refactoring types

Adding a new refactoring type can be broken down into the following steps. 

### 1. Create refactoring object

* Create a new `RefactoringObject` in the refactoringObjects folder. For example, when 
adding extract method, we create `ExtractMethodObject`. 

* Add it to `RefactoringObjectUtils` to create it from the RefMiner object.

### 2. Determine order and transitivity logic

* Figure out which order the new refactoring type should be undone/replayed in. For example, 
when we add extract method, extract method should be undone after rename class and 
rename method.

* Add it to the `RefactoringOrder` enum.

* Add the associated logic cells. The logic cells will be the combination of the new refactoring
type and the currently covered types. When adding extract method, that gives us the extract method/
rename method, extract method/rename class, and extract method/extract method.

* Determine if there is transitivity or if the refactorings can be combined and add the logic. 
Extract method and rename method can have transitivity or be combined while extract method
and rename class can only be combined. 

* Add the logic to the cells and update the refactoring objects as transitivity/combinations are
found.

### 3. Determine conflict and dependence logic

* Figure out how the new refactoring type can conflict or have dependence with a refactoring on
a different branch. For example, extract method and rename method can conflict if the extracted 
method on the left branch has the same name as the renamed method on the right branch in the same
class. They can also conflict if the extracted method and renamed method have the same signature
in classes with an inheritance relationship.

* Add each logic check to the associated logic cell.

### 4. Update logic matrix 

* Add the new refactoring type to the dispatcher and receiver hashmaps. 

* Add the new refactoring type to the vector in `Matrix.getRefactoringValue()`. Make sure to add
it as the last entry to the vector.

### 5. Programmatically revert the refactoring

* Figure out which refactoring processor to use to undo the refactoring. For extract method, it's
`InlineMethodProcessor` and rename method is `RefactoringFactory` and `RenameRefactoring`.

* Use the information in your refactoring object to get the PSI elements necessary to perform
the refactoring.

### 6. Programmatically replay the refactoring

* In most cases the refactoring process to replay the refactoring will be the same. For extract
method, undoing the refactoring uses `InlineMethodProcessor` and replaying uses `ExtractMethodProcessor`.

* Use the information in your refactoring object to get the PSI elements necessary to perform
the refactoring.

## Replicating the refactoring-aware merging comparison findings

The evaluation and instructions for replicating can be found in 
`https://github.com/ualberta-smr/RefactoringAwareMerging`.
