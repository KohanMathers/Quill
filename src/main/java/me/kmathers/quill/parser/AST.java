package me.kmathers.quill.parser;

import java.util.List;

public class AST {
    
    // Base class for all AST nodes
    public static abstract class ASTNode {
        public int line;
        public int column;
        
        public ASTNode(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }
    
    // Program - root node containing all statements
    public static class Program extends ASTNode {
        public List<ASTNode> statements;
        
        public Program(int line, int column) {
            super(line, column);
        }
    }
    
    // === Literals ===
    
    public static class NumberLiteral extends ASTNode {
        public double value;
        
        public NumberLiteral(double value, int line, int column) {
            super(line, column);
            this.value = value;
        }
    }
    
    public static class StringLiteral extends ASTNode {
        public String value;
        
        public StringLiteral(String value, int line, int column) {
            super(line, column);
            this.value = value;
        }
    }
    
    public static class BooleanLiteral extends ASTNode {
        public boolean value;
        
        public BooleanLiteral(boolean value, int line, int column) {
            super(line, column);
            this.value = value;
        }
    }
    
    public static class NullLiteral extends ASTNode {
        public NullLiteral(int line, int column) {
            super(line, column);
        }
    }
    
    public static class ListLiteral extends ASTNode {
        public List<ASTNode> elements;
        
        public ListLiteral(List<ASTNode> elements, int line, int column) {
            super(line, column);
            this.elements = elements;
        }
    }
    
    // === Identifiers and Member Access ===
    
    public static class Identifier extends ASTNode {
        public String name;
        
        public Identifier(String name, int line, int column) {
            super(line, column);
            this.name = name;
        }
    }
    
    public static class MemberExpression extends ASTNode {
        public ASTNode object;
        public String property;
        
        public MemberExpression(ASTNode object, String property, int line, int column) {
            super(line, column);
            this.object = object;
            this.property = property;
        }
    }
    
    // === Expressions ===
    
    public static class BinaryExpression extends ASTNode {
        public ASTNode left;
        public String operator;
        public ASTNode right;
        
        public BinaryExpression(ASTNode left, String operator, ASTNode right, int line, int column) {
            super(line, column);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }
    
    public static class UnaryExpression extends ASTNode {
        public String operator;
        public ASTNode operand;
        
        public UnaryExpression(String operator, ASTNode operand, int line, int column) {
            super(line, column);
            this.operator = operator;
            this.operand = operand;
        }
    }
    
    public static class AssignmentExpression extends ASTNode {
        public ASTNode target;
        public ASTNode value;
        
        public AssignmentExpression(ASTNode target, ASTNode value, int line, int column) {
            super(line, column);
            this.target = target;
            this.value = value;
        }
    }
    
    public static class CallExpression extends ASTNode {
        public ASTNode callee;
        public List<ASTNode> arguments;
        
        public CallExpression(ASTNode callee, List<ASTNode> arguments, int line, int column) {
            super(line, column);
            this.callee = callee;
            this.arguments = arguments;
        }
    }
    
    // === Statements ===
    
    public static class VariableDeclaration extends ASTNode {
        public String name;
        public ASTNode value;
        public boolean isConst;
        
        public VariableDeclaration(String name, ASTNode value, boolean isConst, int line, int column) {
            super(line, column);
            this.name = name;
            this.value = value;
            this.isConst = isConst;
        }
    }
    
    public static class FunctionDeclaration extends ASTNode {
        public String name;
        public List<String> parameters;
        public List<ASTNode> body;
        
        public FunctionDeclaration(String name, List<String> parameters, List<ASTNode> body, int line, int column) {
            super(line, column);
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }
    }
    
    public static class ReturnStatement extends ASTNode {
        public ASTNode value;
        
        public ReturnStatement(ASTNode value, int line, int column) {
            super(line, column);
            this.value = value;
        }
    }
    
    public static class IfStatement extends ASTNode {
        public ASTNode condition;
        public List<ASTNode> thenBranch;
        public List<ASTNode> elseBranch;
        
        public IfStatement(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch, int line, int column) {
            super(line, column);
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }
    
    public static class WhileStatement extends ASTNode {
        public ASTNode condition;
        public List<ASTNode> body;
        
        public WhileStatement(ASTNode condition, List<ASTNode> body, int line, int column) {
            super(line, column);
            this.condition = condition;
            this.body = body;
        }
    }
    
    public static class ForStatement extends ASTNode {
        public String variable;
        public ASTNode iterable;
        public List<ASTNode> body;
        
        public ForStatement(String variable, ASTNode iterable, List<ASTNode> body, int line, int column) {
            super(line, column);
            this.variable = variable;
            this.iterable = iterable;
            this.body = body;
        }
    }
    
    public static class BreakStatement extends ASTNode {
        public BreakStatement(int line, int column) {
            super(line, column);
        }
    }
    
    public static class ContinueStatement extends ASTNode {
        public ContinueStatement(int line, int column) {
            super(line, column);
        }
    }
    
    public static class TryStatement extends ASTNode {
        public List<ASTNode> tryBlock;
        public String errorVariable;
        public List<ASTNode> catchBlock;
        
        public TryStatement(List<ASTNode> tryBlock, String errorVariable, List<ASTNode> catchBlock, int line, int column) {
            super(line, column);
            this.tryBlock = tryBlock;
            this.errorVariable = errorVariable;
            this.catchBlock = catchBlock;
        }
    }
    
    // === Quill Specific Constructs ===
    
    public static class EventHandler extends ASTNode {
        public String eventName;
        public List<ASTNode> body;
        
        public EventHandler(String eventName, List<ASTNode> body, int line, int column) {
            super(line, column);
            this.eventName = eventName;
            this.body = body;
        }
    }
    
    // Note - The arguments mentioned below are (in order): x1, y1, z1, x2, y2, z2
    public static class ScopeCreation extends ASTNode {
        public List<ASTNode> arguments;
        
        public ScopeCreation(List<ASTNode> arguments, int line, int column) {
            super(line, column);
            this.arguments = arguments;
        }
    }
    
    // Expression statement - wraps an expression to be used as a statement
    public static class ExpressionStatement extends ASTNode {
        public ASTNode expression;
        
        public ExpressionStatement(ASTNode expression, int line, int column) {
            super(line, column);
            this.expression = expression;
        }
    }
}