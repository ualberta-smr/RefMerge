# RefactoringAwareMerging

This project evaluates operation-based refactoring-aware merging and graph-based 
refactoring-aware merging.

## System requirements
* Linux
* git
* Java 11
* IntelliJ (Community Addition) Version 2020.1.2

## How to run

### 1. Clone and build RefactoringMiner 
Use `Git Clone https://github.com/tsantalis/RefactoringMiner.git` to clone RefactoringMiner. 
Then build RefactoringMiner with `./gradlew distzip`. It will be under build/distributions.

### 2. Add RefactoringMiner to your local maven repository
You will need to add RefactoringMiner to your local maven repository to
use it in the build.gradle. You can use `mvn install:install-file -Dfile=<path-to-file>`
to add it to your local maven repository. You can verify that it's been installed 
by checking the path `/home/username/.m2/repository/org/refactoringminer`.

### 3. Populate databases
Use the refactoring analysis dump found [here](https://github.com/ualberta-smr/refactoring-analysis-results)
to populate the original_analysis database. Use the database/intelliMerge_data1
sql dump to populate intelliMerge_data1 database. Use the database/refMerge_dataset
to populate refMerge_dataset database.

### 4. Get IntelliMerge replication commits
Run `python intelliMerge_data_resolver` to get the IntelliMerge commits used in
the IntelliMerge replication. 

### 5. Get Refactoring-aware merging commits
Run `python project_sampler` to get the additional 10 projects used in the 
evaluation. Then, run `python refMerge_data_resolver` to get the commits with
refactoring-related conflicts.

## IntelliMerge Replication

### Edit configuration
Edit the configuration tasks to have `:runIde` and include set `-Pmode=` to `replication`. 

## RefactoringAwareMerging Comparison

### Edit configuration
Edit the configuration tasks to have `:runIde` and include set `-Pmode=` to `comparison`.
Then, set `-PevaluationProject=` to the project that you want to evaluate on. For example,
it would look like `-PevaluationProject=error-prone` if you want to evaluate on error-prone.


