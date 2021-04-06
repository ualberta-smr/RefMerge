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

### 3. Add a dispatcher class 

Although the design for the logic matrix is similar to visitor pattern, it does not follow
an existing design pattern. Each time we add a new refactoring type, we need to add a new
dispatcher class that extends `RefactoringDispatcher`. We need to override `dispatch()`
in the new dispatcher class. This is everything that needs to be done in this step!

### 4. Update receiver superclass

Next, update `Receiver` by adding a new `receive()` method with the dispatcher class that you
just added as the parameter. This will allow us to implement the new receive method
in our new receiver and future receivers. Do not add anything else to the receiver class.

### 5. Add a receiver class

Now we can add the receiver for our new refactoring. Create `myRefactoringReceiver` class
and override the `receive()` method for each refactoring dispatcher. 

### 6. Add corresponding logic cells

We need to add the logic cell that performs the logic for each `receive()`. Add a cell
for each logic check in the logicCells package. 

### 7. Update the matrix class

There are three small updates that need to be made to `Matrix.java`. For the first two, add
the new dispatcher and receiver to the corresponding `dispatcherMap` and `receiverMap`.
Lastly, add the `vector.add(RefactoringType.myNewType)` to `Matrix.getRefactoringValue`.
