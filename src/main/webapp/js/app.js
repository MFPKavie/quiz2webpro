(function () {
    "use strict";

    var root = document.documentElement;
    var ctx = document.body.dataset.ctx || "";
    var prefersDark = window.matchMedia("(prefers-color-scheme: dark)");

    function currentTheme() {
        return root.dataset.theme || (prefersDark.matches ? "dark" : "light");
    }

    document.querySelectorAll(".theme-toggle").forEach(function (button) {
        function sync() {
            button.setAttribute("aria-pressed", String(currentTheme() === "dark"));
        }
        sync();
        prefersDark.addEventListener("change", sync);

        button.addEventListener("click", function () {
            var next = currentTheme() === "dark" ? "light" : "dark";
            root.dataset.theme = next;
            document.cookie = "theme=" + next + "; Path=" + (ctx || "/") + "; Max-Age=31536000; SameSite=Lax";
            sync();
        });
    });

    var mainPhoto = document.getElementById("gallery-main");
    document.querySelectorAll(".gallery__thumb").forEach(function (thumb, i, all) {
        thumb.addEventListener("click", function () {
            mainPhoto.src = thumb.dataset.src;
            mainPhoto.alt = thumb.dataset.alt;
            all.forEach(function (t) { t.setAttribute("aria-pressed", String(t === thumb)); });
        });
    });

    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (!window.confirm(form.dataset.confirm)) {
                event.preventDefault();
            }
        });
    });

    var photoInput = document.getElementById("photos");
    if (photoInput) {
        var zone = photoInput.closest(".dropzone");
        var previews = document.querySelector(".photo-previews");
        var photoError = document.querySelector("[data-photo-error]");
        var MAX_BYTES = 2 * 1024 * 1024;
        var TYPES = ["image/jpeg", "image/png", "image/webp"];

        var keptPhotos = function () {
            return document.querySelectorAll(".photo-list input[type=checkbox]:not(:checked)").length;
        };

        var renderPhotos = function () {
            var files = Array.prototype.slice.call(photoInput.files);
            var room = 4 - keptPhotos();
            var problem = "";
            previews.innerHTML = "";

            files.forEach(function (file) {
                if (TYPES.indexOf(file.type) === -1) {
                    problem = "“" + file.name + "” is not a JPEG, PNG or WebP image.";
                } else if (file.size > MAX_BYTES) {
                    problem = "“" + file.name + "” is larger than 2 MB.";
                }
                var item = document.createElement("li");
                var img = document.createElement("img");
                img.alt = "Preview of " + file.name;
                item.appendChild(img);
                previews.appendChild(item);
                var reader = new FileReader();
                reader.onload = function () { img.src = reader.result; };
                reader.readAsDataURL(file);
            });
            if (!problem && files.length > room) {
                problem = room > 0 ? "You can add " + room + " more photo" + (room === 1 ? "" : "s") + "."
                                   : "This listing already has 4 photos. Remove one first.";
            }
            photoError.textContent = problem;
            photoError.hidden = !problem;
            photoInput.setCustomValidity(problem); // blocks submit until fixed; the server checks again
        };

        photoInput.addEventListener("change", renderPhotos);
        document.querySelectorAll(".photo-list input[type=checkbox]").forEach(function (box) {
            box.addEventListener("change", renderPhotos);
        });

        ["dragenter", "dragover"].forEach(function (name) {
            zone.addEventListener(name, function (event) { event.preventDefault(); zone.classList.add("is-over"); });
        });
        ["dragleave", "drop"].forEach(function (name) {
            zone.addEventListener(name, function (event) { event.preventDefault(); zone.classList.remove("is-over"); });
        });
        zone.addEventListener("drop", function (event) {
            photoInput.files = event.dataTransfer.files;
            renderPhotos();
        });
    }

    var loginUrl = function () {
        return ctx + "/login?next=" + encodeURIComponent(location.pathname.substring(ctx.length) + location.search);
    };

    var showMessage = function (text) {
        var old = document.querySelector(".toast");
        if (old) old.remove();
        var toast = document.createElement("div");
        toast.className = "toast toast--error";
        toast.setAttribute("role", "status");
        var p = document.createElement("p");
        p.textContent = text; // never innerHTML: keeps the message plain text
        toast.appendChild(p);
        document.body.appendChild(toast);
        setTimeout(function () { toast.remove(); }, 4500);
    };

    document.addEventListener("click", async function (event) {
        var heart = event.target.closest(".heart");
        if (!heart) return;
        if (heart.hasAttribute("data-login")) {
            location.href = loginUrl();
            return;
        }
        var id = heart.dataset.listing;
        var token = document.querySelector('meta[name="csrf"]').content;
        heart.disabled = true;

        try {
            var response = await fetch(ctx + "/api/wishlist", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded", "X-CSRF-Token": token },
                body: "listingId=" + encodeURIComponent(id),
                credentials: "same-origin"
            });
            if (response.status === 401) {
                location.href = loginUrl();
                return;
            }
            var data = await response.json();
            if (!response.ok) throw new Error(data.error || "Something went wrong.");

            document.querySelectorAll('.heart[data-listing="' + id + '"]').forEach(function (h) {
                h.setAttribute("aria-pressed", String(data.saved));
            });
            document.querySelectorAll('[data-save-count="' + id + '"]').forEach(function (el) {
                el.textContent = data.count;
            });
            heart.classList.remove("is-popping");
            void heart.offsetWidth; // restarts the CSS animation
            if (data.saved) heart.classList.add("is-popping");
            var card = heart.closest(".wishlist .card");
            if (card) card.classList.toggle("is-unsaved", !data.saved);
        } catch (error) {
            showMessage(error.message || "Could not reach the server. Try again.");
        }
        heart.disabled = false;
    });

    var toast = document.querySelector(".toast");
    if (toast) {
        var close = function () { toast.remove(); };
        toast.querySelector(".toast__close").addEventListener("click", close);
        setTimeout(close, 4500);
    }
})();
