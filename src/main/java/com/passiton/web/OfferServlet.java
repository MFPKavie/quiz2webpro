package com.passiton.web;

import com.passiton.util.Money;
import com.passiton.dao.ListingDao;
import com.passiton.dao.OfferDao;
import com.passiton.model.Listing;
import com.passiton.model.Offer;
import com.passiton.model.User;
import com.passiton.util.Flash;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet({"/offers", "/offer/*"})
public class OfferServlet extends HttpServlet {
    private final OfferDao offerDao = new OfferDao();
    private final ListingDao listingDao = new ListingDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!"/offers".equals(req.getServletPath())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        User me = (User) req.getSession().getAttribute("user");
        String tab = "sent".equals(req.getParameter("tab")) ? "sent" : "received";
        try {
            var received = offerDao.findReceived(me.getId());
            var sent = offerDao.findSent(me.getId());
            req.setAttribute("tab", tab);
            req.setAttribute("offers", "sent".equals(tab) ? sent : received);
            req.setAttribute("receivedPending", received.stream().filter(o -> "pending".equals(o.getStatus())).count());
            req.setAttribute("sentPending", sent.stream().filter(o -> "pending".equals(o.getStatus())).count());
        } catch (SQLException e) {
            throw new ServletException("Could not read offers", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/offers.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        String action = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            switch (action) {
                case "/create" -> create(req, resp, me);
                case "/edit" -> edit(req, resp, me);
                case "/withdraw" -> withdraw(req, resp, me);
                case "/accept" -> accept(req, resp, me);
                case "/decline" -> decline(req, resp, me);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Could not save the offer", e);
        }
    }

    private void create(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, ServletException, IOException {
        Integer listingId = Validate.intBetween(req.getParameter("listingId"), 1, Integer.MAX_VALUE);
        Listing listing = listingId == null ? null : listingDao.findById(listingId);
        if (listing == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String back = req.getContextPath() + "/listing?id=" + listing.getId();
        if (listing.getSellerId() == me.getId()) {
            Flash.error(req, "You can't make an offer on your own listing.");
            resp.sendRedirect(back);
            return;
        }
        if (!"available".equals(listing.getStatus())) {
            Flash.error(req, "This item is no longer available for offers.");
            resp.sendRedirect(back);
            return;
        }
        Map<String, String> form = readForm(req);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            showAgain(req, resp, listing.getId(), form, errors);
            return;
        }
        int amount = Integer.parseInt(form.get("amount"));
        String message = form.get("message").isEmpty() ? null : form.get("message");

        Offer latest = offerDao.findLatest(listing.getId(), me.getId());
        if (latest != null && "pending".equals(latest.getStatus())) {
            offerDao.updatePending(latest.getId(), me.getId(), amount, message);
            Flash.ok(req, "You already had an offer here, so it was updated to " + latest.withAmount(amount) + ".");
        } else {
            offerDao.create(listing.getId(), me.getId(), amount, message);
            Flash.ok(req, "Offer sent. The seller will see it in their offers.");
        }
        resp.sendRedirect(back);
    }

    private void edit(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, ServletException, IOException {
        Offer offer = ownOffer(req, resp, me);
        if (offer == null) return;
        Map<String, String> form = readForm(req);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            showAgain(req, resp, offer.getListingId(), form, errors);
            return;
        }
        int amount = Integer.parseInt(form.get("amount"));
        boolean ok = offerDao.updatePending(offer.getId(), me.getId(), amount,
                form.get("message").isEmpty() ? null : form.get("message"));
        if (ok) {
            Flash.ok(req, "Your offer is now " + offer.withAmount(amount) + ".");
        } else {
            Flash.error(req, "That offer can't be changed any more.");
        }
        resp.sendRedirect(back(req, offer));
    }

    private void withdraw(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, IOException {
        Offer offer = ownOffer(req, resp, me);
        if (offer == null) return;
        if (offerDao.withdraw(offer.getId(), me.getId())) {
            Flash.ok(req, "Offer withdrawn.");
        } else {
            Flash.error(req, "That offer can't be withdrawn any more.");
        }
        resp.sendRedirect(back(req, offer));
    }

    private void accept(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, IOException {
        Integer offerId = Validate.intBetween(req.getParameter("offerId"), 1, Integer.MAX_VALUE);
        Offer offer = offerId == null ? null : offerDao.findById(offerId);
        if (offer == null || offer.getSellerId() != me.getId()) {
            resp.sendError(offer == null ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (offerDao.accept(offer.getId(), me.getId())) {
            Flash.ok(req, "You accepted " + offer.getBuyerName() + "'s offer of " + offer.getAmountText()
                    + ". The listing is reserved and other offers were declined.");
        } else {
            Flash.error(req, "That offer can't be accepted any more.");
        }
        resp.sendRedirect(req.getContextPath() + "/offers?tab=received");
    }

    private void decline(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, IOException {
        Integer offerId = Validate.intBetween(req.getParameter("offerId"), 1, Integer.MAX_VALUE);
        Offer offer = offerId == null ? null : offerDao.findById(offerId);
        if (offer == null || offer.getSellerId() != me.getId()) {
            resp.sendError(offer == null ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (offerDao.decline(offer.getId(), me.getId())) {
            Flash.ok(req, "Offer declined.");
        } else {
            Flash.error(req, "That offer can't be declined any more.");
        }
        resp.sendRedirect(req.getContextPath() + "/offers?tab=received");
    }

    private Offer ownOffer(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, IOException {
        Integer offerId = Validate.intBetween(req.getParameter("offerId"), 1, Integer.MAX_VALUE);
        Offer offer = offerId == null ? null : offerDao.findById(offerId);
        if (offer == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (offer.getBuyerId() != me.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return offer;
    }

    private static String back(HttpServletRequest req, Offer offer) {
        return "inbox".equals(req.getParameter("from"))
                ? req.getContextPath() + "/offers?tab=sent"
                : req.getContextPath() + "/listing?id=" + offer.getListingId();
    }

    private static Map<String, String> readForm(HttpServletRequest req) {
        Map<String, String> form = new HashMap<>();
        form.put("amount", Validate.text(req.getParameter("amount")));
        form.put("message", Validate.text(req.getParameter("message")));
        return form;
    }

    private static Map<String, String> validate(Map<String, String> form) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (Validate.intBetween(form.get("amount"), 1, Money.MAX) == null) {
            errors.put("amount", "Offer a whole number of rupiah from 1 to 20.000.000.");
        }
        if (!Validate.length(form.get("message"), 0, 300)) {
            errors.put("message", "Keep the message under 300 characters.");
        }
        return errors;
    }

    private void showAgain(HttpServletRequest req, HttpServletResponse resp, int listingId,
                           Map<String, String> form, Map<String, String> errors)
            throws SQLException, ServletException, IOException {
        if (!ListingDetailServlet.prepare(req, listingId)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("offerForm", form);
        req.setAttribute("offerErrors", errors);
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        req.getRequestDispatcher(ListingDetailServlet.VIEW).forward(req, resp);
    }
}
