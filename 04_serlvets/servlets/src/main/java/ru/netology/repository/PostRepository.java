package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.ArrayList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;



public class PostRepository {
    private final ConcurrentHashMap<Long,Post> posts = new ConcurrentHashMap<>();
    private  final AtomicLong generation = new AtomicLong(1);
  public List<Post> all() {
    return new ArrayList<>(posts.values());
  }

  public Optional<Post> getById(long id) {
    return Optional.ofNullable(posts.get(id));
  }

  public Post save(Post post) {
    if (post.getId()==0){
          long newId = generation.getAndIncrement();
          post.setId(newId);
          posts.put(newId,post);
          return post;
    }else {
        if (!posts.containsKey(post.getId())){
           throw new NotFoundException("такого поста не существует");
        }
        posts.put(post.getId(),post);
        return post;
    }  }

  public void removeById(long id) {
      Post remove = posts.remove(id);
      if (remove==null){
          throw new NotFoundException("Поста с id "+id +" не существует");
      }
  }
}
