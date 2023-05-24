/*
 * FEYZULLAH ASILLIOGLU  150121021
 * MOHAMAD NAEL AYOUBI   150120997
 * KADIR BAT             150120012
 * 
 * CSE2260 Principles of Programming Languages - Project 1 Part 1 & 2
 *     Lexical Analyser (Scanner) & Syntax Analyser (Parser)
 *     
 *     Scanning (DFA) as a set of nested case is our technique.
 *     Analysing (Parser) by implementing a recursive-descent parser.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {

	// used for parsing process
	static ArrayList<String> tokensList;
	static ArrayList<String> lexemesList;
	static ArrayList<String> lineColumn;
	// used for printing the parse tree into an output file called parseTree.txt
	static PrintWriter outputParse;
	
	public static void main(String[] args) throws IOException {

		System.out.println("Enter the name of the input file: ");
		Scanner scanFileName = new Scanner(System.in);
		String input = scanFileName.nextLine();
		scanFileName.close();

		// reading characters from file
		File inputFile = new File(input);
		FileReader fileReader = new FileReader(inputFile);
		// read char by char. Reads characters from another Reader
		BufferedReader readChar = new BufferedReader(fileReader);

		
		// create and write to a file
		File output = new File("tokens.txt");
		// output.txt file is created in the project folder
		if (!output.exists())
			output.createNewFile();
		PrintWriter write = new PrintWriter(output);
		
		// to trace lexemes
		int lineNo = 1;
		int columnNo = 1;
		String token = "";
		String tempIdToken = "";
		String tokenString = "";
		String tokenNumber = "";
		String tokenChar = "";
		
		initializeArrayLists();
		initializePrintWriter("outputParse.txt");
		
		/*------------------------------ Scan ------------------------------*/
		
		int ascii = 0;
		// -1 means end of character stream
		while (true) {
			ascii = readChar.read();
			// ASCII 10 is new line character
			if (ascii == -1) {
				break;
			}
			if (ascii == 10) {
				lineNo++;
				columnNo = 1;
				continue;
			}
			// ASCII 126 is tilde character (~) for comments
			if (ascii == 126) {
				if(isNumber(tokenNumber)) {
					tempIdToken = "";
					tokenString = "";
					if (!tokenNumber.isEmpty()) {
						tokenNumber = toString("NUMBER", tokenNumber, lineNo, columnNo - tokenNumber.length(), write);
					}
				} else if (isIdentifier(tempIdToken)) {
					tokenNumber = "";
					if (!tempIdToken.isEmpty()) {
							tempIdToken = toString("IDENTIFIER", tempIdToken, lineNo, columnNo - tempIdToken.length(), write);
					}
				}
				else if(!tokenNumber.isEmpty()) {
					tokenNumber = toString("LEXICAL ERROR", tokenNumber, lineNo, columnNo - tokenNumber.length(), write);
				}
				else if(!tempIdToken.isEmpty()) {
					tempIdToken = toString("LEXICAL ERROR", tempIdToken, lineNo, columnNo - tempIdToken.length(), write);
				}
				while (ascii != 10) {
					ascii = readChar.read();
				}
				lineNo++;
				columnNo = 1;
				continue;
			}
			// cast to char
			char ch = (char) ascii;


			// check for character literal
			if (ch == '\'') {
				tokenChar += ch;
				// reading next char using nested if technique
				int charBegin = columnNo;
				ascii = readChar.read();
				ch = (char) ascii;
				columnNo++;
				if (ch == '\\') {
					tokenChar += ch;
					ascii = readChar.read();
					ch = (char) ascii;
					columnNo++;
					// double backslashes
					if (ch == '\\') {
						tokenChar += ch;
						ascii = readChar.read();
						ch = (char) ascii;
						columnNo++;
						if (ch != '\'') {
							tokenChar = toString("LEXICAL ERROR", tokenChar, lineNo, columnNo, write);
						}
						else {
							tokenChar += ch;
							tokenChar = toString("CHAR", tokenChar, lineNo, charBegin, write);
						}
					}
					// single quote must be preceded by a backslash char
					else if (ch == '\'') {
						tokenChar += ch;
						ascii = readChar.read();
						ch = (char) ascii;
						columnNo++;
						if (ch != '\'') {
							tokenChar = toString("LEXICAL ERROR", tokenChar, lineNo, columnNo, write);
						}
						else {
							tokenChar += ch;
							tokenChar = toString("CHAR", tokenChar, lineNo, charBegin, write);
						}
					}
				}
				else if (ch != '\'' && ch != '\n') {
					tokenChar += ch;
					ascii = readChar.read();
					ch = (char) ascii;
					columnNo++;
					if (ch != '\'' || ch == '\n') {
						tokenChar = toString("LEXICAL ERROR", tokenChar, lineNo, columnNo, write);
					}
					else {
						tokenChar += ch;
						tokenChar = toString("CHAR", tokenChar, lineNo, charBegin, write);
					}
				}
				else {
					tokenChar = toString("LEXICAL ERROR", tokenChar, lineNo, columnNo, write);
				}
			}

			// initialising String literal with the given expression
			else if (ch == '"') {
				int strBegin = columnNo;
				tokenString += ch;
				while (ascii != 10 && ascii != -1) {
					ascii = readChar.read();
					ch = (char) ascii;
					columnNo++;
					tokenString += ch;

					// end of String
					if (ch == '"' && (tokenString.charAt(tokenString.length() - 2) != '\\')) {
						// check if it is a valid String or not
						if (isString(tokenString)) {
							// is a valid string 
							tokenString = toString("STRING", tokenString, lineNo, columnNo - tokenString.length() + 1, write);
							break;
						}
						else {
							tokenString = toString("LEXICAL ERROR", tokenString, lineNo, strBegin, write);
							break;
						}
					}	
					// end of line or stream and could find the last double quote
					else if (ascii == 10 || ascii == -1) {
						tokenString = toString("LEXICAL ERROR", tokenString, lineNo, strBegin, write);
						lineNo++;
						columnNo = 0;
					}
				}
			}

			// temporary initialising keyword's, number's and identifier's strings
			else if ((isLowerCaseCharacter(ch) || isDecDigit(ch) || isBinDigit(ch) || isHexDigit(ch) || ch == '!' || ch == '*' || ch == '/' || ch == ':' ||
					ch == '<' || ch == '=' || ch == '>' || ch == '?' || ch == '.' || ch == '+' || ch == '-')) {
				token += ch;
				tokenNumber += ch;
				tempIdToken += ch;
				// first char must not be a digit for ID token
			} else if (isKeyword(token)) {
				tempIdToken = "";
				tokenNumber = "";
				tokenString = "";
				if (token.equals("define")) {
					token = toString("DEFINE", token, lineNo, columnNo - token.length(), write);
				} else if (token.equals("let")) {
					token = toString("LET", token, lineNo, columnNo - token.length(), write);
				} else if (token.equals("cond")) {
					token = toString("COND", token, lineNo, columnNo - token.length(), write);
				} else if (token.equals("if")) {
					token = toString("IF", token, lineNo, columnNo - token.length(), write);
				} else if (token.equals("begin")) {
					token = toString("BEGIN", token, lineNo, columnNo - token.length(), write);
				} else if (token.equals("true") || token.equals("false")) {
					token = toString("BOOLEAN", token, lineNo, columnNo - token.length(), write);
				}
			} else if(isNumber(tokenNumber)) {
				tempIdToken = "";
				tokenString = "";
				if (!tokenNumber.isEmpty()) {
					tokenNumber = toString("NUMBER", tokenNumber, lineNo, columnNo - tokenNumber.length(), write);
				}
				// since this literal expression and brackets expression are in different if statements we might not print bracket after a number
				token = printBracket(ch, lineNo, columnNo, write);
			} else if (isIdentifier(tempIdToken)) {
				tokenNumber = "";
				if (!tempIdToken.isEmpty()) {
					tempIdToken = toString("IDENTIFIER", tempIdToken, lineNo, columnNo - tempIdToken.length(), write);
				}
				token = printBracket(ch, lineNo, columnNo, write);
			} else if (isBracket(ch)) {
				token = "";
				tempIdToken = "";
				// if char is a left bracket
				token = printBracket(ch, lineNo, columnNo, write);
			} else if(ch == ' ') {
				if(!tokenNumber.isEmpty()) {
					tokenNumber = toString("LEXICAL ERROR", tokenNumber, lineNo, columnNo - tokenNumber.length(), write);
				}
				else if(!tempIdToken.isEmpty()) {
					tempIdToken = toString("LEXICAL ERROR", tempIdToken, lineNo, columnNo - tempIdToken.length(), write);
				}

				// ASCII 13 is carriage return, ASCII 32 is space
			} else if (ascii != 32 && ascii != 13) {
				token = toString("LEXICAL ERROR", "", lineNo, columnNo, write);
			}
			columnNo++;
		}
		
		readChar.close();
		write.close();
		
		/*------------------------------ Parse ------------------------------*/
		
		parse();
		outputParse.close();
	}

	// prints terminal and non-terminal tokens to construct a parse tree
	public static void printParse(String token) {
		for (int i = 0; i < spaceIndex; i++) {
			System.out.print(" ");
			outputParse.print(" ");
		}
		System.out.println(token);
		outputParse.println(token);
	}
	
	static int index = 0;
	// acts as a dynamic scope to trace the spaces
	static int spaceIndex = 0;
	static String nextToken = "";
	public static void parse() {
		// trace lexemes
		nextToken = tokensList.get(index);
		program();
	}
	
	// check if matches the the non-terminals in regular expressions
	public static void match(String matchedToken) {
		if (nextToken.equals(matchedToken)) {
			printParse(matchedToken + " (" + lexemesList.get(index) + ")");
			index++;
			if (index < tokensList.size())
				nextToken = tokensList.get(index);
		}
		else {
			if (matchedToken.equals("LEFTPAR")) {
				System.out.println("SYNTAX ERROR " + lineColumn.get(index) + ": '(' is expected");
				outputParse.println("SYNTAX ERROR " + lineColumn.get(index) + ": '(' is expected");
			}
			else if (matchedToken.equals("RIGHTPAR")) {
				System.out.println("SYNTAX ERROR " + lineColumn.get(index) + ": ')' is expected");
				outputParse.println("SYNTAX ERROR " + lineColumn.get(index) + ": ')' is expected");
			}
			else {
				System.out.println("SYNTAX ERROR " + lineColumn.get(index) + ": '" + matchedToken + "' is expected");
				outputParse.println("SYNTAX ERROR " + lineColumn.get(index) + ": '" + matchedToken + "' is expected");
			}
			outputParse.close();
			System.exit(1);
		}
		
	}
	
	public static void program() {
		printParse("<Program>");
		spaceIndex++;
		if (nextToken.equals("LEFTPAR")) {
			topLevelForm();
			program();
		}
		else {
			// corresponds to epsilon
			printParse("__");
		}
		spaceIndex--;
	}
	
	public static void topLevelForm() {
		printParse("<TopLevelForm>");
		spaceIndex++;
		match("LEFTPAR");
		secondLevelForm();
		match("RIGHTPAR");
		spaceIndex--;
	}

	public static void secondLevelForm() {
		printParse("<SecondLevelForm>");
		spaceIndex++;
		if (nextToken.equals("LEFTPAR")) {
			match("LEFTPAR");
			funCall();
			match("RIGHTPAR");
		}
		else {
			definition();
		}
		spaceIndex--;
	}

	public static void definition() {
		printParse("<Definition>");
		spaceIndex++;
		match("DEFINE");
		definitionRight();
		spaceIndex--;
	}
	
	public static void definitionRight() {
		printParse("<DefinitionRight>");
		spaceIndex++;
		if (nextToken.equals("IDENTIFIER")) {
			match("IDENTIFIER");
			expression();
		}
		else {
			match("LEFTPAR");
			match("IDENTIFIER");
			argList();
			match("RIGHTPAR");
			statements();
		}
		spaceIndex--;
	}
	
	public static void argList() {
		printParse("<ArgList>");
		spaceIndex++;
		if (nextToken.equals("IDENTIFIER")) {
			match("IDENTIFIER");
			argList();
		}
		else {
			printParse("__");
		}
		spaceIndex--;
	}
	
	public static void statements() {
		printParse("<Statements>");
		spaceIndex++;
		// <Definition> <Statements>
		if (nextToken.equals("DEFINE")) {
			definition();
			statements();
		}
		else {
			expression();
		}
		spaceIndex--;
	}
	
	public static void expressions() {
		printParse("<Expressions>");
		spaceIndex++;
		if (nextToken != "IDENTIFIER" && nextToken != "NUMBER" && nextToken != "CHAR" && nextToken != "BOOLEAN"
				&& nextToken != "STRING" && nextToken != "LEFTPAR") {
			printParse("__");
		}
		else {
			expression();
			expressions();
		}
		spaceIndex--;
	}
	
	public static void expression() {
		printParse("<Expression>");
		spaceIndex++;
		switch (nextToken) {
		case "IDENTIFIER": 
			match("IDENTIFIER");
			break;
		case "NUMBER":
			match("NUMBER");
			break;
		case "CHAR": 
			match("CHAR");
			break;
		case "BOOLEAN":
			match("BOOLEAN");
			break;
		case "STRING": 
			match("STRING");
			break;
		default: 
			match("LEFTPAR");
			expr();
			match("RIGHTPAR");
		}
		spaceIndex--;
	}
	
	public static void expr() {
		printParse("<Expr>");
		spaceIndex++;
		switch (nextToken) {
		case "LET": 
			letExpression();
			break;
		case "COND":
			condExpression();
			break;
		case "IF": 
			ifExpression();
			break;
		case "BEGIN":
			beginExpression();
			break;
		default: 
			funCall();
		}
		spaceIndex--;
	}
	
	public static void funCall() {
		printParse("<FunCall>");
		spaceIndex++;
		match("IDENTIFIER");
		expressions();
		spaceIndex--;
	}
	
	public static void letExpression() {
		printParse("<LetExpression>");
		spaceIndex++;
		match("LET");
		letExpr();
		spaceIndex--;
	}

	public static void letExpr() {
		printParse("<LetExpr>");
		spaceIndex++;
		if (nextToken.equals("LEFTPAR")) {
			match("LEFTPAR");
			varDefs();
			match("RIGHTPAR");
			statements();
		}
		else {
			match("IDENTIFIER");
			match("LEFTPAR");
			varDefs();
			match("RIGHTPAR");
			statements();
		}
		spaceIndex--;
	}

	public static void varDefs() {
		printParse("<VarDefs>");
		spaceIndex++;
		match("LEFTPAR");
		match("IDENTIFIER");
		expression();
		match("RIGHTPAR");
		varDef();
		spaceIndex--;
	}

	public static void varDef() {
		printParse("<VarDef>");
		spaceIndex++;
		if (nextToken.equals("LEFTPAR")) {
			varDefs();
		}
		else {
			printParse("__");
		}
		spaceIndex--;
	}

	public static void condExpression() {
		printParse("<CondExpression>");
		spaceIndex++;
		match("COND");
		condBranches();
		spaceIndex--;
	}

	public static void condBranches() {
		printParse("<CondBranches>");
		spaceIndex++;
		match("LEFTPAR");
		expression();
		statements();
		match("RIGHTPAR");
		condBranch();
		spaceIndex--;
	}

	public static void condBranch() {
		printParse("<CondBranch>");
		spaceIndex++;
		if (nextToken.equals("LEFTPAR")) {
			match("LEFTPAR");
			expression();
			statements();
			match("RIGHTPAR");
		}
		else {
			printParse("__");
		}
		spaceIndex--;
	}

	public static void ifExpression() {
		printParse("<IfExpression>");
		spaceIndex++;
		match("IF");
		expression();
		expression();
		endExpression();
		spaceIndex--;
	}

	public static void endExpression() {
		printParse("<EndExpression>");
		spaceIndex++;
		if (nextToken != "IDENTIFIER" && nextToken != "NUMBER" && nextToken != "CHAR" && nextToken != "BOOLEAN"
				&& nextToken != "STRING" && nextToken != "LEFTPAR") {
			printParse("__");
		}
		else {
			expression();
		}
		spaceIndex--;
	}

	public static void beginExpression() {
		printParse("<BeginExpression>");
		spaceIndex++;
		statements();
		spaceIndex--;
	}
	
	public static boolean isString(String s) {
		for (int i = 1; i < s.length() - 1; i++) {
			char c = s.charAt(i);
			// if the character is a backslash, the next character must be either a quotation mark or backslash
			if (c == '\\') {
				i++;
				if (s.charAt(i) == '"') {
					if (i == s.length() - 1)
						return false;
				}
				else if (s.charAt(i) == '\\')
					continue;
				else
					return false;
			}
		}
		return true;
	}

	public static boolean isBracket(char c) {
		return c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}';
	}

	public static String printBracket(char ch, int lineNo, int columnNo, PrintWriter write) {
		if (ch == '(') {
			return toString("LEFTPAR", "(", lineNo, columnNo, write);
		} else if (ch == ')') {
			return toString("RIGHTPAR", ")", lineNo, columnNo, write);
		} else if (ch == '[') {
			return toString("LEFTSQUAREB", "[", lineNo, columnNo, write);
		} else if (ch == ']') {
			return toString("RIGHTSQUAREB", "]", lineNo, columnNo, write);
		} else if (ch == '{') {
			return toString("LEFTCURLYB", "{", lineNo, columnNo, write);
		} else if (ch == '}') {
			return toString("RIGHTCURLYB", "}", lineNo, columnNo, write);
		}
		else return "";
	}
	
	public static boolean isNumber(String s) {
		//boolean validNumber = true;
		if (s.isEmpty())
			return false;
		if (s.length() == 1 && isDecDigit(s.charAt(0)))
			return true;
		// a check for hexadecimal and binary integer
		if (s.charAt(0) == '0' && s.length() > 2) {
			// check for hexadecimal literal
			if (s.charAt(1) == 'x') {
				if (isHexDigit(s.charAt(2))) {
					for (int i = 3; i < s.length(); i++) {
						if (!isHexDigit(s.charAt(i)))
							return false;
					}
				}
				else
					return false;
			}
			// check for binary literal
			else if (s.charAt(1) == 'b') {
				if (isBinDigit(s.charAt(2))) {
					for (int i = 3; i < s.length(); i++) {
						if (!isBinDigit(s.charAt(i)))
							return false;
					}
				}
				else
					return false;
			}
		}
		// a check for decimal signed and floating point numbers with no points
		else if ((s.charAt(0) == '+' || s.charAt(0) == '-' || isDecDigit(s.charAt(0))) && !s.contains(".")) {
			int exp = 0;
			for (int i = 1; i < s.length(); i++) {
				if (!isDecDigit(s.charAt(i)) && (i != s.length() - 1)) {
					exp = i;
					break;
				}
				// it is a decimal integer
				else if ((i == s.length() - 1) && isDecDigit(s.charAt(i)))
					return true;
			}
			// floating number
			if (s.charAt(exp) == 'e' || s.charAt(exp) == 'E') {
				exp++;
				if ((s.charAt(exp) == '+' || s.charAt(exp) == '-' || isDecDigit(s.charAt(exp)))) {
					for (int i = exp + 1; i < s.length(); i++) {
						if (!isDecDigit(s.charAt(i)))
							return false;
					}
					return true;
				}
			}
			else 
				return false;
		}
		// check for floating point number with a point
		else if ((s.charAt(0) == '+' || s.charAt(0) == '-' || isDecDigit(s.charAt(0)) || s.charAt(0) == '.') && s.contains(".")) {
			int indexOfPoint = 0;
			int exp = 0;
			// check prefix
			if (s.charAt(0) == '+' || s.charAt(0) == '-' || isDecDigit(s.charAt(0)) || s.charAt(0) == '.') {
				if (s.charAt(0) == '.')
					indexOfPoint = 0;
				else { 
					for (int i = 1; i < s.length(); i++) {
						if (!isDecDigit(s.charAt(i))) {
							indexOfPoint = i;
							break;
						}
					}
				}
				if(s.charAt(indexOfPoint) == '.') {
					if (!isDecDigit(s.charAt(++indexOfPoint))) {
						return false;
					}
					for (int i = indexOfPoint; i < s.length(); i++) {
						if (!isDecDigit(s.charAt(i)) && (i != s.length() - 1)) {
							exp = i;
							break;
						}
						else if ((i == s.length() - 1) && isDecDigit(s.charAt(i)))
							return true;
					}
					// floating number
					if (s.charAt(exp) == 'e' || s.charAt(exp) == 'E') {
						exp++;
						if ((s.charAt(exp) == '+' || s.charAt(exp) == '-' || isDecDigit(s.charAt(exp)))) {
							for (int i = exp + 1; i < s.length(); i++) {
								if (!isDecDigit(s.charAt(i)))
									return false;
							}
							return true;
						}
					}
					else 
						return false;
				}
				else
					return false;
			}
		}
		return false;
	}
	
	public static boolean isIdentifier(String s) {
		boolean validChar = true;
		if (!isKeyword(s) && !s.isEmpty()) {
			// check first rightmost BNF choice
			if ((s.charAt(0) == '+' || s.charAt(0) == '-' || s.charAt(0) == '.') && s.length() == 1) 
				return true;
			else if (isLowerCaseCharacter(s.charAt(0)) || s.charAt(0) == '!' || s.charAt(0) == '*' || s.charAt(0) == '/'
					|| s.charAt(0) == ':' ||
					s.charAt(0) == '<' || s.charAt(0) == '=' || s.charAt(0) == '>' || s.charAt(0) == '?') {
				// second rightmost BNF choice
				for (int i = 1; i < s.length(); i++) {
					if (isLowerCaseCharacter(s.charAt(i)) || isDecDigit(s.charAt(i)) || s.charAt(i) == '.' ||
							s.charAt(i) == '+' || s.charAt(i) == '-') {
						continue;
					}
					validChar = false;
				}
				return validChar;
			} 
			return false;
		} 
		return false;
	}

	public static boolean isLowerCaseCharacter(char c) {
		return Character.isLowerCase(c);
	}

	public static boolean isKeyword(String s) {
		return s.equals("define") || s.equals("let") || s.equals("cond") || s.equals("if") || s.equals("begin") ||
				s.equals("true") || s.equals("false");
	}

	public static boolean isBinDigit(char c) {
		return c == '0' || c == '1';
	}
	
	public static boolean isDecDigit(char c) {
		return Character.isDigit(c);
	}

	public static boolean isHexDigit(char c) {
		return Character.isDigit(c) || c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f' ||
				c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E' || c == 'F';
	}
	
	public static void initializeArrayLists() {
		tokensList = new ArrayList<>();
		lexemesList = new ArrayList<>();
		lineColumn = new ArrayList<>();
	}
	
	public static void initializePrintWriter(String fileName) throws IOException {
		outputParse = new PrintWriter(new FileWriter(fileName));
	}
	
	public static String toString(String token, String literal, int lineNo, int columnNo, PrintWriter write) {
		if (token.equals("LEXICAL ERROR")) {
			System.out.println(token + " [" + lineNo + ":" + columnNo + "]: Invalid token '" + literal + "'");
			write.println(token + " [" + lineNo + ":" + columnNo + "]: Invalid token '" + literal + "'");
			write.close();
			System.exit(1);
		}
		// write token to an output file called tokens.txt
		write.println(token + " [" + lineNo + ":" + columnNo + "]");
		
		// adding each token, its corresponding lexeme and line column numbers to ArrayLists to process them in parsing
		tokensList.add(token);
		lexemesList.add(literal);
		lineColumn.add("[" + lineNo + ":" + columnNo + "]");
		// System.out.println(token + " " + lineNo + ":" + columnNo);
		// write.println(token + " " + lineNo + ":" + columnNo);
		return "";
	}
}