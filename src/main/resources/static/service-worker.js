const CACHE_NAME = "redgold-v2";

const urlsToCache = [
  "/",
  "/index.html",
  "/login.html",
  "/home.html",
  "/add-booking.html",
  "/report.html",
  "/reports.html",
  "/history.html",
  "/revenue.html",
  "/manifest.json",
  "/logo.png"
];

// Install
self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
  self.skipWaiting();
});

// Activate
self.addEventListener("activate", event => {
  event.waitUntil(self.clients.claim());
});

// Fetch (network first, fallback cache)
self.addEventListener("fetch", event => {
  event.respondWith(
    fetch(event.request).catch(() => caches.match(event.request))
  );
});
