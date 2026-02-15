const CACHE_NAME = "redgold-v2";

// Only static UI files go here (NEVER cache API)
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


// INSTALL → cache UI shell
self.addEventListener("install", event => {
  self.skipWaiting();

  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(APP_SHELL))
  );
});


// ACTIVATE → remove old cache + control pages immediately
self.addEventListener("activate", event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.filter(key => key !== CACHE_NAME)
            .map(key => caches.delete(key))
      )
    ).then(() => self.clients.claim())
  );
});


// FETCH HANDLER
self.addEventListener("fetch", event => {

  const url = new URL(event.request.url);

  // NEVER cache backend API calls
  if (url.pathname.startsWith("/api/")) {
    return; // let network handle it normally
  }

  // Handle navigation (important for PWA open)
  if (event.request.mode === "navigate") {
    event.respondWith(
      fetch(event.request).catch(() =>
        caches.match("/index.html")
      )
    );
    return;
  }

  // Static files → cache first
  event.respondWith(
    caches.match(event.request)
      .then(res => res || fetch(event.request))
  );
});
