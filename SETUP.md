# Pass It On — setup

Project folder: `D:\Claude\Webpro quiz 2\passiton\` — every relative path below starts there.

## Already done on this PC (portable, in `D:\dev`)
JDK 25.0.5, Tomcat 11.0.29 and MySQL 9.7.2 are unpacked in `D:\dev`. The `passiton` database is loaded,
`pio_app` exists, the jars are in `WEB-INF\lib`, and `PIO_DB_PASSWORD` is set for your Windows user.
Passwords are in `D:\dev\passiton-dev-credentials.txt` (keep it private, never hand it in).
Helper scripts (run them in Git Bash; they live outside the project, which has no build tool):

| Script | Does |
|---|---|
| `bash /d/dev/start-servers.sh` | starts MySQL and Tomcat in the background |
| `bash /d/dev/deploy-passiton.sh` | compiles the project, copies it into Tomcat and waits for the reload (run after every change; it logs you out in the browser) |
| `bash /d/dev/reset-demo-data.sh` | puts the database back to the starting data (empty board, no photos) |
| `bash /d/dev/stop-servers.sh` | stops Tomcat and MySQL |

Then open <http://localhost:8080/passiton/>. Logs: `D:\dev\tomcat.log`, `D:\dev\mysql.log`.

## Starting data
`sql/seed.sql` creates only what the site needs: the six categories and one admin account.
The board starts empty; students create their own accounts at **Sign up**.

| Account | Email | Password |
|---|---|---|
| Admin | admin@campus.ac.th | `Demo1234!` (change it before anyone else can reach the site) |

## Trying it out
1. Sign up two students (for example in two different browsers), and post a few listings with real photos
   (JPEG/PNG/WebP, 2 MB at most; they are saved in the database's `photo_files` table under random names).
2. As the second student: search, save a listing with the heart, make an offer.
3. As the seller: accept it in **Offers** (the listing becomes Reserved), mark it sold; then the buyer can review.
4. As Admin: the report queue, the category list, the dashboard.

---

## Doing the same setup by hand (another PC or Eclipse)

### 1. Install the tools
- **JDK 25 LTS** — `java -version` must print 25.
- **MySQL 9.7 LTS** + MySQL Workbench.
- **Apache Tomcat 11.0.x** — download the zip and unzip it (e.g. `C:\tomcat-11`).
  Never add an admin/admin user to `tomcat-users.xml`.

### 2. The 3 jars in `src\main\webapp\WEB-INF\lib\` (already there in this project)

| Jar | Where to get it |
|---|---|
| `mysql-connector-j-26.10.0.jar` | dev.mysql.com/downloads/connector/j → "Platform Independent" zip, or Maven Central `com/mysql/mysql-connector-j/` |
| `jakarta.servlet.jsp.jstl-api-3.0.2.jar` | Maven Central `jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api/3.0.2/` |
| `jakarta.servlet.jsp.jstl-3.0.1.jar` | Maven Central `org/glassfish/web/jakarta.servlet.jsp.jstl/3.0.1/` |

Tomcat already provides the Servlet / JSP / EL API — do **not** copy `servlet-api.jar` into `WEB-INF/lib`.

### 3. Create the database (MySQL Workbench, logged in as root)
1. File → Open SQL Script → `sql\schema.sql` → run it (drops and recreates the `passiton` database).
2. Same for `sql\seed.sql`.
3. Create the app's own user (pick your own strong password):
   ```sql
   CREATE USER 'pio_app'@'localhost' IDENTIFIED BY '<strong password>';
   GRANT SELECT, INSERT, UPDATE, DELETE ON passiton.* TO 'pio_app'@'localhost';
   ```
   Run the `CREATE USER` only once; re-running `schema.sql` later keeps the user and its grant.

### 4. Give Tomcat the database password
The password is never in the code: Tomcat reads the environment variable `PIO_DB_PASSWORD`.
- **Eclipse:** Run → Run Configurations… → Apache Tomcat → *Environment* tab → add `PIO_DB_PASSWORD`.
- **Or for your Windows user** (then restart Eclipse / Tomcat so it sees the new value):
  ```
  setx PIO_DB_PASSWORD "<the same strong password>"
  ```

### 5. Open the project in Eclipse (IDE for Enterprise Java)
1. File → New → Dynamic Web Project → Project name `passiton`.
2. Untick *Use default location* → Location: `D:\Claude\Webpro quiz 2\passiton`.
3. Target runtime: *Apache Tomcat v11.0* (New Runtime… → point it at your Tomcat folder, with JDK 25).
4. Next → check the source folder is `src\main\java` → Next → check Context root = `passiton`,
   Content directory = `src\main\webapp`, and leave **Generate web.xml deployment descriptor unticked**
   (the project already has its own `web.xml`) → Finish.

The context root `passiton` is what makes the address `http://localhost:8080/passiton/`.

---

## Hosting it online: Render (app) + Aiven (MySQL), both free

Render runs the app from the `Dockerfile` (Tomcat 11 + JDK 25; the site is at the root `/`, not `/passiton/`).
Render has no MySQL, so the database lives on Aiven. Photos are stored in MySQL, so nothing is lost when
Render restarts the app. The free Render app sleeps after 15 minutes without visitors; the next visit
takes about a minute to wake it.

### 1. Database on Aiven
1. Sign up at aiven.io → **Create service** → **MySQL** → **Free** plan → pick a region close to you → create.
2. On the service's **Overview** page, note the **Host**, **Port** and the `avnadmin` **Password**.
3. Load the tables from this folder (PowerShell; it asks for the avnadmin password):
   ```
   D:\dev\mysql-9.7.2-winx64\bin\mysql.exe --host=<host> --port=<port> --user=avnadmin --password --ssl-mode=REQUIRED --execute="source sql/schema.sql; source sql/seed.sql"
   ```
4. Create the app's own user the same way (`--execute="..."`), with a new strong password:
   ```sql
   CREATE USER 'pio_app'@'%' IDENTIFIED BY '<strong password>';
   GRANT SELECT, INSERT, UPDATE, DELETE ON passiton.* TO 'pio_app'@'%';
   ```

### 2. App on Render
1. Put this `passiton` folder in a GitHub repository (a private one is fine). The folder holds no passwords.
2. On render.com: **New** → **Blueprint** → pick the repository. Render reads `render.yaml` and asks for:

   | Variable | Value |
   |---|---|
   | `PIO_DB_URL` | `jdbc:mysql://<host>:<port>/passiton?sslMode=REQUIRED&useUnicode=true&characterEncoding=UTF-8` |
   | `PIO_DB_USER` | `pio_app` |
   | `PIO_DB_PASSWORD` | the `pio_app` password from step 1.4 |

3. **Apply**. The first build takes a few minutes; the address is shown at the top (`https://passiton-….onrender.com/`).
   Every push to the repository redeploys it.

### 3. Replace the demo admin (the site is public now)
Sign up on the live site with your own email and password, then run on Aiven:
```sql
UPDATE passiton.users SET role = 'admin' WHERE email = '<your email>';
DELETE FROM passiton.users WHERE email = 'admin@campus.ac.th';
```
