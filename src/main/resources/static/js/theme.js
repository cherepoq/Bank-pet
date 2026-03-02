(() => {
    const root = document.documentElement;
    const key = 'bankpet-theme';

    const setTheme = (theme) => {
        root.setAttribute('data-theme', theme);
        localStorage.setItem(key, theme);
        const btn = document.getElementById('themeToggle');
        if (btn) {
            btn.textContent = theme === 'dark' ? '☀️ Светлая тема' : '🌙 Тёмная тема';
        }
    };

    const saved = localStorage.getItem(key);
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    setTheme(saved || (prefersDark ? 'dark' : 'light'));

    const setupVoice = () => {
        const agentText = document.getElementById('agentText');
        if (agentText && 'speechSynthesis' in window) {
            const utterance = new SpeechSynthesisUtterance(agentText.textContent);
            utterance.lang = 'ru-RU';
            utterance.rate = 1.0;
            window.speechSynthesis.cancel();
            window.speechSynthesis.speak(utterance);
        }

        const micBtn = document.getElementById('voiceCommandBtn');
        const voiceStatus = document.getElementById('voiceStatus');
        if (!micBtn || !voiceStatus) return;

        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRecognition) {
            voiceStatus.textContent = 'Распознавание речи не поддерживается в этом браузере.';
            micBtn.disabled = true;
            return;
        }

        const recognition = new SpeechRecognition();
        recognition.lang = 'ru-RU';
        recognition.interimResults = false;
        recognition.maxAlternatives = 1;

        micBtn.addEventListener('click', () => {
            voiceStatus.textContent = 'Слушаю... Скажи: "оплатить" или "отмена".';
            recognition.start();
        });

        recognition.onresult = (event) => {
            const text = (event.results?.[0]?.[0]?.transcript || '').toLowerCase();
            voiceStatus.textContent = `Распознано: ${text}`;
            if (text.includes('оплат') || text.includes('да')) {
                document.getElementById('confirmPayBtn')?.click();
            } else if (text.includes('отмен') || text.includes('нет') || text.includes('стоп')) {
                document.getElementById('cancelPayBtn')?.click();
            }
        };

        recognition.onerror = () => {
            voiceStatus.textContent = 'Ошибка распознавания речи. Попробуй ещё раз.';
        };
    };

    window.addEventListener('DOMContentLoaded', () => {
        const btn = document.getElementById('themeToggle');
        if (btn) {
            btn.addEventListener('click', () => {
                const current = root.getAttribute('data-theme') || 'light';
                setTheme(current === 'dark' ? 'light' : 'dark');
            });
        }

        document.querySelectorAll('.tab-btn').forEach(tabBtn => {
            tabBtn.addEventListener('click', () => {
                document.querySelectorAll('.tab-btn').forEach(x => x.classList.remove('active'));
                document.querySelectorAll('.tab-content').forEach(x => x.classList.remove('active'));
                tabBtn.classList.add('active');
                const pane = document.getElementById(tabBtn.dataset.tab);
                if (pane) pane.classList.add('active');
            });
        });

        setupVoice();
    });

    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => {}));
    }
})();
