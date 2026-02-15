const CACHE = "redgold-cache-v2";

const ASSETS = [
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

// Install → cache app shell
self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE).then(cache => cache.addAll(ASSETS))
  );
  self.skipWaiting();
});

// Activate → take control immediately
self.addEventListener("activate", event => {
  event.waitUntil(self.clients.claim());
});

// THIS PART IS WHAT MAKES IT INSTALLABLE
self.addEventListener("fetch", event => {
  if (event.request.mode === "navigate") {
    event.respondWith(
      caches.match("/index.html")
    );
    return;
  }

  event.respondWith(
    caches.match(event.request).then(res => res || fetch(event.request))
  );
});
