package com.tictactoe;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", urlPatterns = {"/logic"})
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (hasWinner(session)) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        Field field = extractField(session);

        int index = getSelectedIndex(req);
        if (field.getField().get(index) != Sign.EMPTY) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }
        field.getField().put(index, Sign.CROSS);

        if (checkWin(resp, session, field)) {
            resp.sendRedirect("/index.jsp");
            return;
        }

        int emptyFieldIndex = field.getEmptyFieldIndex();
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(resp, session, field)) {
                resp.sendRedirect("/index.jsp");
                return;
            }
        } else {
            session.setAttribute("draw", true);
        }

        session.setAttribute("data", field.getFieldData());
        session.setAttribute("field", field);

        resp.sendRedirect("/index.jsp");
    }

    private int getSelectedIndex(HttpServletRequest req) {
        String click = req.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession session) {
        Object fieldAttribute = session.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            session.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }

        return (Field) fieldAttribute;
    }

    private boolean checkWin(HttpServletResponse resp, HttpSession session, Field field) {
        Sign winner = field.checkWin();

        if (winner == Sign.CROSS || winner == Sign.NOUGHT) {
            session.setAttribute("winner", winner);
            session.setAttribute("data", field.getFieldData());
            return true;
        }

        return false;
    }

    private boolean hasWinner(HttpSession session) {
        Object winner = session.getAttribute("winner");
        if (winner == null) {
            return false;
        }

        return true;
    }
}
