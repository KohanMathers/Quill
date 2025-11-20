package me.kmathers.quill.interpreter;

public class LoopDetector {
    private static final int DEFAULT_MAX_ITERATIONS = 10000;
    private static final long DEFAULT_MAX_EXECUTION_TIME_MS = 5000;
    
    private final int maxIterations;
    private final long maxExecutionTimeMs;
    
    private int currentIterations;
    private long loopStartTime;
    private boolean isActive;
    
    public LoopDetector() {
        this(DEFAULT_MAX_ITERATIONS, DEFAULT_MAX_EXECUTION_TIME_MS);
    }
    
    public LoopDetector(int maxIterations, long maxExecutionTimeMs) {
        this.maxIterations = maxIterations;
        this.maxExecutionTimeMs = maxExecutionTimeMs;
        this.currentIterations = 0;
        this.isActive = false;
    }
    
    public void startLoop() {
        this.currentIterations = 0;
        this.loopStartTime = System.currentTimeMillis();
        this.isActive = true;
    }
    
    public void checkIteration() throws InfiniteLoopException {
        if (!isActive) return;
        
        currentIterations++;
        
        if (currentIterations > maxIterations) {
            throw new InfiniteLoopException(
                "Loop exceeded maximum iterations (" + maxIterations + "). " +
                "Possible infinite loop detected."
            );
        }
        
        long elapsed = System.currentTimeMillis() - loopStartTime;
        if (elapsed > maxExecutionTimeMs) {
            throw new InfiniteLoopException(
                "Loop exceeded maximum execution time (" + maxExecutionTimeMs + "ms). " +
                "Possible infinite loop detected."
            );
        }
    }

    public void endLoop() {
        this.isActive = false;
        this.currentIterations = 0;
    }

    public int getCurrentIterations() {
        return currentIterations;
    }

    public static class InfiniteLoopException extends RuntimeException {
        public InfiniteLoopException(String message) {
            super(message);
        }
    }
}