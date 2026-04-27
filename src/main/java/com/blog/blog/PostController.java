package com.blog.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/post")
@CrossOrigin
public class PostController {

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> user) {

        String username = user.get("username");
        String password = user.get("password");

        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT * FROM user WHERE username = ? AND password = ?",
                username, password
        );

        if(result.size() > 0) {

            String token = Jwts.builder()
                    .setSubject(username)   // 用户
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1小时
                    .signWith(SignatureAlgorithm.HS256, "secretKey")
                    .compact();

            return token;
        } else {
            return "登录失败";
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 查询文章
    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        return jdbcTemplate.queryForList("SELECT * FROM post");
    }

    // 添加文章
    @PostMapping("/add")
    public String add(@RequestBody Post post,
                      @RequestHeader(value = "token", required = false) String token) {

        try {
            Jwts.parser()
                    .setSigningKey("secretKey")
                    .parseClaimsJws(token);   // 👈 验证 token
        } catch (Exception e) {
            return "未登录或token无效";
        }

        jdbcTemplate.update(
                "INSERT INTO post(title, content) VALUES (?, ?)",
                post.getTitle(),
                post.getContent()
        );

        return "添加成功";
    }

    // 删除文章
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable int id,
                         @RequestHeader(value = "token", required = false) String token) {
        try {
            Jwts.parser()
                    .setSigningKey("secretKey")
                    .parseClaimsJws(token);
        } catch (Exception e) {
            return "未登录或token无效";
        }
        jdbcTemplate.update(
                "DELETE FROM post WHERE id = ?",
                id
        );
        return "删除成功";
    }
}