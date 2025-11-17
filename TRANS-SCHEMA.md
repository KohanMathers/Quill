# Quill I18n Style Guide

## 1. Global Namespace Rule

All keys MUST be inside a single top-level namespace:

```
quill:
```

This prevents collisions with other plugins or systems.

---

## 2. Structure for New Keys

All keys must follow this pattern:

```
<namespace>.<component>.<subsystem>.<category>.<reason>
```

Example:

```
quill.error.runtime.interpreter.unknown-op
```

---

## 3. Domain Separation Rules

You **must not** mix types of messages.

### User-facing errors → `quill.error.user.*`

Messages the player should see.

### Runtime/internal errors → `quill.error.runtime.*`

Interpreter failures, AST issues, IO failures.

### Developer mistakes → `quill.error.developer.*`

Command misuse, bad arguments, invalid API use.

---

## 4. Naming Conventions

### 4.1 Key segments must be lowercase, hyphen-separated

Good:

```
invalid-prop
not-func
under-1-item
```

Bad:

```
InvalidProp
notFunc
UnderOneItem
```

### 4.2 Never repeat words across levels

Bad:

```
error.error-type.invalid.error
```

Good:

```
error.runtime.interpreter.invalid-op
```

---

## 5. Pluralisation Rule

Plural-sensitive messages must use:

```
<reason>.single
<reason>.multiple
```

Example:

```
requires.single
requires.multiple
```

---

## 6. Placeholder Convention

Placeholders MUST be positional: `{0}`, `{1}`, `{2}`.

Never use named placeholders (e.g., `{filename}`) to ensure compatibility with minimessage and Adventure placeholders.

---

## 7. Future Expansion Stubs

The following sections must always exist even if empty:

```
internal:
error.user:
error.runtime:
```

This ensures API stability for external plugins.

---

## 8. Key Length Policy

Keys may be long but must remain semantic.
Examples of acceptable long keys:

```
quill.error.runtime.interpreter.unknown-prop
quill.commands.scripts.saved
```

Forbidden:

```
quill.error.thing.bad
quill.oops.error123
```