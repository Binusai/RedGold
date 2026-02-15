const CACHE_NAME = "redgold-v1";

const APP_SHELL = [
  "/",
  "/index.html",
  "/login.html",
  "/home.html",
  "/add-booking.html",
  "/report.html",
  "/reports.html",
  "/history.html",
  "/revenue.html",
  "/logo.png",
  "/manifest.json"
];

// Install → cache basic files
self.addEventListener("install", event => {
  self.skipWaiting();

  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(APP_SHELL))
  );
});

// Activate → take control immediately
self.addEventListener("activate", event => {
  event.waitUntil(self.clients.claim());
});

// Fetch → serve from cache first, then network
self.addEventListener("fetch", event => {
  event.respondWith(
    caches.match(event.request).then(response => {
      return response || fetch(event.request);
    })
  );
});
