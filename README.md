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

## Adding new refactoring types

Adding a new refactoring type can be broken down into the following steps. 

### 1. Programmatically revert the refactoring

In the first step, we use the IntelliJ refactoring API to revert the refactoring.
Start by updating the `RefactoringComparator` class. You 
only need to update the `refactoringTypeMap` by adding the line `put(myRefactoringType,
 x)` to the map where myRefactoringType is the new refactoring type and x is the 
 associated ordering value. When we revert the refactoring we need to make sure there 
 is not a refactoring that can be performed in the same commit that the new refactoring
 depends on. For example, rename method needs to happen after rename class when we revert
 the refactorings.
 
 After the refactoring has been added to the refactoring comparator, we need to revert
 the refactoring by performing a new refactoring on it. We revert the refactoring in 
 `UndoOperations`. We need to get the relevant data from RefactoringMiner, then we need
 to use the relevant refactoring processor.

### 2. Programmatically replay the refactoring

Next, we add the code to replay the refactoring since we should have a good idea how to
after reverting it. We use the `ReplayOperations` class to do this. Update the 
`undoOperations()` and `replayOperations()` methods in `RefMerge` to make sure the methods
are called. 

### 3. Add an element class 

We need to add a new element class, so we can dispatch to the correct cell in the logic
matrix. This class will extend `RefactoringElement` and even though it may be tempting
 not to, we need to override `accept()` in the new class. We need to add a method in this class for 
 each existing visitor as well as the corresponding visitor for this refactoring that 
 each visitor will dispatch to. 

### 4. Update visitor superclass and existing classes

Next, update `RefactoringVisitor` by adding a new `visit(myRefactoringElement)` method 
where myRefactoringElement is your new element class. You should not add anything else 
to the superclass. You also need to update each of the existing visitors to visit your
new element class. Override your `visit(myRefactoringElement)` method to dispatch to the
correct method in myRefactoringElement.

### 5. Add a visitor class

We need to add a new visitor class that extends `RefactoringVisitor`. The new visitor class
will only have a `visit()` method for the new refactoring element. 

### 6. Add corresponding logic cells

We need to add the cells that perform the refactoring conflict and dependence logic. Add
a new cell for each comparison in the logicCells package.
