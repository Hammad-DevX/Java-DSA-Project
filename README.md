# Java-DSA-Project
# 🚀 Java Data Structures & Algorithms (DSA) Projects

Welcome to my Java DSA Portfolio! This repository contains two distinct projects built using core Data Structures and Algorithms. Each project demonstrates how abstract mathematical concepts and data structures can be applied to solve real-world problems and visualize logic.

---

## 📌 Project 1: Algorithmic Maze Solver & Comparator

### 📖 Overview
A comprehensive 2D Grid Graph visualizer that generates random mazes and solves them using various pathfinding algorithms. 

**Unique Angle:** This project features an **Algorithm Comparison Mode** that allows users to run different pathfinding algorithms side-by-side. It visually demonstrates which algorithm is faster, how many nodes it explores, and why one is more efficient than another in specific scenarios.

### ✨ Features
* **Random Maze Generation:** Uses Recursive Backtracking to carve out perfect mazes.
* **Side-by-Side Comparison:** Watch two algorithms race to the finish line simultaneously.
* **Performance Metrics:** Calculates and displays execution time and the total number of grids visited.

### 🧠 Core DSA Implemented
* **2D Grid Graph:** The foundational data structure representing the map/maze.
* **Stack (DFS):** Used for Depth-First Search to explore paths deeply before backtracking.
* **Queue (BFS):** Used for Breadth-First Search to guarantee the shortest path in an unweighted grid.
* **Priority Queue (A* Algorithm):** Utilizes a heuristic (distance to target) to find the most optimal path efficiently.
* **Recursive Backtracking:** The core algorithm used to procedurally generate the maze walls and paths.

---

## 📌 Project 2: Crime Investigation Management System (CIMS)

### 📖 Overview
A complete, end-to-end management system for a Police Department. This console-based application simulates the entire lifecycle of a criminal investigation, from the moment a crime is reported to the case being solved. 

### ⚙️ System Workflow
1. **Report:** A new crime is reported and registered in the system.
2. **Investigation:** An investigator is assigned, and a list of suspects is generated.
3. **Evidence Collection:** Evidence is gathered and logged. Mistakes can be easily undone.
4. **Resolution:** The case is tracked through its history until it is marked as solved.

### 🧠 Core DSA Implemented & Their Real-World Use
This project heavily relies on specific data structures to mimic real-world police operations:

* **Queue (Case Queue):** Implements First-In-First-Out (FIFO). New crime reports wait in the queue so that the oldest cases are assigned and investigated first.
* **Stack (Evidence Undo):** Implements Last-In-First-Out (LIFO). If an investigator accidentally logs incorrect evidence, the `pop()` operation is used to easily undo the last action.
* **Linked List (Suspect Chain):** Manages suspects in a sequential chain. Investigators can traverse from the primary suspect to the secondary suspect smoothly.
* **Array List (Case Directory):** Acts as the master database for all cases. It allows for quick, dynamic indexing and O(1) retrieval when searching for specific case records.
* **Recursion (Case Tracking):** Used to trace the complete history of connected cases (e.g., tracking how Case A linked to Case B, which eventually uncovered Case C).

---

## 💻 Technologies Used
* **Language:** Java (JDK 11+)
* **Concepts:** Object-Oriented Programming (OOP), Data Structures, Graph Theory, Algorithm Analysis.

## 🛠️ How to Run
1. Clone this repository to your local machine:
   ```bash
   git clone [https://github.com/YourUsername/Your-Repo-Name.git](https://github.com/YourUsername/Your-Repo-Name.git)
