import * as monaco from 'https://cdn.jsdelivr.net/npm/monaco-editor@0.52.0/+esm';

const RELAY_URL = 'https://quill-relay.kohanmathersmcgonnell.workers.dev';
let sessionId = null;
let editorInstance = null;
let lastActivity = Date.now();
let inactivityTimer = null;
let warningTimer = null;
let hasUnsavedChanges = false;

function createParticles() {
    const particles = document.getElementById('particles');
    const symbols = ['let', 'func', 'if', '{', '}', '(', ')', 'OnEvent', '→', '⚡'];
    for (let i = 0; i < 15; i++) {
        const particle = document.createElement('div');
        particle.className = 'particle';
        particle.textContent = symbols[Math.floor(Math.random() * symbols.length)];
        particle.style.left = Math.random() * 100 + '%';
        particle.style.top = Math.random() * 100 + '%';
        particle.style.animationDelay = Math.random() * 5 + 's';
        particle.style.animationDuration = (10 + Math.random() * 10) + 's';
        particles.appendChild(particle);
    }
}

createParticles();

registerQuill();

if (window.location.hash) {
    const hashSessionId = window.location.hash.substring(1);
    if (hashSessionId.length === 8) {
        document.getElementById('sessionId').value = hashSessionId;
        loadSession();
    }
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const icons = {
        success: '✓',
        error: '✕',
        info: 'ℹ'
    };

    toast.innerHTML = `
                <span class="toast-icon">${icons[type] || icons.info}</span>
                <span class="toast-message">${message}</span>
            `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'toastSlide 0.3s ease-out reverse';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function updateStats() {
    if (!editorInstance) return;
    const content = editorInstance.getValue();
    const lines = content.split('\n').length;
    const chars = content.length;
    document.getElementById('lineCount').textContent = lines;
    document.getElementById('charCount').textContent = chars;
}

function markUnsaved() {
    hasUnsavedChanges = true;
    document.getElementById('saveButton').classList.add('unsaved');
}

function markSaved() {
    hasUnsavedChanges = false;
    document.getElementById('saveButton').classList.remove('unsaved');
}

async function loadSession() {
    const input = document.getElementById('sessionId').value.toLowerCase().trim();
    if (input.length !== 8) {
        showToast('Session ID must be 8 characters', 'error');
        return;
    }

    sessionId = input;
    showToast('Connecting to session...', 'info');

    try {
        const response = await fetch(`${RELAY_URL}/session/${sessionId}`);
        if (!response.ok) throw new Error('Session not found');

        const content = await response.text();
        document.getElementById('currentSession').textContent = sessionId;
        document.getElementById('sessionInput').style.display = 'none';
        document.getElementById('editorContainer').style.display = 'flex';
        document.getElementById('statusDot').classList.add('connected');

        window.location.hash = sessionId;

        if (!editorInstance) {
            editorInstance = monaco.editor.create(document.getElementById('editor'), {
                value: content,
                language: 'quill',
                theme: 'quill-dark',
                automaticLayout: true,
                fontSize: 14,
                minimap: {
                    enabled: true
                },
                lineNumbers: 'on',
                roundedSelection: false,
                scrollBeyondLastLine: false,
                renderWhitespace: 'selection'
            });

            editorInstance.onDidChangeModelContent(() => {
                lastActivity = Date.now();
                resetInactivityTimers();
                markUnsaved();
                updateStats();
            });

            editorInstance.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
                saveContent();
            });
        } else {
            editorInstance.setValue(content);
        }

        updateStats();
        startInactivityTracking();
        showToast('Connected successfully!', 'success');

    } catch (error) {
        showToast('Failed to load session: ' + error.message, 'error');
    }
}

async function saveContent() {
    if (!hasUnsavedChanges) {
        showToast('No changes to save', 'info');
        return;
    }

    const content = editorInstance.getValue();
    const button = document.getElementById('saveButton');
    button.disabled = true;
    showToast('Saving to server...', 'info');

    try {
        const response = await fetch(`${RELAY_URL}/session/${sessionId}`, {
            method: 'POST',
            body: content,
            headers: {
                'Content-Type': 'text/plain'
            }
        });

        if (!response.ok) throw new Error('Failed to save');

        markSaved();
        showToast('Saved successfully!', 'success');
        setTimeout(() => {
            window.location.hash = '';
            window.location.reload();
        }, 2000);
    } catch (error) {
        showToast('Failed to save: ' + error.message, 'error');
        button.disabled = false;
    }
}

