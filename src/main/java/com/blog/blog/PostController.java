package com.blog.blog;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/post")
@CrossOrigin
public class PostController {

    // 1. 从一个安全的字符串生成一个SecretKey实例，确保它足够长
    // 这个密钥应该被安全地存储和管理，而不是硬编码
    private final SecretKey key = Keys.hmacShaKeyFor("MySecureSecretKeyForJWTHS256Algorithm".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");

        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT * FROM user WHERE username = ? AND password = ?",
                username, password
        );

        if (!result.isEmpty()) {
            // 2. 使用新的key对象来生成token
            String token = Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1小时
                    .signWith(key)
                    .compact();
            return token;
        } else {
            return "登录失败";
        }
    }

    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        return jdbcTemplate.queryForList("SELECT * FROM post");
    }

    @PostMapping("/add")
    public String add(@RequestBody Post post,
                      @RequestHeader(value = "token", required = false) String token) {
        if (isTokenInvalid(token)) {
            return "未登录或token无效";
        }

        jdbcTemplate.update(
                "INSERT INTO post(title, content) VALUES (?, ?)",
                post.getTitle(),
                post.getContent()
        );
        return "添加成功";
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable int id,
                         @RequestHeader(value = "token", required = false) String token) {
        if (isTokenInvalid(token)) {
            return "未登录或token无效";
        }

        jdbcTemplate.update("DELETE FROM post WHERE id = ?", id);
        return "删除成功";
    }

    /**
     * 3. 提取出一个通用的方法来验证token
     */
    private boolean isTokenInvalid(String token) {
        if (token == null || token.isEmpty()) {
            return true;
        }
        try {
            // 使用新的parserBuilder和key对象来验证token
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return false; // Token有效
        } catch (Exception e) {
            return true; // Token无效
        }
    }
}