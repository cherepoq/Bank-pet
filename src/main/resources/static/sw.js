self.addEventListener('install', event => {
  event.waitUntil(
    caches.open('rentflow-v2').then(cache => cache.addAll(['/app', '/css/style.css', '/js/theme.js']))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => response || fetch(event.request))
  );
});
