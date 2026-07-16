const CACHE = "redgold-cache-v3";

const ASSETS = [
  "/login.html",
  "/home.html",
  "/add-booking.html",
  "/report.html",
  "/reports.html",
  "/history.html",
  "/revenue.html",
  "/images/logo1.png",
  "/images/background.png",
  "/manifest.json"
];

// Install → cache basic files
self.addEventListener("install", event => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE).then(cache => cache.addAll(ASSETS))
  );
});

// Activate → delete old caches
self.addEventListener("activate", event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.map(key => {
          if (key !== CACHE) return caches.delete(key);
        })
      )
    )
  );
  self.clients.claim();
});

// Fetch → cache-first for static files only
self.addEventListener("fetch", event => {
  if (event.request.method !== "GET") return;

  event.respondWith(
    caches.match(event.request).then(res => res || fetch(event.request))
  );
});
