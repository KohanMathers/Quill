window.addEventListener('load', () => {
    setTimeout(() => {
        document.querySelector('.loading').style.opacity = '0';
        setTimeout(() => {
            document.querySelector('.loading').style.display = 'none';
        }, 800);
    }, 2000);
});

const cursor = document.querySelector('.cursor');
const hoverElements = document.querySelectorAll('a, button, .feature-card');

document.addEventListener('mousemove', (e) => {
    cursor.style.left = e.clientX - 10 + 'px';
    cursor.style.top = e.clientY - 10 + 'px';
    cursor.classList.add('active');
});

hoverElements.forEach(el => {
    el.addEventListener('mouseenter', () => cursor.classList.add('hover'));
    el.addEventListener('mouseleave', () => cursor.classList.remove('hover'));
});

window.addEventListener('scroll', () => {
    const nav = document.querySelector('nav');
    if (window.scrollY > 100) {
        nav.classList.add('scrolled');
    } else {
        nav.classList.remove('scrolled');
    }
});

const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('active');
        }
    });
}, observerOptions);

document.querySelectorAll('.reveal').forEach(el => observer.observe(el));

const modeTabs = document.querySelectorAll('.mode-tab');
const installCode = document.getElementById('install-code');
const installDescription = document.getElementById('install-description');

const modeCommands = {
    admin: {
        code: '/quill scope create <scope_name> <owner> <x1> <y1> <z1> <x2> <y2> <z2>',
        description: 'Create a scope for a player to start scripting'
    },
    player: {
        code: '/quill edit <filename>.ql',
        description: 'Open the web editor to write and test your scripts'
    }
};

modeTabs.forEach(tab => {
    tab.addEventListener('click', () => {
        modeTabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');

        const mode = tab.dataset.mode;
        const command = modeCommands[mode];

        installCode.textContent = command.code;
        installDescription.textContent = command.description;
    });
});

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});