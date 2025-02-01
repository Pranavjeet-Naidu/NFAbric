# NFAbric

A Non-deterministic Finite Automaton (NFA) based Regular Expression Engine implemented in Scala.

## Features
- Support for basic regex operations: concatenation, alternation (|), repetition (*, +)
- Dot (.) operator for matching any character
- Visualization support using GraphViz
- Full string matching and substring matching

## Usage
### Basic Examples
```scala
import com.nfabric.Regex

val result1 = Regex.fullMatch("ab", "ab") // Right(true)
val result2 = Regex.fullMatch("abbbbb", "ab+") // Right(true)
val result3 = Regex.fullMatch("bbbbb", "ab+") // Right(false)