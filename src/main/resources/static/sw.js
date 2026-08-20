self.addEventListener('install', event => {
  event.waitUntil(
    caches.open('rentflow-v3').then(cache => cache.addAll(['./', './css/style.css', './js/theme.js']))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => response || fetch(event.request))
  );
});