function startInactivityTracking() {
    resetInactivityTimers();
}

function resetInactivityTimers() {
    clearTimeout(inactivityTimer);
    clearTimeout(warningTimer);
    document.getElementById('inactivityWarning').style.display = 'none';

    inactivityTimer = setTimeout(() => {
        document.getElementById('inactivityWarning').style.display = 'block';

        warningTimer = setTimeout(() => {
            showToast('Session closed due to inactivity', 'error');
            setTimeout(() => window.location.reload(), 2000);
        }, 15 * 60 * 1000);

    }, 15 * 60 * 1000);
}

function dismissInactivityWarning() {
    lastActivity = Date.now();
    resetInactivityTimers();
    showToast('Welcome back!', 'success');
}

function registerQuill() {
    const QUILL_ID = 'quill';

    monaco.languages.register({
        id: QUILL_ID
    });

    const QUILL_KEYWORDS = [
        'let', 'func', 'return', 'if', 'else', 'for', 'in', 'while',
        'break', 'continue', 'new', 'OnEvent', 'try', 'catch', 'null', 'true', 'false'
    ];

    const QUILL_TYPES = [
        'Player', 'Location', 'Item', 'Scope', 'List', 'Entity', 'World', 'Region'
    ];

    const QUILL_BUILTINS = {
        'teleport(player, x, y, z)': 'Teleports player to coordinates → Boolean',
        'teleport(player, location)': 'Teleports player to location → Boolean',
        'give(player, item_id)': 'Give item to player → Boolean',
        'give(player, item_id, amount)': 'Give items to player → Boolean',
        'give(player, item)': 'Give item object to player → Boolean',
        'remove_item(player, item_id)': 'Remove all items of type → Number',
        'remove_item(player, item_id, amount)': 'Remove items from player → Number',
        'has_item(player, item_id)': 'Check if player has item → Boolean',
        'has_item(player, item_id, amount)': 'Check if player has amount of item → Boolean',
        'set_gamemode(player, mode)': 'Set player gamemode ("survival", "creative", "adventure", "spectator") → Boolean',
        'set_health(player, amount)': 'Set player health (0-20) → Boolean',
        'set_hunger(player, amount)': 'Set player hunger (0-20) → Boolean',
        'heal(player)': 'Fully heal the player → Boolean',
        'kill(player)': 'Kill the player → Boolean',
        'set_flying(player, flying)': 'Set player flying state → Boolean',
        'kick(player, reason)': 'Kick player from server → Boolean',
        'get_health(player)': 'Get player health → Number',
        'get_hunger(player)': 'Get player hunger → Number',
        'get_location(player)': 'Get player location → Location',
        'get_gamemode(player)': 'Get player gamemode → String',
        'get_name(player)': 'Get player name → String',
        'is_online(player)': 'Check if player is online → Boolean',
        'is_op(player)': 'Check if player is operator → Boolean',
        'get_player(name)': 'Get player by name → Player',
        'get_online_players()': 'Get all online players → List',
        'sendmessage(player, message)': 'Send chat message to player → Boolean',
        'sendtitle(player, title, subtitle, fade_in, stay, fade_out)': 'Send title to player (times in ticks) → Boolean',
        'playsound(player, sound, volume, pitch)': 'Play a sound to player → Boolean',
        'broadcast(message)': 'Send message to all players in scope → Boolean',
        'give_effect(player, effect, duration, amplifier)': 'Give player potion effect (duration in ticks) → Boolean',
        'remove_effect(player, effect)': 'Remove potion effect → Boolean',
        'clear_effects(player)': 'Remove all potion effects → Boolean',
        'addtoscope(player, scope)': 'Add player to scope → Boolean',
        'removefromscope(player, scope)': 'Remove player from scope → Boolean',
        'getplayers(scope)': 'Get list of players in scope → List',
        'in_region(player, scope)': 'Check if player is within scope region → Boolean',
        'in_region(location, scope)': 'Check if location is within scope region → Boolean',
        'get_region(scope)': 'Get scope region bounds (x1,y1,z1,x2,y2,z2) → Region',
        'set_region(scope, x1, y1, z1, x2, y2, z2)': 'Update scope physical region → Boolean',
        'set_block(x, y, z, block_id)': 'Set block at coordinates → Boolean',
        'set_block(location, block_id)': 'Set block at location → Boolean',
        'get_block(x, y, z)': 'Get block type at coordinates → String',
        'get_block(location)': 'Get block type at location → String',
        'break_block(x, y, z)': 'Break block naturally (drops items) → Boolean',
        'break_block(location)': 'Break block at location naturally → Boolean',
        'spawn_entity(entity_type, x, y, z)': 'Spawn entity at coordinates → Entity',
        'spawn_entity(entity_type, location)': 'Spawn entity at location → Entity',
        'remove_entity(entity)': 'Remove entity from world → Boolean',
        'create_explosion(x, y, z, power)': 'Create explosion (power 0-10) → Boolean',
        'create_explosion(location, power)': 'Create explosion at location → Boolean',
        'create_explosion(x, y, z, power, fire)': 'Create explosion with fire option → Boolean',
        'create_explosion(location, power, fire)': 'Create explosion at location with fire → Boolean',
        'strike_lightning(x, y, z)': 'Strike lightning at coordinates → Boolean',
        'strike_lightning(location)': 'Strike lightning at location → Boolean',
        'set_time(world, time)': 'Set world time (0-24000) → Boolean',
        'get_time(world)': 'Get world time → Number',
        'set_weather(world, weather, duration)': 'Set weather ("clear", "rain", "thunder"), duration in ticks → Boolean',
        'get_weather(world)': 'Get current weather → String',
        'get_world(name)': 'Get world by name → World',
        'cancel(event)': 'Cancel current event (prevents default behavior) → Boolean',
        'trigger_custom(event_name, data)': 'Trigger custom event with data → Boolean',
        'wait(ticks)': 'Async delay (non-blocking) → Boolean',
        'random(max)': 'Random number (0 to max) → Number',
        'random(min, max)': 'Random number (min to max) → Number',
        'random_choice(list)': 'Random item from list → Any',
        'round(number)': 'Round to nearest integer → Number',
        'floor(number)': 'Round down → Number',
        'ceil(number)': 'Round up → Number',
        'abs(number)': 'Absolute value → Number',
        'sqrt(number)': 'Square root → Number',
        'pow(base, exponent)': 'Power → Number',
        'distance(loc1, loc2)': 'Distance between locations → Number',
        'distance(x1, y1, z1, x2, y2, z2)': 'Distance between coordinates → Number',
        'len(list)': 'Length of list → Number',
        'len(string)': 'Length of string → Number',
        'append(list, item)': 'Add item to end of list → Boolean',
        'remove(list, index)': 'Remove and return item at index → Any',
        'contains(list, item)': 'Check if list contains item → Boolean',
        'contains(string, substring)': 'Check if string contains substring → Boolean',
        'split(string, delimiter)': 'Split string into list → List',
        'join(list, delimiter)': 'Join list into string → String',
        'to_string(value)': 'Convert to string → String',
        'to_number(value)': 'Convert to number → Number',
        'to_boolean(value)': 'Convert to boolean → Boolean',
        'type_of(value)': 'Get type name → String',
        'location(x, y, z)': 'Create location object → Location',
        'location(x, y, z, world)': 'Create location object with world → Location',
        'item(item_id)': 'Create item object → Item',
        'item(item_id, amount)': 'Create item object with amount → Item',
        'item(item_id, amount, metadata)': 'Create item object with metadata → Item',
        'log(message)': 'Log to server console → Boolean'
    };

    monaco.languages.setMonarchTokensProvider(QUILL_ID, {
        defaultToken: '',
        tokenPostfix: '.quill',

        keywords: QUILL_KEYWORDS,
        types: QUILL_TYPES,
        builtins: Object.keys(QUILL_BUILTINS).map(sig => sig.split('(')[0]),

        operators: [
            '+', '-', '*', '/', '%', '==', '!=', '>', '<', '>=', '<=', '&&', '||', '!', '='
        ],

        tokenizer: {
            root: [{
                    include: '@whitespace'
                },

                [/\/\/.*$/, 'comment'],
                [/\/\*/, 'comment', '@comment'],

                [/\bOnEvent\b(?=\s*\()/, 'keyword', '@eventdecl'],

                [/\bfunc\b/, 'keyword', '@funcdef'],

                [/\blet\b/, 'keyword'],

                [/-?\b\d+(\.\d+)?\b/, 'number'],

                [/\blocation\b(?=\s*\()/, 'type.identifier'],
                [/\bitem\b(?=\s*\()/, 'type.identifier'],

                [/\b(new|return|try|catch|break|continue|if|else|for|in|while)\b/, 'keyword'],

                [/\b(true|false|null)\b/, 'constant'],

                [/\b([a-z_][\w$]*)\s*(?=\()/, {
                    cases: {
                        '@builtins': 'predefined',
                        '@default': 'identifier'
                    }
                }],

                [/[A-Z][\w$]*/, {
                    cases: {
                        '@types': 'type',
                        '@default': 'type'
                    }
                }],

                [/[{}()\[\]]/, '@brackets'],
                [/[;,.]/, 'delimiter'],

                [/"/, {
                    token: 'string.quote',
                    bracket: '@open',
                    next: '@string_double'
                }],
                [/'/, {
                    token: 'string.quote',
                    bracket: '@open',
                    next: '@string_single'
                }],

                [/[a-zA-Z_][\w$]*/, 'identifier'],
            ],

            eventdecl: [
                [/\s+/, 'white'],
                [/\(/, 'delimiter.parenthesis', '@eventName'],
                ['', '', '@pop']
            ],

            eventName: [
                [/[A-Za-z_][\w$]*/, 'type'],
                [/\)/, 'delimiter.parenthesis', '@pop']
            ],

            funcdef: [
                [/\s+/, 'white'],
                [/[a-zA-Z_][\w$]*/, 'entity.name.function', '@funcParams'],
                ['', '', '@pop']
            ],

            funcParams: [
                [/\(/, 'delimiter.parenthesis', '@params'],
                ['', '', '@pop']
            ],

            params: [
                [/\)/, 'delimiter.parenthesis', '@pop'],
                [/[a-zA-Z_][\w$]*/, 'variable.parameter'],
                [/,/, 'delimiter'],
                [/\s+/, 'white']
            ],

            comment: [
                [/[^\/*]+/, 'comment'],
                [/\*\//, 'comment', '@pop'],
                [/[\/*]/, 'comment']
            ],

            string_double: [
                [/\{/, {
                    token: 'delimiter.bracket',
                    next: '@interpolation'
                }],
                [/[^{"\\]+/, 'string'],
                [/\\./, 'string.escape.invalid'],
                [/"/, {
                    token: 'string.quote',
                    bracket: '@close',
                    next: '@pop'
                }]
            ],

            string_single: [
                [/\{/, {
                    token: 'delimiter.bracket',
                    next: '@interpolation'
                }],
                [/[^{'\\]+/, 'string'],
                [/\\./, 'string.escape.invalid'],
                [/'/, {
                    token: 'string.quote',
                    bracket: '@close',
                    next: '@pop'
                }]
            ],

            interpolation: [
                [/\}/, {
                    token: 'delimiter.bracket',
                    next: '@pop'
                }],
                {
                    include: 'root'
                }
            ],

            whitespace: [
                [/[ \t\r\n]+/, 'white'],
            ],
        }
    });

    monaco.languages.setLanguageConfiguration(QUILL_ID, {
        comments: {
            lineComment: '//',
            blockComment: ['/*', '*/']
        },
        brackets: [
            ['{', '}'],
            ['[', ']'],
            ['(', ')']
        ],
        autoClosingPairs: [{
                open: '{',
                close: '}'
            },
            {
                open: '[',
                close: ']'
            },
            {
                open: '(',
                close: ')'
            },
            {
                open: '"',
                close: '"'
            },
            {
                open: "'",
                close: "'"
            }
        ],
        surroundingPairs: [{
                open: '"',
                close: '"'
            },
            {
                open: "'",
                close: "'"
            },
            {
                open: '(',
                close: ')'
            },
            {
                open: '[',
                close: ']'
            }
        ]
    });

    monaco.editor.defineTheme('quill-dark', {
        base: 'vs-dark',
        inherit: true,
        rules: [{
                token: 'keyword',
                foreground: 'C586C0',
                fontStyle: 'bold'
            },
            {
                token: 'type',
                foreground: '4EC9B0'
            },
            {
                token: 'type.identifier',
                foreground: '4EC9B0'
            },
            {
                token: 'predefined',
                foreground: 'DCDCAA'
            },
            {
                token: 'number',
                foreground: 'B5CEA8'
            },
            {
                token: 'string',
                foreground: 'CE9178'
            },
            {
                token: 'string.quote',
                foreground: 'CE9178'
            },
            {
                token: 'comment',
                foreground: '6A9955',
                fontStyle: 'italic'
            },
            {
                token: 'identifier',
                foreground: 'D4D4D4'
            },
            {
                token: 'entity.name.function',
                foreground: 'DCDCAA'
            },
            {
                token: 'variable.parameter',
                foreground: '9CDCFE'
            },
            {
                token: 'delimiter',
                foreground: 'D4D4D4'
            },
            {
                token: 'constant',
                foreground: '569CD6',
                fontStyle: 'bold'
            },
        ],
        colors: {
            'editor.background': '#1e1e1e',
            'editorLineNumber.foreground': '#858585'
        }
    });

    monaco.languages.registerCompletionItemProvider(QUILL_ID, {
        triggerCharacters: ['.', ' ', '(', '{'],
        provideCompletionItems: function (model, position) {
            const word = model.getWordUntilPosition(position);
            const range = {
                startLineNumber: position.lineNumber,
                endLineNumber: position.lineNumber,
                startColumn: word.startColumn,
                endColumn: word.endColumn
            };

            const suggestions = [];

            for (const kw of QUILL_KEYWORDS) {
                suggestions.push({
                    label: kw,
                    kind: monaco.languages.CompletionItemKind.Keyword,
                    insertText: kw,
                    range
                });
            }

            Object.keys(QUILL_BUILTINS).forEach((sig) => {
                const label = sig.split('(')[0];
                suggestions.push({
                    label: sig,
                    kind: monaco.languages.CompletionItemKind.Function,
                    insertText: label + '(',
                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                    documentation: QUILL_BUILTINS[sig],
                    range
                });
            });

            QUILL_TYPES.forEach(t => suggestions.push({
                label: t,
                kind: monaco.languages.CompletionItemKind.Class,
                insertText: t,
                range
            }));

            return {
                suggestions
            };
        }
    });

    monaco.languages.registerHoverProvider(QUILL_ID, {
        provideHover: function (model, position) {
            const word = model.getWordAtPosition(position);
            if (!word) return null;
            const token = word.word;
            const match = Object.keys(QUILL_BUILTINS).find(k => k.startsWith(token + '(') || k === token);
            if (match) {
                return {
                    range: new monaco.Range(position.lineNumber, word.startColumn, position.lineNumber, word.endColumn),
                    contents: [{
                            value: `**${match}**`
                        },
                        {
                            value: QUILL_BUILTINS[match]
                        }
                    ]
                };
            }
            return null;
        }
    });
}

document.getElementById('sessionId').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') loadSession();
});

window.addEventListener('beforeunload', (e) => {
    if (hasUnsavedChanges) {
        e.preventDefault();
        e.returnValue = '';
    }
});

window.loadSession = loadSession;
window.saveContent = saveContent;
window.dismissInactivityWarning = dismissInactivityWarning;