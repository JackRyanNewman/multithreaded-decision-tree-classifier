# Custom Threaded Decision Tree Classifier

## Overview
This project implements a **custom, uniquely threaded decision-tree classifier** built from scratch and extensively **profiled and optimized** for performance.  
It was developed as part of a study in algorithmic efficiency, focusing on how multithreading, data access patterns, and tree-construction strategies affect classification accuracy and runtime.

The classifier builds learning curves by training and validating decision trees on progressively larger training subsets. It supports full control over parameters such as base size, increment, maximum depth, number of trials, and randomization. The implementation follows the standard decision tree learning algorithm but replaces traditional recursive splitting with a **custom parallel tree-building routine** designed for speed and scalability.

---

## Features
- **Custom threaded tree-construction algorithm** for faster node expansion and split evaluation.  
- **Profiled and optimized** using data-driven benchmarks to minimize computation bottlenecks.  
- **Configurable parameters** for training size, trials, randomization, and depth control.  
- **Generates learning curve data** for evaluating performance over varying training set sizes.  
- **Command-line support** for flexible experiment setup and reproducibility.  

---

## Command Line Arguments

- **`-f <FILENAME>`**  
  **Required.** Path to the dataset file in CSV format. The file should contain feature columns followed by a class label column.

- **`-b <base>`**  
  Base training set size for learning curves. Specifies the initial number of training examples to use.  
  *Default: 50*

- **`-i <increment>`**  
  Number of additional training examples to add for each learning curve point.  
  *Default: 50*

- **`-l <limit>`**  
  Maximum training set size to use for learning curves.  
  *Default: 500*

- **`-t <trials>`**  
  Number of trials to run for each training set size (for statistical significance).  
  *Default: 5*

- **`-d <depth>`**  
  Maximum depth allowed for the decision tree. Controls tree complexity and prevents overfitting.  
  *Default: 10*

- **`-r`**  
  Enable randomization of training data selection. When set, training examples are randomly sampled rather than using the first N examples.

- **`-v <verbosity>`**  
  Output verbosity level (0-3). Higher levels provide more detailed progress information and debugging output.  
  *Default: 1*

- **`-p`**  
  Enable parallel tree construction. Uses custom threading implementation to speed up the tree building process.

---

## READ references\assignment-02 (3).pdf for more info

---

## Usage
Compile and run from the command line:
```bash
javac *.java
java Driver -f dataset.csv -b 100 -i 25 -l 300 -t 10 -d 8 -r -v 2 -p


