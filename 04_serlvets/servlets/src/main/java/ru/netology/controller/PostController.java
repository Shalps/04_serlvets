package ru.netology.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.netology.model.Post;
import ru.netology.service.PostService;


import java.io.IOException;
import java.io.Reader;
@Controller
public class PostController {
  public static final String APPLICATION_JSON = "application/json";
  private final PostService service;
    final Gson gson = new Gson();

@Autowired
  public PostController(PostService service) {
    this.service = service;
  }

  public void all(HttpServletResponse response) throws IOException {
    response.setContentType(APPLICATION_JSON);
    final var data = service.all();

    response.getWriter().print(gson.toJson(data));
  }

  public void getById(long id, HttpServletResponse response) {
      response.setContentType(APPLICATION_JSON);
     final Post byId = service.getById(id);
      try {
          response.getWriter().print(gson.toJson(byId));
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
      // TODO: deserialize request & serialize response
  }

  public void save(Reader body, HttpServletResponse response) throws IOException {
    response.setContentType(APPLICATION_JSON);
    final var gson = new Gson();
    final var post = gson.fromJson(body, Post.class);
    final var data = service.save(post);
    response.getWriter().print(gson.toJson(data));
  }

  public void removeById(long id, HttpServletResponse response) {
     service.removeById(id);

    // TODO: deserialize request & serialize response
  }
}
