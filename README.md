# brainfuck.clj
A brainfuck VM in Clojure.

## Structure
`brainfuck.clj` executes programs by generating a lazy sequence of program states from brainfuck source code. 

A program's state is represented as a map with the following structure:
```
{
  :c <source code>
  :t <tape (i.e. data section)>
  :ip <instruction pointer>
  :dp <data pointer>
}
```
Source code is represented as a syntactically correct, plain string. where the instruction pointer holds the index of the next operation to be performed within the source code string.

The tape is a map where keys represent data positions in memory and values hold the value present at these position. The map is sparse, i.e. it holds no values for memory locations that haven't been referenced yet. The default value for an unreferred memory location is assumed to be 0.

## Usage
Create a lazy sequence of program states by calling `(state-seq <source code>)`. Side effects (printing and reading) occur when evaluating each individual state is evaluated. The state sequence may be finite or infinite, depending on the structure of the provided program.

You may evaluate the list of states in any way you like, but for convenience's sake you may use `(run <source code>)`, which simply wraps the state sequence in a `dorun` call.

This repo contains a few examples of brainfuck source code that are located in the `samples` namespace.

### Example
Shown below is an example of how to run the `hello-world` using `leiningen`:
```
user=> (use 'brainfuck 'samples :refer-all)
nil
user=> (run hello-world)
Hello World!
nil
user=> 
```
