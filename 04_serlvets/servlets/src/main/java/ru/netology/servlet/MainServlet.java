package ru.netology.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.netology.Config.JavaConfig;
import ru.netology.controller.PostController;


public class MainServlet extends HttpServlet {
    private PostController controller;

    @Override
    public void init() {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JavaConfig.class);
        controller = context.getBean(PostController.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        // если деплоились в root context, то достаточно этого
        try {
            final var path = req.getRequestURI();
            final var method = req.getMethod();
            // primitive routing
            if (method.equals("GET") && path.equals("/api/posts")) {
                controller.all(resp);
                return;
            }
            if (method.equals("GET") && path.matches("/api/posts/\\d+")) {
                // easy way
                final var id = Long.parseLong(path.substring(path.lastIndexOf("/")));
                controller.getById(id, resp);
                return;
            }
            if (method.equals("POST") && path.equals("/api/posts")) {
                controller.save(req.getReader(), resp);
                return;
            }
            if (method.equals("DELETE") && path.matches("/api/posts/\\d+")) {
                // easy way
                final var id = Long.parseLong(path.substring(path.lastIndexOf("/")));
                controller.removeById(id, resp);
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

