(() => {
    const $ = selector => document.querySelector(selector);
    const $$ = selector => [...document.querySelectorAll(selector)];
    const escapeHtml = value => String(value).replace(/[&<>'"]/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char]));
    const toast = message => { $('#toast span').textContent = message; $('#toast').classList.add('show'); setTimeout(() => $('#toast').classList.remove('show'), 2300); };

    const properties = [
        {name:'Студия у метро Динамо', address:'Ленинградский проспект, 33', price:'45 000', type:'Долгосрочно', photo:'room-one', status:'Оплачено'},
        {name:'Апартаменты «Сити»', address:'Пресненская набережная, 12', price:'7 500', type:'Посуточно', photo:'room-two', status:'Занято'},
        {name:'Квартира на Патриарших', address:'Большой Патриарший переулок, 8', price:'85 000', type:'Долгосрочно', photo:'room-three', status:'Активна'}
    ];

    const showScreen = name => {
        $$('.screen').forEach(el => el.classList.toggle('active', el.dataset.screen === name));
        $$('.bottom-nav button').forEach(el => el.classList.toggle('active', el.dataset.go === name));
        window.scrollTo({top: 0, behavior: 'smooth'});
    };
    $$('[data-go]').forEach(button => button.addEventListener('click', () => showScreen(button.dataset.go)));
    $('#profileButton').addEventListener('click', () => showScreen('profile'));

    const renderRent = () => {
        const saved = JSON.parse(localStorage.getItem('rentflow-properties') || '[]');
        $('#fullRentList').innerHTML = [...saved, ...properties].map((p, i) => `<article class="rent-card"><div class="rent-photo ${p.photo || ['room-one','room-two','room-three'][i%3]}"><span class="tag">${escapeHtml(p.type)}</span></div><div class="rent-body"><h3>${escapeHtml(p.name)}</h3><p>${escapeHtml(p.address)}</p><div><strong>${escapeHtml(p.price)} ₽ <small>/ ${p.type === 'Посуточно' ? 'сутки' : 'мес.'}</small></strong><span class="ok">${escapeHtml(p.status || 'Активна')}</span></div></div></article>`).join('');
    };
    renderRent();

    const roleSheet = $('#roleSheet');
    const setRole = role => {
        const tenant = role === 'tenant';
        const title = tenant ? 'Арендатор' : 'Арендодатель';
        $('#roleText').textContent = title; $('#profileRoleText').textContent = title;
        $('#roleSubtitle').textContent = tenant ? 'Аренда и платежи под контролем' : 'Ваши объекты под контролем';
        $$('.role-option').forEach(button => { const active = button.dataset.role === role; button.classList.toggle('selected', active); button.lastElementChild.textContent = active ? '✓' : ''; });
        localStorage.setItem('rentflow-role', role); roleSheet.hidden = true; toast(`Режим: ${title}`);
    };
    [$('#roleButton'), $('#profileRole')].forEach(button => button.addEventListener('click', () => roleSheet.hidden = false));
    $$('.role-option').forEach(button => button.addEventListener('click', () => setRole(button.dataset.role)));
    setRole(localStorage.getItem('rentflow-role') || 'landlord'); $('#toast').classList.remove('show');

    const addSheet = $('#addSheet');
    $$('[data-action="add"]').forEach(button => button.addEventListener('click', () => addSheet.hidden = false));
    $('.sheet-close').addEventListener('click', () => addSheet.hidden = true);
    $$('.sheet-backdrop').forEach(el => el.addEventListener('click', event => { if (event.target === el) el.hidden = true; }));
    $('#rentForm').addEventListener('submit', event => {
        event.preventDefault(); const data = Object.fromEntries(new FormData(event.target));
        const saved = JSON.parse(localStorage.getItem('rentflow-properties') || '[]');
        saved.unshift({...data, price:Number(data.price).toLocaleString('ru-RU'), status:'Активна'});
        localStorage.setItem('rentflow-properties', JSON.stringify(saved)); event.target.reset(); addSheet.hidden = true; renderRent(); toast('Аренда добавлена');
    });

    $('#payButton').addEventListener('click', event => { event.currentTarget.textContent = 'Оплачено ✓'; event.currentTarget.disabled = true; toast('Платёж отмечен'); });
    $$('[data-action="payment"]').forEach(button => button.addEventListener('click', () => { $('#payButton').click(); }));

    const upload = () => $('#fileInput').click();
    $$('[data-action="upload"]').forEach(button => button.addEventListener('click', () => { showScreen('files'); upload(); }));
    $('#fileInput').addEventListener('change', event => {
        [...event.target.files].forEach(file => {
            const article = document.createElement('article'); article.dataset.name = file.name;
            article.innerHTML = `<i class="doc">FILE</i><span><strong>${escapeHtml(file.name)}</strong><small>Загружено · ${(file.size/1024).toFixed(0)} КБ</small></span><time>сейчас</time><button>•••</button>`;
            $('#fileList').prepend(article);
        });
        if (event.target.files.length) toast(`Загружено файлов: ${event.target.files.length}`);
    });
    $('#fileSearch').addEventListener('input', event => $$('#fileList article').forEach(file => file.hidden = !file.dataset.name.toLowerCase().includes(event.target.value.toLowerCase())));
    $$('.segmented button, .file-filters button').forEach(button => button.addEventListener('click', () => { button.parentElement.querySelectorAll('button').forEach(b => b.classList.remove('active')); button.classList.add('active'); }));
    $('#recordButton').addEventListener('click', () => toast('Камера будет доступна в мобильном приложении'));
    $$('.play, .video-thumb i').forEach(button => button.addEventListener('click', () => toast('Запускаем видео…')));

    if ('serviceWorker' in navigator) window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => {}));
})();
