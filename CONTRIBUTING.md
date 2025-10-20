# Contributing to Quill

Thank you for your interest in contributing to Quill! This document provides guidelines and information for contributors.

## Code of Conduct

- Be respectful and constructive
- Welcome newcomers and help them learn
- Focus on what's best for the community
- Show empathy towards other contributors

## How to Contribute

### Reporting Bugs

Before creating a bug report:
1. Check existing [Issues](https://github.com/kohanmathers/quill/issues) to avoid duplicates
2. Update to the latest version and see if the bug persists
3. Collect relevant information (server version, Quill version, error logs)

When filing a bug report, include:
- **Description** - Clear and concise summary of the bug
- **Steps to Reproduce** - Minimal steps to trigger the bug
- **Expected Behavior** - What should happen
- **Actual Behavior** - What actually happens
- **Environment** - Server version, Quill version, Java version
- **Logs** - Relevant error messages or stack traces
- **Script** - The Quill script that causes the issue (if applicable)

### Suggesting Features

Feature requests are welcome! When suggesting a feature:
- Explain the **use case** - what problem does it solve?
- Provide **examples** - show how it would be used
- Consider **scope** - does it fit Quill's philosophy?
- Check if it's **already planned** in the [Roadmap](README.md#roadmap)

### Contributing Code

#### Getting Started

1. **Fork the repository**
2. **Clone your fork**
   ```bash
   git clone https://github.com/kohammathers/quill.git
   cd quill
   ```
3. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

#### Development Setup

```bash
# Run tests
mvn test

# Create a plugin JAR
mvn clean package
```

#### Code Style

- **Java 21** features are encouraged
- **4 spaces** for indentation (no tabs)
- **Clear naming** - descriptive variable and method names
- **Comments** - explain *why*, not *what*
- **Test coverage** - add tests for new features

Follow standard Java conventions:
```java
public class ExampleClass {
    private String fieldName;
    
    public void methodName(String paramName) {
        if (condition) {
            // Do something
        }
    }
}
```

#### Testing

- Unit tests are encouraged but not enforced, I will forget to write them myself
- Test with a real Minecraft server when possible

#### Commit Messages

Use clear, descriptive commit messages:

```
Add support for custom event triggers

- Implement trigger_custom() built-in function
- Add CustomEvent handler to event bridge
- Update documentation with examples
```

Format:
- **First line**: Brief summary (50 chars or less)
- **Body**: Detailed explanation if needed
- **Reference issues**: Use `Fixes #123` or `Relates to #456`

#### Pull Request Process

1. **Update documentation** - README, SPEC.md, or inline docs if needed
2. **Add tests** - Ensure your changes are tested
3. **Update changelog** - Add entry to `CHANGELOG.md` (if it exists)
4. **Lint your code** - Run `./gradlew check`
5. **Create the PR** with a clear description:
   - What does this PR do?
   - Why is this change needed?
   - How has it been tested?
   - Screenshots/examples if applicable

**PR Checklist:**
- [ ] Code follows project style guidelines
- [ ] Tests added/updated and passing
- [ ] Documentation updated
- [ ] No breaking changes (or clearly documented if necessary)
- [ ] Commits are clear and well-organized

### Contributing Documentation

Documentation contributions are highly valued! You can help by:
- Fixing typos or unclear explanations
- Adding examples and tutorials
- Improving API documentation
- Translating documentation *(future)*

## Development Guidelines

### Project Structure

```
quill/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── me/kmathers/quill/
│   │   │       ├── lexer/        # Tokenization
│   │   │       ├── parser/       # AST generation
│   │   │       ├── interpreter/  # Execution engine
│   │   │       ├── scope/        # Scope management
│   │   │       ├── events/       # Event handlers
│   │   │       ├── functions/    # Built-in functions
│   │   │       └── plugin/       # Bukkit integration
│   │   └── resources/
│   │       ├── plugin.yml
│   │       └── config.yml
│   └── test/
│       └── java/
│           └── me/kmathers/quill/
├── examples/                      # Example scripts
├── docs/                          # Documentation
├── SPEC.md                        # Language specification
└── README.md
```

### Key Design Principles

1. **Safety First** - All scripts run in sandboxes with permission controls
2. **Simple Syntax** - Readable by non-programmers
3. **Event-Driven** - Natural fit for Minecraft's event model
4. **Performance** - Scripts should be fast and not lag the server
5. **Debuggable** - Clear error messages and logging

### Adding Built-in Functions

When adding a new built-in function:

1. Add to `functions/` package
2. Register in `FunctionRegistry`
3. Update `SPEC.md` with documentation
4. Add test coverage
5. Consider permission implications

Example:
```java
@BuiltInFunction("my_function")
public class MyFunction implements QuillFunction {
    @Override
    public Object execute(List<Object> args, ScopeContext context) {
        // Implementation
    }
}
```

### Adding Event Handlers

When adding a new event type:

1. Add to `events/` package
2. Create Bukkit event listener
3. Bridge to Quill interpreter
4. Update `SPEC.md` with event documentation
5. Add examples

## License

By contributing to Quill, you agree that your contributions will be licensed under the MIT License and that the project maintainers have the right to relicense your contributions in derivative works (including potential commercial versions).

This ensures the project can:
- Remain open source under MIT
- Be used in commercial/closed-source derivatives by the maintainers
- Evolve without legal complications

You retain copyright to your contributions, but grant these rights to the project.

## Questions?

- **General questions** - Use [GitHub Discussions](https://github.com/kohanmathers/quill/discussions)
- **Bug reports** - Use [GitHub Issues](https://github.com/kohanmathers/quill/issues)
- **Security issues** - Email mathers.kohan@gmail.com

## Recognition

Contributors will be recognized in:
- `CONTRIBUTORS.md` file
- Release notes for significant contributions
- Project documentation where applicable

Thank you for helping make Quill better! 🎉
