(() => {
    const DB_NAME = 'rentflow-secure-v1';
    const openDb = () => new Promise((resolve, reject) => {
        const request = indexedDB.open(DB_NAME, 1);
        request.onupgradeneeded = () => {
            const db = request.result;
            db.createObjectStore('vault', {keyPath: 'id'});
            db.createObjectStore('keys');
        };
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
    });
    const transaction = async (store, mode, action) => {
        const db = await openDb();
        return new Promise((resolve, reject) => {
            const tx = db.transaction(store, mode); const request = action(tx.objectStore(store));
            request.onsuccess = () => resolve(request.result); request.onerror = () => reject(request.error);
            tx.oncomplete = () => db.close();
        });
    };
    const getKey = async () => {
        let key = await transaction('keys', 'readonly', store => store.get('device-key'));
        if (!key) {
            key = await crypto.subtle.generateKey({name:'AES-GCM', length:256}, false, ['encrypt','decrypt']);
            await transaction('keys', 'readwrite', store => store.put(key, 'device-key'));
        }
        return key;
    };
    const compressImage = file => new Promise(resolve => {
        if (!file.type.startsWith('image/') || file.type === 'image/svg+xml' || file.size < 350000) return resolve(file);
        const image = new Image(); const url = URL.createObjectURL(file);
        image.onload = () => {
            const scale = Math.min(1, 1920 / Math.max(image.width, image.height)); const canvas = document.createElement('canvas');
            canvas.width = Math.round(image.width * scale); canvas.height = Math.round(image.height * scale);
            canvas.getContext('2d').drawImage(image, 0, 0, canvas.width, canvas.height); URL.revokeObjectURL(url);
            canvas.toBlob(blob => resolve(blob || file), 'image/webp', .82);
        };
        image.onerror = () => { URL.revokeObjectURL(url); resolve(file); }; image.src = url;
    });
    const save = async (file, kind = 'document') => {
        const prepared = kind === 'document' ? await compressImage(file) : file;
        const iv = crypto.getRandomValues(new Uint8Array(12)); const key = await getKey();
        const encrypted = await crypto.subtle.encrypt({name:'AES-GCM', iv}, key, await prepared.arrayBuffer());
        const record = {id: crypto.randomUUID(), name:file.name, type:prepared.type || file.type, originalType:file.type, size:prepared.size, originalSize:file.size, kind, iv, encrypted, createdAt:new Date().toISOString()};
        await transaction('vault', 'readwrite', store => store.put(record)); return record;
    };
    const list = async kind => (await transaction('vault', 'readonly', store => store.getAll())).filter(item => !kind || item.kind === kind);
    const download = async id => {
        const record = await transaction('vault', 'readonly', store => store.get(id)); if (!record) throw new Error('File not found');
        const data = await crypto.subtle.decrypt({name:'AES-GCM', iv:record.iv}, await getKey(), record.encrypted);
        const url = URL.createObjectURL(new Blob([data], {type:record.type})); const link = document.createElement('a'); link.href=url; link.download=record.name; link.click(); setTimeout(()=>URL.revokeObjectURL(url),1000);
    };
    const savePreferred = async (file, kind = 'document') => {
        const token = sessionStorage.getItem('rentflow-api-token');
        if (!token) return save(file, kind);
        const form = new FormData(); form.append('kind', kind); form.append('file', file);
        const response = await fetch('./api/media', {method:'POST', headers:{'X-RentFlow-Token':token}, body:form});
        if (!response.ok) throw new Error(`Upload failed: ${response.status}`);
        const record = await response.json();
        return {...record, originalSize:file.size, storedSize:record.size, kind, server:true};
    };
    const listPreferred = async kind => {
        const token = sessionStorage.getItem('rentflow-api-token');
        if (!token) return list(kind);
        const response = await fetch('./api/media', {headers:{'X-RentFlow-Token':token}});
        if (!response.ok) throw new Error(`List failed: ${response.status}`);
        return (await response.json()).filter(item => !kind || item.kind === kind).map(item => ({...item, originalSize:item.size, storedSize:item.size, server:true}));
    };
    const downloadPreferred = async record => {
        if (!record.server) return download(record.id);
        const token = sessionStorage.getItem('rentflow-api-token');
        const response = await fetch(`./api/media/${encodeURIComponent(record.id)}`, {headers:{'X-RentFlow-Token':token}});
        if (!response.ok) throw new Error(`Download failed: ${response.status}`);
        const url = URL.createObjectURL(await response.blob()); const link = document.createElement('a'); link.href=url; link.download=record.name; link.click(); setTimeout(()=>URL.revokeObjectURL(url),1000);
    };
    window.RentFlowVault = {save, savePreferred, list, listPreferred, download, downloadPreferred};
})();
