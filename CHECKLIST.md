# Pass It On — demo checklist results

Run on 19 Nov 2026 against the real stack: **Tomcat 11.0.29, JDK 25.0.5, MySQL 9.7.2, Connector/J 26.10.0**.
Re-run the automatic part any time with `bash /d/dev/run-checklist.sh`, then `bash /d/dev/reset-demo-data.sh`.

**Result: 33 automatic checks passed, 0 failed, plus the manual "Looks" checks below.**

## Requirements

| Check | Result | How it was checked |
|---|---|---|
| At least 8 CRUD features work end to end (F1–F10) | PASS | Every feature below, in one run |
| Register, log in, log out | PASS | New account registers, logs in; logout is a POST with the token |
| Wrong password shows the neutral message | PASS | Wrong password **and** unknown email both say "Email or password is incorrect." |
| Foreign keys in MySQL and `schema.sql` | PASS | `information_schema` lists **11** foreign keys |
| Runs on Tomcat 11 + JDK 25, no javax imports | PASS | Tomcat log shows `Apache Tomcat/11.0.29` and JVM 25; no `javax.servlet` anywhere* |

\* `Passwords.java` imports `javax.crypto` — that is the JDK's own security library (PBKDF2), not the old Java EE.
The "no javax" rule is about `javax.servlet`, which the project never uses (only `jakarta.*`).

## Features (demo order: Nina sells, Top buys, Admin moderates)

| Check | Result |
|---|---|
| Post a listing with 2 photos; it appears first in the grid with its price sticker | PASS |
| Edit the title and price; the flash message shows and the change appears | PASS |
| Search "calc" + Electronics + max Rp 100.000 → only the calculator | PASS |
| As Top: make an offer, edit it, withdraw it, then make it again | PASS |
| As Nina: accept Top's offer → the other offer is declined and the listing shows "Reserved" | PASS |
| Mark it sold; as Top, leave a 5-star review; a second review is refused | PASS |
| The heart toggles without a page reload, and the wishlist page shows the item | PASS (no reload proven in the browser: a marker variable survived the click) |
| Delete an available listing; a sold one cannot be deleted | PASS |
| As Admin: delete a category that has listings → friendly message; add, rename and delete an empty one | PASS ("This category still has 1 listing.") |

## Security

| Check | Result |
|---|---|
| Logged out, `/listing/new` redirects to the login page | PASS (`/login?next=%2Flisting%2Fnew`) |
| As Top, Nina's `/listing/edit?id=1` is refused | PASS (403) |
| A form POST with the csrf field removed → 403 | PASS |
| A title like `<script>alert(1)</script>` shows as plain text | PASS (stored as typed, shown as `&lt;script&gt;…` on the grid, the detail page and the tab title) |
| A `.txt` renamed to `.jpg`, or a 5 MB image, is refused with a message | PASS (checked by the file's first bytes, and the 2 MB limit) |
| As a student, `/admin/categories` → 403 | PASS |
| Errors don't leak (extra) | PASS: with MySQL stopped the page is a friendly 500, no stack trace or Tomcat version; the error is in the Tomcat log |

## Looks (checked in the browser)

| Check | Result |
|---|---|
| Desktop, tablet and phone widths look right | PASS: no sideways scroll at 375, 640, 768, 1024, 1100 and 1280 px (logged in as admin, the longest menu). Phone: bottom tab bar. Tablet: the menu moves to a second row. |
| Dark mode and the toggle work; the choice survives a reload | PASS (the server reads the `theme` cookie and draws the right theme straight away) |
| Every empty state has its message and doodle | PASS: no search results, empty wishlist, no offers (both tabs), empty board, empty report queue, 404 and error pages |
| Keyboard only: tab through the nav, the filters and a form, with visible focus | PASS: "Skip to content" is the first stop; every control shows a 3 px blue focus ring |
| Colour contrast 4.5:1 or better for text | PASS: links and buttons use `--accent-strong` (#B8401F, 4.9–5.5:1); sticker text is near-black on tomato (5.6:1); prices show as "Rp 60.000", with "Rp" stacked above the number on the round sticker; dashboard bars checked with a contrast tool in both themes |
| `prefers-reduced-motion` turns off the tilt and animations | PASS by code review: the media query sets both tilt variables to 0 and shortens every animation (the toast still disappears after 4 s) |

## Still to do by you
- **8–10 real photos in the demo data.** The seed listings have no photos on purpose (the plan says to
  upload them through the app). Take photos of real items and add them with **Edit** — see `SETUP.md`.
