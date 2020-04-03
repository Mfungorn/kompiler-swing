package domain.tokens

import domain.lexer.Terminals

object If : Token.Reserved(Terminals.IF)
object Then : Token.Reserved(Terminals.THEN)
object Else : Token.Reserved(Terminals.ELSE)
object ElseIf : Token.Reserved(Terminals.ELSEIF)
object EndIf : Token.Reserved(Terminals.END_IF)

object OpenParen : Token.Reserved(Terminals.OPEN_PAREN)
object CloseParen : Token.Reserved(Terminals.CLOSE_PAREN)