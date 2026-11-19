# Pass It On: are all techniques covered by Lectures 1-12?

Checked 19 Nov 2026 against `WebPro_Lectures_EN.pdf` (16 lectures; only 1-12 count, 13-16 are ASP.NET).
Method: the PDF text was split per lecture, and every technique the project uses was searched in lectures 1-12.
The slides summarise and point to the book, so some exact method names never appear even when the topic is taught.

## Covered by the lectures (examples)

| Technique in Pass It On | Where the lectures teach it |
|---|---|
| Tomcat, servlets with `@WebServlet`, forward / redirect, `sendError` / `setStatus` | L7 |
| Filters, run in the order `web.xml` gives them | L7 |
| Start-up listener that builds shared objects | L7, L10 |
| JSP + JSTL + EL: `c:out c:set c:remove c:if c:choose c:forEach`, `fn:` functions, `<%@ include %>`, `.jspf` | L7 |
| Sessions, `changeSessionId()`, CSRF token (`RANDOM.nextBytes` + `HexFormat`), flash message, PRG | L3, L5, L7, L8 |
| Passwords: PBKDF2, `pbkdf2_sha256$..$salt$hash`, Base64, constant-time compare | L3 |
| JDBC: `Connection`, `PreparedStatement` with `?`, `ResultSet`, `setAutoCommit` / `commit` / `rollback` | L5 |
| MySQL: JOIN, LEFT JOIN, GROUP BY, HAVING, LIMIT, sub-queries, COALESCE, SUM, COUNT | L5 |
| MySQL errors 1062 (duplicate) and 1451 (delete blocked by foreign key) | L5 |
| Foreign keys, UNIQUE, CHECK, ENUM | L5, L8 |
| Uploads: multipart form, file name / content type / bytes, "browser checks are not enough" | L2, L3, L11 |
| `fetch()` with `async` / `await` and JSON | L2 |
| CSS custom properties, dark theme with `prefers-color-scheme`, grid `repeat(auto-fit, minmax())`, flexbox, media queries | L2 |
| `data-*` attributes, `dataset`, `confirm()`, `addEventListener`, `createElement`, `textContent` | L2, L12 |

## Replaced so that only taught techniques remain

| Was | Now | Why |
|---|---|---|
| `SELECT ... FOR UPDATE` row lock when accepting an offer | Each UPDATE checks its own condition; 0 rows changed means `rollback()` | Locking is not in L1-12. Still safe against two accepts at the same moment |
| `UPDATE offers JOIN listings` | `UPDATE ... WHERE listing_id IN (SELECT ...)` | Multi-table UPDATE is not taught; sub-queries are (L5) |
| `INSERT IGNORE` (wishlist) | Plain INSERT, ignore error 1062 | `IGNORE` is not taught; error 1062 is (L5) |
| `catch (SQLIntegrityConstraintViolationException)` | `catch (SQLException)` + `Db.isDuplicate` (1062) / `Db.isStillReferenced` (1451) | Matches L5 "three errors worth memorizing" |
| `<jsp-config>` (scripting-invalid, trim whitespace, page-encoding) and session cookie settings in `web.xml` | Removed | Not in L1-12. No scriptlets are used in any JSP anyway |
| `.then().catch().finally()` in the wishlist script | `async` / `await` with `try` / `catch` | L2 uses `async` / `await` |

## Kept on purpose (you chose to keep them)

These come from your own spec and design brief but are **not shown in the lecture slides**.
If the quiz is strict, be ready to explain them as "standard Java / CSS / HTML5 beyond the slides".

- **Database:** `AUTO_INCREMENT` with `getGeneratedKeys()`, `LIMIT ? OFFSET ?` paging, `ON DELETE CASCADE`, `DEFAULT` / `ON UPDATE CURRENT_TIMESTAMP`, `AVG()`.
- **Java library:** `DriverManager`, `Class.forName` for the driver, `LocalDateTime` / `DateTimeFormatter`, `SecureRandom` for the salt (L7 uses `RANDOM`), `java.nio` `Files` / `Path`, `List.of` / `Map.of`, `String.format`, `Normalizer`, `Serializable`.
- **Servlet extras:** `@MultipartConfig` + `getParts()` (L11 teaches the idea, not this API), `getServletPath`, `session.invalidate()`, `error-page` and `session-timeout` in `web.xml`, `fn:escapeXml`, EL `.contains()`.
- **CSS:** transitions, `@keyframes`, `transform` / `rotate` (card tilt), gradients, `box-shadow`, `border-radius`, `position: sticky` / `fixed`, `clamp()`, `color-mix()`, `aspect-ratio`, `object-fit`, `clip-path`, `backdrop-filter`, `:has()`, `prefers-reduced-motion`, Google Fonts.
- **HTML / JS:** inline `<svg>`, `<figure>`, `type="search"`, `loading="lazy"`, `accept=`, `autocomplete=`, `FileReader`, drag-and-drop, `classList`, `closest()`, `matchMedia`, `setCustomValidity`.

## Result
The **stack** (Tomcat, JSP, servlets, filters, JSTL, JDBC, MySQL, PBKDF2, CSRF, PRG, uploads, `fetch`) and every
**rule of your own prompt** are in L1-12. The "kept" list above is mostly the visual design and a few Java / HTML5 details.
After the replacements the app still passes the 33 automatic checks of `CHECKLIST.md`.
