package io.github.pve123.techpoc.support;

import com.jayway.jsonpath.JsonPath;
import io.github.pve123.techpoc.member.application.port.MemberMailSender;
import io.github.pve123.techpoc.member.domain.Member;
import io.github.pve123.techpoc.member.domain.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API를 처음부터 끝까지 호출해 보는 테스트의 공통 준비물입니다.
 * 테스트마다 DB와 Redis를 비워서, 테스트끼리 서로 영향을 주지 않게 합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

    protected static final String PASSWORD = "password1234";
    protected static final Path UPLOAD_DIRECTORY = createTempDirectory();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @MockitoBean
    protected MemberMailSender mailSender;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", IntegrationTest::randomJwtSecret);
        registry.add("app.upload.directory", UPLOAD_DIRECTORY::toString);
    }

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        Stream.of("comment", "post_image", "post", "withdrawn_member", "member")
                .forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        redisTemplate.execute(connection -> {
            connection.serverCommands().flushAll();
            return null;
        }, true);
    }

    /** 회원가입하고 로그인해서 액세스 토큰을 돌려줍니다. */
    protected String signUpAndLogin(String email, String nickname) throws Exception {
        signUp(email, nickname);
        return login(email, PASSWORD).accessToken();
    }

    protected long signUp(String email, String nickname) throws Exception {
        String body = mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s", "nickname": "%s"}
                                """.formatted(email, PASSWORD, nickname)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    protected Tokens login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Tokens(JsonPath.read(body, "$.accessToken"), JsonPath.read(body, "$.refreshToken"));
    }

    protected String createAdminAndLogin() throws Exception {
        memberRepository.save(Member.admin("admin@test.com", passwordEncoder.encode(PASSWORD), "관리자"));
        return login("admin@test.com", PASSWORD).accessToken();
    }

    protected static <T extends AbstractMockHttpServletRequestBuilder<?>> T as(String accessToken, T request) {
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        return request;
    }

    protected static long countUploadedFiles() throws IOException {
        try (Stream<Path> files = Files.walk(UPLOAD_DIRECTORY)) {
            return files.filter(Files::isRegularFile).count();
        }
    }

    protected record Tokens(String accessToken, String refreshToken) {
    }

    private static String randomJwtSecret() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("tech-poc-upload");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
