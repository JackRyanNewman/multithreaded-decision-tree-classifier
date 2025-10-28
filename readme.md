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

## Usage
Compile and run from the command line:
```bash
java Driver -f <FILENAME> [-b base] [-i increment] [-l limit] [-t trials] [-d depth] [-r] [-v verbosity] [-p]
