package com.passiton.web;

import com.passiton.util.Money;
import com.passiton.dao.CategoryDao;
import com.passiton.dao.ListingDao;
import com.passiton.dao.PhotoDao;
import com.passiton.model.Category;
import com.passiton.model.Listing;
import com.passiton.model.Photo;
import com.passiton.model.User;
import com.passiton.util.Flash;
import com.passiton.util.Uploads;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet({"/listing/new", "/listing/edit"})
// Tomcat accepts up to 10 MB so a too-big photo reaches Uploads.check and gets a message, not a broken connection
@MultipartConfig(fileSizeThreshold = 1024 * 1024,
                 maxFileSize = 10L * 1024 * 1024,
                 maxRequestSize = 45L * 1024 * 1024)
public class ListingFormServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/listing-form.jsp";
    private static final Set<String> CONDITIONS = Set.of("new", "like_new", "good", "fair");

    private final ListingDao listingDao = new ListingDao();
    private final PhotoDao photoDao = new PhotoDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Map<String, String> form = new HashMap<>();
            List<Photo> photos = List.of();
            Listing listing = null;

            if (isEdit(req)) {
                listing = loadOwnListing(req, resp);
                if (listing == null) return;
                photos = photoDao.findByListing(listing.getId());
                form.put("title", listing.getTitle());
                form.put("description", listing.getDescription());
                form.put("price", String.valueOf(listing.getPrice()));
                form.put("itemCondition", listing.getItemCondition());
                form.put("categoryId", String.valueOf(listing.getCategoryId()));
                form.put("meetupSpot", listing.getMeetupSpot() == null ? "" : listing.getMeetupSpot());
            } else {
                form.put("itemCondition", "good");
            }
            show(req, resp, form, Map.of(), listing, photos);
        } catch (SQLException e) {
            throw new ServletException("Could not open the listing form", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        try {
            boolean edit = isEdit(req);
            Listing existing = null;
            List<Photo> photos = List.of();
            if (edit) {
                existing = loadOwnListing(req, resp);
                if (existing == null) return;
                photos = photoDao.findByListing(existing.getId());
            }

            Map<String, String> form = new HashMap<>();
            for (String field : List.of("title", "description", "price", "itemCondition", "categoryId", "meetupSpot")) {
                form.put(field, Validate.text(req.getParameter(field)));
            }

            Map<String, String> errors = new LinkedHashMap<>();
            List<Category> categories = categoryDao.findAll();
            if (!Validate.length(form.get("title"), 5, 100)) {
                errors.put("title", "Give it a title of 5 to 100 characters.");
            }
            if (!Validate.length(form.get("description"), 10, 2000)) {
                errors.put("description", "Describe it in 10 to 2,000 characters.");
            }
            Integer price = Validate.intBetween(form.get("price"), 0, Money.MAX);
            if (price == null) {
                errors.put("price", "Enter a whole number of rupiah from 0 (free) to 20.000.000.");
            }
            if (!CONDITIONS.contains(form.get("itemCondition"))) {
                errors.put("itemCondition", "Pick the condition.");
            }
            Integer categoryId = Validate.intBetween(form.get("categoryId"), 1, Integer.MAX_VALUE);
            if (categoryId == null || categories.stream().noneMatch(c -> c.getId() == categoryId)) {
                errors.put("categoryId", "Pick a category.");
            }
            if (!Validate.length(form.get("meetupSpot"), 0, 100)) {
                errors.put("meetupSpot", "Keep the meetup spot under 100 characters.");
            }

            List<Integer> removeIds = new ArrayList<>();
            List<String> removeNames = new ArrayList<>();
            String[] ticked = req.getParameterValues("removePhoto");
            if (ticked != null) {
                for (Photo p : photos) {
                    if (List.of(ticked).contains(String.valueOf(p.getId()))) {
                        removeIds.add(p.getId());
                        removeNames.add(p.getFileName());
                    }
                }
            }
            List<Part> newPhotos;
            try {
                newPhotos = Uploads.photoParts(req);
            } catch (IllegalStateException e) {
                newPhotos = List.of();
                errors.put("photos", "Those photos are too large. Each photo must be 2 MB or smaller.");
            }
            int total = photos.size() - removeIds.size() + newPhotos.size();
            if (!errors.containsKey("photos")) {
                if (total < 1) {
                    errors.put("photos", "Add at least one photo.");
                } else if (total > Uploads.MAX_FILES) {
                    errors.put("photos", "A listing can have at most 4 photos.");
                } else {
                    for (Part part : newPhotos) {
                        String problem = Uploads.check(part);
                        if (problem != null) {
                            errors.put("photos", problem + " Use JPEG, PNG or WebP, 2 MB at most.");
                            break;
                        }
                    }
                }
            }

            if (!errors.isEmpty()) {
                req.setAttribute("reselectPhotos", !newPhotos.isEmpty());
                req.setAttribute("removed", removeIds);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                show(req, resp, form, errors, existing, photos);
                return;
            }

            List<String> saved = new ArrayList<>();
            for (Part part : newPhotos) {
                saved.add(Uploads.save(part));
            }
            Listing l = new Listing();
            l.setSellerId(me.getId());
            l.setCategoryId(categoryId);
            l.setTitle(form.get("title"));
            l.setDescription(form.get("description"));
            l.setPrice(price);
            l.setItemCondition(form.get("itemCondition"));
            l.setMeetupSpot(form.get("meetupSpot").isEmpty() ? null : form.get("meetupSpot"));

            int id;
            try {
                if (edit) {
                    l.setId(existing.getId());
                    if (!listingDao.update(l, removeIds, saved)) {
                        saved.forEach(name -> Uploads.delete(name));
                        Flash.error(req, "This listing can no longer be edited.");
                        resp.sendRedirect(req.getContextPath() + "/listing?id=" + existing.getId());
                        return;
                    }
                    id = existing.getId();
                    removeNames.forEach(name -> Uploads.delete(name));
                } else {
                    id = listingDao.create(l, saved);
                }
            } catch (SQLException e) {
                saved.forEach(name -> Uploads.delete(name));
                throw e;
            }

            Flash.ok(req, edit ? "Changes saved." : "Your listing is on the board.");
            resp.sendRedirect(req.getContextPath() + "/listing?id=" + id);
        } catch (SQLException e) {
            throw new ServletException("Could not save the listing", e);
        }
    }

    private static boolean isEdit(HttpServletRequest req) {
        return "/listing/edit".equals(req.getServletPath());
    }

    private Listing loadOwnListing(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        Integer id = Validate.intBetween(req.getParameter("id"), 1, Integer.MAX_VALUE);
        Listing listing = id == null ? null : listingDao.findById(id);
        if (listing == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        User me = (User) req.getSession().getAttribute("user");
        if (listing.getSellerId() != me.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        if ("sold".equals(listing.getStatus())) {
            Flash.error(req, "A sold listing can't be edited.");
            resp.sendRedirect(req.getContextPath() + "/listing?id=" + listing.getId());
            return null;
        }
        return listing;
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Map<String, String> form,
                      Map<String, String> errors, Listing listing, List<Photo> photos)
            throws ServletException, IOException, SQLException {
        req.setAttribute("form", form);
        req.setAttribute("errors", errors);
        req.setAttribute("listing", listing);
        req.setAttribute("photos", photos);
        req.setAttribute("categories", categoryDao.findAll());
        req.setAttribute("maxPhotos", Uploads.MAX_FILES);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
