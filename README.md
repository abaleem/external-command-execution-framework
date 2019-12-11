# External Command Execution Framework in Scala
### Description: object-oriented pure functional design and implementation of an external command execution framework as an I/O monad.

## Overview

In this project I have designed a pure pure functional object-oriented design of framework for composing and executing external commands and applications from Scala programs and obtaining and processing the results of these executions. 

This is an extensible framework with typed external commands that the clients will execute using such monadic combinators as. Each external command type is be implemented using the pattern [Builder](https://en.wikipedia.org/wiki/Builder_pattern). Additional I have  implement this pattern using [phantom types](https://medium.com/@maximilianofelice/builder-pattern-in-scala-with-phantom-types-3e29a167e863). Phantom type has allowed us to restrict error at code writing time. That means if a command needs to be executed in a certain way in cmd, the way the code is designed in this project wont allow it.

In this project I have implemented 5 commands with different variations. These commands can also be pipelined with each other where possible. After that I have implemented some tests (results and compile). The input to these tests are provided through config file (you might need to change that accordingly. There is all logging provided at different levels with each command. Below i'll describe each command I executed, and examples of how to execute it. All these commands are valid for Windows Only! The commands that I have implemented are:

1. tasklist (TaskListBuilder)
2. findstr (FindStrBuilder)
3. dir  (DirBuilder)
4. taskkill (TaskKillBuilder)
5. ipconfig (IpConfigBuilder)

####  Semantics and Structure
Each builder can initated with its name followed by parentheses. e.g FindStrBuilder(). Each Builder has build function that returns the results. Some functions also have a buildParsed function that returns the parsed output.

Each function has its constrainted built into it using **Phatom Types** meaning they can't be called in wrong order and the command will not execute unless the command has been properly created.

Where **pipelining** is possible is a part of the class. The function that takes the pipeline e.g findstr has pipelineFrom function and there a pipeline function in the classes which gives that an input.

There are also some helper functions I have created for Execution, Parsing and Column Indexing. All these functions are implemented as Singleton objects.


## Demo examples
There are few examples of how this framework works.
There is detailed explanation with states and constraints provided for each command.

#### Example 1
```scala
  FindStrBuilder().pipelineFrom(IpConfigBuilder().pipeline).ignoreCase.setString("ip").build
```

The above command simple gets output for ipconfig and pipelines it to findstr and searching for "ip" and returns the result that have ip in it.

Command prompt from equivalent of this would be:
```
ipconfig | findstr \C:"ip"
```
 
#### Example 2
 
```scala
  FindStrBuilder().pipelineFrom(TaskListBuilder().pipeline).setString("chrome").build
```
The above command simple gets output for tasklist and pipelines it to findstr and searching for "chrome" and returns the result that have ip in it.

Command prompt from equivalent of this would be:
```
tasklist | findstr \C:"chrome"
```

#### Example 3 - a little complex example
```scala
TaskKillBuilder().pipelineFrom(parseTaskList(FindStrBuilder().pipelineFrom(TaskListBuilder().pipeline).setString("chrome").build)(1)).build
```

Lets start from the inside. TaskListBuilder pipelines its output to FindStrBuilder which searches for "Snipping" in the result. The output is then fed to parseTaskList which parses the string into 2d array.. We then extract the first column which is PID's and input into TaskKillBuilder pipeline option and execute it.

NOTE: RUNNING THIS MAY KILL IMPORTANT TASKS AND CAUSE UNWANTED CONSEQUENCES IF IMPROPERLY USED. REFER TO DOCUMENTATION BEFORE UNCOMMENTING

ALSO NOTE: CHANGE NAME FROM CHROME IF YOU DONT WANT TO CLOSE CHROME TABS (CONFIG FILE)!

I dont know how to parse PID's in command prompt from string hence I have just used getpid here. It is parsed and implemented is scala
Command prompt from equivalent of this would be:

```
tasklist | findstr \C:"chrome" | getpid | taskkill
```




## tasklist

Displays a list of currently running processes on the local computer or on a remote computer. Tasklist replaces the tlist tool.

Building a tasklist object is as simple as. There are no compulsory parameters for tasklist.
```scala
  TaskListBuilder().build
```

**options**

1. setUser(string):  Filter the results based on the username provided.
2. setRunning: Filter the results for the tasks that are running.
3. memoryUsageAbove(int): Filters the results that have memory usage (in kbs) above a certain point

**a more elaborate command would be:**

```scala
  TaskListBuilder().setUser(userName).buildParsed.filter(Column(Seq("PID"))).flatten
```

Over here, we are getting the user of task from config file and we are building the parsed version. then we fillter for any columns that have a title of PID and then we flatten to results to get a list of all PID's started by userName.   // returns a 2d list as we can filter multiple columns (add to Seq) at once hence we flatten


## taskkill

taskkill requires are method to be build. this method can either be done by providing it a pid or pipelining its to task list.

**options**

1. killChilds: Allows us to kill all child processes started by the pid
2. pid(string): Kills task the correspond to this pid
3. pipelineFrom(some other builder pipeline): Pipelines from a list of pids. checks to see if all digits.

**constraints**:

1. if pipelineFrom is implemneted it must be called right after builder initalization
2. to build we must it pipeline this command or give it a pid

**example**

```scala
  TaskKillBuilder().pid("1000").killChilds.build
```

this would kill the process with pid 1000 and kill all its children processes.

## dir

dir just requires setDir to be build. It can be left empty, which defaults to current directory.

**options**:

1. setDir: Path to list the directory for.
2. makeRecursive: makes the search recursive (checks all subfolders)
3. sortBy: sorts the result of search result according to name, date or size. defaults to name.
4. filetype: filters the type of file search for. e.g. directory, file, hidden and system.
5. can be pipelined to other commands.


**constraints**:

1. setDir must be added for the build to be called. It can be left blank in while case current directly would be called.
2. setDir must be the first thing after initialization the should be called.

**example**:

```scala
  DirBuilder().setDir().sortBy("name").fileType("hidden").makeRecursive.build
```

This would return all the hidden files sorted by name, recursively searched in current directory.


## findstr

findstr requires a string and an inputStyle (pipelined and path) to be build. if path left empty it will default to current directory.


**options**:

1. setString: The string to be searched from
2. setPath: inputStyle option1. Path to search in. Empty defaults to current directory
3. ignoreCase: Ignores the case while searching for the string
4. pipelineFrom: inputStyle option2. Pipelines another command. Takes the seq of strings to be executed.

**constaints**: 

1. setString can only follow empty, pipelined or other args.
2. setString must be implemented before setPath can be called
3. ignoreCase can only follow empty, piplined or other args. Can't be put after setString or setPath.
4. pipelineFrom must be the first command if declared.
5. to build there must be a setString and inputStyle called.

**example**:
```scala
  FindStrBuilder().ignoreCase.setString("stringToSearch").setPath().build
```

Searches for the string in the current directory whilst ignore letter case.
 

## ipconfig

ipconfig can be build directly, no option constants.

**options**:

1. getAll: Displays the full TCP/IP configuration for all adapters.
2. getDNS: Displays the contents of the DNS client resolver cache

**constants**:

1. from the above options only one can be implemented at a time.

**example**:
```scala
  IpConfigBuilder().getDNS.build
```


## Instructions and Setup

**Prerequisites** 

* JVM and SBT must be installed on your system.
* This project was done is Intellij Idea IDE. And can be easily loaded for proper project access.

**Running project using SBT**

* Clone or download this repository onto your system
* Open the Command Prompt (if using Windows) or the Terminal (if using Linux/Mac) and go to the project directory
* Build the project and generate the jar file using SBT

 **sbt clean compile run** 


* To run the scalatest using SBT use


 **sbt clean compile test**


**Running project using Intellij Idea**

* Clone or download this repository onto your system
* Go to the project and open it using Intellij, or open Intellij and go to file and load the project.
* The entry point of the program is src->main->scala->Main. You can run this by right clicking the screen and clicking run Main.
* You can run the tests by navigating to src->test->scala. You can right click this folder and run all tests from different classes.



## Testing and Examples

The tests for this can be found in: src>test>scala

I have divided tests into 2 parts:

1. **ResultsTests**: Checks wheaten the command gives correct results
2. **CompileResults**: Tests the checks the compile fails if executed in wrong order or minimum requirements is not met the code should not compile

Examples for this project can also be found in: src>test>scala. Examples are mostly the examples provided above. Ther have been divided into to files BasicExamples and PipliningExamples.

There are a few parameters that change from user to user e.g. PC's username. Make sure you make these changes in config file.
