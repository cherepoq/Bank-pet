(() => {
    const $ = selector => document.querySelector(selector);
    const $$ = selector => [...document.querySelectorAll(selector)];
    const escapeHtml = value => String(value).replace(/[&<>'"]/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char]));
    const toast = message => { $('#toast span').textContent = message; $('#toast').classList.add('show'); setTimeout(() => $('#toast').classList.remove('show'), 2300); };
    const defaultPreview = {amount: 45000, days: 4, property: 'Студия у метро Динамо', color: '#0ca56d'};

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
    let currentRole = localStorage.getItem('rentflow-role') || 'landlord';
    const updatePaymentForRole = role => {
        const tenant = role === 'tenant';
        $('#paymentCaption').textContent = tenant ? 'БЛИЖАЙШИЙ ПЛАТЁЖ' : 'БЛИЖАЙШЕЕ ПОСТУПЛЕНИЕ';
        $('#amountLabel').textContent = tenant ? 'К ОПЛАТЕ' : 'К ПОЛУЧЕНИЮ';
        $('#partyLabel').textContent = tenant ? 'Арендодатель' : 'Арендатор';
        $('#partyName').textContent = tenant ? 'Алексей Морозов' : 'Мария Соколова';
        $('#payButton').textContent = tenant ? 'Отметить оплату' : 'Подтвердить получение';
        $('#paymentCard').classList.remove('paid');
        $('#payButton').disabled = false;
    };
    const setRole = role => {
        const tenant = role === 'tenant';
        const title = tenant ? 'Арендатор' : 'Арендодатель';
        $('#roleText').textContent = title; $('#profileRoleText').textContent = title;
        $('#roleSubtitle').textContent = tenant ? 'Аренда и платежи под контролем' : 'Ваши объекты под контролем';
        $$('.role-option').forEach(button => { const active = button.dataset.role === role; button.classList.toggle('selected', active); button.lastElementChild.textContent = active ? '✓' : ''; });
        currentRole = role; updatePaymentForRole(role);
        localStorage.setItem('rentflow-role', role); roleSheet.hidden = true; toast(`Режим: ${title}`);
    };
    [$('#roleButton'), $('#profileRole')].forEach(button => button.addEventListener('click', () => roleSheet.hidden = false));
    $$('.role-option').forEach(button => button.addEventListener('click', () => setRole(button.dataset.role)));
    setRole(currentRole); $('#toast').classList.remove('show');

    const pluralDays = value => value % 10 === 1 && value % 100 !== 11 ? 'день' : ([2,3,4].includes(value % 10) && ![12,13,14].includes(value % 100) ? 'дня' : 'дней');
    const operationsKey = 'rentflow-payment-operations';
    const getOperations = () => JSON.parse(localStorage.getItem(operationsKey) || '[]');
    const getOutstanding = () => Math.max(0, Number(previewSettings?.amount || defaultPreview.amount) + getOperations().reduce((sum, item) => sum + (item.kind === 'charge' ? item.amount : -item.amount), 0));
    const applyPreview = settings => {
        const days = Math.max(0, Number(settings.days));
        $('#days').textContent = days; $('#daysWord').textContent = pluralDays(days);
        $('.timer-wrap').setAttribute('aria-label', `До платежа ${days} ${pluralDays(days)}`);
        $('#timerProgress').style.strokeDashoffset = String(326.73 * (1 - Math.min(days, 30) / 30));
        $('#paymentAmount').innerHTML = `${Number(settings.amount).toLocaleString('ru-RU')} <small>₽</small>`;
        $('.payment-amount p').textContent = settings.property;
        document.documentElement.style.setProperty('--green', settings.color);
        $('#demoAmount').value = settings.amount; $('#demoDays').value = days; $('#demoProperty').value = settings.property;
        $('#demoColor').value = settings.color; $('#demoColorValue').textContent = settings.color;
        renderOperations();
    };
    let previewSettings = {...defaultPreview, ...JSON.parse(localStorage.getItem('rentflow-preview') || '{}')};
    applyPreview(previewSettings);

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

    const paymentSheet = $('#paymentSheet');
    function renderOperations() {
        if (!$('#operationList')) return;
        const operations = getOperations(); const outstanding = getOutstanding();
        $('#paymentAmount').innerHTML = `${outstanding.toLocaleString('ru-RU')} <small>₽</small>`;
        $('#sheetOutstanding').textContent = `${outstanding.toLocaleString('ru-RU')} ₽`;
        $('#operationAmount').value = outstanding || Number(previewSettings.amount);
        $('#operationList').innerHTML = operations.length ? operations.map(item => `<div class="operation-row ${item.kind}"><i>${item.kind === 'charge' ? '+' : '−'}</i><span><strong>${escapeHtml(item.comment || (item.kind === 'charge' ? 'Дополнительное начисление' : 'Оплата аренды'))}</strong><small>${escapeHtml(item.role)} · ${escapeHtml(item.date)}${item.receiptId ? ' · чек приложен' : ''}</small></span><b>${item.kind === 'charge' ? '+' : '−'}${item.amount.toLocaleString('ru-RU')} ₽</b></div>`).join('') : '<p>Операций пока нет</p>';
        $('#paymentCard').classList.toggle('paid', outstanding === 0);
    }
    const openPaymentSheet = () => {
        $('#paymentSheetTitle').textContent = currentRole === 'tenant' ? 'Отметить оплату' : 'Подтвердить получение';
        renderOperations(); paymentSheet.hidden = false;
    };
    $('#payButton').addEventListener('click', openPaymentSheet);
    $$('[data-action="payment"]').forEach(button => button.addEventListener('click', openPaymentSheet));
    $('.payment-close').addEventListener('click', () => paymentSheet.hidden = true);
    let pendingReceipt = null;
    $('#receiptInput').addEventListener('change', event => { pendingReceipt = event.target.files[0] || null; $('#receiptLabel').textContent = pendingReceipt ? pendingReceipt.name : 'Приложить чек'; });
    $('#paymentForm').addEventListener('submit', async event => {
        event.preventDefault(); const amount = Number($('#operationAmount').value); if (!amount) return;
        let receiptId = null;
        if (pendingReceipt) {
            try { receiptId = (await window.RentFlowVault.savePreferred(pendingReceipt, 'receipt')).id; }
            catch { toast('Не удалось зашифровать чек'); return; }
        }
        const operations = getOperations(); operations.unshift({id:crypto.randomUUID(), amount, kind:new FormData(event.target).get('operationKind'), comment:$('#operationComment').value.trim(), role:currentRole === 'tenant' ? 'Арендатор' : 'Арендодатель', date:new Date().toLocaleDateString('ru-RU'), receiptId});
        localStorage.setItem(operationsKey, JSON.stringify(operations)); pendingReceipt = null; event.target.reset(); $('#receiptLabel').textContent = 'Приложить чек'; renderOperations();
        toast('Операция добавлена');
    });
    $('#balanceToggle').addEventListener('click', event => {
        const hidden = $('#paymentCard').classList.toggle('amount-hidden');
        event.currentTarget.setAttribute('aria-pressed', String(hidden)); event.currentTarget.setAttribute('aria-label', hidden ? 'Показать сумму' : 'Скрыть сумму');
    });

    const previewSheet = $('#previewSheet');
    $('#previewSettings').addEventListener('click', () => previewSheet.hidden = false);
    $('.preview-close').addEventListener('click', () => previewSheet.hidden = true);
    ['demoAmount','demoDays','demoProperty'].forEach(id => $(`#${id}`).addEventListener('input', () => {
        applyPreview({...previewSettings, amount: $('#demoAmount').value, days: $('#demoDays').value, property: $('#demoProperty').value});
    }));
    $('#demoColor').addEventListener('input', event => { $('#demoColorValue').textContent = event.target.value; document.documentElement.style.setProperty('--green', event.target.value); });
    $('#previewForm').addEventListener('submit', event => {
        event.preventDefault(); previewSettings = {amount: $('#demoAmount').value, days: $('#demoDays').value, property: $('#demoProperty').value, color: $('#demoColor').value};
        localStorage.setItem('rentflow-preview', JSON.stringify(previewSettings)); applyPreview(previewSettings); previewSheet.hidden = true; toast('Настройки демо сохранены');
    });
    $('#resetPreview').addEventListener('click', () => { previewSettings = {...defaultPreview}; localStorage.removeItem('rentflow-preview'); applyPreview(previewSettings); toast('Настройки сброшены'); });

    const upload = () => $('#fileInput').click();
    $$('[data-action="upload"]').forEach(button => button.addEventListener('click', () => { showScreen('files'); upload(); }));
    const appendVaultFile = record => {
        if ($(`#fileList [data-vault-id="${record.id}"]`)) return;
        const article = document.createElement('article'); article.dataset.name = record.name; article.dataset.vaultId = record.id;
        const saving = record.originalSize > record.size ? ` · сжато на ${Math.round((1-record.size/record.originalSize)*100)}%` : '';
        article.innerHTML = `<i class="doc">AES</i><span><strong>${escapeHtml(record.name)}</strong><small>Зашифровано · ${(record.size/1024).toFixed(0)} КБ${saving}</small></span><time>сейчас</time><button class="vault-download" aria-label="Скачать">↓</button>`;
        article.querySelector('.vault-download').addEventListener('click', () => window.RentFlowVault.downloadPreferred(record).catch(() => toast('Не удалось скачать файл')));
        $('#fileList').prepend(article);
    };
    window.RentFlowVault.listPreferred('document').then(items => items.forEach(appendVaultFile)).catch(() => toast('Серверное хранилище недоступно'));
    $('#fileInput').addEventListener('change', async event => {
        const files = [...event.target.files];
        for (const file of files) {
            try { appendVaultFile(await window.RentFlowVault.savePreferred(file, 'document')); }
            catch { toast('Ошибка защищённого хранилища'); return; }
        }
        if (files.length) toast(`Зашифровано файлов: ${files.length}`); event.target.value = '';
    });
    $('#fileSearch').addEventListener('input', event => $$('#fileList article').forEach(file => file.hidden = !file.dataset.name.toLowerCase().includes(event.target.value.toLowerCase())));
    $$('.segmented button, .file-filters button').forEach(button => button.addEventListener('click', () => { button.parentElement.querySelectorAll('button').forEach(b => b.classList.remove('active')); button.classList.add('active'); }));
    $('#recordButton').addEventListener('click', () => $('#videoInput').click());
    $('#videoInput').addEventListener('change', async event => {
        const file = event.target.files[0]; if (!file) return;
        if (file.size > 100 * 1024 * 1024) { toast('Видео больше 100 МБ'); return; }
        toast('Шифруем видео…');
        try { await window.RentFlowVault.savePreferred(file, 'video'); toast('Видео зашифровано и сохранено'); }
        catch { toast('Не удалось сохранить видео'); }
        event.target.value = '';
    });
    $$('.play, .video-thumb i').forEach(button => button.addEventListener('click', () => toast('Запускаем видео…')));

    if ('serviceWorker' in navigator) window.addEventListener('load', () => navigator.serviceWorker.register('./sw.js').catch(() => {}));
})();
