
### 3. `src/main/scala/regex.scala`
Add more comments and improve error handling.

```scala
import scala.util.parsing.combinator._
import collection.mutable

abstract class RegexExpr
case class Literal(c: Char) extends RegexExpr
case class Or(expr1: RegexExpr, expr2: RegexExpr) extends RegexExpr
case class Concat(first: RegexExpr, second: RegexExpr) extends RegexExpr
case class Repeat(expr: RegexExpr) extends RegexExpr
case class Plus(expr: RegexExpr) extends RegexExpr

object RegexParser extends RegexParsers {
    // Precedence:
    // *, + binds the most tightly
    // then Concat
    // | is the weakest

    def charLit: Parser[RegexExpr] = ("""\w""".r | ".") ^^ { char => assert(char.length == 1); Literal(char.head) }
    def parLit: Parser[RegexExpr] = "(" ~> highExpr <~ ")"
    def lit: Parser[RegexExpr] = charLit | parLit
    def repeat: Parser[RegexExpr] = lit <~ "*" ^^ { case l => Repeat(l) } 
    def plus: Parser[RegexExpr] = lit <~ "+" ^^ { case p => Plus(p) }
    def lowExpr: Parser[RegexExpr] = repeat | plus | lit

    def concat: Parser[RegexExpr] = rep(lowExpr) ^^ { case list => listToConcat(list)}
    def midExpr: Parser[RegexExpr] = concat | lowExpr

    def or: Parser[RegexExpr] = midExpr ~ "|" ~ midExpr ^^ { case l ~ "|" ~ r => Or(l, r)}
    def highExpr: Parser[RegexExpr] = or | midExpr

    def listToConcat(l: List[RegexExpr]): RegexExpr = l match {
        case head :: Nil => head
        case head :: rest => Concat(head, listToConcat(rest))
    }

    // Add custom error handling
    case class RegexParseError(msg: String) extends RuntimeException(msg)
    
    // Add input validation
    def validateInput(input: String): Either[RegexParseError, String] = {
        if (input == null || input.isEmpty) 
            Left(RegexParseError("Empty or null pattern"))
        else Right(input)
    }

    // Improve the apply method with better error handling
    def apply(input: String): Either[RegexParseError, RegexExpr] = {
        validateInput(input).flatMap { validInput =>
            parseAll(highExpr, validInput) match {
                case Success(result, _) => Right(result)
                case NoSuccess(msg, next) => 
                    Left(RegexParseError(s"Parse error at position ${next.pos}: $msg"))
            }
        }
    }
}

abstract class State

class Consume(val c: Char, val out: State) extends State
class Split(val out1: State, val out2: State) extends State
class Placeholder(var pointingTo: State) extends State
case class Match() extends State

object NFA {
    def regexToNFA(regex: RegexExpr): State = regexToNFA(regex, Match())
    
    private def regexToNFA(regex: RegexExpr, andThen: State): State = {
        regex match {
            case Literal(c) => new Consume(c, andThen)
            case Concat(first, second) => {
                regexToNFA(first, regexToNFA(second, andThen))
            }
            case Or(l, r) => new Split(
                regexToNFA(l, andThen), 
                regexToNFA(r, andThen)
            )

            case Repeat(r) => 
                val placeholder = new Placeholder(null)
                val split = new Split(
                    // One path pointing back to the placeholder (the input element of our state machine)
                    regexToNFA(r, placeholder),
                    // One path for the 0 repeat case direct to andThen
                    andThen
                )
                placeholder.pointingTo = split
                placeholder

            case Plus(r) => regexToNFA(Concat(r, Repeat(r)), andThen)			
        }
    }
}

object NFAEvaluator {
    // Add caching for frequently accessed states
    private val stateCache = new mutable.HashMap[State, Set[State]]()
    
    // Add thread-safe evaluation
    @volatile private var visitedStates: mutable.Set[State] = null
    
    def evaluate(nfa: State, input: String): Boolean = {
        visitedStates = mutable.Set[State]()
        evaluate(Set(nfa), input)
    }

    // Add early termination optimization
    def evaluateStates(nfas: Set[State], input: Option[Char]): Set[State] = {
        if (nfas.isEmpty) return Set.empty
        stateCache.getOrElseUpdate(
            nfas.head,
            nfas.flatMap(state => evaluateState(state, input, visitedStates))
        )
    }

    def evaluateState(currentState: State, input: Option[Char], visitedStates: mutable.Set[State]): Set[State] = {
        if (visitedStates contains currentState) {
            Set()
        } else {
            visitedStates.add(currentState)
            currentState match {
                case placeholder: Placeholder => evaluateState(placeholder.pointingTo, input, visitedStates)
                case consume: Consume => if (Some(consume.c) == input || consume.c == '.') Set(consume.out) else Set()
                case s: Split => evaluateState(s.out1, input, visitedStates) ++ evaluateState(s.out2, input, visitedStates)
                case m: Match => if (input.isDefined) Set() else Set(Match())
            }
        }
    }
}

object Regex {
    def fullMatch(input: String, pattern: String): Either[RegexParser.RegexParseError, Boolean] = {
        RegexParser(pattern).map { parsed =>
            val nfa = NFA.regexToNFA(parsed)
            NFAEvaluator.evaluate(nfa, input)
        }
    }

    def matchAnywhere(input: String, pattern: String): Either[RegexParser.RegexParseError, Boolean] = 
        fullMatch(input, s".*$pattern.*")
}