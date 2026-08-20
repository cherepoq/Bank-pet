self.addEventListener('install', event => {
  event.waitUntil(
    caches.open('rentflow-v6').then(cache => cache.addAll(['./', './css/style.css', './js/theme.js', './js/secure-storage.js']))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => response || fetch(event.request))
  );
});
