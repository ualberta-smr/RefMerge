# RefMerge

RefMerge is a refactoring-aware merging IntelliJ plugin. RefMerge relies on [RefactoringMiner](https://github.com/tsantalis/RefactoringMiner) to detect refactorings and works by undoing refactorings, merging the resulting code, and then replaying the refactorings. More in-depth details about the technique can be found in "Refactoring-aware Operation-based Merging: An Empirical Evaluation" by Max Ellis, Sarah Nadi, and Danny Dig , available on [arXiv](http://arxiv.org/abs/2112.10370).

If you are looking for how to run an evaluation using RefMerge, or how we compared it to IntelliMerge, please checkour [our evaluation repo](https://github.com/ualberta-smr/RefactoringAwareMerging)

## Refactoring Conflict & Simplification Logic

We consider the interactions between each pair of refactorings and how these interactions can lead to a conflict or how they can result in a dependence relationship. We provide the conflict and dependence detection logic for each pair in the [conflict detection wiki](https://github.com/ualberta-smr/RefMerge/wiki/Conflict-&-Dependence-Logic). 

## Running RefMerge

### System requirements

* Linux
* git
* Java 8
* IntelliJ (Community Addition) Version 2020.1.2

### How to run

1. `git clone https://github.com/tsantalis/RefactoringMiner.git` to clone RefactoringMiner. Then build RefactoringMiner with `./gradlew distzip`. It will be under build/distributions.

2. Add RefactoringMiner to your local maven repository. You will need to add RefactoringMiner to your local maven repository to
use it in the build.gradle. You can use `mvn install:install-file -Dfile=<path-to-file>` to add it to your local maven repository. You can verify that it's been installed by checking the path `/home/username/.m2/repository/org/refactoringminer`.

3. Build the project. Click on build tab in the IntelliJ IDE and select `Build Project` to build RefMerge.

4. Set up configuration
Edit the configuration and under environment variables, set `LEFT_COMMIT` to the respective left
commit and set `RIGHT_COMMIT` to the respective right commit. RefMerge will merge the two commits provided for a single merge scenario. This works for any merge scenario in history. We need to add the commits in the configuration under environment variables because RefMerge is currently designed as `AnAction` which cannot retrieve command line variables. This works when running the evaluation pipeline because an `ApplicationStarter` calls RefMerge supplying the commits.


5. Run the plugin. Click `Run 'Plugin'` or `Debug 'Plugin'`. When the plug is running, click the `Tools` tab and select `RunRefMerge`. This will run RefMerge, then performs the following steps: 

	1. Detects refactorings on the left and right branch using RefactoringMiner.
	
	2. Checks the detected refactorings for transitive relationships and refactoring chains and simplifies them (Check our [Simplification Logic Wiki Page](https://github.com/ualberta-smr/RefMerge/wiki/Simplification-Logic) for details)
	
	3. Inverts the refactorings in the left and right parent commits.
	
	4. Textually merges the branches with git.
	
	5. Detects refactoring conflicts and refactoring relationships between the left and right branches (Check our [Conflict and Dependence Logic Wiki page](https://github.com/ualberta-smr/RefMerge/wiki/Conflict-&-Dependence-Logic) for details).
	
	6. Adds conflicting refactorings to a conflicting refactoring list and prints conflicting refactoring pairs.
	
	7. Replays the non-conflicting refactorings on the textually merged code. 


## Adding new refactoring types

RefMerge currently supports 17 refactorings (listed [here](todo)). We describe here how you can add new refactoring types to RefMerge to extend it.

Adding a new refactoring type (assuming it is called `NewRefactoring`) can be broken down into the following steps. 

1. Create a new `NewRefactoringObject.java` class that implements `RefactoringObject` in the `src/main/java/ca/ualberta/cs/smr/refMerge/refactoringObjects` package. For example, when 
adding extract method, we created `ExtractMethodObject` which implements `RefactoringObject`. The `RefactoringObject` class will store all of the necessary information about the refactored program element, such as its signature.

2. Add a new `case` for the new refactoring object to `utils/RefactoringObjectUtils.java` to enable its creation. See [the `createRefactoringObject` method](https://github.com/ualberta-smr/RefMerge/blob/d78250fc80405cb3c07106bc62a42ac9bbde1c15/src/main/java/ca/ualberta/cs/smr/refmerge/utils/RefactoringObjectUtils.java#L14)

3. Figure out which order the new refactoring type should be undone/replayed in. For example, when we add extract method, extract method should be undone after rename class and rename method. Once you decide on the logic, do the following:

	1. Add the new `NewRefactoringObject` class to the `RefactoringOrder` enum in `src/main/java/ca/ualberta/cs/smr/refmergre/refactoringObjects/RefactoringOrder.java`. The `NewRefactoringObject ` class needs to be put in its respective position in the top-down order (i.e., variables < methods < classes such that variable refactorings are done before method refactorings and method refactorings will be done before class refactorings). For example, `Move Method` will come before `Rename Variable` and after `Move Class`.
	
	2. Add the associated logic for each combination of the new refactoring
	type and the currently covered types. for example, when adding extract method, that gives us the extract method/
	rename method, extract method/rename class, and extract method/extract method. All of the existing simplification logic can be found in the [simplification logic wiki](https://github.com/ualberta-smr/RefMerge/wiki/Simplification-Logic).
	
	3. Determine if there is transitivity or if the refactorings can be combined and add the logic. For example, Extract method and rename method can have transitivity and be combined while extract method and rename class can only be combined. 
	
	* Add the logic to the cells and update the refactoring objects as transitivity/combinations are
	found.

4. Determine conflict and dependence logic. Figure out how the new refactoring type can conflict or have dependence with a refactoring on
a different branch. For example, extract method and rename method can conflict if the extracted method on the left branch has the same name as the renamed method on the right branch in the same class. They can also conflict if the extracted method and renamed method have the same signature
in classes with an inheritance relationship. Add each logic check to the associated logic "cell" in `src/main/java/ca/ualberta/cs/smr/refmerge/matrix/logicCells`. For example, if we add `Rename Variable`, 
we would create `RenameVariableRenameVariableCell.java` with the conflict and dependence logic. We document our currently implemented conflict logic in the [Conflict-&-Dependence-Logic wiki](https://github.com/ualberta-smr/RefMerge/wiki/Conflict-&-Dependence-Logic) 

5. Update logic matrix 

	1. Add the new refactoring type to the dispatcher and receiver hashmaps. 
	
	2. Add the new refactoring type to the vector in `Matrix.getRefactoringValue()`. Make sure to add
	it as the last entry to the vector.

6. Programmatically revert the refactoring

	1. Figure out which refactoring processor to use to undo the refactoring. For extract method, it's
	`InlineMethodProcessor` and rename method is `RefactoringFactory` and `RenameRefactoring`.
	
	2. Use the information in your refactoring object to get the [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) elements necessary to perform
	the refactoring.

7. Programmatically replay the refactoring. In most cases, the refactoring process to replay the refactoring will be the same. For extract
method, undoing the refactoring uses `InlineMethodProcessor` and replaying it uses `ExtractMethodProcessor`. Use the information in your refactoring object to get the PSI elements necessary to perform
the refactoring.



## Contributors

- [Max Ellis](https://github.com/max-ellis) (main developer)
- [Sarah Nadi](https://sarahnadi.org/)
- [Danny Dig](http://dig.cs.illinois.edu/)

Please open an Issue for any problems/questions.

